package instructions.registermemory;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;

public class STYInstruction extends Instruction {

	public STYInstruction(AddressingMode mode) {
		super(mode);
	}

	public STYInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		// M = Y
		// Set address to write
		updateMemoryAddress();

		// Set in memory
		storeMemory(cpu.cpuInfo.Y);
	}

	@Override
	public int getCycle() throws InstructionNotSupportedException {
		switch (getMode()) {
		case ZEROPAGE:
			return 3;

		case ZEROPAGE_X:
		case ABSOLUTE:
			return 4;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}
	}

	@Override
	public String getName() {
		return "STY";
	}

	@Override
	public Instruction newInstruction(int constant) {
		return new STYInstruction(getMode(), constant);
	}
}
