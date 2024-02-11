package disassemble;

import instructions.Instruction;

public abstract  class DisassemblyInfo {
	
	private byte[] prgRom;
	private byte[] chrRom;

	private Instruction[] instructions;

	public DisassemblyInfo(byte[] prgRom, byte[] chrRom, Instruction[] instructions) {
		this.prgRom = prgRom;
		this.chrRom = chrRom;
		this.instructions = instructions;
	}

	public byte[] getPrgRom() {
		return prgRom;
	}

	public byte[] getChrRom() {
		return chrRom;
	}

	public Instruction[] getInstructions() {
		return instructions;
	}
	
	public abstract int getMapper();

	public abstract boolean isVerticalNametableMirroring();
	
	public abstract boolean isPALSystem();
}
