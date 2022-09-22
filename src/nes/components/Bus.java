package nes.components;

import java.util.LinkedHashMap;
import java.util.Map;

import nes.exceptions.AddressException;
import nes.listener.EventManager;
import nes.listener.RegisterListener;

public abstract class Bus {

	private Map<Integer, byte[]> memoryMap;
	private int lastAddress;

	public Bus() {
		memoryMap = new LinkedHashMap<>();
		lastAddress = -1;
	}

	public void addToMemoryMap(byte[] byteArray) {
		lastAddress += byteArray.length;
		memoryMap.put(lastAddress, byteArray);
	}

	protected synchronized byte getByte(int address) throws AddressException {
		// Il n'y a aucune raison qu'il ne trouve pas si c'est dans l'adressage !
		byte res = 0x00;

		if (address > this.lastAddress)
			throw new AddressException(String.format("The adress is out of the memory! (0x%04X)", address));

		for (Integer maxAddress : memoryMap.keySet()) {
			// Si l'adresse est inférieure à l'adresse alors c'est good !
			if (address <= maxAddress) {
				// On soustrait l'adresse à l'adresse max et on prend dans le tableau de byte
				res = memoryMap.get(maxAddress)[address + memoryMap.get(maxAddress).length - maxAddress - 1];
				break;
			}
		}

		return res;
	}

	protected synchronized void setByte(int address, byte toSet) throws AddressException {
		// Il n'y a aucune raison qu'il ne trouve pas si c'est dans l'adressage !

		if (address > this.lastAddress)
			throw new AddressException("The adress is out of the memory!");

		for (Integer maxAddress : memoryMap.keySet()) {
			// Si l'adresse est inférieure à l'adresse alors c'est good !
			if (address <= maxAddress) {
				// On soustrait l'adresse à l'adresse max et on prend dans le tableau de byte
				memoryMap.get(maxAddress)[address + memoryMap.get(maxAddress).length - maxAddress - 1] = toSet;
				break;
			}
		}
	}
	
	public int getSize() {
		return lastAddress + 1;
	}
	
	public byte[] getValues() {
		byte[] res = new byte[lastAddress + 1];
		int currentAddress = 0;
		
		for (byte[] array : memoryMap.values()) {
			System.arraycopy(array, 0, res, currentAddress, array.length);
			currentAddress += array.length;
		}
		
		return res;
	}

	public abstract byte getByteFromMemory(int address) throws AddressException;

	public abstract void setByteToMemory(int address, byte toSet) throws AddressException;

}
