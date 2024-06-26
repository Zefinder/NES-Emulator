package instructions.alu;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;

// TODO For some ALU instructions (like ADC, SBC, etc...) wouldn't it be more efficient to do an already prepared array? Yes it is! Need to do to optimise time
public abstract class AluInstruction extends Instruction {

	public AluInstruction(AddressingMode mode) {
		super(mode);
	}

	protected AluInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	/**
	 * Executes the ALU instruction with the 2 inputs. Output is directly placed in
	 * A
	 * 
	 * @param operand1
	 * @param operand2
	 */
	protected abstract void execute(int operand1, int operand2);

	@Override
	public void execute() throws InstructionNotSupportedException {
		int operand2 = fetchOperand2();
		execute(cpu.cpuInfo.A, operand2);
	}

	@Override
	public abstract AluInstruction newInstruction(int constant);

	protected void updateFlags(int result, boolean updateC) {
		if (updateC) {
			cpu.cpuInfo.C = result > 255 ? 1 : 0;
		}

		cpu.cpuInfo.Z = (result & 0xFF) == 0 ? 1 : 0;
		cpu.cpuInfo.N = (result & 0x80) != 0 || result < 0 ? 1 : 0;
	}

	protected void updateV(int operand1, int operand2, int result) {
		cpu.cpuInfo.V = ((operand1 ^ result) & (operand2 ^ result) & 0x80) == 0 ? 0 : 1;
	}
}
