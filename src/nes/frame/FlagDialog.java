package frame;

public class FlagDialog extends CpuInfoDialog {

	private static final String TITLE = "Flags";
	private static final int ELEMENT_NUMBER = 7;

	/**
	 * 
	 */
	private static final long serialVersionUID = -3356536921605440890L;

	public FlagDialog() {
		super(TITLE, ELEMENT_NUMBER);
	}

	@Override
	protected void registerElements() {
		addElement("C");
		addElement("Z");
		addElement("I");
		addElement("D");
		addElement("B");
		addElement("V");
		addElement("N");
	}

	@Override
	protected void update() {
		setElementValue(0, "%d".formatted(cpuInfo.C));
		setElementValue(1, "%d".formatted(cpuInfo.Z));
		setElementValue(2, "%d".formatted(cpuInfo.I));
		setElementValue(3, "%d".formatted(cpuInfo.D));
		setElementValue(4, "%d".formatted(cpuInfo.B));
		setElementValue(5, "%d".formatted(cpuInfo.V));
		setElementValue(6, "%d".formatted(cpuInfo.N));
	}

}
