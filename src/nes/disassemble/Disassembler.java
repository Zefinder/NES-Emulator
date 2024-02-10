package disassemble;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import exceptions.NotNesFileException;
import instructions.Instruction;
import instructions.InstructionInfo;

public class Disassembler {

	private static final int prgChunk = 0x4000;
	private static final int chrChunk = 0x2000;

	private static final Map<Integer, Instruction> instructionMap = InstructionInfo.getInstance().getInstructionMap();

	public Disassembler() {

	}

	/**
	 * Disassembles the instruction with the given opcode and operands. If the
	 * opcode does not exist, then this returns null. If needed operands are -1,
	 * then this returns null.
	 * 
	 * @param opcode   the opcode of the instruction
	 * @param operand1 the first operand of the instruction, it represents either an
	 *                 immediate value or the least significant byte of an address
	 * @param operand2 the second operand of the instruction, it represents the most
	 *                 significant byte of an address
	 * @return an instruction, or null if not possible
	 */
	public Instruction disassemble(int opcode, int operand1, int operand2) {
		Instruction instruction = instructionMap.get(opcode);

		if (instruction != null) {
			int byteNumber = instruction.getByteNumber();

			// Two bytes can be an address or a value
			// Three bytes is an address
			if (byteNumber == 2) {
				if (operand1 == -1) {
					instruction = null;
				} else {
					instruction = instruction.newInstruction(operand1);
				}
			} else if (byteNumber == 3) {
				if (operand1 == -1 && operand2 == -1) {
					instruction = null;
				} else {
					instruction = instruction.newInstruction(operand2 << 8 | operand1);
				}
			}
		}

		return instruction;
	}

	/**
	 * <p>
	 * This method disassembles a NES file and returns a lot of interesting things
	 * to begin to emulate.
	 * </p>
	 * 
	 * <p>
	 * This can take INES format as well as NES 2.0 format (even if the latter is
	 * not ready yet).
	 * </p>
	 *
	 * @param nesFile the NES file to read
	 * @return a lot of good informations
	 * @throws NotNesFileException if the file is not a NES file
	 * @throws IOException         if there is a problem with the NES file in itself
	 */
	public DisassemblyInfo disassembleFile(File nesFile) throws NotNesFileException, IOException {
		final FileInputStream reader = new FileInputStream(nesFile);
		byte[] allBytes = reader.readAllBytes();
		reader.close();

		// Reads the first magic bytes to be sure it's a NES file
		if (!isNesFile(allBytes)) {
			throw new NotNesFileException("The file isn't a NES file!");
		}

		// Checks if it's a NES 2.0 file
		if (isNes2File(allBytes)) {
			return disassembleNES2File(allBytes);
		} else {
			return disassembleINESFile(allBytes);
		}
	}

	private DisassemblyInfo disassembleINESFile(byte[] allBytes) {
		// Save useful flags for later treatment
		byte[] romFlags = disassembleINESHeaderFlags(allBytes);

		// Gets programmable ROM
		byte[] prgRom = getINESPrgRom(allBytes);

		// Gets character ROM
		byte[] chrRom = getINESChrRom(allBytes);

		// Puts instructions from prgRom
		int instructionNumber = romFlags[0] * prgChunk;
		Instruction[] instructions = new Instruction[instructionNumber];
		for (int instruction = 0; instruction < instructionNumber; instruction++) {
			int opcode = prgRom[instruction] & 0xFF;
			int operand1 = instruction + 1 < instructionNumber ? (prgRom[instruction + 1]) & 0xFF : -1;
			int operand2 = instruction + 2 < instructionNumber ? (prgRom[instruction + 2]) & 0xFF : -1;

			instructions[instruction] = disassemble(opcode, operand1, operand2);
		}

		return new DisassemblyInfoINES(romFlags, prgRom, chrRom, instructions);
	}

	private DisassemblyInfo disassembleNES2File(byte[] allBytes) {
		return null;
	}

	private byte[] getINESPrgRom(byte[] allBytes) {
		byte[] chrRom = Arrays.copyOfRange(allBytes, 0x10, allBytes[4] * prgChunk + 0x10);
		return chrRom;
	}

	private byte[] getINESChrRom(byte[] allBytes) {
		byte[] prgRom = Arrays.copyOfRange(allBytes, 0x10 + prgChunk, 0x10 + prgChunk + allBytes[5] * chrChunk);
		return prgRom;
	}

	private boolean isNesFile(byte[] allBytes) {
		byte[] nesVerif = { allBytes[0], allBytes[1], allBytes[2], allBytes[3] };
		return Arrays.equals(nesVerif, new byte[] { 0x4E, 0x45, 0x53, 0x1A });
	}

	private boolean isNes2File(byte[] allBytes) {
		return (allBytes[7] & 0x0C) == 0x08;
	}

	private byte[] disassembleINESHeaderFlags(byte[] allBytes) {
		return new byte[] { allBytes[4], allBytes[5], allBytes[6], allBytes[7], allBytes[8], allBytes[9],
				allBytes[10] };
	}

	public static void main(String[] args) throws NotNesFileException, IOException {
		File inputFile = new File("./console.nes");
		File resultFile = new File(inputFile.getName() + "lst");

		Disassembler disass = new Disassembler();
		DisassemblyInfo info = disass.disassembleFile(inputFile);

		final BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile));
		int instructionCount = 0;
		for (Instruction instruction : info.getInstructions()) {
			if (instruction != null) {
				writer.write(String.format("0x%04X: %s\n", 0x8000 + instructionCount, instruction.toString()));
			}

			instructionCount++;
		}

		writer.close();
	}
}
