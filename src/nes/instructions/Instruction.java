package instructions;

import components.Cpu;
import exceptions.InstructionNotSupportedException;

public abstract class Instruction {

	protected static final Cpu cpu = Cpu.getInstance();
	private final AddressingMode mode;
	protected int pageCrossed = 0;
	private final int constant;

	/**
	 * Address stored when fetching operand to use in {@link #storeMemory(int)}
	 */
	private int address;

	public Instruction(AddressingMode mode) {
		this.mode = mode;
		this.constant = -1;
	}

	protected Instruction(AddressingMode mode, int constant) {
		this.mode = mode;
		this.constant = constant;
	}

	/**
	 * Executes the instruction according to its addressing mode. If this updates
	 * {@link #pageCrossed}, then it <em>MUST BE</em> executed only once. Else
	 * {@link #pageCrossed} will never be set back to 0
	 * 
	 * @return the instruction result
	 * @throws InstructionNotSupportedException if an exception occurs during the
	 *                                          execution
	 */
	public abstract void execute() throws InstructionNotSupportedException;

	/**
	 * Gets the number of cycles of the instruction. Can depend on the result of the
	 * execution, that's why this should be called AFTER {@link #execute()}
	 * 
	 * @return the number of cycles taken by the instructions
	 * @throws InstructionNotSupportedException if the addressing mode does not
	 *                                          correspond to the instruction
	 */
	public abstract int getCycle() throws InstructionNotSupportedException;

	public abstract String getName();

	/**
	 * <p>
	 * Creates a new instruction with the same addressing mode but with a constant.
	 * This is used to basically clone the instruction so modifying the constant
	 * won't modify for every existing instruction objects of the same addressing
	 * mode (if you don't understand, take a look at {@link InstructionInfo}).
	 * </p>
	 * 
	 * <p>
	 * Call this method if and only if you need to put a constant in your
	 * instruction.
	 * </p>
	 * 
	 * <p>
	 * The following addressing modes do not require a call of this method:
	 * <ul>
	 * <li>{@link AddressingMode#IMPLICIT}
	 * <li>{@link AddressingMode#ACCUMULATOR}
	 * </ul>
	 * </p>
	 * 
	 * @param constant the constant that the instruction will use
	 * @return a new instance of the instruction with a constant
	 */
	protected abstract Instruction newInstruction(int constant);

	/**
	 * Gets the addressing mode of the instruction
	 * 
	 * @return the addressing mode
	 */
	protected AddressingMode getMode() {
		return mode;
	}

	/**
	 * Fetches the first operand for the instruction
	 * 
	 * @return the value of the first operand
	 * @throws InstructionNotSupportedException if the addressing mode does not
	 *                                          correspond to something that can be
	 *                                          fetched
	 */
	protected int fetchOperand1() throws InstructionNotSupportedException {
		int operand;
		// The first operand will always be the accumulator or nothing
		switch (mode) {
		case IMMEDIATE:
		case ACCUMULATOR:
		case ZEROPAGE:
		case ZEROPAGE_X:
		case ABSOLUTE:
		case ABSOLUTE_X:
		case ABSOLUTE_Y:
		case INDIRECT_X:
		case INDIRECT_Y:
			operand = cpu.cpuInfo.A;
			break;

		default:
			throw new InstructionNotSupportedException("Cannot fetch first operand: addressing mode is wrong!");
		}

		return operand;
	}

	/**
	 * Fetches the second operand for the instruction
	 * 
	 * @return the value of the second operand
	 * @throws InstructionNotSupportedException if the addressing mode does not
	 *                                          correspond to something that can be
	 *                                          fetched
	 */
	protected int fetchOperand2() throws InstructionNotSupportedException {
		if (constant == -1) {
			return -1;
		}

		int operand;
		// TODO Remove fetchAddress to do everything here
		switch (mode) {
		case IMMEDIATE:
		case RELATIVE:
			operand = constant & 0xFF;
			break;

		case ZEROPAGE:
			operand = cpu.fetchMemory(constant & 0xFF);
			break;

		case ZEROPAGE_X:
			operand = cpu.fetchMemory((constant + cpu.cpuInfo.X) & 0xFF);
			break;

		case ZEROPAGE_Y:
			operand = cpu.fetchMemory((constant + cpu.cpuInfo.Y) & 0xFF);
			break;

		case ABSOLUTE:
			operand = cpu.fetchMemory(constant & 0xFFFF);
			break;

		case ABSOLUTE_X:
			operand = cpu.fetchMemory((constant + cpu.cpuInfo.X) & 0xFFFF);
			pageCrossed = (constant & 0xFF) + cpu.cpuInfo.X > 0xFF ? 1 : 0;
			break;

		case ABSOLUTE_Y:
			operand = cpu.fetchMemory((constant + cpu.cpuInfo.Y) & 0xFFFF);
			pageCrossed = (constant & 0xFF) + cpu.cpuInfo.Y > 0xFF ? 1 : 0;
			break;

		case INDIRECT_X:
			operand = cpu.fetchMemory(cpu.fetchAddress((constant + cpu.cpuInfo.X) & 0xFFFF));
			break;

		case INDIRECT_Y:
			int address = cpu.fetchAddress(constant & 0xFFFF);
			operand = cpu.fetchMemory((address + cpu.cpuInfo.Y) & 0xFFFF);
			pageCrossed = (constant & 0xFF) + cpu.cpuInfo.Y > 0xFF ? 1 : 0;
			break;

		default:
			throw new InstructionNotSupportedException("Cannot fetch second operand: addressing mode is wrong!");
		}

		return operand;
	}

	/**
	 * Fetches the address for the instruction
	 * 
	 * @return the address
	 * @throws InstructionNotSupportedException if constant is not set
	 */
	protected int fetchAddress() throws InstructionNotSupportedException {
		if (constant == -1) {
			throw new InstructionNotSupportedException("Cannot fetch address: no constant!");
		}

		switch (mode) {
		case RELATIVE:
			int rel = constant & 0xFF;
			address = cpu.cpuInfo.PC + (rel > 127 ? rel - 256 : rel);
			break;

		case ABSOLUTE:
			address = constant & 0xFFFF;
			break;

		case INDIRECT:
			address = cpu.fetchAddress(constant & 0xFF);
			break;

		default:
			throw new InstructionNotSupportedException("Cannot fetch address: addressing mode is wrong!");
		}

		return address;
	}
	
	/**
	 * Fetch address in memory at the given address
	 * 
	 * @param address the address to look at in the bus
	 * @return an address in memory
	 */
	protected int fetchAddress(int address) {
		return cpu.fetchAddress(address);
	}

	/**
	 * Stores the result in memory exactly where the 2nd operand have been taken.
	 * 
	 * @param value the value to store
	 */
	protected void storeMemory(int value) {
		cpu.storeMemory(address, value);
	}
}
