package instructions.flags;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class CLDInstruction extends FlagInstruction {

	public CLDInstruction(AddressingMode mode) {
		super(mode);
	}

	public CLDInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		cpu.cpuInfo.D = 0;
	}

	@Override
	public String getName() {
		return "CLD";
	}

	@Override
	public FlagInstruction newInstruction(int constant) {
		return null;
	}
}
