package mapper;

import components.CpuBus;
import components.PpuBus;

public abstract class Mapper {

	private CpuBus cpuBus;
	private PpuBus ppuBus;
	
	public Mapper() {

	}
	
	public abstract int readCpuBus(int address);
	
	public abstract int writeCpuBus(int address);

	protected CpuBus getCpuBus() {
		return cpuBus;
	}
	
	protected PpuBus getPpuBus() {
		return ppuBus;
	}
	
}
