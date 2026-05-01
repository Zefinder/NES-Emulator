package components;

public abstract class Bus implements Memory {
	
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
	
	protected abstract TranslatedAddress translateAddress(int address);
	
//	protected abstract int readImpl(TranslatedAddress translatedAddress);
//	
//	protected abstract void writeImpl(TranslatedAddress translatedAddress, int value);
	
	// TODO Add open bus and check if memory is readable/writable
	public int getOpenBus() {
		return 0;
	}
	
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
