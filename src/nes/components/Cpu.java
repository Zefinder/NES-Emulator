package components;

import mapper.Mapper;

public class Cpu {

	private static final Cpu instance = new Cpu();

	/* Mapper */
	private Mapper mapper;

	/* Registers & Flags */
	public CpuInfo cpuInfo = new CpuInfo();

	private Cpu() {
	}

	/**
	 * Sets the mapper for the CPU. Do not change while running
	 * 
	 * @param mapper the mapper to use
	 */
	public void setMapper(Mapper mapper) {
		this.mapper = mapper;
	}

	/**
	 * Fetches a value in memory
	 * 
	 * @param address the address to look for the value
	 * @return a value
	 */
	public int fetchMemory(int address) {
		return mapper.readCpuBus(address)[0];
	}

	/**
	 * Fetches an address in memory
	 * 
	 * @param address the address to look for the address
	 * @return an address
	 */
	public int fetchAddress(int address) {
		int[] valueFetched = mapper.readCpuBus(address);
		return valueFetched[1] << 8 | valueFetched[0];
	}

	/**
	 * Stores a value in memory
	 * 
	 * @param address the address to store the value in the memory
	 * @param value   the value to store
	 */
	public void storeMemory(int address, int value) {
		mapper.writeCpuBus(address, value);
	}

	/**
	 * Pushes the value into the stack
	 * 
	 * @param value the value to push
	 */
	public void push(int value) {
		// Remember that SP points on nothing
		int SP = cpuInfo.SP;

		// Put value in memory
		mapper.writeCpuBus(0x100 | SP, value);

		// Decrement SP (wrap around 0x100)
		cpuInfo.SP = (SP - 1) & 0xFF;
	}

	/**
	 * Pops a value from the stack
	 * 
	 * @return the value poped
	 */
	public int pop() {
		// Remember that SP points on nothing
		int SP = cpuInfo.SP;

		// Increment SP (wrap around 0x100)
		SP = (SP + 1) & 0xFF;

		// Put value in memory
		int value = mapper.readCpuBus(0x100 | SP)[0];

		// Update SP
		cpuInfo.SP = SP;

		return value;
	}

	public static Cpu getInstance() {
		return instance;
	}
}
