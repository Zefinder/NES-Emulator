package instructions.jump;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class JMPInstruction extends JumpInstruction {

	public JMPInstruction(AddressingMode mode) {
		super(mode);
	}

	public JMPInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	protected void jump(int address) {
		// Set new PC (indirect or absolute are on 3 bytes)
		cpu.cpuInfo.PC = (address - 3) & 0xFFFF;
	}

	@Override
	public int getCycles() throws InstructionNotSupportedException {
		switch (getMode()) {
		case ABSOLUTE:
			return 3;
			
		case INDIRECT:
			return 5;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}
	}

	@Override
	public String getName() {
		return "JMP";
	}

	@Override
	public JumpInstruction newInstruction(int constant) {
		return new JMPInstruction(getMode(), constant);
	}
}
