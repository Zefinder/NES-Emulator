package instructions.flags;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class SEDInstruction extends FlagInstruction {

	public SEDInstruction(AddressingMode mode) {
		super(mode);
	}

	public SEDInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		cpu.cpuInfo.D = 1;
	}

	@Override
	public String getName() {
		return "SED";
	}

	@Override
	public FlagInstruction newInstruction(int constant) {
		return new SEDInstruction(getMode(), constant);
	}
}
