package instructions.flags;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class SEIInstruction extends FlagInstruction {

	public SEIInstruction(AddressingMode mode) {
		super(mode);
	}

	public SEIInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		cpu.cpuInfo.I = 1;
	}

	@Override
	public String getName() {
		return "SEI";
	}

	@Override
	public FlagInstruction newInstruction(int constant) {
		return new SEIInstruction(getMode(), constant);
	}
}
