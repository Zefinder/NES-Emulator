package components;

import exceptions.AddressException;
import listener.EventManager;

public abstract class Bus {

	private byte[] memoryMap;
	private boolean isCPU;
	
	public Bus(int memorySize, boolean isCPU) {
		memoryMap = new byte[memorySize];
		this.isCPU = isCPU;
	}

	protected synchronized byte getByte(int address) throws AddressException {
		if (address > memoryMap.length)
			throw new AddressException(String.format("The adress is out of the memory! (0x%04X)", address));

		return memoryMap[address];
	}

	protected synchronized void setByte(int address, byte toSet) throws AddressException {
		// Il n'y a aucune raison qu'il ne trouve pas si c'est dans l'adressage !

		if (address > memoryMap.length)
			throw new AddressException(String.format("The adress is out of the memory! (0x%04X)", address));

		memoryMap[address] = toSet;
		
		EventManager.getInstance().fireValueChanged(isCPU, address, toSet);
	}

	public byte[] getValues() {
		return memoryMap;
	}

	public abstract byte getByteFromMemory(int address) throws AddressException;

	public abstract void setByteToMemory(int address, byte toSet) throws AddressException;

}
