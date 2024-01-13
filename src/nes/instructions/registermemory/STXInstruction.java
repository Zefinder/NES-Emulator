package instructions.registermemory;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;

public class STXInstruction extends Instruction {

	public STXInstruction(AddressingMode mode) {
		super(mode);
	}

	public STXInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		// M = X
		// Set address to write
		updateMemoryAddress();
		
		// Set in memory
		storeMemory(cpu.cpuInfo.X);
	}

	@Override
	public int getCycle() throws InstructionNotSupportedException {
		switch (getMode()) {
		case ZEROPAGE:
			return 3;

		case ZEROPAGE_Y:
		case ABSOLUTE:
			return 4;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}
	}

	@Override
	public String getName() {
		return "STX";
	}

	@Override
	public Instruction newInstruction(int constant) {
		return new STXInstruction(getMode(), constant);
	}
}
