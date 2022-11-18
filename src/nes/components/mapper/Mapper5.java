package nes.components.mapper;

import nes.components.Bus;
import nes.exceptions.AddressException;

public class Mapper5 extends Mapper {

	public Mapper5(byte[] prgRom, byte[] chrRom, boolean verticalScrolling) {
		super(prgRom, chrRom, verticalScrolling);
	}

	@Override
	public int mapCPUMemory(Bus cpuBus) throws AddressException {
		return 0;
	}

	@Override
	public boolean mapPPUMemory(Bus ppuBus) throws AddressException {
		
		return verticalScrolling;
	}

}
