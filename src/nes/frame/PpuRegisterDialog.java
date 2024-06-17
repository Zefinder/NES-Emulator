package frame;

public class PpuRegisterDialog extends ComponentInfoDialog {

	private static final String TITLE = "PPU Registers";
	private static final int ELEMENT_NUMBER = 8;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 325983604803180635L;
	
	public PpuRegisterDialog(int posX, int posY) {
		super(TITLE, ELEMENT_NUMBER, posX, posY);
	}

	@Override
	protected void registerElements() {
		addElement("PPU Controller");
		addElement("PPU Mask");
		addElement("PPU Status");
		addElement("PPU OAM Address");
		addElement("PPU OAM Data");
		addElement("PPU Scroll");
		addElement("PPU Address");
		addElement("PPU Data");
	}

	@Override
	protected void update() {
		setElementValue(0, "0x%02X".formatted(ppuInfo.getPpuController()));
		setElementValue(1, "0x%02X".formatted(ppuInfo.getPpuMask()));
		setElementValue(2, "0x%02X".formatted(ppuInfo.getPpuStatus()));
		setElementValue(3, "0x%02X".formatted(ppuInfo.ppuOamAddress));
		setElementValue(4, "0x%02X".formatted(ppuInfo.ppuOamData));
		setElementValue(5, "0x%04X".formatted(ppuInfo.ppuScroll));
		setElementValue(6, "0x%04X".formatted(ppuInfo.ppuAddress));
		setElementValue(7, "0x%02X".formatted(ppuInfo.ppuData));
	}

}
