package instructions.jump;

import components.Cpu;
import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;

public class BRKInstruction extends Instruction {

	public BRKInstruction(AddressingMode mode) {
		super(mode);
	}

	public BRKInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {
		// Push PC (remove 1 for RTI)
		int address = (cpuRegisters.PC - 1) & 0xFFFF;
		push(address >> 8); // MSB
		push(address & 0xFF); // LSB

		// Push flags
		push(cpuRegisters.getP());

		// Load PC with address at 0xFFFE (remove 1 for BRK)
		cpuRegisters.PC = (fetchAddress(Cpu.BREAK_VECTOR) - 1) & 0xFFFF;

		// Put break to 1
		cpuRegisters.B = 1;
	}

	@Override
	public int getCycles() throws InstructionNotSupportedException {
		switch (getMode()) {
		case IMPLICIT:
			return 7;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}
	}

	@Override
	public String getName() {
		return "BRK";
	}

	@Override
	public Instruction newInstruction(int constant) {
		return null;
	}

}
