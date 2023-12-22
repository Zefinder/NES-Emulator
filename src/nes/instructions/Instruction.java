package instructions;

public interface Instruction {

	/**
	 * Executes the instruction according to its addressing mode
	 * 
	 * @return the instruction result
	 */
	InstructionResult execute();

	/**
	 * Gets the number of cycles of the instruction. Can depend on the result of the
	 * execution, that's why this should be called AFTER {@link #execute()}
	 * 
	 * @return the number of cycles taken by the instructions
	 */
	int getCycle();

	/**
	 * Gets the addressing mode of the instruction
	 * 
	 * @return the addressing mode
	 */
	AddressingMode getMode();

}
