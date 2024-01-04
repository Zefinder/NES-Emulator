package instructions;

import exceptions.InstructionNotSupportedException;

public class NOPInstruction extends Instruction {

	public NOPInstruction(AddressingMode mode) {
		super(mode);
	}
	
	public NOPInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		// Nothing, this is NOP
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
	public String getName() {
		return "NOP";
	}

	@Override
	protected Instruction newInstruction(int constant) {
		return new NOPInstruction(getMode(), constant);
	}

}
