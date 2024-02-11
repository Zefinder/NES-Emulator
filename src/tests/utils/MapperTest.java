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

	@Override
	public int[] readCpuBus(int address) {
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
