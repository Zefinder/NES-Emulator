package frame;

public class RegisterDialog extends CpuInfoDialog {

	private static final String TITLE = "Registers";
	private static final int ELEMENT_NUMBER = 5;

	/**
	 * 
	 */
	private static final long serialVersionUID = -3904741370682224481L;

	public RegisterDialog() {
		super(TITLE, ELEMENT_NUMBER);
	}

	@Override
	protected void registerElements() {
		addElement("A");
		addElement("X");
		addElement("Y");
		addElement("SP");
		addElement("PC");
	}

	@Override
	protected void update() {
		setElementValue(0, "0x%02X".formatted(cpuInfo.A));
		setElementValue(1, "0x%02X".formatted(cpuInfo.X));
		setElementValue(2, "0x%02X".formatted(cpuInfo.Y));
		setElementValue(3, "0x%02X".formatted(cpuInfo.SP));
		setElementValue(4, "0x%04X".formatted(cpuInfo.PC));
	}

}
