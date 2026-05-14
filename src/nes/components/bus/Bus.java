package components.bus;

import components.Component;
import components.Memory;
import components.TranslatedAddress;

public abstract class Bus extends Component implements Memory {
	
//	private final int busSize;
//	public final int[] busContent;
//
//	public Bus(int busSize) {
//		this.busSize = busSize;
//		busContent = new int[busSize];
//	}
//	
//	// TODO Remove these functions
//
//	/**
//	 * <p>
//	 * Returns the element pointing at the address
//	 * </p>
//	 * 
//	 * @param address to look in the bus
//	 * @return the element at the specified address
//	 */
//	public int getFromBus(int address) {
//		return busContent[address];
//	}
//
//	/**
//	 * Writes the values in the bus at the specified address
//	 * 
//	 * @param address the address to write to
//	 * @param values  the values to write
//	 */
//	public void writeToBus(int address, int... values) {
//		for (int value : values) {
//			busContent[address & (busSize - 1)] = value & 0xFF;
//			address++;
//		}
//	}
	
	protected int openBus;
	
	public Bus() {
		this.openBus = 0;
	}
	
	protected abstract TranslatedAddress translateAddress(int address);
	
//	protected abstract int readImpl(TranslatedAddress translatedAddress);
//	
//	protected abstract void writeImpl(TranslatedAddress translatedAddress, int value);
	
	@Override
	public int read(int address) {
//		return readImpl(translateAddress(address));
		return translateAddress(address).read();
	}
	
	@Override
	public void write(int address, int value) {
//		writeImpl(translateAddress(address), value);
		translateAddress(address).write(value);
	}
}
