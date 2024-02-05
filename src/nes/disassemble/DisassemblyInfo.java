package disassemble;

import instructions.Instruction;

public class DisassemblyInfo {

	private int mapper;
	private boolean mirroring;
	
	private byte[] prgRom;
	private byte[] chrRom;

	private Instruction[] instructions;

	public DisassemblyInfo(int mapper, boolean mirroring, byte[] prgRom, byte[] chrRom, Instruction[] instructions) {
		this.mapper = mapper;
		this.mirroring = mirroring;
		this.prgRom = prgRom;
		this.chrRom = chrRom;
		this.instructions = instructions;
	}

	public int getMapper() {
		return mapper;
	}

	public boolean getMirroring() {
		return mirroring;
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

}
