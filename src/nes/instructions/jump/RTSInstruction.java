package instructions.jump;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;

public class RTSInstruction extends Instruction {

	public RTSInstruction(AddressingMode mode) {
		super(mode);
	}

	public RTSInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		// Popping values
		int address = pop() | pop() << 8;
		
		// Update PC
		cpuRegisters.PC = address;
	}

	@Override
	public int getCycles() throws InstructionNotSupportedException {
		switch (getMode()) {
		case IMPLICIT:
			return 6;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}	}

	@Override
	public String getName() {
		return "RTS";
	}

	@Override
	public Instruction newInstruction(int constant) {
		return new RTSInstruction(getMode(), constant);
	}

}
