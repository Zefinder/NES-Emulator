package instructions.register;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class TAYInstruction extends TransferInstruction {

	public TAYInstruction(AddressingMode mode) {
		super(mode);
	}

	public TAYInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		// Y = A
		int value = cpu.cpuInfo.A;
		cpu.cpuInfo.Y = value;
		
		// Update flags
		updateFlags(value);
	}

	@Override
	public String getName() {
		return "TAY";
	}

	@Override
	public TransferInstruction newInstruction(int constant) {
		return new TAYInstruction(getMode(), constant);
	}
}
