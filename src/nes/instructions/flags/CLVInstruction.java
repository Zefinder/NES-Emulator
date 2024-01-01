package instructions.flags;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class CLVInstruction extends FlagInstruction {

	public CLVInstruction(AddressingMode mode) {
		super(mode);
	}

	public CLVInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		cpu.cpuInfo.V = 0;
	}

	@Override
	public String getName() {
		return "CLV";
	}

	@Override
	public FlagInstruction newInstruction(int constant) {
		return null;
	}
}
