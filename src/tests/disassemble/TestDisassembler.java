package disassemble;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import exceptions.NotNesFileException;
import instructions.AddressingMode;
import instructions.Instruction;
import instructions.alu.ADCInstruction;
import instructions.alu.ASLInstruction;
import instructions.alu.ORAInstruction;
import instructions.branch.BCCInstruction;
import instructions.branch.BEQInstruction;
import instructions.branch.BPLInstruction;
import instructions.branch.BVCInstruction;
import instructions.flags.SEDInstruction;
import instructions.flags.SEIInstruction;
import instructions.jump.BRKInstruction;
import instructions.registermemory.LDAInstruction;
import instructions.registermemory.LDXInstruction;
import instructions.registermemory.STAInstruction;

public class TestDisassembler {

	private static final Disassembler disass = new Disassembler();

	@Test
	void testDisassembleNonNesFile() {
		File notNesFile = new File("./src/tests/disassemble/notNesFile.nes");
		assertThrows(NotNesFileException.class, () -> disass.disassembleFile(notNesFile));
	}

	@Test
	void testDisassembleEmptyINESFile() throws NotNesFileException, IOException {
		File notNesFile = new File("./src/tests/disassemble/emptyNesFile.nes");
		DisassemblyInfo info = disass.disassembleFile(notNesFile);

		assertInstanceOf(DisassemblyInfoINES.class, info);
		assertArrayEquals(new byte[0], info.getPrgRom());
		assertArrayEquals(new byte[0], info.getChrRom());
		assertArrayEquals(new Instruction[0], info.getInstructions());
	}

	@Test
	void testDisassembleUnknownInstruction() {
		Instruction instruction = disass.disassemble(0xFC, 0, 0);
		assertNull(instruction);
	}

	@Test
	void testDisassembleIncompleteInstruction2Bytes() {
		Instruction instruction = disass.disassemble(0x69, -1, 0);
		assertNull(instruction);
	}

	@Test
	void testDisassembleIncompleteInstruction3Bytes() {
		Instruction instruction = disass.disassemble(0x6D, 0, -1);
		assertNull(instruction);

		instruction = disass.disassemble(0x6D, -1, -1);
		assertNull(instruction);
	}

	@Test
	void testDisassembleNesFile() throws NotNesFileException, IOException {
		// Content of instructions in littleNesFile.nes
		// LDA #$50
		// BVC *-121
		// STA #$1A
		// null
		// LDA #$03
		// null
		// LDX #$0A
		// ASL A
		// ADC $10,X
		// BPL *-110
		// BCC *+3
		// ORA ($78,X)
		// SEI
		// SED
		// ...null...
		// BEQ *+1
		// null
		// BRK
		// null
		// BEQ *+1
		// null

		File littleNesFile = new File("./src/tests/disassemble/littleNesFile.nes");
		DisassemblyInfo info = disass.disassembleFile(littleNesFile);

		byte[] expectedPrgRom = new byte[0x8000];
		final FileInputStream reader = new FileInputStream(littleNesFile);
		reader.readNBytes(0x10);
		reader.readNBytes(expectedPrgRom, 0, 0x8000);
		reader.close();

		Instruction[] instructions = new Instruction[0x8000];
		instructions[0] = new LDAInstruction(AddressingMode.IMMEDIATE, 0x50);
		instructions[1] = new BVCInstruction(AddressingMode.RELATIVE, 0x85);
		instructions[2] = new STAInstruction(AddressingMode.ZEROPAGE, 0x1A);
		instructions[3] = null;
		instructions[4] = new LDAInstruction(AddressingMode.IMMEDIATE, 0x03);
		instructions[5] = null;
		instructions[6] = new LDXInstruction(AddressingMode.IMMEDIATE, 0x0A);
		instructions[7] = new ASLInstruction(AddressingMode.ACCUMULATOR);
		instructions[8] = new ADCInstruction(AddressingMode.ZEROPAGE_X, 0x10);
		instructions[9] = new BPLInstruction(AddressingMode.RELATIVE, 0x90);
		instructions[10] = new BCCInstruction(AddressingMode.RELATIVE, 0x01);
		instructions[11] = new ORAInstruction(AddressingMode.INDIRECT_X, 0x78);
		instructions[12] = new SEIInstruction(AddressingMode.IMPLICIT);
		instructions[13] = new SEDInstruction(AddressingMode.IMPLICIT);

		for (int address = 14; address < 0x3FFA; address++) {
			instructions[address] = null;
		}

		instructions[0x7FFA] = new BEQInstruction(AddressingMode.RELATIVE, 0xFF);
		instructions[0x7FFB] = null;
		instructions[0x7FFC] = new BRKInstruction(AddressingMode.IMPLICIT);
		instructions[0x7FFD] = null;
		instructions[0x7FFE] = new BEQInstruction(AddressingMode.RELATIVE, 0xFF);
		instructions[0x7FFF] = null;

		assertArrayEquals(expectedPrgRom, info.getPrgRom());
		assertArrayEquals(new byte[0], info.getChrRom());
		assertArrayEquals(instructions, info.getInstructions());
	}

}
