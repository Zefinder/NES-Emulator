package instructions.alu;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class CPXInstruction extends AluInstruction {

	public CPXInstruction(AddressingMode mode) {
		super(mode);
	}

	public CPXInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	protected void execute(int operand1, int operand2) {
		// X - M
		int result = cpu.cpuInfo.X - operand2;
		
		// Flag update
		cpu.cpuInfo.C = result >= 0 ? 1 : 0;
		updateFlags(result, false);
	}

	@Override
	public int getCycles() throws InstructionNotSupportedException {
		switch (getMode()) {
		case IMMEDIATE:
			return 2;			
		
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
		return "CPX";
	}
	
	@Override
	public AluInstruction newInstruction(int constant) {
		return new CPXInstruction(getMode(), constant);
	}
}
