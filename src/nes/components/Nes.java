package components;

import java.io.File;
import java.io.IOException;

import components.cpu.Cpu;
import components.ppu.Ppu;
import disassemble.Disassembler;
import disassemble.DisassemblyInfo;
import exceptions.InstructionNotSupportedException;
import exceptions.NotNesFileException;
import frame.GameFrame;
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
		cpu.setRomInstructions(info.getInstructions());
		cpu.warmUp();
		
		ppu.setMapper(mapper);
		
		GameFrame frame = new GameFrame(info.getInstructions());
		ppu.setScreen(frame.getScreenPanel());
		frame.initFrame(nesFile.getName());
	}
}
