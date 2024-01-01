package instructions.flags;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class CLIInstruction extends FlagInstruction {

	public CLIInstruction(AddressingMode mode) {
		super(mode);
	}

	public CLIInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		cpu.cpuInfo.I = 0;
	}

	@Override
	public String getName() {
		return "CLI";
	}

	@Override
	public FlagInstruction newInstruction(int constant) {
		return null;
	}
}
