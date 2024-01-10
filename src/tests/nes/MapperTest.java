package nes;

import mapper.Mapper;

public class MapperTest extends Mapper {

	public MapperTest() {
	}

	@Override
	public int[] readCpuBus(int address) {
		return cpuBus.getFromBus(address);
	}

	@Override
	public void writeCpuBus(int address, int... values) {
		cpuBus.writeToBus(address, values);
	}

}