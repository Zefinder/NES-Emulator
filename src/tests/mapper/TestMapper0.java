package mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
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
		resetOamMemory();
		resetPpuPatternTable();
	}

	private static void resetOamMemory() {
		// Fill OAM memory
		for (int index = 0; index <= 0xFF; index++) {
			ppu.oamMemory[index] = (index + 1) & 0xFF;
		}
	}

	private static void resetPpuPatternTable() {
		// Fill PPU pattern table (works only if PPU Address and PPU Data works)
		for (int address = 0; address < 0x2000; address++) {
			cpu.storeMemory(0x2007, 0);
		}

		// Set 0x6A in 0x1234
		cpu.storeMemory(0x2006, 0x12);
		cpu.storeMemory(0x2006, 0x34, 0x6A);

		// Reset PPU Address
		cpu.storeMemory(0x2006, 0x00);
		cpu.storeMemory(0x2006, 0x00);
	}

	@Test
	void testCPURAM() {
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

	@Test
	void testPPURegisters() {
		for (int address = 0x2000; address < 0x4000; address++) {
			int register = address & 0x7;
			if (register == 0) {
				resetOamMemory();
				resetPpuPatternTable();
			}

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
				assertEquals(0xFF, cpu.fetchMemory(address), "Reading PPU OAM Address should return the latch value");
				break;

			// PPU OAM Data (R/W)
			case 4:
				ppu.ppuInfo.ppuOamAddress = 0;
				// Writing (will increment PPU OAM Address)
				for (int value = 0; value <= 0xFF; value++) {
					// Write value + 1 do be sure that it has been updated
					cpu.storeMemory(address, (value + 1) & 0xFF);
					assertEquals((value + 1) & 0xFF, ppu.oamMemory[value],
							"Writing to PPU OAM Data should write to the memory");
					assertEquals((value + 1) & 0xFF, ppu.ppuInfo.ppuOamAddress,
							"Writing to PPU OAM Data should increment PPU OAM Address");
				}

				// Reading
				for (int index = 0; index <= 0xFF; index++) {
					cpu.storeMemory(0x2003, index);
					int value = cpu.fetchMemory(address);
					assertEquals(ppu.oamMemory[index], value,
							"Reading to PPU OAM Data should return the value of the memory");
					assertEquals(ppu.ppuInfo.ppuOamData, value,
							"Reading to PPU OAM Data should return the same value than the register");
					assertEquals(ppu.ppuInfo.ppuOamAddress, index,
							"Reading to PPU OAM Data do not increment PPU OAM Address");
				}

			// Write and read
			{
				ppu.ppuInfo.ppuOamAddress = 0x42;
				cpu.storeMemory(address, 0x6A);
				// Now pointing to 0x43
				int value = cpu.fetchMemory(address);
				assertEquals(0x43, ppu.ppuInfo.ppuOamAddress,
						"Writing to PPU OAM Data should increment PPU OAM Address");
				assertEquals(ppu.oamMemory[0x43], value,
						"Reading to PPU OAM Data should return the same value than the memory");
				assertEquals(ppu.ppuInfo.ppuOamData, value,
						"Reading to PPU OAM Data after writing should return the same value than the register");

			}
				break;

			// PPU Scroll (Wx2)
			case 5:
				// First write
				cpu.storeMemory(address, 0x12);
				assertEquals(ppu.ppuInfo.w, 1, "w must be 1 after one write");
				assertEquals(ppu.ppuInfo.t, 0x12, "t must be written");

				// Second write
				cpu.storeMemory(address, 0x34);
				assertEquals(ppu.ppuInfo.w, 0, "w must be back to 0 after two writes");
				assertEquals(ppu.ppuInfo.ppuScroll, 0x1234, "Write twice to PPU Scroll updates its value");

				// Reading (write-only so 0x34)
				assertEquals(0x34, cpu.fetchMemory(address), "Reading PPU Scroll should return the latch value");

				// TODO Check scroll value
				break;

			// PPU Address (Wx2)
			case 6:
				// First write
				cpu.storeMemory(address, 0x12);
				assertEquals(ppu.ppuInfo.w, 1, "w must be 1 after one write");
				assertEquals(ppu.ppuInfo.t, 0x12, "t must be written");

				// Second write
				cpu.storeMemory(address, 0x34);
				assertEquals(ppu.ppuInfo.w, 0, "w must be back to 0 after two writes");
				assertEquals(ppu.ppuInfo.ppuAddress, 0x1234, "Write twice to PPU Address updates its value");
				assertEquals(0x6A, ppu.ppuInfo.ppuData, "Write twice to PPU Address updates PPU Data value");

				// Reading (write-only so 0x34)
				assertEquals(0x34, cpu.fetchMemory(address), "Reading PPU Scroll should return the latch value");
				break;

			// PPU Data (R/W)
			case 7:
				// Writing (test increment 1) (remember address is at 0x1234)
				cpu.storeMemory(address, 0x42);
				assertEquals(0x1235, ppu.ppuInfo.ppuAddress, "Writing to PPU Data should increment PPU Address by 1");

				// Writing (test increment 32)
				ppu.ppuInfo.setPpuController(0x04);
				cpu.storeMemory(address, 0x43);
				assertEquals(0x1255, ppu.ppuInfo.ppuAddress, "Writing to PPU Data should increment PPU Address by 32");

			{
				// Reset PPU Address
				cpu.storeMemory(0x2006, 0x12);
				cpu.storeMemory(0x2006, 0x34);

				// Reading (test increment 1)
				ppu.ppuInfo.setPpuController(0x00);
				int value = cpu.fetchMemory(address);
				assertEquals(0x42, value, "Reading to PPU Data should return the content of the memory");
				assertEquals(0x1235, ppu.ppuInfo.ppuAddress, "Writing to PPU Data should increment PPU Address by 1");

				// Reading (test increment 32)
				ppu.ppuInfo.setPpuController(0x04);
				value = cpu.fetchMemory(address);
				assertEquals(0x43, value, "Reading to PPU Data should return the content of the memory");
				assertEquals(0x1255, ppu.ppuInfo.ppuAddress, "Writing to PPU Data should increment PPU Address by 32");
			}

			{
				// Reset PPU Address
				cpu.storeMemory(0x2006, 0x12);
				cpu.storeMemory(0x2006, 0x34);
				ppu.ppuInfo.setPpuController(0x00);

				// Write and read
				cpu.storeMemory(address, 0x20);
				int value = cpu.fetchMemory(address);
				assertEquals(0x43, value, "Reading to PPU Data should return the content of the memory after");
				assertEquals(0x1236, ppu.ppuInfo.ppuAddress, "Writing to PPU Data should increment PPU Address by 2");
			}
				break;

			default:
				break;
			}
		}
	}

	@Test
	void testCPUROM() {
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

	@Test
	void testPPUPatternTable() {
		for (int address = 0; address < 0x2000; address++) {
			// Pattern table, nothing special...
			Mapper mapper = new Mapper0(new byte[0x8000], new byte[0x2000]);
			mapper.writePpuBus(address, address & 0xFF);
			int value = mapper.readPpuBus(address);
			assertEquals(address & 0xFF, value, "Value read must be the value written...");
		}
	}

	@Disabled("Make PPU nametable reflexion for this test")
	@Test
	void testPPUNametable() {
	}

	void testPPUPalettes() {
		for (int address = 0x3F00; address < 0x4000; address++) {
			Mapper mapper = new Mapper0(new byte[0x8000], new byte[0x2000]);
			mapper.writePpuBus(address, address & 0xFF);
			int value = mapper.readPpuBus(address);
			int valueNotMirrored = mapper.readPpuBus(0x3F00 + address & 0x1F);
			assertEquals(address & 0xFF, value, "Value read must be the value written...");
			assertEquals(address & 0xFF, valueNotMirrored, "Value read must be the value written in the non mirrored part...");
		}
	}
}
