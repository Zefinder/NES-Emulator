package instructions.stack;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;

public class PHPInstruction extends Instruction {

	public PHPInstruction(AddressingMode mode) {
		super(mode);
	}

	public PHPInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		cpu.push(cpu.cpuInfo.getP());
	}

	@Override
	public int getCycle() throws InstructionNotSupportedException {
		switch (getMode()) {
		case IMPLICIT:
			return 3;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}	}

	@Override
	public String getName() {
		return "PHP";
	}

	@Override
	protected Instruction newInstruction(int constant) {
		return new PHPInstruction(getMode(), constant);
	}
}
