package instructions.jump;

import components.cpu.Cpu;
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
		int address = (cpu.cpuInfo.PC - 1) & 0xFFFF;
		cpu.push(address >> 8); // MSB
		cpu.push(address & 0xFF); // LSB

		// Push flags
		cpu.push(cpu.cpuInfo.getP());

		// Load PC with address at 0xFFFE (remove 1 for BRK)
		cpu.cpuInfo.PC = (fetchAddress(Cpu.BREAK_VECTOR) - 1) & 0xFFFF;

		// Put break to 1
		cpu.cpuInfo.B = 1;
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
