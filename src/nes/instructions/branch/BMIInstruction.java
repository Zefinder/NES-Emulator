package instructions.branch;

import java.util.function.BooleanSupplier;

import instructions.AddressingMode;

public class BMIInstruction extends BranchInstruction {

	private static final BooleanSupplier branchCondition = () -> cpu.cpuInfo.N == 1;

	public BMIInstruction(AddressingMode mode) {
		super(mode, branchCondition);
	}

	public BMIInstruction(AddressingMode mode, int constant) {
		super(mode, branchCondition, constant);
	}

	@Override
	public String getName() {
		return "BMI";
	}

	@Override
	public BranchInstruction newInstruction(int constant) {
		return new BMIInstruction(getMode(), constant);
	}
}
