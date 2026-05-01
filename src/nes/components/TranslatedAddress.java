package components;

public record TranslatedAddress(Memory memory, int address) {
	
	public void write(int value) {
		memory.write(address, value);
	}
	
	public int read() {
		return memory.read(address);
	}
	
};