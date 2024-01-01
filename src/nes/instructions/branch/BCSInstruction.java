package instructions.branch;

import java.util.function.BooleanSupplier;

import instructions.AddressingMode;

public class BCSInstruction extends BranchInstruction {

	private static final BooleanSupplier branchCondition = () -> cpu.cpuInfo.C == 1;

	public BCSInstruction(AddressingMode mode) {
		super(mode, branchCondition);
	}

	public BCSInstruction(AddressingMode mode, int constant) {
		super(mode, branchCondition, constant);
	}

	@Override
	public String getName() {
		return "BCS";
	}

	@Override
	public BranchInstruction newInstruction(int constant) {
		return new BCSInstruction(getMode(), constant);
	}
}
