package instructions;

import exceptions.InstructionNotSupportedException;

public class BRKInstruction extends Instruction {

	public BRKInstruction(AddressingMode mode) {
		super(mode);
	}

	public BRKInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	public void execute() throws InstructionNotSupportedException {		
		// Push PC
		cpu.push(cpu.cpuInfo.PC);
		
		// Push	flags
		cpu.push(cpu.cpuInfo.getP());
		
		// Load PC with address at 0xFFFE
		cpu.cpuInfo.PC = fetchAddress(0xFFFE);

		// Put break to 1
		cpu.cpuInfo.B = 1;
	}

	@Override
	public int getCycle() throws InstructionNotSupportedException {
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
	protected Instruction newInstruction(int constant) {
		return null;
	}

}
