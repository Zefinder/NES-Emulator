package instructions;

public interface Instruction {
	
	InstructionResult implicit();
	
	InstructionResult accumulator();
	
	InstructionResult immediate();
	
	InstructionResult zeroPage();
	
	InstructionResult zeroPageX();
	
	InstructionResult zeroPageY();
	
	InstructionResult relative();
	
	InstructionResult absulute();
	
	InstructionResult absuluteX();
	
	InstructionResult absuluteY();
	
	InstructionResult indirect();
	
	InstructionResult indirectX();
	
	InstructionResult indirectY();
	
	int getCycle();
	
}
