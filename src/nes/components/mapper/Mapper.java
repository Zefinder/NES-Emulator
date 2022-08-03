package nes.components.mapper;

import nes.components.Bus;
import nes.exceptions.AddressException;

public abstract class Mapper {

	protected byte[] prgRom, chrRom;
	protected boolean verticalScrolling;

	public Mapper(byte[] prgRom, byte[] chrRom, boolean verticalScrolling) {
		this.prgRom = prgRom;
		this.chrRom = chrRom;
		this.verticalScrolling = verticalScrolling;
	}

	public abstract int mapCPUMemory(Bus cpuBus) throws AddressException;

	public abstract boolean mapPPUMemory(Bus ppuBus) throws AddressException;
}
