package nes.components.mapper;

import java.util.Map;

import nes.components.Bus;
import nes.exceptions.AddressException;
import nes.instructions.Instruction;

public class Mapper5 extends Mapper {

	public Mapper5(byte[] prgRom, byte[] chrRom, Map<Integer, Instruction> instructionMap, boolean verticalScrolling) {
		super(prgRom, chrRom, instructionMap, verticalScrolling);
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
