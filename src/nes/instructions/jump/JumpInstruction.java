package instructions.jump;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;

public abstract class JumpInstruction extends Instruction {

	public JumpInstruction(AddressingMode mode) {
		super(mode);
	}

	public JumpInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	/**
	 * Executes the jump instruction to the specified address
	 * 
	 * @param address the address where to jump
	 */
	protected abstract void jump(int address);

	@Override
	public void execute() throws InstructionNotSupportedException {
		int address = fetchJumpAddress();
		jump(address);
	}

	@Override
	public abstract JumpInstruction newInstruction(int constant);
}
