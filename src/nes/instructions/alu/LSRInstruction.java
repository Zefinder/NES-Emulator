package instructions.alu;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class LSRInstruction extends AluInstruction {

	public LSRInstruction(AddressingMode mode) {
		super(mode);
	}

	public LSRInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	protected void execute(int operand1, int operand2) {
		// If ACCUMULATOR then use operand1 and store in A
		// If not use operand2 and store in memory
		// A/M = A/M >> 1
		int result;
		if (getMode() == AddressingMode.ACCUMULATOR) {
			result = operand1 >> 1;

			// Register A update
			cpu.cpuInfo.A = result & 0xFF;

			// Flag C update
			cpu.cpuInfo.C = operand1 & 0b00000001;
		} else {
			result = operand2 >> 1;

			// Memory update
			storeMemory(result & 0xFF);

			// Flag C update
			cpu.cpuInfo.C = operand2 & 0b00000001;
		}

		// Flags update
		updateFlags(result, false);
	}

	@Override
	public int getCycles() throws InstructionNotSupportedException {
		switch (getMode()) {
		case ACCUMULATOR:
			return 2;

		case ZEROPAGE:
			return 5;

		case ZEROPAGE_X:
		case ABSOLUTE:
			return 6;

		case ABSOLUTE_X:
			return 7;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}
	}

	@Override
	public String getName() {
		return "LSR";
	}

	@Override
	public AluInstruction newInstruction(int constant) {
		return new LSRInstruction(getMode(), constant);
	}
}
