package components.cpu;

import components.Bus;

public class CpuBus extends Bus {

	private static final int CPU_BUS_SIZE = 0x10000;
	
	public CpuBus() {
		super(CPU_BUS_SIZE);
		// TODO Remove when PPU
		busContent[0x2002] = 0x80;
	}

}
