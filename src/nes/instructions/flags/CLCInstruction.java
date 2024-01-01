package instructions.flags;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class CLCInstruction extends FlagInstruction {

	public CLCInstruction(AddressingMode mode) {
		super(mode);
	}

	public CLCInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		cpu.cpuInfo.C = 0;
	}

	@Override
	public String getName() {
		return "CLC";
	}

	@Override
	public FlagInstruction newInstruction(int constant) {
		return null;
	}
}
