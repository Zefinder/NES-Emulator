package instructions.stack;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;

public class PLAInstruction extends Instruction {

	public PLAInstruction(AddressingMode mode) {
		super(mode);
	}

	public PLAInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		// Pop value
		int value = cpu.pop();

		// Update A
		cpu.cpuInfo.A = value;

		// Update flags
		cpu.cpuInfo.Z = value == 0 ? 1 : 0;
		cpu.cpuInfo.N = value >= 0x80 ? 1 : 0;
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
		return "PLA";
	}

	@Override
	public Instruction newInstruction(int constant) {
		return new PLAInstruction(getMode(), constant);
	}
}
