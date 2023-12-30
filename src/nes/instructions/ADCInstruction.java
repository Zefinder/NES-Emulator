package instructions;

import exceptions.InstructionNotSupportedException;

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
		updateFlags(result);
		
		// Gym for V
		updateV((result & 0x80) == 0, (operand1 & 0x80) == 0, (operand2 & 0x80) == 0, operand1 > operand2);
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
	public Instruction newInstruction(int constant) {
		return new ADCInstruction(getMode(), constant);
	}

	private void updateV(boolean positiveResult, boolean positive1, boolean positive2, boolean greater) {
		// If result is positive, then positive1 and greater or positive2 and lower
		// If result is negative, then negative1 and greater or negative2 and lower
		// If not the case then there is an overflow somewhere...
		cpu.cpuInfo.V = positiveResult 
				? (positive1 && greater) || (positive2 && !greater) ? 0 : 1
				: (!positive1 && greater) || (!positive2 && !greater) ? 0 : 1;
	}
}
