package components.mapper;

import java.util.Map;

import components.Bus;
import exceptions.AddressException;
import instructions.Instruction;

public abstract class Mapper {

	protected byte[] prgRom, chrRom;
	protected boolean verticalScrolling;
	private Map<Integer, Instruction> instructionMap;

	public Mapper(byte[] prgRom, byte[] chrRom, Map<Integer, Instruction> instructionMap, boolean verticalScrolling) {
		this.prgRom = prgRom;
		this.chrRom = chrRom;
		this.verticalScrolling = verticalScrolling;
		this.instructionMap = instructionMap;
	}

	public abstract int mapCPUMemory(Bus cpuBus) throws AddressException;

	public abstract boolean mapPPUMemory(Bus ppuBus) throws AddressException;

	public Map<Integer, Instruction> getInstructionMap() {
		return instructionMap;
	}
}
