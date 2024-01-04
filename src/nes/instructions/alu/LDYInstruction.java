package instructions.alu;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class LDYInstruction extends AluInstruction {

	public LDYInstruction(AddressingMode mode) {
		super(mode);
	}

	public LDYInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	protected void execute(int operand1, int operand2) {
		// A = M
		cpu.cpuInfo.Y = operand2;
		
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
			return 4 + pageCrossed;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}
	}

	@Override
	public String getName() {
		return "LDY";
	}

	@Override
	public AluInstruction newInstruction(int constant) {
		return new LDYInstruction(getMode(), constant);
	}
}
