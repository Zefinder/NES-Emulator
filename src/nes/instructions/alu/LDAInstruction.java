package instructions.alu;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class LDAInstruction extends AluInstruction {

	public LDAInstruction(AddressingMode mode) {
		super(mode);
	}

	public LDAInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	protected void execute(int operand1, int operand2) {
		// A = M
		cpu.cpuInfo.A = operand2;
		
		// Flags update
		updateFlags(operand2, false);
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
		return "LDA";
	}

	@Override
	public AluInstruction newInstruction(int constant) {
		return new LDAInstruction(getMode(), constant);
	}
}
