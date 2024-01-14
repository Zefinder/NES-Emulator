package instructions.register;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class TYAInstruction extends TransferInstruction {

	public TYAInstruction(AddressingMode mode) {
		super(mode);
	}

	public TYAInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		// A = Y
		int value = cpu.cpuInfo.Y;
		cpu.cpuInfo.A = value;
		
		// Update flags
		updateFlags(value);
	}

	@Override
	public String getName() {
		return "TYA";
	}

	@Override
	public TransferInstruction newInstruction(int constant) {
		return new TYAInstruction(getMode(), constant);
	}
}
