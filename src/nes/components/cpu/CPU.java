package nes.components.cpu;

import java.util.ArrayList;

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

public class CPU implements Component, Runnable {

	// Registres CPU
	private CPURegisters registres;

	private volatile int waitingCycles = 0;
	private volatile boolean readyForNext;

	// A effacer après les tests
	private ArrayList<Instruction> listInstructions;
	private ArrayList<Integer> listPc;

	private Bus bus;
	private InstructionReader instructionReader;

	private Thread cpuThread;

	public CPU() {
		registres = new CPURegisters();

		bus = new CPUBus(0x10000);

		instructionReader = new InstructionReader(bus);

		listInstructions = new ArrayList<>();
		listPc = new ArrayList<>();

		cpuThread = new Thread(this);
		cpuThread.setName("Thread CPU");
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

		if (waitingCycles <= 0) {
			while (!readyForNext) {
				// Si pas prêt, alors on fait rien
			}
			readyForNext = false;

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
		// TODO Prendre depuis l'instruction
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
	public Bus getBus() {
		return bus;
	}

	@Override
	public void run() {
		Instruction instruction;
		while (true)
			try {
				if (!readyForNext) {
					if (registres.hasNMI()) {
						instruction = new Instruction(InstructionSet.NMI, AddressingMode.NMI);
					} else {
						instruction = getInstruction();
//						if (instruction.getAdress() == 0x2007)
//							System.out.println("AAAAAAAAAAAH");
					}

					if (waitingCycles < -1)
						System.out.println("Cycles d'attente en retard : " + waitingCycles);

//					waitingCycles = instructionReader.getInstructionCycles(instruction, registres);
					waitingCycles = instructionReader.processInstruction(instruction, registres);
					--waitingCycles;
					registres.setPc(registres.getPc() + instruction.getByteNumber());

					listInstructions.add(instruction);
					listPc.add(registres.getPc());
					readyForNext = true;
				}
			} catch (AddressException e) {
				e.printStackTrace();
			}

	}

}
