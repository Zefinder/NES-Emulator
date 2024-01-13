package instructions.flags;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class SECInstruction extends FlagInstruction {

	public SECInstruction(AddressingMode mode) {
		super(mode);
	}

	public SECInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}


	@Override
	public void execute() throws InstructionNotSupportedException {
		cpu.cpuInfo.C = 1;
	}

	@Override
	public String getName() {
		return "SEC";
	}

	@Override
	public FlagInstruction newInstruction(int constant) {
		return new SECInstruction(getMode(), constant);
	}
}
