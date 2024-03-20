package mapper;

import components.cpu.CpuBus;
import components.ppu.Ppu;
import components.ppu.PpuBus;
import components.ppu.PpuInfo;

public abstract class Mapper {

	protected final CpuBus cpuBus = new CpuBus();
	protected final PpuBus ppuBus = new PpuBus();
	protected final PpuInfo ppuInfo = Ppu.getInstance().ppuInfo;
	protected final int[] oamMemory = Ppu.getInstance().oamMemory;
	
	protected int ppuBusLatch = 0;
	
	public Mapper() {
	}

	public abstract int readCpuBus(int address);

	public abstract void writeCpuBus(int address, int... values);

	public abstract int readPpuBus(int address);

	public abstract void writePpuBus(int address, int... values);

}
