package instructions.branch;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class BCCInstruction extends BranchInstruction {

	public BCCInstruction(AddressingMode mode) {
		super(mode);
	}

	public BCCInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	protected void execute(int offset) {
		if (cpu.cpuInfo.C == 0) {
			int oldPC = cpu.cpuInfo.PC;
			
			// Applying offset and wrap to avoid negative
			int newPC = (oldPC + offset) & 0xFFFF;

			// Update PC
			cpu.cpuInfo.PC = newPC;
			
			// Branch succeeded
			branchSucceed = 1;
			
			// Test new page
			newPage = (oldPC & 0xFF00) == (newPC & 0xFF00) ? 0 : 2;
			
		} else {
			// Reset values
			branchSucceed = 0;
			newPage = 0;
		}
	}

	@Override
	public int getCycle() throws InstructionNotSupportedException {
		switch (getMode()) {
		case IMMEDIATE:
			return 2 + branchSucceed + newPage;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}
	}

	@Override
	public String getName() {
		return "BCC";
	}

	@Override
	public BranchInstruction newInstruction(int constant) {
		return new BCCInstruction(getMode(), constant);
	}
}
