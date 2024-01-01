package instructions.branch;

import java.util.function.BooleanSupplier;

import instructions.AddressingMode;

public class BPLInstruction extends BranchInstruction {

	private static final BooleanSupplier branchCondition = () -> cpu.cpuInfo.N == 0;

	public BPLInstruction(AddressingMode mode) {
		super(mode, branchCondition);
	}

	public BPLInstruction(AddressingMode mode, int constant) {
		super(mode, branchCondition, constant);
	}

	@Override
	public String getName() {
		return "BPL";
	}

	@Override
	public BranchInstruction newInstruction(int constant) {
		return new BPLInstruction(getMode(), constant);
	}

}
