package instructions.flags;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;

public abstract class FlagInstruction extends Instruction {

	public FlagInstruction(AddressingMode mode) {
		super(mode);
	}

	public FlagInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public int getCycle() throws InstructionNotSupportedException {
		switch (getMode()) {
		case IMPLICIT:
			return 2;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}
	}

	@Override
	public abstract FlagInstruction newInstruction(int constant);

}
