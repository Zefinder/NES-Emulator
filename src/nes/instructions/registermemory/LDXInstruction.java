package instructions.registermemory;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.alu.AluInstruction;

public class LDXInstruction extends AluInstruction {

	public LDXInstruction(AddressingMode mode) {
		super(mode);
	}

	public LDXInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	protected void execute(int operand1, int operand2) {
		// A = M
		cpu.cpuInfo.X = operand2;
		
		// Flags update
		updateFlags(operand2, false);
	}

	@Override
	public int getCycles() throws InstructionNotSupportedException {
		switch (getMode()) {
		case IMMEDIATE:
			return 2;

		case ZEROPAGE:
			return 3;

		case ZEROPAGE_Y:
		case ABSOLUTE:
			return 4;

		case ABSOLUTE_Y:
			return 4 + pageCrossed;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}
	}

	@Override
	public String getName() {
		return "LDX";
	}

	@Override
	public AluInstruction newInstruction(int constant) {
		return new LDXInstruction(getMode(), constant);
	}
}
