package instructions;

import exceptions.InstructionNotSupportedException;

public abstract class AluInstruction extends Instruction {

	public AluInstruction(AddressingMode mode) {
		super(mode);
	}
	
	protected AluInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	protected abstract void execute(int operand1, int operand2);
	
	@Override
	public void execute() throws InstructionNotSupportedException {
		int operand1 = fetchOperand1();
		int operand2 = fetchOperand2();
		execute(operand1, operand2);
	}
	
	protected void updateFlags(int result) {
		cpu.cpuInfo.C = result > 255 ? 1 : 0;
		cpu.cpuInfo.Z = (result & 0xFF) == 0 ? 1 : 0;
		cpu.cpuInfo.N = (result & 0x80) != 0 ? 1 : 0;
	}
}
