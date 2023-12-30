package components;

import mapper.Mapper;

public class Cpu {
	
	private static final Cpu instance = new Cpu();
	
	/* Mapper */
	private Mapper mapper;
	
	/* Registers & Flags */
	public CpuInfo cpuInfo = new CpuInfo();

	public Cpu() {
	}
	
	public int fetchMemory(int address) {
		return mapper.readCpuBus(address);
	}
	
	public int fetchAddress(int address) {
		return mapper.readCpuBus(address + 1) << 8 | mapper.readCpuBus(address);
	}
	
	public static Cpu getInstance() {
		return instance;
	}
}
