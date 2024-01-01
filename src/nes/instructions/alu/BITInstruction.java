package instructions.alu;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class BITInstruction extends AluInstruction {

	public BITInstruction(AddressingMode mode) {
		super(mode);
	}

	public BITInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	protected void execute(int operand1, int operand2) {
		// A = A & M
		int result = operand1 & operand2;

		// Flags update
		updateFlags(result, false);
		
		// V update
		cpu.cpuInfo.V = (result & 0b01000000) >> 6;
	}

	@Override
	public int getCycle() throws InstructionNotSupportedException {
		switch (getMode()) {
		case ZEROPAGE:
			return 3;

		case ABSOLUTE:
			return 4;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}
	}

	@Override
	public String getName() {
		return "BIT";
	}

	@Override
	public AluInstruction newInstruction(int constant) {
		return new BITInstruction(getMode(), constant);
	}

}
