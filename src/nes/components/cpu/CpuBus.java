package components.cpu;

import components.Memory;
import components.MemoryImpl;
import mapper.Mapper;

public class CpuBus {

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
	private final Mapper mapper;

	public CpuBus(Mapper mapper) {
		ram = new MemoryImpl(RAM_SIZE);
		ppuRegisters = new MemoryImpl(PPU_REGISTER_SIZE);
		apuRegisters = new MemoryImpl(APU_REGISTER_SIZE);
		testModeRegisters = new MemoryImpl(TEST_MODE_SIZE);
		this.mapper = mapper;
	}

	private TranslatedAddress translateAddress(int address) {
		if (address < PPU_REGISTER_OFFSET) {
			return new TranslatedAddress(ram, address & 0x7FF);
		} else if (address < APU_REGISTER_OFFSET) {
			return new TranslatedAddress(ppuRegisters, address & 0b111);
		} else if (address < TEST_MODE_OFFSET) {
			return new TranslatedAddress(apuRegisters, address & 0x17);
		} else if (address < CARTRIDGE_OFFSET){
			return new TranslatedAddress(testModeRegisters, address & 0b111);
		} else {
			return new TranslatedAddress(mapper, address);
		}
	}

	public void writeBus(int address, int value) {
		if (address >= CARTRIDGE_OFFSET) {
			// TODO Use mapper
			System.out.println("Mapper write at address %04X with value %d".formatted(address, value & 0xFF));
		} else {
			translateAddress(address).write(value);	
		}

		// TODO Find a way to deal with PPU registers
		// Create Memory interface where you can read and write and return TranslatedAddress(memory, address)
	}

	public int readBus(int address) {
		if (address >= CARTRIDGE_OFFSET) {
			// TODO Use mapper
		}

		return 0;
	}

	private record TranslatedAddress(Memory memory, int address) {
		
		public void write(int value) {
			memory.write(address, value);
		}
		
		public int read() {
			return memory.read(address);
		}
		
	};
	
}
