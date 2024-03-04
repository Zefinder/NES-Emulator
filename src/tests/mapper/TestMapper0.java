package mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import components.Cpu;
import components.Ppu;

public class TestMapper0 {

	static final Cpu cpu = Cpu.getInstance();
	static final Ppu ppu = Ppu.getInstance();

	@BeforeAll
	static void init() {
		// Set mapper
		cpu.setMapper(new Mapper0(new byte[0x8000], new byte[0x2000]));

		// Fill OAM memory
		for (int index = 0; index <= 0xFF; index++) {
			ppu.oamMemory[index] = (index + 1) & 0xFF;
		}
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

//	@Disabled("Waiting the PPU to check if values were written and to write there")
	@Test
	void testPPURegisters() {
		for (int address = 0x2000; address < 0x4000; address++) {
			int register = address & 0x7;

			switch (register) {
			// PPU Controller (W)
			case 0:
				// Writing
				for (int value = 0; value <= 0xFF; value++) {
					cpu.storeMemory(address, value);
					assertEquals(value, ppu.ppuInfo.getPpuControl(),
							"Writing to PPU Controller should write to the register");
				}

				// Reading (write-only so 0xFF even after modification)
				ppu.ppuInfo.setPpuController(0x42);
				assertEquals(0xFF, cpu.fetchMemory(address), "Reading PPU Controller should return the latch value");
				break;

			// PPU Mask (W)
			case 1:
				// Writing
				for (int value = 0; value <= 0xFF; value++) {
					cpu.storeMemory(address, value);
					assertEquals(value, ppu.ppuInfo.getPpuMask(), "Writing to PPU Mask should write to the register");
				}

				// Reading (write-only so 0xFF even after modification)
				ppu.ppuInfo.setPpuMask(0x42);
				assertEquals(0xFF, cpu.fetchMemory(address), "Reading PPU Mask should return the latch value");
				break;

			// PPU Status (R)
			case 2:
				ppu.ppuInfo.setPpuStatus(0);

				// Writing (read-only so fills the latch)
				for (int value = 0; value <= 0xFF; value++) {
					cpu.storeMemory(address, value);
					assertEquals(0, ppu.ppuInfo.getPpuStatus(), "Writing to PPU Status should not modify the register");
					// Reading a write only register
					assertEquals(value, cpu.fetchMemory(0x2000), "Writing to PPU Status should fill the latch");
				}

				// Reading
				for (int value = 0; value <= 0xFF; value++) {
					ppu.ppuInfo.setPpuStatus(value);
					ppu.ppuInfo.w = 1;

					assertEquals(value & 0xE0, cpu.fetchMemory(address),
							"Reading PPU Status should return the register value");
					// VBlank always set to 0 after reading
					assertEquals(0, ppu.ppuInfo.verticalBlankStart,
							"Reading PPU Status should reset the NMI signal (if set)");
					// w always set to 0 after reading
					assertEquals(0, ppu.ppuInfo.w, "Reading PPU Status should reset the w register");
				}
				break;

			// PPU OAM Address (W)
			case 3:
				// Writing (will change PPU OAM Data)
				for (int value = 0; value <= 0xFF; value++) {
					cpu.storeMemory(address, value);
					assertEquals(value, ppu.ppuInfo.ppuOamAddress,
							"Writing to PPU OAM Address should write to the register");
					// Check value of PPU OAM Data (value + 1 = data)
					assertEquals((value + 1) & 0xFF, ppu.ppuInfo.ppuOamData);
				}

				// Reading (write-only so 0xFF even after modification)
				ppu.ppuInfo.ppuOamAddress = 0x42;
				assertEquals(0xFF, cpu.fetchMemory(address), "Reading PPU Address should return the latch value");
				break;

			default:
				break;
			}
		}
	}

	@Test
	void testROM() {
		// Init ROM
		byte[] rom = new byte[0x8000];
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
