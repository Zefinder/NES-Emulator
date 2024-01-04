package instructions.alu;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class DECInstruction extends AluInstruction {

	public DECInstruction(AddressingMode mode) {
		super(mode);
	}

	public DECInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	protected void execute(int operand1, int operand2) {
		// M = M - 1
		int result = operand2 - 1;
		
		// Update memory
		storeMemory(result & 0xFF);
		
		// Update flags
		updateFlags(result, false);
	}

	@Override
	public int getCycle() throws InstructionNotSupportedException {
		switch (getMode()) {		
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
		return "DEC";
	}
	
	@Override
	public AluInstruction newInstruction(int constant) {
		return new DECInstruction(getMode(), constant);
	}
}
