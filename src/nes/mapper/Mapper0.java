package mapper;

import components.MemoryImpl;
import components.TranslatedAddress;
import components.bus.Bus;

public class Mapper0 extends Mapper {

	private static final int UNUSED_SIZE = 0x1FE0;
	private static final int WRAM_MAX_SIZE = 0x2000;
	private static final int PRG_ROM_SIZE = 0x4000;
	private static final int CHR_ROM_SIZE = 0x2000;

	private static final int WRAM_OFFSET = 0x1FE0;
	private static final int PRG_ROM_OFFSET = 0x3FE0;
	private static final int PRG_ROM2_OFFSET = 0x7FE0;

	private static class CpuMapper0 extends Bus {

		private MemoryImpl unused;
		private MemoryImpl wram;
		private MemoryImpl prgrom;
		private MemoryImpl prgrom2;
		private boolean isPrgromMirrored;

		public CpuMapper0(byte[] prgRom) {
			unused = new MemoryImpl(UNUSED_SIZE);
			wram = new MemoryImpl(WRAM_MAX_SIZE);

			// Write prog rom
			this.prgrom = new MemoryImpl(PRG_ROM_SIZE);
			for (int address = 0; address < PRG_ROM_SIZE; address++) {
				this.prgrom.write(address, prgRom[address]);
			}

			// Write prog rom 2 is not mirrored
			if (!(isPrgromMirrored = (prgRom.length == PRG_ROM_SIZE))) {
				prgrom2 = new MemoryImpl(PRG_ROM_SIZE);
				for (int address = 0; address < PRG_ROM_SIZE; address++) {
					this.prgrom2.write(address, prgRom[PRG_ROM_SIZE + address]);
				}
			}
		}

		@Override
		protected boolean checkImpl() {
			// Not link to another component
			return true;
		}
		
		@Override
		protected TranslatedAddress translateAddress(int address) {
			if (address < WRAM_OFFSET) {
				return new TranslatedAddress(unused, address);
			} else if (address < PRG_ROM_OFFSET) {
				return new TranslatedAddress(wram, address - WRAM_OFFSET);
			} else if (address < PRG_ROM2_OFFSET) {
				return new TranslatedAddress(prgrom, address - PRG_ROM_OFFSET);
			} else {
				if (isPrgromMirrored) {
					// Still use the first bank
					return new TranslatedAddress(prgrom, address - PRG_ROM2_OFFSET);
				} else {
					return new TranslatedAddress(prgrom2, address - PRG_ROM2_OFFSET);
				}
			}
		}
	}

	private static class PpuMapper0 extends MemoryImpl {

		public PpuMapper0(byte[] chrRom) {
			super(CHR_ROM_SIZE);
			for (int address = 0; address < Math.max(CHR_ROM_SIZE, chrRom.length); address++) {
				this.write(address, chrRom[address] & 0xFF);
			}
		}

	}

	public Mapper0(byte[] prgRom, byte[] chrRom) {
		super(new CpuMapper0(prgRom), new PpuMapper0(chrRom));
//
//		// PPU bus ChrROM
//		for (int address = 0; address < Math.max(0x2000, chrRom.length); address++) {
//			ppuBus.busContent[address] = chrRom[address] & 0xFF;
//		}
//
//		// TODO PPU nametable mirroring
//
//		// Set PC from reset vector
//		Cpu.getInstance().cpuInfo.PC = (cpuBus.busContent[Cpu.RESET_VECTOR + 1] << 8)
//				| cpuBus.busContent[Cpu.RESET_VECTOR];
	}

//	@Override
//	public void writeCpuBus(int address, int... values) {
//		int offset = 0;
//		for (int value : values) {
//			int writeAddress = (address + offset) & 0xFFFF;
//
//			// If in RAM, write in the RAM (and not in mirrors)
//			if (writeAddress < 0x2000) {
//				cpuBus.busContent[writeAddress & 0x7FF] = value;
//			}
//
//			// If in PPU registers or their mirrors then it's complicated
//			else if (writeAddress < 0x4000) {
//				int ppuRegister = 0x2000 + (writeAddress & 0x7);
//				ppuBusLatch = value;
//				switch (ppuRegister) {
//
//			} else if (writeAddress == 0x4014) {
//				// Launch the OAM DMA to fill the primary OAM
//				ppuBusLatch = value;
//
//				// Set DMA action
//				DmaAction action = new DmaAction(value << 8, 0x100,
//						(dmaAddress, dmaValue) -> Ppu.getInstance().setOamValue(value));
//				cpuInfo.oamDmaAction = action;
//
//				// Request DMA
//				cpuInfo.oamDmaRequested = true;
//
//			} else if (address < 0x8000) { // We don't want to write in the ROM
//				cpuBus.busContent[writeAddress] = value;
//			}
//
//			offset++;
//		}
//	}
//
//	@Override
//	public int readPpuBus(int address) {
//		int value = 0;
//
//		if (address < 0x3000) {
//			value = ppuBus.busContent[address];
//		} else if (address < 0x3F00) {
//			value = ppuBus.busContent[address - 0x1000];
//		} else {
//			int paletteIndex = address & 0x1F;
//			if ((paletteIndex & 0b1111) == 0) {
//				value = ppuBus.busContent[0x3F00];
//			} else if ((paletteIndex & 0b1111) == 0b0100) {
//				value = ppuBus.busContent[0x3F04];
//			} else if ((paletteIndex & 0b1111) == 0b1000) {
//				value = ppuBus.busContent[0x3F08];
//			} else if ((paletteIndex & 0b1111) == 0b1100) {
//				value = ppuBus.busContent[0x3F0C];
//			} else {
//				value = ppuBus.busContent[0x3F00 + paletteIndex];
//			}
//		}
//
//		return value;
//	}
//
//	@Override
//	public void writePpuBus(int address, int... values) {
//		int offset = 0;
//		for (int value : values) {
//			int writeAddress = (address + offset) & 0xFFFF;
//
//			if (writeAddress < 0x3000) {
//				ppuBus.busContent[writeAddress] = value;
//			} else if (writeAddress < 0x3F00) {
//				ppuBus.busContent[writeAddress - 0x1000] = value;
//			} else {
//				int paletteIndex = writeAddress & 0x1F;
//				if ((paletteIndex & 0b1111) == 0) {
//					ppuBus.busContent[0x3F00] = value;
//				} else if ((paletteIndex & 0b1111) == 0b0100) {
//					ppuBus.busContent[0x3F04] = value;
//				} else if ((paletteIndex & 0b1111) == 0b1000) {
//					ppuBus.busContent[0x3F08] = value;
//				} else if ((paletteIndex & 0b1111) == 0b1100) {
//					ppuBus.busContent[0x3F0C] = value;
//				} else {
//					ppuBus.busContent[0x3F00 + paletteIndex] = value;
//				}
//			}
//			offset++;
//		}
//	}

}
