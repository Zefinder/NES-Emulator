package components.cpu;

import components.Bus;
import components.Cartridge;
import components.Memory;
import components.MemoryImpl;
import components.TranslatedAddress;

public class CpuBus extends Bus {

	private static final int RAM_SIZE = 0x800;
	private static final int PPU_REGISTER_SIZE = 0x8;
	private static final int APU_REGISTER_SIZE = 0x18;
	private static final int TEST_MODE_SIZE = 0x8;

	private static final int PPU_REGISTER_OFFSET = 0x2000;
	private static final int APU_REGISTER_OFFSET = 0x4000;
	private static final int TEST_MODE_OFFSET = 0x4018;
	private static final int CARTRIDGE_OFFSET = 0x4020;

	private final Memory ram;
	private final Memory ppuRegisters;
	private final Memory apuRegisters;
	private final Memory testModeRegisters;
	private Memory cpuBusMemory;

	private boolean cartridgePresent;

	public CpuBus() {
		ram = new MemoryImpl(RAM_SIZE);
		ppuRegisters = new MemoryImpl(PPU_REGISTER_SIZE);
		apuRegisters = new MemoryImpl(APU_REGISTER_SIZE);
		testModeRegisters = new MemoryImpl(TEST_MODE_SIZE);
		cartridgePresent = false;
	}

	public void insertCartridge(Cartridge cartridge) {
		if (cartridgePresent) {
			// TODO Raise error
		} else {
			cpuBusMemory = cartridge.getCpuBusMemory();
			cartridgePresent = true;
		}
	}

	public void removeCartridge() {
		if (!cartridgePresent) {
			// TODO Raise error
		} else {
			cpuBusMemory = null;
			cartridgePresent = false;
		}
	}

	@Override
	public TranslatedAddress translateAddress(int address) {
		if (address < PPU_REGISTER_OFFSET) {
			return new TranslatedAddress(ram, address & 0x7FF);
		} else if (address < APU_REGISTER_OFFSET) {
			return new TranslatedAddress(ppuRegisters, address & 0b111);
		} else if (address < TEST_MODE_OFFSET) {
			return new TranslatedAddress(apuRegisters, address & 0x17);
		} else if (address < CARTRIDGE_OFFSET) {
			return new TranslatedAddress(testModeRegisters, address & 0b111);
		} else {
			if (cartridgePresent) {
				return new TranslatedAddress(cpuBusMemory, address - CARTRIDGE_OFFSET);
			} else {
				// TODO Return OpenBus
				return new TranslatedAddress(new MemoryImpl(1), 0);
			}
		}
	}

}
