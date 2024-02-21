package instructions.alu;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class SBCInstruction extends AluInstruction {

	public SBCInstruction(AddressingMode mode) {
		super(mode);
	}

	public SBCInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	protected void execute(int operand1, int operand2) {
		// A = A - M - (1 - C)
		int result = operand1 + (255 - operand2) + cpu.cpuInfo.C;

		// Register A update
		cpu.cpuInfo.A = result & 0xFF;

		// Flags update
		updateFlags(result, true);

		// C re-update (SBC is always 1 - C)
		cpu.cpuInfo.C = 1 - cpu.cpuInfo.C;

		// Gym for V
		updateV(operand1, (255 - operand2), result);
	}

	@Override
	public int getCycles() throws InstructionNotSupportedException {
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
		return "SBC";
	}

	@Override
	public AluInstruction newInstruction(int constant) {
		return new SBCInstruction(getMode(), constant);
	}
}
