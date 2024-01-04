package instructions.alu;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class DEXInstruction extends AluInstruction {

	public DEXInstruction(AddressingMode mode) {
		super(mode);
	}

	public DEXInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	protected void execute(int operand1, int operand2) {
		// X = X - 1
		int result = cpu.cpuInfo.X - 1;
		
		// Update X register
		cpu.cpuInfo.X = result & 0xFF;
		
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
		return "DEX";
	}
	
	@Override
	public AluInstruction newInstruction(int constant) {
		return new DEXInstruction(getMode(), constant);
	}
}
