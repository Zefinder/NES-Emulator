package instructions.branch;

import java.util.function.BooleanSupplier;

import instructions.AddressingMode;

public class BVCInstruction extends BranchInstruction {

	private static final BooleanSupplier branchCondition = () -> cpu.cpuInfo.V == 0;

	public BVCInstruction(AddressingMode mode) {
		super(mode, branchCondition);
	}

	public BVCInstruction(AddressingMode mode, int constant) {
		super(mode, branchCondition, constant);
	}

	@Override
	public String getName() {
		return "BVC";
	}

	@Override
	public BranchInstruction newInstruction(int constant) {
		return new BVCInstruction(getMode(), constant);
	}
}
