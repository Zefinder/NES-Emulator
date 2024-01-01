package instructions.branch;

import java.util.function.BooleanSupplier;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;

public abstract class BranchInstruction extends Instruction {
	
	protected int branchSucceed = 0;
	protected int newPage = 0;
	
	private final BooleanSupplier branchCondition;

	public BranchInstruction(AddressingMode mode, BooleanSupplier branchCondition) {
		super(mode);
		this.branchCondition = branchCondition;
	}

	public BranchInstruction(AddressingMode mode, BooleanSupplier branchCondition, int constant) {
		super(mode, constant);
		this.branchCondition = branchCondition;
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		// Fetch branch offset
		int offset = fetchOperand2();
		
		// Offset is a signed byte
		offset = offset > 0x7F ? offset - 256 : offset;
		
		if (branchCondition.getAsBoolean()) {
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
		case RELATIVE:
			return 2 + branchSucceed + newPage;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}
	}
	
	@Override
	public abstract BranchInstruction newInstruction(int constant);
}
