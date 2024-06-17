package mapper;

import components.DmaAction;
import components.cpu.Cpu;
import components.ppu.Ppu;

public class Mapper0 extends Mapper {

	public Mapper0(byte[] prgRom, byte[] chrRom) {
		// PrgROM can have a size of 0x4000 or 0x8000
		// Be careful, bytes are signed!
		if (prgRom.length == 0x4000) {
			// Then put it twice
			for (int address = 0; address < 0x4000; address++) {
				cpuBus.busContent[0x8000 + address] = prgRom[address] & 0xFF;
				cpuBus.busContent[0xC000 + address] = prgRom[address] & 0xFF;
			}
		} else {
			for (int address = 0; address < 0x8000; address++) {
				cpuBus.busContent[0x8000 + address] = prgRom[address] & 0xFF;
			}
		}

		// PPU bus ChrROM
		for (int address = 0; address < Math.max(0x2000, chrRom.length); address++) {
			ppuBus.busContent[address] = chrRom[address] & 0xFF;
		}

		// TODO PPU nametable mirroring

		// Set PC from reset vector
		Cpu.getInstance().cpuInfo.PC = (cpuBus.busContent[Cpu.RESET_VECTOR + 1] << 8)
				| cpuBus.busContent[Cpu.RESET_VECTOR];
	}

	@Override
	public int readCpuBus(int address) {
		int value = 0;

		if (address < 0x2000) {
			value = cpuBus.busContent[address & 0x7FF];
		}

		// If in PPU registers or their mirrors then it's complicated
		else if (address < 0x4000) {
			int ppuRegister = 0x2000 + (address & 0x7);

			switch (ppuRegister) {
			case 0x2000:
				value = ppuBusLatch;
				break;

			case 0x2001:
				value = ppuBusLatch;
				break;

			case 0x2002:
				value = ppuInfo.getPpuStatus();

				// Remove NMI in 2002 register
				ppuInfo.verticalBlankStart = 0;

				// Clear address latch used by PPUScroll and PPUAddress (0x2005 and 0x2006)
				ppuInfo.w = 0;

				ppuBusLatch = value;
				break;

			case 0x2003:
				value = ppuBusLatch;
				break;

			case 0x2004:
				value = ppuInfo.ppuOamData;
				ppuBusLatch = value;
				break;

			case 0x2005:
				value = ppuBusLatch;
				break;

			case 0x2006:
				value = ppuBusLatch;
				break;

			case 0x2007:
				value = ppuInfo.ppuData;
				ppuBusLatch = value;

				// Increment 0x2006 register
				ppuInfo.ppuAddress = (ppuInfo.ppuAddress + 1 + 31 * ppuInfo.vramAddressIncrement) & 0x3FFF;

				// Update 0x2007 with new address
				ppuInfo.ppuData = readPpuBus(ppuInfo.ppuAddress);
				break;
			}
		} else if (address == 0x4014) {
			// OAM DMA
			value = ppuBusLatch;

		} else {
			value = cpuBus.busContent[address];
		}

		return value;
	}

	@Override
	public void writeCpuBus(int address, int... values) {
		int offset = 0;
		for (int value : values) {
			int writeAddress = (address + offset) & 0xFFFF;

			// If in RAM, write in the RAM (and not in mirrors)
			if (writeAddress < 0x2000) {
				cpuBus.busContent[writeAddress & 0x7FF] = value;
			}

			// If in PPU registers or their mirrors then it's complicated
			else if (writeAddress < 0x4000) {
				int ppuRegister = 0x2000 + (writeAddress & 0x7);
				ppuBusLatch = value;
				switch (ppuRegister) {
				case 0x2000:
					ppuInfo.setPpuController(value);
					break;

				case 0x2001:
					ppuInfo.setPpuMask(value);
					break;

				case 0x2003:
					// Set PPU 0x2003 register
					ppuInfo.ppuOamAddress = value;

					// Update 0x2004 register with sprite value
					ppuInfo.ppuOamData = oamMemory[value];
					break;

				case 0x2004:
					// Set PPU 0x2004 register
					ppuInfo.ppuOamData = value;

					// Change sprite data
					oamMemory[ppuInfo.ppuOamAddress] = value;

					// Increment 0x2003 register
					ppuInfo.ppuOamAddress = (ppuInfo.ppuOamAddress + 1) & 0xFF;

					// Update 0x2004
					ppuInfo.ppuOamData = oamMemory[ppuInfo.ppuOamAddress];
					break;

				case 0x2005:
					// Set PPU 0x2005 register
					// This update must be triggered with a function since it required 2 writes
					ppuInfo.setPpuScroll(value);
					break;

				case 0x2006:
					// Set PPU 0x2006 register
					// This update must be triggered with a function since it required 2 writes
					ppuInfo.setPpuAddress(value);

					// Update 0x2007 with new address
					ppuInfo.ppuData = readPpuBus(ppuInfo.ppuAddress);
					break;

				case 0x2007:
					// Set PPU 0x2007 register
					ppuInfo.ppuData = value;

					// Change data at address
					writePpuBus(ppuInfo.ppuAddress, value);

					// Increment 0x2006 register
					ppuInfo.ppuAddress = (ppuInfo.ppuAddress + 1 + 31 * ppuInfo.vramAddressIncrement) & 0x3FFF;

					// Update 0x2007 with new address
					ppuInfo.ppuData = readPpuBus(ppuInfo.ppuAddress);
					break;
				}

			} else if (writeAddress == 0x4014) {
				// Launch the OAM DMA to fill the primary OAM
				ppuBusLatch = value;

				// Set DMA action
				DmaAction action = new DmaAction(value << 8, 0x100,
						(dmaAddress, dmaValue) -> Ppu.getInstance().setOamValue(value));
				cpuInfo.oamDmaAction = action;

				// Request DMA
				cpuInfo.oamDmaRequested = true;

			} else if (address < 0x8000) { // We don't want to write in the ROM
				cpuBus.busContent[writeAddress] = value;
			}

			offset++;
		}
	}

	@Override
	public int readPpuBus(int address) {
		int value = 0;

		if (address < 0x3000) {
			value = ppuBus.busContent[address];
		} else if (address < 0x3F00) {
			value = ppuBus.busContent[address - 0x1000];
		} else {
			int paletteIndex = address & 0x1F;
			if ((paletteIndex & 0b1111) == 0) {
				value = ppuBus.busContent[0x3F00];
			} else if ((paletteIndex & 0b1111) == 0b0100) {
				value = ppuBus.busContent[0x3F04];
			} else if ((paletteIndex & 0b1111) == 0b1000) {
				value = ppuBus.busContent[0x3F08];
			} else if ((paletteIndex & 0b1111) == 0b1100) {
				value = ppuBus.busContent[0x3F0C];
			} else {
				value = ppuBus.busContent[0x3F00 + paletteIndex];
			}
		}

		return value;
	}

	@Override
	public void writePpuBus(int address, int... values) {
		int offset = 0;
		for (int value : values) {
			int writeAddress = (address + offset) & 0xFFFF;

			if (writeAddress < 0x3000) {
				ppuBus.busContent[writeAddress] = value;
			} else if (writeAddress < 0x3F00) {
				ppuBus.busContent[writeAddress - 0x1000] = value;
			} else {
				int paletteIndex = writeAddress & 0x1F;
				if ((paletteIndex & 0b1111) == 0) {
					ppuBus.busContent[0x3F00] = value;
				} else if ((paletteIndex & 0b1111) == 0b0100) {
					ppuBus.busContent[0x3F04] = value;
				} else if ((paletteIndex & 0b1111) == 0b1000) {
					ppuBus.busContent[0x3F08] = value;
				} else if ((paletteIndex & 0b1111) == 0b1100) {
					ppuBus.busContent[0x3F0C] = value;
				} else {
					ppuBus.busContent[0x3F00 + paletteIndex] = value;
				}
			}
			offset++;
		}
	}

}
