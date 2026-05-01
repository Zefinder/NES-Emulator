package components;

public class MemoryImpl implements Memory {

	private int[] memory;
	
	public MemoryImpl(int size) {
		memory = new int[size];
	}
	
	@Override
	public int read(int address) {
		return memory[address];
	}
	
	@Override
	public void write(int address, int value) {
		memory[address] = value;
	}

}
