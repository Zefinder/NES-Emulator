package instructions.alu;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class ADCInstruction extends AluInstruction {

	public ADCInstruction(AddressingMode mode) {
		super(mode);
	}

	public ADCInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	protected void execute(int operand1, int operand2) {
		// A = A + M + C
		int result = operand1 + operand2 + cpu.cpuInfo.C;

		// Register A update
		cpu.cpuInfo.A = result & 0xFF;

		// Flags update
		updateFlags(result, true);

		// Gym for V
		updateV(operand1, operand2, result & 0xFF);
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
		return "ADC";
	}

	@Override
	public AluInstruction newInstruction(int constant) {
		return new ADCInstruction(getMode(), constant);
	}
}
