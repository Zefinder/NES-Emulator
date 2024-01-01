package instructions.branch;

import java.util.function.BooleanSupplier;

import instructions.AddressingMode;

public class BEQInstruction extends BranchInstruction {

	private static final BooleanSupplier branchCondition = () -> cpu.cpuInfo.Z == 1;
	
	public BEQInstruction(AddressingMode mode) {
		super(mode, branchCondition);
	}

	public BEQInstruction(AddressingMode mode, int constant) {
		super(mode, branchCondition, constant);
	}

	@Override
	public String getName() {
		return "BEQ";
	}
	
	@Override
	public BranchInstruction newInstruction(int constant) {
		return new BEQInstruction(getMode(), constant);
	}

}
