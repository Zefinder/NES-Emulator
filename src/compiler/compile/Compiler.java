package compile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import instructions.Instruction;
import instructions.Instruction.AddressingMode;
import instructions.Instruction.InstructionSet;

public class Compiler {

	private int currentAddress;
	private int registerSize, memorySize;

	private List<String> linesToCompile;
	private List<Constant> constantList;
	private List<Label> labelList;

	private Map<Integer, Instruction> instructionMap;

	public Compiler(File toCompile) {
		constantList = new ArrayList<>();
		linesToCompile = new ArrayList<>();
		labelList = new ArrayList<>();

		instructionMap = new LinkedHashMap<>();

		registerSize = 0;
		memorySize = 0;

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

				if (!line.isEmpty())
					linesToCompile.add(line);
			}

			reader.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void compile() throws CompilerException {
		boolean error = readCompilationDirectives();
		if (error)
			throw new CompilerException("Compiler encountered errors while reading directives!");

		error = readConstants();
		if (error)
			throw new CompilerException("Compiler encountered errors while reading constants!");

		error = initLabelsAndInstructions();
		if (error)
			throw new CompilerException("Compiler encountered errors while reading instructions!");
	}

	private boolean readCompilationDirectives() {
		boolean hadError = false;
		List<String> toRemove = new ArrayList<>();
		for (int i = 0; i < linesToCompile.size(); i++) {
			String line = linesToCompile.get(i);

			try {
				if (line.contains(".mem ")) {
					if (memorySize != 0)
						System.out.println(
								String.format("[WARNING]: Multiple definition of memory size (line %d)", i + 1));

					try {
						memorySize = getNumberFromString(line.substring(5));
						toRemove.add(line);
					} catch (NumberFormatException e) {
						CompilerException.callNumberException(i + 1, line, 5);
					}
				} else if (line.contains(".index ")) {
					if (registerSize != 0)
						System.out.println(
								String.format("[WARNING]: Multiple definition of register size (line %d)", i + 1));

					try {
						registerSize = getNumberFromString(line.substring(7));
						toRemove.add(line);
					} catch (NumberFormatException e) {
						CompilerException.callNumberException(i + 1, line, 7);
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

		linesToCompile.removeAll(toRemove);
		return hadError;
	}

	private boolean readConstants() {
		boolean hadError = false;
		List<String> toRemove = new ArrayList<>();

		for (int i = 0; i < linesToCompile.size(); i++) {
			String line = linesToCompile.get(i);
			try {
				if (line.contains("=")) {
					String[] split = line.split("=");
					if (split.length != 2)
						throw new CompilerException(
								String.format("[ERROR]: Syntax error for constant definition (line %d)\n%s\n%s\n",
										i + 1, line, padLeftSpaces("^", line.indexOf("=", line.indexOf("=") + 1))));

					String constantName = split[0].strip();
					int value = 0;
					try {
						value = getNumberFromString(split[1].strip());
					} catch (NumberFormatException e) {
						CompilerException.callNumberException(i + 1, line, constantName.length() + 2);
					}

					if (constantName.isEmpty())
						throw new CompilerException(
								String.format("[ERROR]: Constant name can't be empty (line %d)\n%s\n^\n", i + 1, line));

					if (constantName.contains(" "))
						throw new CompilerException(
								String.format("[ERROR]: Constant name can't contain a space (line %d)\n%s\n%s\n", i + 1,
										line, padLeftSpaces("^", constantName.indexOf(' '))));

					if (constantName.contains("+") || constantName.contains("-"))
						throw new CompilerException(String.format(
								"[ERROR]: Constant name can't contain a + or - sign (line %d)\n%s\n%s\n", i + 1, line,
								padLeftSpaces("^", Math.max(constantName.indexOf('+'), constantName.indexOf('-')))));

					Constant constant = new Constant(constantName, value);
					if (constantList.contains(constant))
						throw new CompilerException(String.format("[ERROR]: Duplicate constant name (line %d)", i + 1));

					if (!Charset.forName("US-ASCII").newEncoder().canEncode(constantName))
						System.out.println(String.format(
								"[WARNING]: You should use ASCII only characters for constant name %s (line %d)",
								constantName, i + 1));

					constantList.add(constant);
					toRemove.add(line);

				} else if (line.contains(".eq ")) {
					String[] split = line.split(" ");
					if (split.length != 3)
						throw new CompilerException(String
								.format("[ERROR]: Syntax error for constant definition (line %d)\n%s\n", i + 1, line));

					if (!split[0].equals(".eq"))
						throw new CompilerException(String.format(
								"[ERROR]: Syntax error for constant definition, must begin with \".eq\" (line %d)\n%s\n^\n",
								i + 1, line));

					String constantName = split[1].strip();

					int value = 0;
					try {
						value = getNumberFromString(split[2].strip());
					} catch (NumberFormatException e) {
						CompilerException.callNumberException(i + 1, line, 4 + constantName.length());
					}

					if (constantName.isEmpty())
						throw new CompilerException(
								String.format("[ERROR]: Constant name can't be empty (line %d)\n%s\n%s\n", i + 1, line,
										padLeftSpaces("^", 4)));

					if (constantName.contains(" "))
						throw new CompilerException(
								String.format("[ERROR]: Constant name can't contain a space (line %d)\n%s\n%s\n", i + 1,
										line, padLeftSpaces("^", constantName.indexOf(' '))));

					if (constantName.contains("+") || constantName.contains("-"))
						throw new CompilerException(String.format(
								"[ERROR]: Constant name can't contain a + or - sign (line %d)\n%s\n%s\n", i + 1, line,
								padLeftSpaces("^", Math.max(constantName.indexOf('+'), constantName.indexOf('-')))));

					Constant constant = new Constant(constantName, value);
					if (constantList.contains(constant))
						throw new CompilerException(String.format("[ERROR]: Duplicate constant name (line %d)", i + 1));

					if (!Charset.forName("US-ASCII").newEncoder().canEncode(constantName))
						System.out.println(String.format(
								"[WARNING]: You should use ASCII only characters for constant name %s (line %d)",
								constantName, i + 1));

					constantList.add(constant);
					toRemove.add(line);
				}
			} catch (CompilerException e) {
				System.err.println(e.getMessage());
				hadError = true;
			}
		}

		linesToCompile.removeAll(toRemove);
		return hadError;
	}

	// TODO: check .eq dans les instructions sans espace pour conseiller une
	// constante (idem pour .mem et .index)
	private boolean initLabelsAndInstructions() {
		boolean hadError = false;

		for (int i = 0; i < linesToCompile.size(); i++) {
			String line = linesToCompile.get(i);

			try {
				// On modifie l'adresse d'écriture
				if (line.startsWith(".org ")) {
					try {
						currentAddress = getNumberFromString(line.substring(5));
					} catch (NumberFormatException e) {
						CompilerException.callNumberException(i + 1, line, 5);
					}

					// On vérifie s'il y a un label
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

		return hadError;
	}

	private void readLabel(String labelName, String line, int i) throws CompilerException {
		if (labelName.isEmpty())
			throw new CompilerException(
					String.format("[ERROR]: Label name can't be empty (line %d)\n%s\n^\n", i + 1, line));

		if (labelName.contains(" "))
			throw new CompilerException(String.format("[ERROR]: Label name can't contain a space (line %d)\n%s\n%s\n",
					i + 1, line, padLeftSpaces("^", labelName.indexOf(' '))));

		if (labelName.contains("+") || labelName.contains("-"))
			throw new CompilerException(
					String.format("[ERROR]: Label name can't contain a + or - sign (line %d)\n%s\n%s\n", i + 1, line,
							padLeftSpaces("^", Math.max(labelName.indexOf('+'), labelName.indexOf('-')))));

		if (!Charset.forName("US-ASCII").newEncoder().canEncode(labelName))
			System.out.println(String.format(
					"[WARNING]: You should use ASCII only characters for label name %s (line %d)", labelName, i + 1));

		Label label = new Label(labelName, currentAddress);

		if (labelList.contains(label))
			throw new CompilerException(String.format("[ERROR]: Duplicate label name (line %d)", i + 1));

		labelList.add(label);
	}

	private void readInstruction(String instructionLine, String line, int i) throws CompilerException {
		String[] split = instructionLine.split(" ");

		String instructionName = split[0].strip();
		Instruction instruction;
		if (instructionName.toLowerCase().equals(".db")) {
			if (split.length == 1) {
				throw new CompilerException(
						String.format("[ERROR]: Byte directive argument cannot be empty (line %d)\n%s\n%s\n", i + 1,
								line, padLeftSpaces("^", 4)));
			}

			String[] bytes = instructionLine.substring(4).split(",");
			int offset = 4;
			for (String s : bytes) {
				if (s.strip().length() == 0)
					throw new CompilerException(
							String.format("[ERROR]: Byte directive argument cannot be empty (line %d)\n%s\n%s\n", i + 1,
									line, padLeftSpaces("^", offset)));

				instruction = new Instruction(InstructionSet.DB, AddressingMode.DB);
				try {
					int value = getNumberFromString(s.strip());
					if (Integer.toBinaryString(value).length() > 8)
						throw new CompilerException(String.format(
								"[ERROR]: Number of bits for byte directive is greater than a byte (here %d bits). Try to use \".dw\" for 16 bits numbers (line %d)\n%s\n%s\n",
								Integer.toBinaryString(value).length(), i + 1, line, padLeftSpaces("^", offset)));

					instruction.setArgument(value, 0);
				} catch (NumberFormatException e) {
					int index;
					if ((index = constantList.indexOf(new Constant(s.strip(), 0))) != -1)
						instruction.setArgument(constantList.get(index).getValue(), 0);
					else
						throw new CompilerException(String.format(
								"[ERROR]: Constant not found when reading byte directive (line %d)\n%s\n%s\n", i + 1,
								line, padLeftSpaces("^", offset)));
				}

				offset += s.length() + 1;
				instructionMap.put(currentAddress, instruction);
				currentAddress += instruction.getByteNumber();
			}
		} else if (instructionName.toLowerCase().equals(".dw")) {
//			instruction = new Instruction(InstructionSet.DW, AddressingMode.DW);
			// TODO Faire sur le modèle de db
		} else if (instructionName.toUpperCase().equals("DB") || instructionName.toUpperCase().equals("DW")) {
			throw new CompilerException(
					String.format("[ERROR]: Error in instruction name, add a '.' at the beginning (line %d)\n%s\n%s\n",
							i + 1, line, padLeftSpaces("^", 0)));
		} else {
			// TODO Instructions et mettre les arguments SSI ce n'est pas un jump ou un
			// branch (Tout ce qui n'est pas un label)

			InstructionSet[] instructions = InstructionSet.values();
			AddressingMode addressingSelected = null;
			for (InstructionSet ins : instructions) {
				int lsb = 0, msb = 0;

				if (ins.toString().equals(instructionName.toUpperCase())) {
					byte[] opCodes = ins.getOpCodes();
					if (split.length == 1) {
						// On regarde si c'est en implicite ou accumulateur
						if (opCodes[AddressingMode.IMPLICIT.ordinal()] != (byte) 0xFF) {
							addressingSelected = AddressingMode.IMPLICIT;
						} else if (opCodes[AddressingMode.ACCUMULATOR.ordinal()] != (byte) 0xFF) {
							addressingSelected = AddressingMode.ACCUMULATOR;
						} else
							CompilerException.callAddressingException(ins, AddressingMode.IMPLICIT, i + 1);

					} else {
						String argument = instructionLine.substring(4).strip().replace(" ", "");
						// On regarde pour les différents modes d'adressage
						// Accumulateur
						if (argument.equalsIgnoreCase("A"))
							if (opCodes[AddressingMode.ACCUMULATOR.ordinal()] != (byte) 0xFF)
								addressingSelected = AddressingMode.ACCUMULATOR;
							else
								CompilerException.callAddressingException(ins, AddressingMode.ACCUMULATOR, i + 1);

						// Immédiat (non constante)
						else if (argument.startsWith("#")) {
							if (opCodes[AddressingMode.IMMEDIATE.ordinal()] != 0xFF) {
								addressingSelected = AddressingMode.IMMEDIATE;
								try {
									lsb = getNumberFromString(argument.substring(1));
									if (Integer.toBinaryString(lsb).length() > registerSize)
										throw new CompilerException(String.format(
												"[ERROR]: Immediate value must have a bit number (here %d) lesser or equal than max register bit number (here %d) (line %d)\n",
												Integer.toBinaryString(lsb).length(), registerSize, i + 1));

								} catch (NumberFormatException e) {
									CompilerException.callNumberException(i + 1, line, 4);
								}
							} else
								CompilerException.callAddressingException(ins, AddressingMode.IMMEDIATE, i + 1);
						}

						// Zero page et absolu (non constante)
						else if (argument.startsWith("$")) {
							String[] args = argument.split(",");
							try {
								int value = getNumberFromString(args[0]);

								// Zero page
								if (Integer.toBinaryString(value).length() <= 8) {
									if (args.length == 1) {
										if (opCodes[AddressingMode.ZEROPAGE.ordinal()] != -1) {
											addressingSelected = AddressingMode.ZEROPAGE;
											lsb = value;
										} else
											CompilerException.callAddressingException(ins, AddressingMode.ZEROPAGE,
													i + 1);

									} else if (args.length == 2) {
										// Zero page X
										if (args[1].equalsIgnoreCase("X")) {
											if (opCodes[AddressingMode.ZEROPAGE_X.ordinal()] != -1) {
												addressingSelected = AddressingMode.ZEROPAGE_X;
												lsb = value;
											} else
												CompilerException.callAddressingException(ins,
														AddressingMode.ZEROPAGE_X, i + 1);

											// Zero page Y
										} else if (args[1].equalsIgnoreCase("Y")) {
											if (opCodes[AddressingMode.ZEROPAGE_Y.ordinal()] != -1) {
												addressingSelected = AddressingMode.ZEROPAGE_Y;
												lsb = value;
											} else
												CompilerException.callAddressingException(ins,
														AddressingMode.ZEROPAGE_Y, i + 1);

										} else
											throw new CompilerException(String.format(
													"[ERROR]: Syntax error for instruction %s, expected X or Y (line %d)\n%s\n%s\n",
													ins.toString(), i + 1, line,
													padLeftSpaces("^", line.indexOf(',') + 1)));
									} else
										throw new CompilerException(String.format(
												"[ERROR]: Syntax error for instruction %s (line %d)\n%s\n%s\n",
												ins.toString(), i + 1, line,
												padLeftSpaces("^", line.indexOf(",", line.indexOf(",") + 1))));
								}

								// Absolu
								else if (Integer.toBinaryString(value).length() <= 16) {
									if (args.length == 1) {
										if (opCodes[AddressingMode.ABSOLUTE.ordinal()] != -1) {
											addressingSelected = AddressingMode.ABSOLUTE;
											lsb = value & 0xFF;
											msb = (value & 0xFF00) >> 8;
										} else
											CompilerException.callAddressingException(ins, AddressingMode.ZEROPAGE,
													i + 1);

									} else if (args.length == 2) {
										// Absolu X
										if (args[1].equalsIgnoreCase("X")) {
											if (opCodes[AddressingMode.ABSOLUTE_X.ordinal()] != -1) {
												addressingSelected = AddressingMode.ABSOLUTE_X;
												lsb = value & 0xFF;
												msb = (value & 0xFF00) >> 8;
											} else
												CompilerException.callAddressingException(ins,
														AddressingMode.ABSOLUTE_X, i + 1);

											// Absolu Y
										} else if (args[1].equalsIgnoreCase("Y")) {
											if (opCodes[AddressingMode.ABSOLUTE_Y.ordinal()] != -1) {
												addressingSelected = AddressingMode.ABSOLUTE_Y;
												lsb = value & 0xFF;
												msb = (value & 0xFF00) >> 8;
											} else
												CompilerException.callAddressingException(ins,
														AddressingMode.ABSOLUTE_Y, i + 1);

										} else
											throw new CompilerException(String.format(
													"[ERROR]: Syntax error for instruction %s, expected X or Y (line %d)\n%s\n%s\n",
													ins.toString(), i + 1, line,
													padLeftSpaces("^", line.indexOf(',') + 1)));
									} else
										throw new CompilerException(String.format(
												"[ERROR]: Syntax error for instruction %s (line %d)\n%s\n%s\n",
												ins.toString(), i + 1, line,
												padLeftSpaces("^", line.indexOf(",", line.indexOf(",") + 1))));
								} else
									throw new CompilerException(String.format(
											"[ERROR]: Number for zero page or absolute addressing must be lesser or equal than 16 bits (here %d) (line %d)\n%s\n%s\n",
											Integer.toBinaryString(value).length(), i + 1, line,
											padLeftSpaces("^", line.indexOf('$'))));

							} catch (NumberFormatException e) {
								CompilerException.callNumberException(i + 1, line, 4);
							}

						} else
							return;
					}

					instruction = new Instruction(ins, addressingSelected);
					instruction.setArgument(lsb, msb);
					instructionMap.put(currentAddress, instruction);
					currentAddress += instruction.getByteNumber();
				}
			} // TODO: Si pas trouvé -> erreur
		}
	}

	private int getNumberFromString(String strNumber) {
		int result;

		switch (strNumber.charAt(0)) {
		case '$':
			result = Integer.parseInt(strNumber, 1, strNumber.length(), 16);
			break;

		case '%':
			result = Integer.parseInt(strNumber, 1, strNumber.length(), 2);
			break;

		default:
			result = Integer.valueOf(strNumber);
			break;
		}

		return result;
	}

	private List<String> getLines() {
		return linesToCompile;
	}

	private List<Constant> getConstantList() {
		return constantList;
	}

	private List<Label> getLabelList() {
		return labelList;
	}

	private Map<Integer, Instruction> getInstructionMap() {
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
		Compiler compiler = new Compiler(new File("test.nesasm"));
		System.out.println(compiler.getLines());
		try {
			compiler.compile();
		} catch (CompilerException e) {
			System.err.println(e.getMessage());
		}
		System.out.println(compiler.getLines());
		System.out.println(compiler.getConstantList());
		System.out.println(compiler.getLabelList());
		System.out.println(compiler.getInstructionMap());
	}

}
