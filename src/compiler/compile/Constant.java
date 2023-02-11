package compile;

public class Constant {

	private String name;
	private int value;
	private boolean used;

	public Constant(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public boolean hasBeenUsed() {
		return used;
	}
	
	public String getName() {
		return name;
	}

	public int getValue() {
		used = true;
		return value;
	}

	@Override
	public boolean equals(Object other) {
		return name.equals(((Constant) other).name);
	}

	@Override
	public String toString() {
		return String.format("%s = %d", name, value);
	}

}
