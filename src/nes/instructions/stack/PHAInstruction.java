package instructions.stack;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;

public class PHAInstruction extends Instruction {

	public PHAInstruction(AddressingMode mode) {
		super(mode);
	}

	public PHAInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		cpu.push(cpu.cpuInfo.A);
	}

	@Override
	public int getCycles() throws InstructionNotSupportedException {
		switch (getMode()) {
		case IMPLICIT:
			return 3;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}	}

	@Override
	public String getName() {
		return "PHA";
	}

	@Override
	public Instruction newInstruction(int constant) {
		return new PHAInstruction(getMode(), constant);
	}
}
