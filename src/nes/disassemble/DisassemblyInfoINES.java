package disassemble;

import instructions.Instruction;

public class DisassemblyInfoINES extends DisassemblyInfo {

	private final int mapper;
	private final boolean nametableVerticalMirroring;
	private final boolean palSystem;

	public DisassemblyInfoINES(byte[] romFlags, byte[] prgRom, byte[] chrRom, Instruction[] instructions) {
		super(prgRom, chrRom, instructions);
		nametableVerticalMirroring = (romFlags[2] & 0b1) == 0;
		mapper = romFlags[3] & 0xFFFF0000 | romFlags[2] >> 4;
		palSystem = romFlags[6] != 0;
	}

	@Override
	public int getMapper() {
		return mapper;
	}

	@Override
	public boolean isVerticalNametableMirroring() {
		return nametableVerticalMirroring;
	}

	@Override
	public boolean isPALSystem() {
		return palSystem;
	}

}
