package compile;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import instructions.Instruction;
import instructions.Instruction.AddressingMode;
import instructions.Instruction.InstructionSet;

public class Compiler {

	private int currentAddress;
	private int registerSize, memorySize;
	private int fillValue;

	private List<String> linesToCompile;
	private List<Constant> constantList;
	private List<Label> labelList;

	private Map<Integer, Instruction> instructionMap;
	private List<String> postLineConstantList;
	private Map<Integer, String> postInstructionMap;
	private boolean post;

	public Compiler(File toCompile) {
		constantList = new ArrayList<>();
		linesToCompile = new ArrayList<>();
		labelList = new ArrayList<>();
		postLineConstantList = new ArrayList<>();

		instructionMap = new TreeMap<>();
		postInstructionMap = new HashMap<>();

		registerSize = 0;
		memorySize = 0;
		fillValue = 0;
		post = false;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(toCompile));
			String line;
			boolean ignore = false, firstIgnore = false;

			while ((line = reader.readLine()) != null) {
				String ignoredLine = "";
				line = line.strip();

				if (line.contains("+;")) {
					String[] splitedLine = line.split("\\+;");
					line = splitedLine.length == 0 ? "" : splitedLine[0].strip();
					ignoredLine = splitedLine.length < 2 ? "" : splitedLine[1].strip();
					ignore = true;
					firstIgnore = true;
				}

				if (ignore) {
					if (line.contains("-;")) {
						ignore = false;
						String[] splitedLine = line.split("-;");
						if (splitedLine.length == 1 || splitedLine.length == 0)
							line = "";
						else
							line = line.split("-;")[1];

					} else if (ignoredLine.contains("-;")) {
						ignore = false;
						line += " " + ignoredLine.split("-;")[1].strip();
					} else if (firstIgnore)
						firstIgnore = false;
					else
						continue;
				}

				String[] splitedLine = line.split(";");
				line = splitedLine.length == 0 ? "" : splitedLine[0].strip();

				linesToCompile.add(line);
			}

			reader.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void compile(File outputFile) throws CompilerException, IOException {
		// On lit les directives
		boolean error = readCompilationDirectives();
		if (error)
			throw new CompilerException("Compiler encountered errors while reading directives!");

		// On lit les constantes
		error = readConstants();
		if (error)
			throw new CompilerException("Compiler encountered errors while reading constants!");

		// On lit les instructions
		error = initLabelsAndInstructions();
		if (error)
			throw new CompilerException("Compiler encountered errors while reading instructions!");

		// On vérifie que chaque constante a été utilisée
		for (Constant c : constantList) {
			if (!c.hasBeenUsed())
				System.out.println("[WARNING]: Unused variable " + c.getName());
		}

		// On transforme en bytecode
		writeFile(outputFile);

	}

	private boolean readCompilationDirectives() {
		boolean hadError = false;

		for (int i = 0; i < linesToCompile.size(); i++) {
			String line = linesToCompile.get(i);
			if (line.isEmpty())
				continue;

			try {
				if (line.toLowerCase().contains(".mem ")) {
					if (memorySize != 0)
						System.out.println(
								String.format("[WARNING]: Multiple definition of memory size (line %d)", i + 1));

					try {
						memorySize = getNumberFromString(line.substring(5));
					} catch (NumberFormatException e) {
						callNumberException(i + 1, line, 5);
					}
				} else if (line.toLowerCase().contains(".index ")) {
					if (registerSize != 0)
						System.out.println(
								String.format("[WARNING]: Multiple definition of register size (line %d)", i + 1));

					try {
						registerSize = getNumberFromString(line.substring(7));
					} catch (NumberFormatException e) {
						callNumberException(i + 1, line, 7);
					}
				}
			} catch (CompilerException e) {
				System.err.println(e.getMessage());
				hadError = true;
			}
		}

		if (memorySize == 0) {
			System.out.println("[WARNING]: Memory size was never set, put default value 8");
			memorySize = 8;
		}

		if (registerSize == 0) {
			System.out.println("[WARNING]: Register size was never set, put default value 8");
			registerSize = 8;
		}

		return hadError;
	}

	private boolean readConstants() {
		boolean hadError = false;

		for (int i = 0; i < linesToCompile.size(); i++) {
			String line = linesToCompile.get(i);
			if (line.isEmpty())
				continue;

			try {
				if (line.contains("=")) {
					String[] split = line.split("=");
					if (split.length != 2)
						throw new CompilerException(
								String.format("[ERROR]: Syntax error for constant definition (line %d)\n%s\n%s\n",
										i + 1, line, padLeftSpaces("^", line.indexOf("=", line.indexOf("=") + 1))));

					Constant constant = getConstant(split[0].strip(), split[1].strip().replace(" ", ""), i + 1, line);
					if (constant == null)
						continue;

					constantList.add(constant);

				} else if (line.toLowerCase().contains(".eq ")) {
					String[] split = line.split(" ");
					if (split.length != 3)
						throw new CompilerException(String
								.format("[ERROR]: Syntax error for constant definition (line %d)\n%s\n", i + 1, line));

					if (!split[0].equals(".eq"))
						throw new CompilerException(String.format(
								"[ERROR]: Syntax error for constant definition, must begin with \".eq\" (line %d)\n%s\n^\n",
								i + 1, line));

					Constant constant = getConstant(split[1].strip(), split[2].strip(), i + 1, line);
					if (constant == null)
						continue;

					constantList.add(constant);
				}
			} catch (CompilerException e) {
				System.err.println(e.getMessage());
				hadError = true;
			}
		}

		return hadError;
	}

	private boolean initLabelsAndInstructions() {
		boolean hadError = false;

		// On définit les instructions et les labels
		for (int i = 0; i < linesToCompile.size(); i++) {
			String line = linesToCompile.get(i);
			if (line.isEmpty())
				continue;

			try {
				// On modifie l'adresse d'écriture
				if (line.toLowerCase().startsWith(".org ")) {
					try {
						currentAddress = getNumberFromString(line.substring(5));
						verifyBits(currentAddress, 16, i + 1);
					} catch (NumberFormatException e) {
						callNumberException(i + 1, line, 5);
					}
				} else if (line.toLowerCase().startsWith(".fillvalue ")) {
					try {
						fillValue = getNumberFromString(line.substring(11));
						verifyBits(fillValue, 8, i + 1);
					} catch (NumberFormatException e) {
						callNumberException(i + 1, line, 11);
					}

				} else if (line.contains(":")) {
					String[] splited = line.split(":");
					readLabel(splited[0].strip(), line, i);

					if (splited.length == 2)
						readInstruction(splited[1].strip(), line, i);
					else if (splited.length > 2)
						throw new CompilerException(String.format("[ERROR]: Syntax error on line %d\n%s\n%s\n", i + 1,
								line, padLeftSpaces("^", line.indexOf(":", line.indexOf(":") + 1))));
				} else {
					// Sinon c'est une instruction
					readInstruction(line, line, i);
				}
			} catch (CompilerException e) {
				System.err.println(e.getMessage());
				hadError = true;
			}
		}

		if (!hadError)
			try {
				post = true;
				finalizeReading();
			} catch (CompilerException e) {
				System.err.println(e.getMessage());
				hadError = true;
			}

		return hadError;
	}

	private void readLabel(String labelName, String line, int i) throws CompilerException {
		if (labelName.isEmpty())
			throw new CompilerException(
					String.format("[ERROR]: Label name can't be empty (line %d)\n%s\n^\n", i + 1, line));

		Pattern spacePattern = Pattern.compile("[.*\\s.*]", Pattern.CASE_INSENSITIVE);
		Pattern specialPattern = Pattern.compile("[.*[\\+|[-]|$|#|%|<|>|^|:|\\(|\\)|\\[|\\]|{|}|'].*]");
		Pattern numberPattern = Pattern.compile("^[0-9].*");

		if (spacePattern.matcher(labelName).find())
			throw new CompilerException(String.format("[ERROR]: Label name can't contain a space (line %d)\n%s\n%s\n",
					i + 1, line, padLeftSpaces("^", labelName.indexOf(' '))));

		if (specialPattern.matcher(labelName).find())
			throw new CompilerException(String.format(
					"[ERROR]: Label name contain any of the following characters: +, -, *, $, #, %%, <, >, ^, :, (, ), [, ], {, }, ' (line %d)\n%s\n",
					i + 1, line));

		if (numberPattern.matcher(labelName).find())
			throw new CompilerException(
					String.format("[ERROR]: Label name can't begin with a number (line %d)\n%s\n", i + 1, line));

		if (!Charset.forName("US-ASCII").newEncoder().canEncode(labelName))
			System.out.println(String.format(
					"[WARNING]: You should use ASCII only characters for label name %s (line %d)", labelName, i + 1));

		Label label = new Label(labelName, currentAddress);

		if (labelList.contains(label))
			throw new CompilerException(String.format("[ERROR]: Duplicate label name (line %d)", i + 1));

		if (constantList.contains(new Constant(labelName, 0)))
			throw new CompilerException(String.format("[ERROR]: Duplicate label name (line %d)", i + 1));

		labelList.add(label);
	}

	private void readInstruction(String instructionLine, String line, int i) throws CompilerException {
		String[] split = instructionLine.split(" ");

		String instructionName = split[0].strip();
		Instruction instruction;

		if (line.toLowerCase().contains(".eq ") || line.toLowerCase().contains(".mem ")
				|| line.toLowerCase().contains(".index ") || line.toLowerCase().contains(".fillValue ")
				|| line.contains("="))
			return;

		if (instructionName.toLowerCase().equals(".db")) {
			if (split.length == 1)
				throw new CompilerException(
						String.format("[ERROR]: Byte directive argument cannot be empty (line %d)\n%s\n%s\n", i + 1,
								line, padLeftSpaces("^", 4)));

			if (instructionLine.endsWith(","))
				throw new CompilerException(
						String.format("[ERROR]: Byte directive argument cannot be empty (line %d)\n%s\n%s\n", i + 1,
								line, padLeftSpaces("^", instructionLine.length())));

			String[] bytes = instructionLine.substring(4).split(",");
			int offset = 4;
			for (String s : bytes) {
				if (s.strip().length() == 0)
					throw new CompilerException(
							String.format("[ERROR]: Byte directive argument cannot be empty (line %d)\n%s\n%s\n", i + 1,
									instructionLine, padLeftSpaces("^", offset)));

				instruction = new Instruction(InstructionSet.DB, AddressingMode.DB);
				try {
					int value = getNumberFromString(s.strip());
					verifyBits(value, 8, i + 1);

					instruction.setArgument(value, 0);
					instructionMap.put(currentAddress, instruction);
				} catch (NumberFormatException e) {
					if (!isNumeral(s.strip()) && !post) {
						postInstructionMap.put(currentAddress, instructionLine);
					} else
						callNumberException(i + 1, line, offset);
				}

				offset += s.length() + 1;
				currentAddress += instruction.getByteNumber();
			}
		} else if (instructionName.toLowerCase().equals(".dw")) {
			if (split.length == 1)
				throw new CompilerException(
						String.format("[ERROR]: Word directive argument cannot be empty (line %d)\n%s\n%s\n", i + 1,
								instructionLine, padLeftSpaces("^", 4)));

			if (instructionLine.endsWith(","))
				throw new CompilerException(
						String.format("[ERROR]: Byte directive argument cannot be empty (line %d)\n%s\n%s\n", i + 1,
								line, padLeftSpaces("^", instructionLine.length())));

			String[] bytes = instructionLine.substring(4).split(",");
			int offset = 4;
			for (String s : bytes) {
				if (s.strip().length() == 0)
					throw new CompilerException(
							String.format("[ERROR]: Word directive argument cannot be empty (line %d)\n%s\n%s\n", i + 1,
									instructionLine, padLeftSpaces("^", offset)));

				instruction = new Instruction(InstructionSet.DW, AddressingMode.DW);
				try {
					int value = getNumberFromString(s.strip());
					verifyBits(value, 16, i + 1);

					instruction.setArgument(value & 0xFF, (value & 0xFF00) >> 8);
					instructionMap.put(currentAddress, instruction);
				} catch (NumberFormatException e) {
					if (!isNumeral(s.strip()) && !post) {
						postInstructionMap.put(currentAddress, instructionLine);
					} else
						callNumberException(i + 1, line, offset);
				}

				offset += s.length() + 2;
				currentAddress += instruction.getByteNumber();
			}
		} else if (instructionName.toLowerCase().equals(".align")) {
			if (split.length == 1)
				throw new CompilerException(
						String.format("[ERROR]: Align directive argument cannot be empty (line %d)\n%s\n%s\n", i + 1,
								instructionLine, padLeftSpaces("^", 4)));

			try {
				int modulo = getNumberFromString(instructionLine.substring(7));
				verifyBits(modulo, 8, i + 1);

				while (currentAddress % modulo != 0) {
					instruction = new Instruction(InstructionSet.DB, AddressingMode.DB);
					instruction.setArgument(fillValue, 0);
					instructionMap.put(currentAddress, instruction);
					currentAddress += 1;
				}

			} catch (NumberFormatException e) {
				callNumberException(i + 1, instructionLine, 7);
			}
		} else if (instructionName.toUpperCase().equals("DB") || instructionName.toUpperCase().equals("DW")) {
			throw new CompilerException(
					String.format("[ERROR]: Instruction %s not found (line %d)\nDid you mean %s ?\n", instructionLine,
							i + 1, "." + instructionLine));
		} else {
			InstructionSet[] instructions = InstructionSet.values();
			AddressingMode addressingSelected = null;
			for (InstructionSet ins : instructions) {
				int lsb = 0, msb = 0;
				boolean toAdd = false;

				if (ins.toString().equals(instructionName.toUpperCase())) {
					byte[] opCodes = ins.getOpCodes();
					if (split.length == 1) {
						// On regarde si c'est en implicite ou accumulateur
						if (opCodes[AddressingMode.IMPLICIT.ordinal()] != (byte) 0xFF) {
							addressingSelected = AddressingMode.IMPLICIT;
						} else if (opCodes[AddressingMode.ACCUMULATOR.ordinal()] != (byte) 0xFF) {
							addressingSelected = AddressingMode.ACCUMULATOR;
						} else
							callAddressingException(ins, AddressingMode.IMPLICIT, i + 1);

					} else {
						String argument = instructionLine.substring(4).strip().replace(" ", "");
						// On regarde pour les différents modes d'adressage

						// On regarde si c'est un jump ou un branch
						if (ins == InstructionSet.JMP) {
							if (argument.contains("(")) {
								if (argument.endsWith(")")) {
									addressingSelected = AddressingMode.INDIRECT;
									argument = argument.substring(1, argument.length() - 1);
									if (argument.isBlank())
										throw new CompilerException(String.format(
												"[ERROR]: Syntax error for indirect jump (line %d)\n%s\n%s\n", i + 1,
												instructionLine, padLeftSpaces("^", 5)));

									try {
										int value = getNumberFromString(argument);
										verifyBits(value, 16, i + 1);

										lsb = value & 0xFF;
										msb = (value & 0xFF00) >> 8;
									} catch (NumberFormatException e) {
										if (isNumeral(argument))
											toAdd = true;
										else
											callNumberException(i + 1, instructionLine, 5);
									}

								} else
									throw new CompilerException(String.format(
											"[ERROR]: Syntax error for indirect jump (line %d)\n%s\n%s\n", i + 1,
											instructionLine, padLeftSpaces("^", instructionLine.length())));
							} else {
								addressingSelected = AddressingMode.ABSOLUTE;
								try {
									int value = getNumberFromString(argument);
									verifyBits(value, 16, i + 1);

									lsb = value & 0xFF;
									msb = (value & 0xFF00) >> 8;
								} catch (NumberFormatException e) {
									if (!isNumeral(argument))
										toAdd = true;
									else
										callNumberException(i + 1, instructionLine, 4);

								}
							}

						} else if (ins == InstructionSet.JSR) {
							addressingSelected = AddressingMode.ABSOLUTE;
							try {
								int value = getNumberFromString(argument);
								verifyBits(value, 16, i + 1);

								lsb = value & 0xFF;
								msb = (value & 0xFF00) >> 8;
							} catch (NumberFormatException e) {
								if (!isNumeral(argument))
									toAdd = true;
								else
									callNumberException(i + 1, instructionLine, 4);
							}

						} else if (ins == InstructionSet.BCC || ins == InstructionSet.BCS || ins == InstructionSet.BEQ
								|| ins == InstructionSet.BMI || ins == InstructionSet.BNE || ins == InstructionSet.BPL
								|| ins == InstructionSet.BVC || ins == InstructionSet.BVS) {
							addressingSelected = AddressingMode.RELATIVE;
							if (argument.startsWith("*")) {
								argument = argument.substring(1);
								if (argument.isBlank())
									throw new CompilerException(
											String.format("[ERROR] Syntax error for relative mode (line %d)\n%s\n%s\n",
													i + 1, instructionLine, padLeftSpaces("^", 5)));

								try {
									int value = getNumberFromString(argument);
									verifyBits(value - 2, 8, i + 1);

									System.out.println(String
											.format("[WARNING]: Branching used with hardcoded value (line %d)", i + 1));
									lsb = value - 2;

								} catch (NumberFormatException e) {
									if (!isNumeral(argument))
										toAdd = true;
									else
										callNumberException(i + 1, instructionLine, 5);
								}
							} else {
								try {
									if (isNumeral(argument))
										throw new CompilerException(String.format(
												"[ERROR] Syntax error for relative mode, number must start with * (line %d)\n%s\n%s\n",
												i + 1, instructionLine, padLeftSpaces("^", 4)));

									int value = getNumberFromString(argument);

									verifyBits(value, 16, i + 1);
									int branchAddress = value - currentAddress - 2;
									verifyBits(branchAddress, 16, i + 1);
									lsb = branchAddress;

								} catch (NumberFormatException e) {
									lsb = currentAddress & 0xFF;
									msb = (currentAddress & 0xFF00) >> 8;
									toAdd = true;
								}
							}
						}

						// Accumulateur
						else if (argument.equalsIgnoreCase("A"))
							if (opCodes[AddressingMode.ACCUMULATOR.ordinal()] != -1)
								addressingSelected = AddressingMode.ACCUMULATOR;
							else
								callAddressingException(ins, AddressingMode.ACCUMULATOR, i + 1);

						// Immédiat (non constante)
						else if (argument.startsWith("#")) {
							if (opCodes[AddressingMode.IMMEDIATE.ordinal()] != -1) {
								addressingSelected = AddressingMode.IMMEDIATE;
								argument = argument.substring(1);
								if (argument.isBlank())
									throw new CompilerException(String.format(
											"[ERROR] Syntax error for immediate assignment (line %d)\n%s\n%s\n", i + 1,
											instructionLine, padLeftSpaces("^", 5)));

								try {
									lsb = getNumberFromString(argument);
									verifyBits(lsb, registerSize, i + 1);

								} catch (NumberFormatException e) {
									if (!isNumeral(argument))
										toAdd = true;
									else
										callNumberException(i + 1, instructionLine, 4);
								}
							} else
								callAddressingException(ins, AddressingMode.IMMEDIATE, i + 1);
						}

						// Indirect X et Y
						else if (argument.startsWith("(")) {
							// Indirect X
							if (argument.toLowerCase().endsWith(",x)")) {
								if (opCodes[AddressingMode.INDIRECT_X.ordinal()] != -1) {
									addressingSelected = AddressingMode.INDIRECT_X;
									String[] splitIndirect = argument.split(",");
									try {
										lsb = getNumberFromString(splitIndirect[0].substring(1));
										verifyBits(lsb, 8, i + 1);
									} catch (NumberFormatException e) {
										if (!isNumeral(splitIndirect[0].substring(1)))
											toAdd = true;
										else
											callNumberException(i + 1, instructionLine, 5);
									}

								} else
									callAddressingException(ins, AddressingMode.INDIRECT_X, i + 1);
							}

							// Indirect Y
							else if (argument.toLowerCase().endsWith("),y")) {
								if (opCodes[AddressingMode.INDIRECT_Y.ordinal()] != -1) {
									addressingSelected = AddressingMode.INDIRECT_Y;
									String[] splitIndirect = argument.split("\\)");
									try {
										lsb = getNumberFromString(splitIndirect[0].substring(1));
										verifyBits(lsb, 8, i + 1);
									} catch (NumberFormatException e) {
										if (!isNumeral(splitIndirect[0].substring(1)))
											toAdd = true;
										else
											callNumberException(i + 1, instructionLine, 5);
									}

								} else
									callAddressingException(ins, AddressingMode.INDIRECT_Y, i + 1);

							} else
								throw new CompilerException(String.format(
										"[ERROR]: Syntax error for indirect X or Y addressing mode (line %d)\n",
										i + 1));
						}

						// Zero page et absolu
						else {
							String[] args = argument.split(",");
							try {
								if (args[0].isBlank())
									throw new CompilerException(String.format(
											"[ERROR]: Syntax error for zero page or absolute addressing mode (line %d)\n%s\n%s\n",
											i + 1, instructionLine, padLeftSpaces("^", 4)));

								int value = getNumberFromString(args[0]);
								Instruction in = readZPAbsoluteInstruction(ins, value, args, opCodes, instructionLine,
										i);
								addressingSelected = in.getAddressingMode();
								lsb = in.getLsb();
								msb = in.getMsb();

							} catch (NumberFormatException e) {
								if (!isNumeral(args[0]))
									toAdd = true;
								else
									callNumberException(i + 1, instructionLine, 4);
							}

						}
					}

					if (toAdd) {
						if (post)
							throw new CompilerException(
									String.format("[ERROR]: Constant or label not found (line %d)\n%s\n", i + 1, line));

						postInstructionMap.put(currentAddress, instructionLine);
						if (addressingSelected == null)
							currentAddress += 3;
						else {
							instruction = new Instruction(ins, addressingSelected);
							currentAddress += instruction.getByteNumber();
						}

					} else {
						instruction = new Instruction(ins, addressingSelected);
						instruction.setArgument(lsb, msb);
						instructionMap.put(currentAddress, instruction);
						currentAddress += instruction.getByteNumber();
					}

					return;
				}
			}

			String err = String.format("[ERROR]: Instruction %s not found (line %d)\n", instructionName, i + 1);

			if (line.toLowerCase().startsWith(".eq"))
				err += String.format("Did you mean %s ?\n",
						instructionLine.substring(0, 3) + " " + instructionLine.substring(3));

			else if (line.toLowerCase().startsWith(".mem"))
				err += String.format("Did you mean %s ?\n",
						instructionLine.substring(0, 4) + " " + instructionLine.substring(4));

			else if (line.toLowerCase().startsWith(".index"))
				err += String.format("Did you mean %s ?\n",
						instructionLine.substring(0, 6) + " " + instructionLine.substring(6));

			else if (line.toLowerCase().startsWith(".align"))
				err += String.format("Did you mean %s ?\n",
						instructionLine.substring(0, 6) + " " + instructionLine.substring(6));

			else if (line.toLowerCase().startsWith(".org"))
				err += String.format("Did you mean %s ?\n",
						instructionLine.substring(0, 4) + " " + instructionLine.substring(4));

			else if (line.toLowerCase().startsWith(".fillvalue"))
				err += String.format("Did you mean %s ?\n",
						instructionLine.substring(0, 10) + " " + instructionLine.substring(10));

			throw new CompilerException(err);
		}

		if (currentAddress > 0x10000) {
			throw new CompilerException(String.format("[ERROR]: Address beyond bus' capacity! (line %d)\n", i + 1));
		}
	}

	private Instruction readZPAbsoluteInstruction(InstructionSet ins, int value, String[] args, byte[] opCodes,
			String line, int i) throws CompilerException {

		Instruction instruction;
		AddressingMode addressingSelected = null;
		int lsb = 0, msb = 0;
		boolean zp = false;

		try {
			verifyBits(value, 8, i + 1);
			zp = true;
		} catch (CompilerException e) {
			verifyBits(value, 16, i + 1);
			zp = false;
		}

		// Zero page
		if (zp) {
			if (args.length == 1) {
				if (line.endsWith(","))
					throw new CompilerException(
							String.format("[ERROR]: Syntax error for zero page X or Y (line %d)\n%s\n", i + 1, line));

				if (opCodes[AddressingMode.ZEROPAGE.ordinal()] != -1) {
					addressingSelected = AddressingMode.ZEROPAGE;
					lsb = value;
				} else
					callAddressingException(ins, AddressingMode.ZEROPAGE, i + 1);

			} else if (args.length == 2) {
				// Zero page X
				if (args[1].equalsIgnoreCase("X")) {
					if (opCodes[AddressingMode.ZEROPAGE_X.ordinal()] != -1) {
						addressingSelected = AddressingMode.ZEROPAGE_X;
						lsb = value;
					} else if (opCodes[AddressingMode.ABSOLUTE_X.ordinal()] != -1) {
						addressingSelected = AddressingMode.ABSOLUTE_X;
						lsb = value;
						msb = 0;
					} else
						callAddressingException(ins, AddressingMode.ZEROPAGE_X, i + 1);

					// Zero page Y
				} else if (args[1].equalsIgnoreCase("Y")) {
					if (opCodes[AddressingMode.ZEROPAGE_Y.ordinal()] != -1) {
						addressingSelected = AddressingMode.ZEROPAGE_Y;
						lsb = value;
					} else if (opCodes[AddressingMode.ABSOLUTE_Y.ordinal()] != -1) {
						addressingSelected = AddressingMode.ABSOLUTE_Y;
						lsb = value;
						msb = 0;
					} else {
						System.out.println(String.format("0x%04X", currentAddress));
						callAddressingException(ins, AddressingMode.ZEROPAGE_Y, i + 1);
					}
				} else
					throw new CompilerException(String.format(
							"[ERROR]: Syntax error for instruction %s, expected X or Y (line %d)\n%s\n%s\n",
							ins.toString(), i + 1, line, padLeftSpaces("^", line.indexOf(',') + 1)));
			} else
				throw new CompilerException(
						String.format("[ERROR]: Syntax error for instruction %s (line %d)\n%s\n%s\n", ins.toString(),
								i + 1, line, padLeftSpaces("^", line.indexOf(",", line.indexOf(",") + 1))));
		}

		// Absolu
		else {
			if (args.length == 1) {
				if (line.endsWith(","))
					throw new CompilerException(
							String.format("[ERROR]: Syntax error for absolute X or Y (line %d)\n%s\n", i + 1, line));

				if (opCodes[AddressingMode.ABSOLUTE.ordinal()] != -1) {
					addressingSelected = AddressingMode.ABSOLUTE;
					lsb = value & 0xFF;
					msb = (value & 0xFF00) >> 8;
				} else
					callAddressingException(ins, AddressingMode.ZEROPAGE, i + 1);

			} else if (args.length == 2) {
				// Absolu X
				if (args[1].equalsIgnoreCase("X")) {
					if (opCodes[AddressingMode.ABSOLUTE_X.ordinal()] != -1) {
						addressingSelected = AddressingMode.ABSOLUTE_X;
						lsb = value & 0xFF;
						msb = (value & 0xFF00) >> 8;
					} else
						callAddressingException(ins, AddressingMode.ABSOLUTE_X, i + 1);

					// Absolu Y
				} else if (args[1].equalsIgnoreCase("Y")) {
					if (opCodes[AddressingMode.ABSOLUTE_Y.ordinal()] != -1) {
						addressingSelected = AddressingMode.ABSOLUTE_Y;
						lsb = value & 0xFF;
						msb = (value & 0xFF00) >> 8;
					} else
						callAddressingException(ins, AddressingMode.ABSOLUTE_Y, i + 1);

				} else
					throw new CompilerException(String.format(
							"[ERROR]: Syntax error for instruction %s, expected X or Y (line %d)\n%s\n%s\n",
							ins.toString(), i + 1, line, padLeftSpaces("^", line.indexOf(',') + 1)));
			} else
				throw new CompilerException(
						String.format("[ERROR]: Syntax error for instruction %s (line %d)\n%s\n%s\n", ins.toString(),
								i + 1, line, padLeftSpaces("^", line.indexOf(",", line.indexOf(",") + 1))));
		}

		instruction = new Instruction(ins, addressingSelected);
		instruction.setArgument(lsb, msb);
		return instruction;
	}

	private void finalizeReading() throws CompilerException {
		// On ajoute d'abord les constantes qui attendent
		for (int i = 0; i < postLineConstantList.size(); i++) {
			String line = postLineConstantList.get(i);
			if (line.contains("=")) {
				String[] split = line.split("=");
				Constant constant = getConstant(split[0].strip(), split[1].strip().replace(" ", ""), i + 1, line);
				if (constant == null)
					throw new CompilerException(
							String.format("[ERROR]: Constant or label not found (line %d)\n%s\n", i + 1, line));

				constantList.add(constant);
			} else {
				String[] split = line.split(" ");

				Constant constant = getConstant(split[1].strip(), split[2].strip(), i + 1, line);
				if (constant == null)
					throw new CompilerException(
							String.format("[ERROR]: Constant or label not found (line %d)\n%s\n", i + 1, line));

				constantList.add(constant);
			}
		}

		for (Map.Entry<Integer, String> instructionLine : postInstructionMap.entrySet()) {
			currentAddress = instructionLine.getKey();
			readInstruction(instructionLine.getValue(), instructionLine.getValue(), instructionLine.getKey());
		}

	}

	private void writeFile(File outFile) throws IOException {
		if (outFile.exists())
			outFile.delete();

		outFile.createNewFile();

		DataOutputStream out = new DataOutputStream(new FileOutputStream(outFile, true));

		List<Integer> addresses = new ArrayList<>(instructionMap.keySet());

		if (addresses.size() != 0) {
			currentAddress = addresses.get(0);

			int index = 0;
			while (index < addresses.size()) {
				if (currentAddress == addresses.get(index)) {
					Instruction instruction = instructionMap.get(currentAddress);
					int byteNumber = instruction.getByteNumber();

					if (instruction.getAddressingMode() == AddressingMode.DB) {
						out.writeByte(instruction.getLsb());

					} else if (instruction.getAddressingMode() == AddressingMode.DW) {
						out.writeByte(instruction.getLsb());
						out.writeByte(instruction.getMsb());

					} else {
						out.writeByte(
								instruction.getInstruction().getOpCode(instruction.getAddressingMode().ordinal()));

						if (byteNumber >= 2)
							out.writeByte(instruction.getLsb());

						if (byteNumber == 3)
							out.writeByte(instruction.getMsb());
					}

					currentAddress += byteNumber;
					index++;

				} else {
					out.writeByte(fillValue);
					currentAddress++;
				}
			}
		}

		out.close();
	}

	private Constant getConstant(String constantName, String number, int lineNumber, String line)
			throws CompilerException {

		Pattern spacePattern = Pattern.compile("[.*\\s.*]", Pattern.CASE_INSENSITIVE);
		Pattern specialPattern = Pattern.compile("[.*[\\+|[-]|$|#|%|<|>|^|:|\\(|\\)|\\[|\\]|{|}|'].*]");
		Pattern numberPattern = Pattern.compile("^[0-9].*");

		if (spacePattern.matcher(number).find())
			throw new CompilerException(String.format("[ERROR]: Number can't contain a space (line %d)\n%s\n%s\n",
					lineNumber, line, padLeftSpaces("^", number.indexOf(' '))));

		int value = 0;
		try {
			value = getNumberFromString(number);
		} catch (NumberFormatException e) {
			if (!isNumeral(number)) {
				postLineConstantList.add(line);
				return null;
			}

			callNumberException(lineNumber, line, constantName.length() + 2);
		}

		if (constantName.isEmpty())
			throw new CompilerException(
					String.format("[ERROR]: Constant name can't be empty (line %d)\n%s\n", lineNumber, line));

		if (spacePattern.matcher(constantName).find())
			throw new CompilerException(
					String.format("[ERROR]: Constant name can't contain a space (line %d)\n%s\n%s\n", lineNumber, line,
							padLeftSpaces("^", constantName.indexOf(' '))));

		if (specialPattern.matcher(constantName).find())
			throw new CompilerException(String.format(
					"[ERROR]: Constant name can't contain any of the following characters: +, -, *, $, #, %%, <, >, ^, :, (, ), [, ], {, }, ' (line %d)\n%s\n",
					lineNumber, line));

		if (numberPattern.matcher(constantName).find())
			throw new CompilerException(String
					.format("[ERROR]: Constant name can't begin with a number (line %d)\n%s\n", lineNumber, line));

		Constant constant = new Constant(constantName, value);
		if (constantList.contains(constant))
			throw new CompilerException(String.format("[ERROR]: Duplicate constant name (line %d)", lineNumber));

		if (!Charset.forName("US-ASCII").newEncoder().canEncode(constantName))
			System.out.println(
					String.format("[WARNING]: You should use ASCII only characters for constant name %s (line %d)",
							constantName, lineNumber));

		return constant;
	}

	private int getNumberFromString(String strNumber) {
		int result = 0;
		boolean low = false, high = false, third = false, negative = false;

		if (!strNumber.startsWith("'"))
			strNumber = strNumber.strip().replace(" ", "").replace("-", "+-");

		if (strNumber.startsWith("+")) {
			strNumber = strNumber.substring(1);
		}

		if (strNumber.contains("+")) {
			String[] split = strNumber.split("\\+");
			strNumber = split[0];

			for (int i = 0; i < split.length; i++)
				result += getNumberFromString(split[i]);

		} else {
			if (strNumber.contains("-")) {
				if (strNumber.startsWith("-")) {
					negative = true;
					strNumber = strNumber.substring(1);
				}
			}

			if (strNumber.startsWith("<")) {
				low = true;
				strNumber = strNumber.substring(1);
			} else if (strNumber.startsWith(">")) {
				high = true;
				strNumber = strNumber.substring(1);
			} else if (strNumber.startsWith("^")) {
				third = true;
				strNumber = strNumber.substring(1);
			}

			if (strNumber.startsWith("'") && strNumber.endsWith("'") && strNumber.length() == 3) {
				result += strNumber.charAt(1);

			} else {
				switch (strNumber.charAt(0)) {
				case '$':
					result += Integer.parseInt(strNumber, 1, strNumber.length(), 16);
					break;

				case '%':
					result += Integer.parseInt(strNumber, 1, strNumber.length(), 2);
					break;

				default:
					int index;
					if ((index = constantList.indexOf(new Constant(strNumber, 0))) != -1) {
						result += constantList.get(index).getValue();
					} else if ((index = labelList.indexOf(new Label(strNumber, 0))) != -1) {
						result += labelList.get(index).getAddress();
					} else
						result += Integer.valueOf(strNumber);
					break;
				}
			}

			if (low)
				result = result & 0xFF;

			else if (high)
				result = (result & 0xFF00) >> 8;

			else if (third)
				result = (result & 0xFF0000) >> 16;

			if (negative)
				result = -result;
		}
		return result;
	}

	private void verifyBits(int value, int bitNumber, int line) throws CompilerException {
		int bound = 1 << (bitNumber - 1);
		if ((value < 0 && value < -bound) || (value >= (bound << 1)))
			throw new CompilerException(
					String.format("[ERROR]: Value must have a bit number lesser or equal than %d (here %d) (line %d)\n",
							bitNumber, Integer.toBinaryString(value).length(), line));
	}

	private boolean isNumeral(String number) {
		Pattern numberPattern = Pattern.compile("^[0-9|$|%|[+]|[-]]");
		return numberPattern.matcher(number).find();
	}

	private void callNumberException(int lineNumber, String line, int offset) throws CompilerException {
		throw new CompilerException(
				String.format("[ERROR]: Number argument must be a valid number! (line %d)\n%s\n%s\n", lineNumber, line,
						padLeftSpaces("^", offset)));
	}

	private void callAddressingException(InstructionSet instruction, AddressingMode addressingMode, int lineNumber)
			throws CompilerException {
		throw new CompilerException(
				String.format("[ERROR]: Adressing mode %s do not exist for instruction %s (line %d)\n",
						addressingMode.toString(), instruction.toString(), lineNumber));
	}

	public List<String> getLines() {
		return linesToCompile;
	}

	public List<Constant> getConstantList() {
		return constantList;
	}

	public List<Label> getLabelList() {
		return labelList;
	}

	public Map<Integer, Instruction> getInstructionMap() {
		return instructionMap;
	}

	private String padLeftSpaces(String inputString, int length) {
		StringBuilder sb = new StringBuilder();
		while (sb.length() < length) {
			sb.append(' ');
		}
		sb.append(inputString);

		return sb.toString();
	}

	public static void main(String[] args) {
		Compiler compiler = new Compiler(new File("console.nesasm"));
		try {
			compiler.compile(new File("./console.nes"));
		} catch (CompilerException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
//		System.out.println(compiler.getLines());
//		System.out.println(compiler.getConstantList());
//		System.out.println(compiler.getLabelList());
//		System.out.println(compiler.getInstructionMap());
		System.out.println("Compilation terminée !");
	}

}
