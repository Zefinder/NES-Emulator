package decompile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import exceptions.InstructionException;
import exceptions.NotNesFileException;
import instructions.Instruction;
import instructions.Instruction.AddressingMode;
import instructions.Instruction.InstructionSet;

public class Desassembler {

	private final int prgChunk = 16384;
	private final int chrChunk = 8192;

	private final FileInputStream reader;
	private byte prgChunksNumber;
	private byte chrChunksNumber;
	@SuppressWarnings("unused")
	private byte flag6, flag7, flag8, flag9, flag10;

	private byte[] allBytes;
	private byte[] prgRom;
	private byte[] chrRom;

	private Map<Integer, Instruction> instructionMap;

	public Desassembler(File nesFile) throws IOException, NotNesFileException {
		instructionMap = new TreeMap<>();
		reader = new FileInputStream(nesFile);
		allBytes = reader.readAllBytes();

		disassembleHeaderFlags();

		prgRom = Arrays.copyOfRange(allBytes, 0x10, prgChunksNumber * prgChunk + 0x10);
		chrRom = Arrays.copyOfRange(allBytes, 0x10 + prgChunksNumber * prgChunk,
				0x10 + prgChunksNumber * prgChunk + chrChunksNumber * chrChunk);

		reader.close();

		if (!isNesFile())
			throw new NotNesFileException("The file isn't a NES file!");

	}

	private void disassembleHeaderFlags() throws IOException {
		prgChunksNumber = allBytes[4];
		chrChunksNumber = allBytes[5];
		flag6 = allBytes[6];
		flag7 = allBytes[7];
		flag8 = allBytes[8];
		flag9 = allBytes[9];
		flag10 = allBytes[10];
	}

	public void disassemble(int counterOffset) throws IOException, InstructionException {
		int counter = counterOffset;
		boolean finished = false;

		System.out.println("Starting main branch desassembling...");
		while (!finished) {
			Instruction instruction = getInstruction(counter);
			instructionMap.put(counter, instruction);

			if (isJump(instruction)) {
				int address = instruction.getAdress();

				if (instruction.getAddressingMode() == AddressingMode.INDIRECT) {
					System.out.println("Indirect jump detected, aborting main branch...");
					finished = true;
				}
				if (address == counter)
					finished = true;

				counter = address;

			} else if (isSubRoutine(instruction)) {
				int address = instruction.getAdress();
				if (!instructionMap.containsKey(address))
					subRoutine(address);

				counter += 3;
			} else
				counter += instruction.getByteNumber();
		}
		
		System.out.println("Starting NMI desassembling...");

		int lsb = prgRom[0xFFFA - 0x8000];
		int msb = prgRom[0xFFFB - 0x8000];
		lsb = (lsb < 0 ? lsb + 256 : lsb);
		msb = (msb < 0 ? msb + 256 : msb);
		int address = (msb << 8) | lsb;
		nmi(address);
	}

	private Instruction getInstruction(int counter) throws InstructionException {
		counter = counter - 0x8000;
		byte byteRead = prgRom[counter];
		byte msb, lsb = 0;

		Instruction instruction = new Instruction(byteRead);
		int byteNumber = instruction.getByteNumber();

		if (byteNumber >= 2) {
			if (counter + 1 == prgRom.length)
				throw new InstructionException("Incomplete instruction for " + instruction.getInstruction() + "!");

			lsb = prgRom[++counter];
			instruction.setArgument(lsb, 0);

		}
		if (byteNumber == 3) {
			if (counter + 1 == prgRom.length)
				throw new InstructionException("Incomplete instruction for " + instruction.getInstruction() + "!");
			msb = prgRom[++counter];

			instruction.setArgument(lsb, msb);
		}

		return instruction;
	}

	private void subRoutine(int counter) throws InstructionException {
		Instruction instruction;

		do {
			instruction = getInstruction(counter);
			instructionMap.put(counter, instruction);

			if (isJump(instruction)) {
				if (instruction.getAddressingMode() == AddressingMode.ABSOLUTE)
					counter = instruction.getAdress();
				else {
					System.out.println("Indirect jump detected, return subroutine...");
					break;
				}

			} else if (isSubRoutine(instruction)) {
				int address = instruction.getAdress();
				if (!instructionMap.containsKey(address))
					subRoutine(address);

				counter += 3;
			} else
				counter += instruction.getByteNumber();
		} while (instruction == null || instruction.getInstruction() != InstructionSet.RTS);
	}

	private void nmi(int counter) throws InstructionException {
		Instruction instruction;

		do {
			instruction = getInstruction(counter);
			instructionMap.put(counter, instruction);

			if (isJump(instruction)) {
				if (instruction.getAddressingMode() == AddressingMode.ABSOLUTE)
					counter = instruction.getAdress();
				else {
					System.out.println("Indirect jump detected, aborting nmi...");
					break;
				}

			} else if (isSubRoutine(instruction)) {
				int address = instruction.getAdress();
				if (!instructionMap.containsKey(address))
					subRoutine(address);

				counter += 3;
			} else
				counter += instruction.getByteNumber();
		} while (instruction == null || instruction.getInstruction() != InstructionSet.RTI);
	}

	private boolean isJump(Instruction instruction) {
		return instruction.getInstruction() == InstructionSet.JMP;
	}

	private boolean isSubRoutine(Instruction instruction) {
		return instruction.getInstruction() == InstructionSet.JSR;
	}

	public Map<Integer, Instruction> getInstructionList() {
		if (prgChunksNumber == 1) {
			try {
				disassemble(0xC000);
			} catch (IOException | InstructionException e) {
				e.printStackTrace();
			}
		}
		return instructionMap;
	}

	public void writeInstructions(File file) throws IOException {
		if (file.exists())
			file.delete();

		file.createNewFile();

		BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
		Object[] keys = instructionMap.keySet().toArray();
		Object[] instructions = instructionMap.values().toArray();
		for (int i = 0; i < instructionMap.size(); i++) {
			writer.append(String.format("0x%04X:\t\t", keys[i]) + instructions[i].toString() + "\n");
		}

		writer.close();
	}

	public int getMapper() {
		return flag6 >> 4;
	}

	public boolean getMirroring() {
		return (flag6 & 0b00000001) == 1;
	}

	public int getPrgChunksNumber() {
		return prgChunksNumber;
	}

	public int getChrChunksNumber() {
		return chrChunksNumber;
	}

	public byte[] getPrgRom() {
		return prgRom;
	}

	public byte[] getChrRom() {
		return chrRom;
	}

	private boolean isNesFile() throws IOException {
		byte[] nesVerif = { allBytes[0], allBytes[1], allBytes[2], allBytes[3] };
		return Arrays.equals(nesVerif, new byte[] { 0x4E, 0x45, 0x53, 0x1A });
	}

	public static void main(String[] args) {
		try {
			Desassembler des = new Desassembler(new File("./Super Mario Bros.nes"));
			des.disassemble(0x8000);
			des.writeInstructions(new File("./Super Mario Bros.neslst"));
		} catch (IOException | NotNesFileException | InstructionException e) {
			e.printStackTrace();
		}
	}

}
