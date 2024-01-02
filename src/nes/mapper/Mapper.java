package mapper;

import components.CpuBus;
import components.PpuBus;

public abstract class Mapper {

	protected static final CpuBus cpuBus = new CpuBus();
	protected static final PpuBus ppuBus = new PpuBus();

	public Mapper() {
	}

	public abstract int[] readCpuBus(int address);

	public abstract void writeCpuBus(int address, int... values);

}
