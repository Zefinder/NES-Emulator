package mapper;

import components.Cpu;

public class Mapper0 extends Mapper {

	public Mapper0(byte[] prgRom, byte[] chrRom) {
		// PrgROM can have a size of 0x4000 or 0x8000
		if (prgRom.length == 0x4000) {
			// Then put it twice
			for (int address = 0; address < 0x4000; address++) {
				cpuBus.busContent[0x8000 + address] = prgRom[address];
				cpuBus.busContent[0xC000 + address] = prgRom[address];
			}
		} else {
			for (int address = 0; address < 0x8000; address++) {
				cpuBus.busContent[0x8000 + address] = prgRom[address];
			}
		}

		// TODO PPU bus ChrROM

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
				value = cpuBus.busContent[ppuRegister];
				// Remove NMI in 2002 register
				// Clear PPUScroll and PPUAddress (0x2005 and 0x2006) registers
				ppuBusLatch = value;
				break;

			case 0x2003:
				value = ppuBusLatch;
				break;

			case 0x2004:
				value = cpuBus.busContent[ppuRegister];
				ppuBusLatch = value;
				break;

			case 0x2005:
				value = ppuBusLatch;
				break;

			case 0x2006:
				value = ppuBusLatch;
				break;

			case 0x2007:
				value = cpuBus.busContent[ppuRegister];
				ppuBusLatch = value;
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
			else if (address < 0x4000) {
				int ppuRegister = 0x2000 + (address & 0x7);
				ppuBusLatch = value;
				switch (ppuRegister) {
				case 0x2000:
					// TODO Set PPU 0x2000 register
					cpuBus.busContent[ppuRegister] = value;
					break;

				case 0x2001:
					// TODO Set PPU 0x2001 register
					cpuBus.busContent[ppuRegister] = value;
					break;

				case 0x2003:
					// TODO Set PPU 0x2003 register
					cpuBus.busContent[ppuRegister] = value;
					break;

				case 0x2004:
					// TODO Set PPU 0x2004 register
					cpuBus.busContent[ppuRegister] = value;
					break;

				case 0x2005:
					// TODO Set PPU 0x2005 register
					cpuBus.busContent[ppuRegister] = value;
					break;

				case 0x2006:
					// TODO Set PPU 0x2006 register
					cpuBus.busContent[ppuRegister] = value;
					break;

				case 0x2007:
					// TODO Set PPU 0x2007 register
					cpuBus.busContent[ppuRegister] = value;
					break;
				}
			} else if (address == 0x4014) {
				// OAM DMA
				// TODO Launch OAM DMA action
				ppuBusLatch = value;
			} else if (address < 0x8000) { // We don't want to write in the ROM
				cpuBus.busContent[writeAddress] = value;
			}
		}
	}
}
