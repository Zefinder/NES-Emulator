package components;

import disassemble.Disassembler;
import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;
import instructions.InstructionInfo;
import instructions.jump.BRKInstruction;
import mapper.Mapper;

public class Cpu {

	public static final int RESET_VECTOR = 0xFFFA;
	public static final int NMI_VECTOR = 0xFFFC;
	public static final int BREAK_VECTOR = 0xFFFE;

	private static final Cpu instance = new Cpu();

	/**
	 * Clock speed in nano seconds
	 */
	public static final int CLOCK_SPEED = 601;

	/* Mapper */
	private Mapper mapper;

	/* ROM Instructions */
	private Instruction[] romInstructions;

	/* Registers & Flags */
	public CpuInfo cpuInfo = new CpuInfo();

	private Cpu() {
	}

	/**
	 * Sets the mapper for the CPU. Do not change while running
	 * 
	 * @param mapper the mapper to use
	 */
	public void setMapper(Mapper mapper) {
		this.mapper = mapper;
	}

	/**
	 * Sets the ROM instructions. This is easy because as the section is in read
	 * only, they won't change!
	 * 
	 * @param romInstructions the instructions
	 */
	public void setRomInstructions(Instruction[] romInstructions) {
		this.romInstructions = romInstructions;
	}

	/**
	 * Fetches a value in memory
	 * 
	 * @param address the address to look for the value
	 * @return a value
	 */
	public int fetchMemory(int address) {
		return mapper.readCpuBus(address);
	}

	/**
	 * Fetches an address in memory
	 * 
	 * @param address the address to look for the address
	 * @return an address
	 */
	public int fetchAddress(int address) {
		int lsbFetched = mapper.readCpuBus(address);
		int msbFetched = mapper.readCpuBus((address + 1) & 0xFFFF);
		return msbFetched << 8 | lsbFetched;
	}

	/**
	 * Stores values in memory
	 * 
	 * @param address the address to store the value in the memory
	 * @param values  the values to store
	 */
	public void storeMemory(int address, int... values) {
		mapper.writeCpuBus(address, values);
	}

	/**
	 * Pushes the value into the stack
	 * 
	 * @param value the value to push
	 */
	public void push(int value) {
		// Remember that SP points on nothing
		int SP = cpuInfo.SP;

		// Put value in memory
		mapper.writeCpuBus(0x100 | SP, value);

		// Decrement SP (wrap around 0x100)
		cpuInfo.SP = (SP - 1) & 0xFF;
	}

	/**
	 * Pops a value from the stack
	 * 
	 * @return the value poped
	 */
	public int pop() {
		// Remember that SP points on nothing
		int SP = cpuInfo.SP;

		// Increment SP (wrap around 0x100)
		SP = (SP + 1) & 0xFF;

		// Put value in memory
		int value = mapper.readCpuBus(0x100 | SP);

		// Update SP
		cpuInfo.SP = SP;

		return value;
	}

	/**
	 * <p>
	 * This is a lie, this method doesn't really tick the CPU. In fact it will go to
	 * the next instruction... At least it returns how many (true) cycles it will
	 * need to tick (like truly) before the next instruction.
	 * </p>
	 * 
	 * <p>
	 * For example, immediate value ADC will return 2 cycles whereas absolute STA to
	 * OAM DMA will return 4 cycles + 513 (or 514 if DMA is in a put cycle). This is
	 * important! I must not forget about it!
	 * </p>
	 * 
	 * <p>
	 * This method checks if an NMI has been triggered BUT do not triggers it nor
	 * disables it. Triggering is the PPU work and disabling is done reading the
	 * 0x2002 PPU register with the CPU (resetting the NMI flag). This then pushes
	 * everything into the stack and jumps to NMI routine (like BRK) taking 7 cycles
	 * in total. We can consider the NMI as a virtual instruction taking priority on
	 * any other normally happening instructions. For more information on the NMI
	 * routine, maybe check the TODO ppu info
	 * </p>
	 * 
	 * <p>
	 * This method updates the DMA cycle status. As it is important (not really) to
	 * know which DMA cycle we are in before enabling it, we need to keep track of
	 * if it is in a put or a get cycle. If the number of cycles is even, we get
	 * back to the same cycle, so it is pretty easy to determine!
	 * </p>
	 * 
	 * @return the number of waiting cycles
	 * @throws InstructionNotSupportedException if an instruction is setup with an
	 *                                          unsupported addressing mode
	 */
	public int tick() throws InstructionNotSupportedException {
		// Check if NMI
		// TODO check for NMI

		// Get the instruction
		Instruction instruction;
		if (cpuInfo.PC >= 0x8000) {
			// If in the ROM it's great, there is an array for that!
			instruction = romInstructions[cpuInfo.PC - 0x8000];

		} else {
			System.out.println("Instruction not in ROM!");

			// Well we need to disassemble
			// We need to get the number of bytes we will need
			int opcode = fetchMemory(cpuInfo.PC);
			int byteNumber = InstructionInfo.getInstance().getByteNumberFromOpcode(opcode);

			// We declare operands and use the byte number to fetch them if needed (we don't
			// want to provoke a mapper secret sauce if not needed...
			int operand1 = -1;
			int operand2 = -1;

			// Two bytes
			if (byteNumber >= 2) {
				operand1 = fetchMemory(cpuInfo.PC + 1);
			}

			// Three bytes
			if (byteNumber == 3) {
				operand2 = fetchMemory(cpuInfo.PC + 2);
			}

			// Finally disassemble the instruction (way longer...)
			// TODO Maybe make it a singleton... But actually never should be here except
			// special cases
			Disassembler disassembler = new Disassembler();
			instruction = disassembler.disassemble(opcode, operand1, operand2);
		}

		if (instruction.toString().equals(new BRKInstruction(AddressingMode.IMPLICIT).toString())) {
			System.err.println("OUCH BRK");
			System.exit(3);
		}

		// Execute the instruction
		instruction.execute();

		// Get waiting cycles
		int cycles = instruction.getCycles();

		// Increment PC by the byte number of the instruction
		cpuInfo.PC = (cpuInfo.PC + instruction.getByteNumber()) & 0xFFFF;

		// If DMA requested, add cycles (+1 if DMA put)
		if (cpuInfo.dmaRequested) {
			// Reset the DMA request
			cpuInfo.dmaRequested = false;

			// Update the state with cycles we've done
			cpuInfo.dmaState = (cpuInfo.dmaState + cycles) & 0b1;

			// Adding the DMA transaction
			cycles += cpuInfo.dmaHaltCycles + cpuInfo.dmaState;
		}

		// Update DMA state (even cycles = same state)
		cpuInfo.dmaState = (cpuInfo.dmaState + cycles) & 0b1;

		System.out.println(instruction.toString());

		// Return the waiting cycles
		return cycles;
	}

	public static Cpu getInstance() {
		return instance;
	}
}
