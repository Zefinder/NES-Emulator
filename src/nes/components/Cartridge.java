package components;

import java.io.File;
import java.io.IOException;

import disassemble.Disassembler;
import disassemble.DisassemblyInfo;
import exceptions.NotNesFileException;
import instructions.Instruction;
import mapper.Mapper;
import mapper.Mapper0;

public class Cartridge {

	private static final int INSTRUCTION_NUMBER = 0x8000;
	
	private final String fileName;
	private final Disassembler disassembler;
	
	private byte[] prgRom;
	private byte[] chrRom;
	private Mapper mapper;
	
	private Instruction[] instructions;
	
	public Cartridge(String fileName) {
		this.fileName = fileName;
		this.disassembler = new Disassembler();
		
		this.instructions = new Instruction[INSTRUCTION_NUMBER];
	}
	
	public void loadGame() throws NotNesFileException, IOException {
		DisassemblyInfo info = disassembler.disassembleFile(new File(fileName));
		
		if (!info.isPALSystem()) {
			System.err.println("Not a PAL file, running but can do weird things...");
		}
		
		if (info.getMapper() != 0) {
			System.err.println("Mapper %d not implemented...".formatted(info.getMapper()));
			System.exit(2);
		}
		prgRom = info.getPrgRom();
		chrRom = info.getChrRom();
		// TODO Create MapperFactory to input mapper number and return mapper
		mapper = new Mapper0(prgRom, chrRom);
		
		Instruction[] romInstructions = info.getInstructions();
		
		// There are 0x8000 instructions, but there can be only 0x4000 for old/small games
		// In this case, there are 0x4000 bytes mirrored
		if (romInstructions.length != INSTRUCTION_NUMBER) {			
			for (int instructionNumber = 0; instructionNumber < INSTRUCTION_NUMBER / 2; instructionNumber++) {
				instructions[instructionNumber] = romInstructions[instructionNumber];
				instructions[INSTRUCTION_NUMBER + instructionNumber] = romInstructions[instructionNumber];
			}
		} else {			
			for (int instructionNumber = 0; instructionNumber < INSTRUCTION_NUMBER; instructionNumber++) {
				instructions[instructionNumber] = romInstructions[instructionNumber];
			}
		}
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public Mapper getMapper() {
		return mapper;
	}
	
	public Memory getCpuBusMemory() {
		return mapper.getCpuBusMemory();
	}

	public Memory getPpuBusMemory() {
		return mapper.getPpuBusMemory();
	}
	
	public Instruction[] getInstructions() {
		return instructions;
	}
	
	public byte[] getPrgRom() {
		return prgRom;
	}
	
	public byte[] getChrRom() {
		return chrRom;
	}

}
