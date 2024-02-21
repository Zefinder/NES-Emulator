package instructions.registermemory;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;

public class STAInstruction extends Instruction {

	public STAInstruction(AddressingMode mode) {
		super(mode);
	}

	public STAInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		// M = A
		// Set address to write
		updateMemoryAddress();
		
		// Set in memory
		storeMemory(cpu.cpuInfo.A);
	}

	@Override
	public int getCycles() throws InstructionNotSupportedException {
		switch (getMode()) {
		case ZEROPAGE:
			return 3;

		case ZEROPAGE_X:
		case ABSOLUTE:
			return 4;

		case ABSOLUTE_X:
		case ABSOLUTE_Y:
			return 5;

		case INDIRECT_X:
		case INDIRECT_Y:
			return 6;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}
	}

	@Override
	public String getName() {
		return "STA";
	}

	@Override
	public Instruction newInstruction(int constant) {
		return new STAInstruction(getMode(), constant);
	}
}
