package instructions.alu;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class ROLInstruction extends AluInstruction {

	public ROLInstruction(AddressingMode mode) {
		super(mode);
	}

	public ROLInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	protected void execute(int operand1, int operand2) {
		// If ACCUMULATOR then use operand1 and store in A
		// If not use operand2 and store in memory
		// A/M = (A/M << 1) | C
		int result;
		if (getMode() == AddressingMode.ACCUMULATOR) {
			result = (operand1 << 1) | cpu.cpuInfo.C;
			
			// Register A update
			cpu.cpuInfo.A = result & 0xFF;
		} else {
			result = (operand2 << 1) | cpu.cpuInfo.C;
			
			// Memory update
			storeMemory(result & 0xFF);
		}
		
		// Flags update
		updateFlags(result, true);
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
		return "ROL";
	}
	
	@Override
	public AluInstruction newInstruction(int constant) {
		return new ROLInstruction(getMode(), constant);
	}
}
