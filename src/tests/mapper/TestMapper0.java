package mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import components.Cpu;

public class TestMapper0 {

	static final Cpu cpu = Cpu.getInstance();

	@BeforeAll
	static void init() {
		cpu.setMapper(new Mapper0(new byte[0x8000], new byte[0x2000]));
	}

	@Test
	void testRAM() {
		int value = 0;
		for (int address = 0; address < 0x2000; address++) {
			value = (value + 1 + address / 0x800) & 0xFF;
			cpu.storeMemory(address, value);

			// We check that we have the same result for the normal read and the read in the
			// RAM (no mirror)
			assertEquals(value, cpu.fetchMemory(address));
			assertEquals(value, cpu.fetchMemory(address & 0x7FF));
		}
	}

	@Disabled("Waiting the PPU to check if values were written and to write there")
	@Test
	void testPPURegisters() {
		int value = 0;
		for (int address = 0x2000; address < 0x4000; address++) {
			value = (value + 1) & 0xFF;
			cpu.storeMemory(address, value);

			// We check that we have the same result for the normal read and the read in the
			// RAM (no mirror)
			assertEquals(value, cpu.fetchMemory(address));
			assertEquals(value, cpu.fetchMemory(0x2000 + address & 0x7));
		}
	}

	@Test
	void testROM() {
		// Init ROM
		byte rom[] = new byte[0x8000];
		for (int index = 0; index < 0x8000; index++) {
			rom[index] = (byte) (index & 0xFF);
		}
		cpu.setMapper(new Mapper0(rom, new byte[0x2000]));

		for (int address = 0x8000; address <= 0xFFFF; address++) {
			int value = address & 0xFF;
			int valueToWrite = (value + 1) & 0xFF;
			cpu.storeMemory(address, valueToWrite);

			// We check that it's not written
			assertEquals(value, cpu.fetchMemory(address));
		}
	}
}
