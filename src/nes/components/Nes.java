package components;

import java.io.File;
import java.io.IOException;

import disassemble.Disassembler;
import disassemble.DisassemblyInfo;
import exceptions.InstructionNotSupportedException;
import exceptions.NotNesFileException;
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
			System.err.println("Not a PAL file, this is not taken into account...");
			System.exit(1);
		}

		final Cpu cpu = Cpu.getInstance();

		if (info.getMapper() != 0) {
			System.err.println("Mapper %d not implemented...".formatted(info.getMapper()));
			System.exit(2);
		}

		cpu.setMapper(new Mapper0(info.getPrgRom(), info.getChrRom()));
		cpu.setRomInstructions(info.getInstructions());

		// Must be set by the mapper or by some init function
		cpu.cpuInfo.PC = 0x8000;
		Thread cpuThread = new Thread(() -> {
			try {
				cpuCycle(cpu);
			} catch (InstructionNotSupportedException e) {
				e.printStackTrace();
			}
		});

		cpuThread.start();
	}

	private static void cpuCycle(Cpu cpu) throws InstructionNotSupportedException {
		// Time for next activation (in ns)
		long nextTick = System.nanoTime();

		while (true) {
			long currentTime = System.nanoTime();

			// If it's time to tick, we tick
			if (currentTime >= nextTick) {
				// Tick!
				int cycles = cpu.tick();

				// Setting next tick
				nextTick = currentTime + cycles * Cpu.CLOCK_SPEED;
			}
		}
	}
}
