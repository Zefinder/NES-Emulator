package instructions.register;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class TXAInstruction extends TransferInstruction {

	public TXAInstruction(AddressingMode mode) {
		super(mode);
	}

	public TXAInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		// A = X
		int value = cpu.cpuInfo.X;
		cpu.cpuInfo.A = value;
		
		// Update flags
		updateFlags(value);
	}

	@Override
	public String getName() {
		return "TXA";
	}

	@Override
	public TransferInstruction newInstruction(int constant) {
		return new TXAInstruction(getMode(), constant);
	}
}
