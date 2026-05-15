package instructions.register;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class TXSInstruction extends TransferInstruction {

	public TXSInstruction(AddressingMode mode) {
		super(mode);
	}

	public TXSInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		// SP = X
		int value = cpuRegisters.X;
		cpuRegisters.SP = value;
	}

	@Override
	public String getName() {
		return "TXS";
	}

	@Override
	public TransferInstruction newInstruction(int constant) {
		return new TXSInstruction(getMode(), constant);
	}
}
