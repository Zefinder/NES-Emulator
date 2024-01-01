package instructions.branch;

import java.util.function.BooleanSupplier;

import instructions.AddressingMode;

public class BVSInstruction extends BranchInstruction {

	private static final BooleanSupplier branchCondition = () -> cpu.cpuInfo.V == 1;

	public BVSInstruction(AddressingMode mode) {
		super(mode, branchCondition);
	}

	public BVSInstruction(AddressingMode mode, int constant) {
		super(mode, branchCondition, constant);
	}

	@Override
	public String getName() {
		return "BVS";
	}

	@Override
	public BranchInstruction newInstruction(int constant) {
		return new BVSInstruction(getMode(), constant);
	}
}
