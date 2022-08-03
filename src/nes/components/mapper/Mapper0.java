package nes.components.mapper;

import nes.components.Bus;
import nes.exceptions.AddressException;

public class Mapper0 extends Mapper {

	public Mapper0(byte[] prgRom, byte[] chrRom, boolean verticalScrolling) {
		super(prgRom, chrRom, verticalScrolling);
	}

	@Override
	public int mapCPUMemory(Bus cpuBus) throws AddressException {
		int repeat = (prgRom.length == 16384 ? 1 : 0);
		for (int i = 0x8000; i <= 0xBFFF; i++) {
			cpuBus.setByteToMemory(i, prgRom[i - 0x8000]);
		}

		for (int i = 0xC000; i <= 0xFFFF; i++) {
			cpuBus.setByteToMemory(i, prgRom[i - 0x8000 - repeat * 0x4000]); // Si on répète, on reprend les 0x4000 premiers
		}

		return 0x8000;
	}

	@Override
	public boolean mapPPUMemory(Bus ppuBus) throws AddressException {
		for (int i = 0; i < chrRom.length; i++) {
			ppuBus.setByteToMemory(i, chrRom[i]);
		}
		
		return verticalScrolling;
	}

}
