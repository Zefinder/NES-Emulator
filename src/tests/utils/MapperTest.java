package utils;

import mapper.Mapper;

public class MapperTest extends Mapper {

	// Address of special register
	public static final int SPECIAL_REGISTER_ADDRESS = 0x1234;

	// Read and write counter for special register
	private int readCounter = 0;
	private int writeCounter = 0;

	public MapperTest() {
	}
	
	public MapperTest(int[] cpuRom, int[] ppuRom) {
		for (int address = 0; address < cpuRom.length; address++) {
			cpuBus.busContent[address + 0x8000] = cpuRom[address];
		}
		
		for (int address = 0; address < ppuRom.length; address++) {
			ppuBus.busContent[address] = ppuRom[address];
		}
	}

	@Override
	public int readCpuBus(int address) {
		if (address == SPECIAL_REGISTER_ADDRESS) {
			readCounter++;
		}

		return cpuBus.getFromBus(address);
	}

	@Override
	public void writeCpuBus(int address, int... values) {
		if (address == SPECIAL_REGISTER_ADDRESS) {
			writeCounter++;
		}

		cpuBus.writeToBus(address, values);
	}

	@Override
	public int readPpuBus(int address) {
		return ppuBus.busContent[address];
	}

	@Override
	public void writePpuBus(int address, int... values) {
		int offset = 0;
		for (int value : values) {
			int writeAddress = (address + offset) & 0xFFFF;
			ppuBus.busContent[writeAddress] = value;
		}
	}

	public int getReadCounter() {
		return readCounter;
	}

	public int getWriteCounter() {
		return writeCounter;
	}

	public void resetCounters() {
		readCounter = 0;
		writeCounter = 0;
	}

}
