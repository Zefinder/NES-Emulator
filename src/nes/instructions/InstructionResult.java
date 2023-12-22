package instructions;

public class InstructionResult {
	
	private int value;
	private int flags;
	
	public InstructionResult(int value, int flags) {
		this.value = value;
		this.flags = flags;
	}
	
	public int getValue() {
		return value;
	}
	
	public int getFlags() {
		return flags;
	}

}
