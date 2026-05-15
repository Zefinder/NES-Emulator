package instructions.branch;

import java.util.function.BooleanSupplier;

import instructions.AddressingMode;

public class BCCInstruction extends BranchInstruction {

	private static final BooleanSupplier branchCondition = () -> cpuRegisters.C == 0;

	public BCCInstruction(AddressingMode mode) {
		super(mode, branchCondition);
	}

	public BCCInstruction(AddressingMode mode, int constant) {
		super(mode, branchCondition, constant);
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
