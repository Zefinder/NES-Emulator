package mapper;

import components.Memory;

public abstract class Mapper {

//	protected static final int 

//	protected final Cpu cpu = Cpu.getInstance();
//	protected final Ppu ppu = Ppu.getInstance();
//	protected final CpuBus cpuBus = new CpuBus();
//	protected final PpuBus ppuBus = new PpuBus();
//	protected final PpuInfo ppuInfo = Ppu.getInstance().ppuInfo;
//	protected final int[] oamMemory = Ppu.getInstance().oamMemory;

	protected int ppuBusLatch = 0;

	private final Memory cpuMapper;
	private final Memory ppuMapper;

	public Mapper(Memory cpuMapper, Memory ppuMapper) {
		this.cpuMapper = cpuMapper;
		this.ppuMapper = ppuMapper;
	}

	public Memory getCpuMapper() {
		return cpuMapper;
	}

	public Memory getPpuMapper() {
		return ppuMapper;
	}

}
