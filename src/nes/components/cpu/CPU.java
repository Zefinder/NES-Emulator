package components.cpu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import components.Bus;
import components.Component;
import components.cpu.bus.CPUBus;
import components.cpu.register.CPURegisters;
import components.mapper.Mapper;
import exceptions.AddressException;
import instructions.Instruction;
import instructions.Instruction.AddressingMode;
import instructions.Instruction.InstructionSet;
import instructions.InstructionReader;

public class CPU implements Component, Runnable {

	// Registres CPU
	private CPURegisters registres;

	private int waitingCycles = 0;
	private volatile boolean readyForNext;
	private Instruction instruction;

	private Map<Integer, Instruction> instructionMap;

	private List<Instruction> instructionList;
	private List<Integer> pcList;

	private Bus bus;
	private InstructionReader instructionReader;

	private Thread cpuThread;

	public CPU() {
		registres = new CPURegisters();
		bus = new CPUBus(0x10000);
		instructionReader = new InstructionReader(bus);

		instructionList = new ArrayList<>();
		pcList = new ArrayList<>();

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
		this.instructionMap = mapper.getInstructionMap();
	}

	public CPURegisters getRegistres() {
		return registres;
	}

	private Instruction getInstruction() throws AddressException {
		// FIXME On ne peut pas faire comme ça !
		// Désassembler les instructions jusqu'à un jump.
		// Refaire jusqu'au prochain jump
		int pc = registres.getPc();
		byte byteRead = bus.getByteFromMemory(pc);
		Instruction instruction = new Instruction(byteRead);
		int byteNumber = instruction.getByteNumber();
		byte lsb = bus.getByteFromMemory(pc + 1), msb = bus.getByteFromMemory(pc + 2);

		switch (byteNumber) {
		case 2:
			instruction.setArgument(lsb, 0);
			break;

		case 3:
			instruction.setArgument(lsb, msb);
			break;

		default:
			break;
		}
		return instruction;
	}

	/* A supprimer après les tests */
	public Bus getBus() {
		return bus;
	}

	@Override
	public void run() {
		
		while (true)
			try {
				if (!readyForNext) {
					if (waitingCycles > 0) {
						waitingCycles--;
						if (instruction == null)
							instruction = getInstruction();
						
						readyForNext = true;

					} else {
						if (registres.hasNMI()) {
							instruction = new Instruction(InstructionSet.NMI, AddressingMode.NMI);
							instructionReader.processInstruction(instruction, registres);
							instruction = getInstruction();
						}

						if (waitingCycles < -1)
							System.out.println("Cycles d'attente en retard : " + waitingCycles);

						if (instruction == null) {
							System.err.println(String.format("Instruction null, pc=0x%04X", registres.getPc()));
							System.exit(0);
						}

						waitingCycles = instructionReader.processInstruction(instruction, registres);
						--waitingCycles;
						registres.setPc(registres.getPc() + instruction.getByteNumber());

						instructionList.add(instruction);
						pcList.add(registres.getPc());
						readyForNext = true;
					}
				}
			} catch (AddressException e) {
				e.printStackTrace();
			}

	}

}
