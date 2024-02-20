package mapper;

public class Mapper0 extends Mapper {

	public Mapper0(byte[] prgRom, byte[] chrRom) {
	}

	@Override
	public int readCpuBus(int address) {
		int value = 0;

		if (address < 0x2000) {
//			value = cpuBus.getFromBus(address & 0x7FF);
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
//				value = cpuBus.getFromBus(ppuRegister);
				value = cpuBus.busContent[ppuRegister];
				// Remove NMI in 2002 register
				// Clear PPUScroll and PPUAddress (0x2005 and 0x2006) registers
				ppuBusLatch = value;
				break;

			case 0x2003:
				value = ppuBusLatch;
				break;

			case 0x2004:
//				value = cpuBus.getFromBus(ppuRegister);
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
//				value = cpuBus.getFromBus(ppuRegister);
				value = cpuBus.busContent[ppuRegister];
				ppuBusLatch = value;
				break;
			}
		} else if (address == 0x4014) {
			// OAM DMA
			value = ppuBusLatch;

		} else {
//			value = cpuBus.getFromBus(address);
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
//				cpuBus.writeToBus(writeAddress & 0x7FF, value);
				cpuBus.busContent[writeAddress & 0x7FF] = value;
			}

			// If in PPU registers or their mirrors then it's complicated
			else if (address < 0x4000) {
				int ppuRegister = 0x2000 + (address & 0x7);
				ppuBusLatch = value;
				switch (ppuRegister) {
				case 0x2000:
					// TODO Set PPU 0x2000 register
//					cpuBus.writeToBus(ppuRegister, value);
					cpuBus.busContent[ppuRegister] = value;
					break;

				case 0x2001:
					// TODO Set PPU 0x2001 register
//					cpuBus.writeToBus(ppuRegister, value);
					cpuBus.busContent[ppuRegister] = value;
					break;

				case 0x2003:
					// TODO Set PPU 0x2003 register
//					cpuBus.writeToBus(ppuRegister, value);
					cpuBus.busContent[ppuRegister] = value;
					break;

				case 0x2004:
					// TODO Set PPU 0x2004 register
//					cpuBus.writeToBus(ppuRegister, value);
					cpuBus.busContent[ppuRegister] = value;
					break;

				case 0x2005:
					// TODO Set PPU 0x2005 register
//					cpuBus.writeToBus(ppuRegister, value);
					cpuBus.busContent[ppuRegister] = value;
					break;

				case 0x2006:
					// TODO Set PPU 0x2006 register
//					cpuBus.writeToBus(ppuRegister, value);
					cpuBus.busContent[ppuRegister] = value;
					break;

				case 0x2007:
					// TODO Set PPU 0x2007 register
//					cpuBus.writeToBus(ppuRegister, value);
					cpuBus.busContent[ppuRegister] = value;
					break;
				}
			} else if (address == 0x4014) {
				// OAM DMA
				// TODO Launch OAM DMA action and add wait cycles to CPU (256?)
				ppuBusLatch = value;
			} else if (address < 0x8000) { // We don't want to write in the ROM
//				cpuBus.writeToBus(writeAddress & 0x7FF, value);
				cpuBus.busContent[writeAddress] = value;
			}
		}
	}
}
