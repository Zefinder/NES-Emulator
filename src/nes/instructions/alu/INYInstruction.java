package instructions.alu;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class INYInstruction extends AluInstruction {

	public INYInstruction(AddressingMode mode) {
		super(mode);
	}

	public INYInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	protected void execute(int operand1, int operand2) {
		// Y = Y + 1
		int result = cpu.cpuInfo.Y + 1;

		// Update Y register
		cpu.cpuInfo.Y = result & 0xFF;

		// Update flags
		updateFlags(result, false);
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
		return "INY";
	}

	@Override
	public AluInstruction newInstruction(int constant) {
		return new INYInstruction(getMode(), constant);
	}
}
