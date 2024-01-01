package instructions.branch;

import java.util.function.BooleanSupplier;

import instructions.AddressingMode;

public class BNEInstruction extends BranchInstruction {

	private static final BooleanSupplier branchCondition = () -> cpu.cpuInfo.Z == 0;

	public BNEInstruction(AddressingMode mode) {
		super(mode, branchCondition);
	}

	public BNEInstruction(AddressingMode mode, int constant) {
		super(mode, branchCondition, constant);
	}

	@Override
	public String getName() {
		return "BNE";
	}

	@Override
	public BranchInstruction newInstruction(int constant) {
		return new BNEInstruction(getMode(), constant);
	}
}
