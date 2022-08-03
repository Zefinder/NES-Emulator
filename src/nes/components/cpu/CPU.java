package nes.components.cpu;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import nes.components.Bus;
import nes.components.Component;
import nes.components.cpu.bus.CPUBus;
import nes.components.cpu.register.CPURegisters;
import nes.components.mapper.Mapper;
import nes.exceptions.AddressException;
import nes.instructions.Instruction;
import nes.instructions.Instruction.AddressingMode;
import nes.instructions.Instruction.InstructionSet;
import nes.instructions.InstructionReader;

public class CPU implements Component {

	// Registres CPU
	private CPURegisters registres;

	private int waitingCycles = 0;
	private boolean readyForNext;
//	private Instruction instruction;

	// Liste des constituants du bus
	private byte[] memory;
	private byte[] ppuRegisters;
	private byte[] apuIORegisters;
	private byte[] testMode;
	private byte[] catridge;

	private Bus bus;
	private InstructionReader instructionReader;

	private Thread cpuThread;
	private BlockingQueue<Instruction> instructionQueue;

	public CPU() {
		registres = new CPURegisters();

		memory = new byte[0x800];
		ppuRegisters = new byte[0x08];
		apuIORegisters = new byte[0x18];
		testMode = new byte[0x08];
		catridge = new byte[0xBFE0];

		bus = new CPUBus();

		// Mémoire et ses réflexions
		bus.addToMemoryMap(memory);
		bus.addToMemoryMap(memory);
		bus.addToMemoryMap(memory);
		bus.addToMemoryMap(memory);

		// Registres PPU et ses réflexions
		for (int i = 0x2007; i <= 0x3FFF; i = i + 0x08) {
			bus.addToMemoryMap(ppuRegisters);
		}

		// APU et les registres IO
		bus.addToMemoryMap(apuIORegisters);

		// APU et Registres IO non utilisés (test mode)
		bus.addToMemoryMap(testMode);

		// Cartouche
		bus.addToMemoryMap(catridge);

		instructionReader = new InstructionReader(bus);
		instructionQueue = new LinkedBlockingDeque<>();

		cpuThread = new Thread(new InstructionThread());
		cpuThread.start();
		readyForNext = true;
	}

	@Override
	public void start() throws AddressException {
		registres.setP((byte) 0x34);
		registres.setA((byte) 0);
		registres.setX((byte) 0);
		registres.setY((byte) 0);
		registres.setSp(0x1FD);

		bus.setByteToMemory(0x4017, (byte) 0x00);
		bus.setByteToMemory(0x4015, (byte) 0x00);
		for (int i = 0x4000; i <= 0x4013; i++) {
			bus.setByteToMemory(i, (byte) 0x00);
		}
	}

	@Override
	public void reset() throws AddressException {
		registres.setSp(registres.getSp() - 3);
		bus.setByteToMemory(0x4015, (byte) 0x00);

		// Gestion du son
		/*
		 * A, X, Y were not affected S was decremented by 3 (but nothing was written to
		 * the stack)[3] The I (IRQ disable) flag was set to true (status ORed with $04)
		 * The internal memory was unchanged APU mode in $4017 was unchanged APU was
		 * silenced ($4015 = 0) APU triangle phase is reset to 0 (i.e. outputs a value
		 * of 15, the first step of its waveform) APU DPCM output ANDed with 1 (upper 6
		 * bits cleared) 2A03G: APU Frame Counter reset. (but 2A03letterless: APU frame
		 * counter retains old value)
		 */
	}

	@Override
	public void tick() throws AddressException {
		Instruction instruction;
		if (waitingCycles == 0 && readyForNext) {
			readyForNext = false;
			if (registres.hasNMI()) {
				instruction = new Instruction(InstructionSet.NMI, AddressingMode.IMPLICIT);
			} else {
				instruction = getInstruction();
			}
			waitingCycles = instructionReader.getInstructionCycles(instruction, registres);
			try {
				instructionQueue.put(instruction);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//			cpuThread = new Thread(new InstructionThread(instruction));
//			cpuThread.start();
		} else {
			--waitingCycles;
		}
	}

	@Override
	public void initMapping(Mapper mapper) throws AddressException {
		registres.setPc(mapper.mapCPUMemory(bus));
	}

	public CPURegisters getRegistres() {
		return registres;
	}

	private Instruction getInstruction() throws AddressException {
		Instruction instruction = new Instruction(bus.getByteFromMemory(registres.getPc()));
		if (instruction.getByteNumber() == 2) {
			instruction.setArgument(bus.getByteFromMemory(registres.getPc() + 1), (byte) 0);

		} else if (instruction.getByteNumber() == 3) {
			instruction.setArgument(bus.getByteFromMemory(registres.getPc() + 1),
					bus.getByteFromMemory(registres.getPc() + 2));

		}
		return instruction;
	}

	/* A supprimer après les tests */
	public void nextInstruction() throws AddressException {
		Instruction toProcess = getInstruction();
		instructionReader.processInstruction(toProcess, registres);
		System.out.println(toProcess);
		registres.setPc(registres.getPc() + toProcess.getByteNumber());
	}

	private class InstructionThread implements Runnable {

//		private Instruction instruction;
//		
//		public InstructionThread(Instruction instruction) {
//			this.instruction = instruction;
//		}

		@Override
		public void run() {
			while (true)
				try {
					Instruction instruction = instructionQueue.take();
					System.out.println(instruction);
					instructionReader.processInstruction(instruction, registres);
					--waitingCycles;
					registres.setPc(registres.getPc() + instruction.getByteNumber());
					readyForNext = true;
				} catch (AddressException | InterruptedException e) {
					e.printStackTrace();
				}

		}

	}

}
