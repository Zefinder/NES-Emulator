package components.bus;

import components.Cartridge;
import components.Memory;
import components.MemoryImpl;
import components.TranslatedAddress;
import components.register.IoRegisters;

public class CpuBus extends Bus {

	private static final int RAM_SIZE = 0x800;
	private static final int TEST_MODE_SIZE = 0x8;

	private static final int PPU_REGISTER_OFFSET = 0x2000;
	private static final int IO_REGISTER_OFFSET = 0x4000;
	private static final int TEST_MODE_OFFSET = 0x4018;
	private static final int CARTRIDGE_OFFSET = 0x4020;

	private final Memory ram;
	private MmioBus ppuMmioBus;
	private IoRegisters ioRegisters;
	private final Memory testModeRegisters;
	private Memory cpuBusMemory;

	private boolean cartridgePresent;

	public CpuBus() {
		ram = new MemoryImpl(RAM_SIZE);
		testModeRegisters = new MemoryImpl(TEST_MODE_SIZE);
		cartridgePresent = false;
	}
	
	public void setPpuMmioBus(MmioBus ppuMmioBus) {
		this.ppuMmioBus = ppuMmioBus;
	}
	
	public void setIoRegisters(IoRegisters ioRegisters) {
		this.ioRegisters = ioRegisters;
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
	protected boolean checkImpl() {
		// Cartridge can be null
		return ppuMmioBus != null && ioRegisters != null;
	}
	
	@Override
	public TranslatedAddress translateAddress(int address) {
		if (address < PPU_REGISTER_OFFSET) {
			return new TranslatedAddress(ram, address & 0x7FF);
		} else if (address < IO_REGISTER_OFFSET) {
			return new TranslatedAddress(ppuMmioBus, address & 0b111);
		} else if (address < TEST_MODE_OFFSET) {
			return new TranslatedAddress(ioRegisters, address & 0x17);
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
