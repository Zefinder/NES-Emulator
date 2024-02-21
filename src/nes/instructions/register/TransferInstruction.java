package instructions.register;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;

public abstract class TransferInstruction extends Instruction {

	public TransferInstruction(AddressingMode mode) {
		super(mode);
	}

	public TransferInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public int getCycles() throws InstructionNotSupportedException {
		switch (getMode()) {
		case IMPLICIT:
			return 2;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}
	}

	@Override
	public abstract TransferInstruction newInstruction(int constant);
	
	protected void updateFlags(int result) {
		cpu.cpuInfo.Z = (result & 0xFF) == 0 ? 1 : 0;
		cpu.cpuInfo.N = (result & 0x80) != 0 ? 1 : 0;
	}
}
