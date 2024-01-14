package instructions.register;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class TSXInstruction extends TransferInstruction {

	public TSXInstruction(AddressingMode mode) {
		super(mode);
	}

	public TSXInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		// X = SP
		int value = cpu.cpuInfo.SP;
		cpu.cpuInfo.X = value;
		
		// Update flags
		updateFlags(value);
	}

	@Override
	public String getName() {
		return "TSX";
	}

	@Override
	public TransferInstruction newInstruction(int constant) {
		return new TSXInstruction(getMode(), constant);
	}
}
