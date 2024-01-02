package components;

public abstract class Bus {

	private final int busSize;
	private final int[] busContent;

	public Bus(int busSize) {
		this.busSize = busSize;
		busContent = new int[busSize];
	}

	/**
	 * <p>
	 * Returns the element pointing at the address and the next one. If the address
	 * is the last element of the bus, then this will return the element at address
	 * 0.
	 * </p>
	 * 
	 * <p>
	 * It also returns the element at the next address since we can sometimes want
	 * to get the address stored in memory in one call. Optimization !
	 * </p>
	 * 
	 * @param address to look in the bus
	 * @return the element at the specified address and the next one
	 */
	public int[] getFromBus(int address) {
		if (address + 1 == busSize) {
			return new int[] { busContent[address], busContent[0] };
		}

		return new int[] { busContent[address], busContent[address + 1] };
	}

	/**
	 * Writes the values in the bus at the specified address
	 * 
	 * @param address the address to write to
	 * @param values   the values to write
	 */
	public void writeToBus(int address, int... values) {
		for (int value : values) {
			busContent[address & (busSize - 1)] = value & 0xFF;
			address++;
		}
	}
}
