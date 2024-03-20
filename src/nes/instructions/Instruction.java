package instructions;

import components.cpu.Cpu;
import exceptions.InstructionNotSupportedException;

public abstract class Instruction {

	protected static final Cpu cpu = Cpu.getInstance();
	private final AddressingMode mode;
	protected int pageCrossed = 0;
	private final int constant;

	/**
	 * Address stored when fetching operand to use in {@link #storeMemory(int)}
	 */
	private int address = -1;

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
	public abstract int getCycles() throws InstructionNotSupportedException;

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
	public abstract Instruction newInstruction(int constant);

	/**
	 * Returns the byte number of the instruction. This calls the getByteNumber
	 * method from {@link AddressingMode}
	 * 
	 * @return the byte number of the instruction
	 */
	public int getByteNumber() {
		return mode.getByteNumber();
	}

	/**
	 * Gets the addressing mode of the instruction
	 * 
	 * @return the addressing mode
	 */
	protected AddressingMode getMode() {
		return mode;
	}

	/**
	 * Updates the address according to the value in the constant to store in memory
	 * WITHOUT accessing to the memory and provoke PPU read when we only want to
	 * write (for example)
	 */
	protected void updateMemoryAddress() {
		if (constant == -1) {
			return;
		}

		switch (mode) {
		case ZEROPAGE:
			address = constant & 0xFF;
			break;

		case ZEROPAGE_X:
			address = (constant + cpu.cpuInfo.X) & 0xFF;
			break;

		case ZEROPAGE_Y:
			address = (constant + cpu.cpuInfo.Y) & 0xFF;
			break;

		case ABSOLUTE:
			address = constant & 0xFFFF;
			break;

		case ABSOLUTE_X:
			address = (constant + cpu.cpuInfo.X) & 0xFFFF;
			pageCrossed = (constant & 0xFF) + cpu.cpuInfo.X > 0xFF ? 1 : 0;
			break;

		case ABSOLUTE_Y:
			address = (constant + cpu.cpuInfo.Y) & 0xFFFF;
			pageCrossed = (constant & 0xFF) + cpu.cpuInfo.Y > 0xFF ? 1 : 0;
			break;

		case INDIRECT_X:
			address = cpu.fetchAddress((constant + cpu.cpuInfo.X) & 0xFFFF);
			break;

		case INDIRECT_Y:
			int tmpAddress = cpu.fetchAddress(constant & 0xFFFF);
			address = (tmpAddress + cpu.cpuInfo.Y) & 0xFFFF;
			pageCrossed = (tmpAddress & 0xFF) + cpu.cpuInfo.Y > 0xFF ? 1 : 0;
			break;

		default:
			break;
		}
	}

	/**
	 * Fetches the second operand for the instruction since the first one is always
	 * the accumulator. It also sets the memory address to store in memory
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

		if (address == -1) {
			updateMemoryAddress();
		}

		int operand;
		switch (mode) {
		case IMMEDIATE:
		case RELATIVE:
			operand = constant & 0xFF;
			break;

		case ZEROPAGE:
			operand = cpu.fetchMemory(address);
			break;

		case ZEROPAGE_X:
			operand = cpu.fetchMemory(address);
			break;

		case ZEROPAGE_Y:
			operand = cpu.fetchMemory(address);
			break;

		case ABSOLUTE:
			operand = cpu.fetchMemory(address);
			break;

		case ABSOLUTE_X:
			operand = cpu.fetchMemory(address);
			break;

		case ABSOLUTE_Y:
			operand = cpu.fetchMemory(address);
			break;

		case INDIRECT_X:
			operand = cpu.fetchMemory(address);
			break;

		case INDIRECT_Y:
			operand = cpu.fetchMemory(address);
			break;

		default:
			throw new InstructionNotSupportedException("Cannot fetch second operand: addressing mode is wrong!");
		}

		return operand;
	}

	/**
	 * Fetches the address for jump instructions
	 * 
	 * @return the address where to jump
	 * @throws InstructionNotSupportedException if the addressing mode does not
	 *                                          correspond to something that can be
	 *                                          fetched
	 */
	protected int fetchJumpAddress() throws InstructionNotSupportedException {
		int retAddress;
		switch (mode) {
		case ABSOLUTE:
			retAddress = constant & 0xFFFF;
			break;

		case INDIRECT:
			retAddress = cpu.fetchAddress(constant & 0xFFFF);
			break;

		default:
			throw new InstructionNotSupportedException("Cannot fetch address: addressing mode is wrong!");
		}

		return retAddress;
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
		if (address == -1) {
			updateMemoryAddress();
		}

		cpu.storeMemory(address, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Instruction)) {
			return false;
		}

		Instruction other = (Instruction) obj;
		return other.mode == this.mode && other.constant == this.constant && other.getName().equals(this.getName());
	}

	@Override
	public String toString() {
		String suffix;
		int lsb = constant & 0xFF;
		int msb = (constant >> 8) & 0xFF;

		switch (mode) {
		case IMPLICIT:
			suffix = "";
			break;

		case ACCUMULATOR:
			suffix = "A";
			break;

		case IMMEDIATE:
			suffix = String.format("#$%02X", lsb, lsb);
			break;

		case ZEROPAGE:
			suffix = String.format("$%02X", lsb);
			break;

		case ZEROPAGE_X:
			suffix = String.format("$%02X,X", lsb);
			break;

		case ZEROPAGE_Y:
			suffix = String.format("$%02X,Y", lsb);
			break;

		case RELATIVE:
			lsb = (lsb >= 0x80 ? lsb - 256 : lsb) + 2;
			if (lsb > 0)
				suffix = String.format("*+%d", lsb);
			else
				suffix = String.format("*%d", lsb);
			break;

		case ABSOLUTE:
			suffix = String.format("$%02X%02X", msb, lsb);
			break;

		case ABSOLUTE_X:
			suffix = String.format("$%02X%02X,X", msb, lsb);
			break;

		case ABSOLUTE_Y:
			suffix = String.format("$%02X%02X,Y", msb, lsb);
			break;

		case INDIRECT:
			suffix = String.format("($%02X%02X)", msb, lsb);
			break;

		case INDIRECT_X:
			suffix = String.format("($%02X,X)", lsb);
			break;

		case INDIRECT_Y:
			suffix = String.format("($%02X),Y", lsb);
			break;

		default:
			suffix = "";
			break;
		}

		return getName() + " " + suffix;
	}
}
