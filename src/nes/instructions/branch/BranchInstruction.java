package instructions.branch;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;

public abstract class BranchInstruction extends Instruction {
	
	protected int branchSucceed = 0;
	protected int newPage = 0;

	public BranchInstruction(AddressingMode mode) {
		super(mode);
	}

	public BranchInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	/**
	 * Executes branch instruction with the given offset. Do not apply the +2 to PC
	 * 
	 * @param offset the offset if branch succeeds
	 */
	protected abstract void execute(int offset);

	@Override
	public void execute() throws InstructionNotSupportedException {
		// Fetch branch offset
		int offset = fetchOperand2();
		
		// Offset is a signed byte
		offset = offset > 0x7F ? offset - 256 : offset;
		
		// Execute
		execute(offset);
	}

	@Override
	public abstract BranchInstruction newInstruction(int constant);
}
