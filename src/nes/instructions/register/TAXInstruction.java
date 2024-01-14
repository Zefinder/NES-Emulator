package instructions.register;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class TAXInstruction extends TransferInstruction {

	public TAXInstruction(AddressingMode mode) {
		super(mode);
	}

	public TAXInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		// X = A
		int value = cpu.cpuInfo.A;
		cpu.cpuInfo.X = value;
		
		// Update flags
		updateFlags(value);
	}

	@Override
	public String getName() {
		return "TAX";
	}

	@Override
	public TransferInstruction newInstruction(int constant) {
		return new TAXInstruction(getMode(), constant);
	}
}
