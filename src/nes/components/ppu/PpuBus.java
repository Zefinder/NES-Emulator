package components.ppu;

import components.Bus;
import components.Cartridge;
import components.Memory;
import components.MemoryImpl;
import components.TranslatedAddress;

public class PpuBus extends Bus {
	
	private static final int PALETTE_MEMORY_SIZE = 0x20;
	private static final int PALETTE_MEMORY_OFFSET = 0x3F00;
	
	private Memory ppuBusMemory;
	private final Memory paletteMemory;
	
	private boolean cartridgePresent;
	
	public PpuBus() {
		this.paletteMemory = new MemoryImpl(PALETTE_MEMORY_SIZE);
	}
	
	public int fetchAddress(int address) {
		int lsbFetched = read(address);
		int msbFetched = read((address + 1) & 0x3FFF);
		return msbFetched << 8 | lsbFetched;
	}
	
	public void insertCartridge(Cartridge cartridge) {
		if (cartridgePresent) {
			// TODO Raise error
		} else {
			ppuBusMemory = cartridge.getCpuBusMemory();
			cartridgePresent = true;
		}
	}

	public void removeCartridge() {
		if (!cartridgePresent) {
			// TODO Raise error
		} else {
			ppuBusMemory = null;
			cartridgePresent = false;
		}
	}
	
	@Override
	protected TranslatedAddress translateAddress(int address) {
		if (address < PALETTE_MEMORY_OFFSET) {
			if (cartridgePresent) {
				return new TranslatedAddress(ppuBusMemory, address);
			} else {
				// TODO Return OpenBus
				return new TranslatedAddress(new MemoryImpl(1), 0);
			}
		} else {
			return new TranslatedAddress(paletteMemory, address - PALETTE_MEMORY_OFFSET);
		}
	}

}
