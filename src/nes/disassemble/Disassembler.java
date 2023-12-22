package disassemble;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import exceptions.InstructionException;
import exceptions.NotNesFileException;
import instructions.InstructionInfo;

public class Disassembler {

	private final int prgChunk = 16384;
	private final int chrChunk = 8192;

	private final FileInputStream reader;
	private byte prgChunksNumber;
	private byte chrChunksNumber;
	@SuppressWarnings("unused")
	private byte flag6, flag7, flag8, flag9, flag10;

	private byte[] allBytes;

	private Map<Integer, InstructionInfo> instructionMap;

	public Disassembler(File nesFile) throws IOException, NotNesFileException {
		instructionMap = new LinkedHashMap<>();
		reader = new FileInputStream(nesFile);
		allBytes = reader.readAllBytes();
		reader.close();

		// Reads the first magic bytes to be sure it's a NES file
		if (!isNesFile())
			throw new NotNesFileException("The file isn't a NES file!");

	}

	public void disassemble(int counterOffset) throws IOException, InstructionException {
//		int counter = counterOffset;
//		byte byteRead;
//		for (int i = 0x10; i < allBytes.length; i++) {
//			byteRead = allBytes[i];
			
			// For the first byte we look in the table
//			InstructionInfo instruction = new InstructionInfo(byteRead);
//			int byteNumber = instruction.getByteNumber();
//
//			if (byteNumber == 2) {
//				if (i + 1 == allBytes.length)
//					throw new InstructionException("Incomplete instruction for " + instruction.getInstruction() + "!");
//
//				byteRead = allBytes[++i];
//				instruction.setArgument(byteRead, (byte) 0);
//
//			} else if (byteNumber == 3) {
//				byte msb, lsb;
//				if (i + 1 == allBytes.length)
//					throw new InstructionException("Incomplete instruction for " + instruction.getInstruction() + "!");
//				lsb = allBytes[++i];
//
//				if (i + 1 == allBytes.length)
//					throw new InstructionException("Incomplete instruction for " + instruction.getInstruction() + "!");
//				msb = allBytes[++i];
//
//				instruction.setArgument(lsb, msb);
//			}
//
//			instructionMap.put(counter, instruction);
//			counter += byteNumber;
//		}
	}

	public void showInstructions() {
		for (InstructionInfo instruction : instructionMap.values()) {
			System.out.println(instruction);
		}
	}

	public Map<Integer, InstructionInfo> getInstructionList() {
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
		byte[] prgRom = Arrays.copyOfRange(allBytes, 0x10, prgChunksNumber * prgChunk + 0x10);
		return prgRom;
	}

	public byte[] getChrRom() {
		byte[] prgRom = Arrays.copyOfRange(allBytes, 0x10 + prgChunk, 0x10 + prgChunk + chrChunksNumber * chrChunk);
		return prgRom;
	}

	private boolean isNesFile() throws IOException {
		byte[] nesVerif = { allBytes[0], allBytes[1], allBytes[2], allBytes[3] };
		return Arrays.equals(nesVerif, new byte[] { 0x4E, 0x45, 0x53, 0x1A });
	}

	public void disassembleHeaderFlags() throws IOException {
		prgChunksNumber = allBytes[4];
		chrChunksNumber = allBytes[5];
		flag6 = allBytes[6];
		flag7 = allBytes[7];
		flag8 = allBytes[8];
		flag9 = allBytes[9];
		flag10 = allBytes[10];
	}

	public static void main(String[] args) {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("NES files", "nes");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				Disassembler des = new Disassembler(chooser.getSelectedFile());
				
				System.out.println("Disassembling " + chooser.getSelectedFile().getName() + "...");
				des.disassemble(0x8000);
				
				System.out.println("Done! Writing in file...");
				des.writeInstructions(new File("./" + chooser.getSelectedFile().getName() + "lst"));
				System.out.println("Done!");
			} catch (IOException | NotNesFileException | InstructionException e) {
				e.printStackTrace();
			}
		}
	}
}
