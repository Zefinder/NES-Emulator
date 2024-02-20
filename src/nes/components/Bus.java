package components;

public abstract class Bus {
	
	private final int busSize;
	public final int[] busContent;

	public Bus(int busSize) {
		this.busSize = busSize;
		busContent = new int[busSize];
	}

	/**
	 * <p>
	 * Returns the element pointing at the address
	 * </p>
	 * 
	 * @param address to look in the bus
	 * @return the element at the specified address
	 */
	public int getFromBus(int address) {
		return busContent[address];
	}

	/**
	 * Writes the values in the bus at the specified address
	 * 
	 * @param address the address to write to
	 * @param values  the values to write
	 */
	public void writeToBus(int address, int... values) {
		for (int value : values) {
			busContent[address & (busSize - 1)] = value & 0xFF;
			address++;
		}
	}
}
