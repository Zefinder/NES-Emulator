package components.ppu;

public class OAM {

	// Which tile it is in the patternTable
	private int tileAddress;
	
	// X position
	private int xPosition;
	
	private int paletteNumber;
	private int priority;
	private int flipSpriteHorizontally;
	private int flipSpriteVertically;
	
	// Which color from palette to choose (for the 8 pixels)
	private int[] paletteColor = new int[8];

	public OAM(int tile) {
		if (Ppu.getInstance().ppuInfo.spriteSize == 1) {
			// 8x16 mode
			tileAddress = (0x1000 * (tile & 0b1));
			tileAddress |= (tile & 0x1) << 4;
		} else {
			// 8x8 mode
			tileAddress = (0x1000 * Ppu.getInstance().ppuInfo.spritePatternTableAddress) | (tile << 4);
		}
	}

	public void setX(int x) {
		this.xPosition = x;
	}

	public void setAttribute(int attribute) {
		paletteNumber = attribute & 0b11;
		priority = (attribute >> 5) & 0b1;
		flipSpriteHorizontally = (attribute >> 6) & 0b1;
		flipSpriteVertically = (attribute >> 7) & 0b1;		
	}

	public void fetchLowPatternTable() {
		// Low plane
		int lowPattern = Ppu.getInstance().fetchMemory(tileAddress);
		for (int index = 0; index < 8; index++) {
			paletteColor[7 - index] = lowPattern & 0b1;
			lowPattern >>= 1;
		}
	}

	public void fetchHighPatternTable() {
		// High plane (+8)
		int highPattern = Ppu.getInstance().fetchMemory(tileAddress + 8);
		for (int index = 0; index < 8; index++) {
			paletteColor[7 - index] += 2 * (highPattern & 0b1);
			highPattern >>= 1;
		}
	}

	public void drawPixel() {

	}
}
