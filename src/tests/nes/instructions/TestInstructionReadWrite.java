package nes.instructions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import components.Cpu;
import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.alu.ASLInstruction;
import instructions.registermemory.LDAInstruction;
import instructions.registermemory.STAInstruction;
import nes.MapperTest;

class TestInstructionReadWrite {

	static final MapperTest mapper = new MapperTest();
	static final Cpu cpu = Cpu.getInstance();

	@BeforeEach
	public void resetCPU() {
		// Set mapper
		cpu.setMapper(mapper);

		// Registers at 0
		cpu.cpuInfo.A = 0;
		cpu.cpuInfo.X = 0;
		cpu.cpuInfo.Y = 0;
		cpu.cpuInfo.SP = 0xFD;
		cpu.cpuInfo.PC = 0;

		// Flags at 0
		cpu.cpuInfo.C = 0;
		cpu.cpuInfo.Z = 0;
		cpu.cpuInfo.I = 0;
		cpu.cpuInfo.D = 0;
		cpu.cpuInfo.B = 0;
		cpu.cpuInfo.V = 0;
		cpu.cpuInfo.N = 0;

		// Reset all memory
		for (int address = 0; address <= 0xFFFF; address++) {
			cpu.storeMemory(address, 0);
		}

		// Reset counters
		mapper.resetCounters();
	}
	
	@Test
	void testReadWriteSpecialRegister() throws InstructionNotSupportedException {
		// Here we test if writing only writes and do not read, read only reads and do
		// not write, etc...
		// LDA reads but don't write
		LDAInstruction ldaInstruction = new LDAInstruction(AddressingMode.ABSOLUTE,
				MapperTest.SPECIAL_REGISTER_ADDRESS);
		ldaInstruction.execute();
		assertEquals(1, mapper.getReadCounter(), "LDA reads register!");
		assertEquals(0, mapper.getWriteCounter(), "LDA do not write register!");
		mapper.resetCounters();

		// STA writes but don't read
		STAInstruction staInstruction = new STAInstruction(AddressingMode.ABSOLUTE,
				MapperTest.SPECIAL_REGISTER_ADDRESS);
		staInstruction.execute();
		assertEquals(0, mapper.getReadCounter(), "STA do not read register!");
		assertEquals(1, mapper.getWriteCounter(), "STA writes register!");
		mapper.resetCounters();

		// ASL reads and writes
		ASLInstruction aslInstruction = new ASLInstruction(AddressingMode.ABSOLUTE,
				MapperTest.SPECIAL_REGISTER_ADDRESS);
		aslInstruction.execute();
		assertEquals(1, mapper.getReadCounter(), "ASL reads register!");
		assertEquals(1, mapper.getWriteCounter(), "ASL writes register!");
		mapper.resetCounters();
	}

	// TODO Here put tests for memory-based addressing mode
	// If all addressing modes work, then each instruction must be tested with only
	// one addressing mode! (Like ASL)

}
