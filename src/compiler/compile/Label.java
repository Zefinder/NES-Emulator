package compile;

public class Label {

	static final int NO_ADDRESS = -1;

	private String name;
	private int address;

	public Label(String name, int address) {
		this.name = name;
		this.address = address;
	}
	
	public int getAddress() {
		return address;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		return name.equals(((Label) obj).name);
	}

	@Override
	public String toString() {
		return String.format("%s (0x%04X)", name, address);
	}
}
