package instructions.alu;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class EORInstruction extends AluInstruction{

	public EORInstruction(AddressingMode mode) {
		super(mode);
	}

	public EORInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	protected void execute(int operand1, int operand2) {
		// A = A ^ M
		int result = operand1 ^ operand2;

		// Register A update
		// No need & 0xFF since EOR has no overflow
		cpu.cpuInfo.A = result;

		// Flags update
		updateFlags(result, false);
	}

	@Override
	public int getCycle() throws InstructionNotSupportedException {
		switch (getMode()) {
		case IMMEDIATE:
			return 2;

		case ZEROPAGE:
			return 3;

		case ZEROPAGE_X:
		case ABSOLUTE:
			return 4;

		case ABSOLUTE_X:
		case ABSOLUTE_Y:
			return 4 + pageCrossed;

		case INDIRECT_X:
			return 6;

		case INDIRECT_Y:
			return 5 + pageCrossed;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}
	}

	@Override
	public String getName() {
		return "EOR";
	}

	@Override
	public AluInstruction newInstruction(int constant) {
		return new EORInstruction(getMode(), constant);
	}
}
