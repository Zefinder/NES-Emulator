package instructions.stack;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;

public class PLPInstruction extends Instruction {

	public PLPInstruction(AddressingMode mode) {
		super(mode);
	}

	public PLPInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		// Pop value and update flags
		cpu.cpuInfo.setP(cpu.pop());		
	}

	@Override
	public int getCycle() throws InstructionNotSupportedException {
		switch (getMode()) {
		case IMPLICIT:
			return 4;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}
	}

	@Override
	public String getName() {
		return "PLP";
	}

	@Override
	protected Instruction newInstruction(int constant) {
		return new PLPInstruction(getMode(), constant);
	}
}
