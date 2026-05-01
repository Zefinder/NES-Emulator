package components.cpu;

import components.Bus;
import components.Memory;
import components.MemoryImpl;
import components.TranslatedAddress;
import mapper.Mapper;

public class CpuBus extends Bus {

	private static final int RAM_SIZE = 0x800;
	private static final int PPU_REGISTER_SIZE = 0x8;
	private static final int APU_REGISTER_SIZE = 0x18;
	private static final int TEST_MODE_SIZE = 0x8;

	private static final int PPU_REGISTER_OFFSET = 0x2000;
	private static final int APU_REGISTER_OFFSET = 0x4000;
	private static final int TEST_MODE_OFFSET = 0x4018;
	private static final int CARTRIDGE_OFFSET = 0x4020;

	private final MemoryImpl ram;
	private final MemoryImpl ppuRegisters;
	private final MemoryImpl apuRegisters;
	private final MemoryImpl testModeRegisters;
	private final Memory cpuMapper;

	public CpuBus(Mapper mapper) {
		ram = new MemoryImpl(RAM_SIZE);
		ppuRegisters = new MemoryImpl(PPU_REGISTER_SIZE);
		apuRegisters = new MemoryImpl(APU_REGISTER_SIZE);
		testModeRegisters = new MemoryImpl(TEST_MODE_SIZE);
		cpuMapper = mapper.getCpuMapper();
	}

	@Override
	public TranslatedAddress translateAddress(int address) {
		if (address < PPU_REGISTER_OFFSET) {
			return new TranslatedAddress(ram, address & 0x7FF);
		} else if (address < APU_REGISTER_OFFSET) {
			return new TranslatedAddress(ppuRegisters, address & 0b111);
		} else if (address < TEST_MODE_OFFSET) {
			return new TranslatedAddress(apuRegisters, address & 0x17);
		} else if (address < CARTRIDGE_OFFSET){
			return new TranslatedAddress(testModeRegisters, address & 0b111);
		} else {
			return new TranslatedAddress(cpuMapper, address - CARTRIDGE_OFFSET);
		}
	}
	
}
