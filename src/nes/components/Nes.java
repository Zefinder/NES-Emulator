package components;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import components.cpu.Cpu;
import components.ppu.Ppu;
import disassemble.Disassembler;
import disassemble.DisassemblyInfo;
import exceptions.InstructionNotSupportedException;
import exceptions.NotNesFileException;
import frame.GameFrame;
import instructions.Instruction;
import mapper.Mapper;
import mapper.Mapper0;

public class Nes {

	public Nes() {

	}

	public static void main(String[] args) throws NotNesFileException, IOException, InstructionNotSupportedException {
		// TODO When everything will be ok to run
		
		// Call CPU

		// Call PPU

		// Create frame

		// TODO Remove below when tests over
		File nesFile = new File("./Super Mario Bros.nes");
		Disassembler disassembler = new Disassembler();
		DisassemblyInfo info = disassembler.disassembleFile(nesFile);

		if (!info.isPALSystem()) {
			System.err.println("Not a PAL file, running but can do weird things...");
		}

		final Cpu cpu = Cpu.getInstance();
		final Ppu ppu = Ppu.getInstance();

		if (info.getMapper() != 0) {
			System.err.println("Mapper %d not implemented...".formatted(info.getMapper()));
			System.exit(2);
		}

		Mapper mapper = new Mapper0(info.getPrgRom(), info.getChrRom());
		
		cpu.setMapper(mapper);
		Instruction[] romInstructions = info.getInstructions();
		if (info.getInstructions().length != 0x8000) {
			
			Instruction[] instructions = new Instruction[0x8000];
			for (int instructionNumber = 0; instructionNumber < 0x8000; instructionNumber++) {
				instructions[instructionNumber] = romInstructions[instructionNumber % 0x4000];
			}
			cpu.setRomInstructions(instructions);
		} else {			
			cpu.setRomInstructions(romInstructions);
		}
		cpu.warmUp();
		
		ppu.setMapper(mapper);
		
		Random r = new Random();
		for (int address = 0; address < 0x800; address++) {
			mapper.writeCpuBus(address, r.nextInt(0x100));
		}
		
		// Palette colors
		cpu.storeMemory(0x2006, 0x3F);
		cpu.storeMemory(0x2006, 0x00);

		// Palette 0
		cpu.storeMemory(0x2007, 0x03);
		cpu.storeMemory(0x2007, 0x04);
		cpu.storeMemory(0x2007, 0x05);
		cpu.storeMemory(0x2007, 0x06);

		// Palette 1
		cpu.storeMemory(0x2007, 0x21);
		cpu.storeMemory(0x2007, 0x17);
		cpu.storeMemory(0x2007, 0x18);
		cpu.storeMemory(0x2007, 0x19);

		// Palette 2
		cpu.storeMemory(0x2007, 0x0B);
		cpu.storeMemory(0x2007, 0x1D);
		cpu.storeMemory(0x2007, 0x2D);
		cpu.storeMemory(0x2007, 0x3D);
		
		GameFrame frame = new GameFrame(info.getInstructions(), mapper);
		ppu.setScreen(frame.getScreenPanel());
		frame.initFrame(nesFile.getName());
	}
}
