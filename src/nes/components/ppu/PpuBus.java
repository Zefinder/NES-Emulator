package components.ppu;

import components.Bus;

public class PpuBus extends Bus {
	
	private static final int PPU_BUS_SIZE = 0x4000;
	
	public PpuBus() {
		super(PPU_BUS_SIZE);
	}

}
