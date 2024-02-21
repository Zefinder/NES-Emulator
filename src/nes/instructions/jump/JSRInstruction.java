package instructions.jump;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;

public class JSRInstruction extends JumpInstruction {

	public JSRInstruction(AddressingMode mode) {
		super(mode);
	}

	public JSRInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}

	@Override
	protected void jump(int address) {
		// Push next address (PC + 3) minus RTS size (1) to the stack
		int pushAddress = (cpu.cpuInfo.PC + 2) & 0xFFFF;
		cpu.push((pushAddress & 0xFF00) >> 8); // MSB
		cpu.push(pushAddress & 0xFF); // LSB

		// Set new PC at address - 3 (PC will be updated with the 3 bytes)
		cpu.cpuInfo.PC = (address - 3) & 0xFFFF;
	}

	@Override
	public int getCycles() throws InstructionNotSupportedException {
		switch (getMode()) {
		case ABSOLUTE:
			return 6;

		default:
			throw new InstructionNotSupportedException("Cannot get cycles: addressing mode is wrong!");
		}
	}

	@Override
	public String getName() {
		return "JSR";
	}

	@Override
	public JumpInstruction newInstruction(int constant) {
		return new JSRInstruction(getMode(), constant);
	}
}
