package disassemble;

import instructions.Instruction;

public class DisassemblyInfoINES extends DisassemblyInfo {

	public DisassemblyInfoINES(byte[] romFlags, byte[] prgRom, byte[] chrRom, Instruction[] instructions) {
		super(romFlags[2] >> 4, (romFlags[2] & 0b00000001) == 1, prgRom, chrRom, instructions);
	}

}
