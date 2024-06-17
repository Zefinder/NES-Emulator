package components.ppu;

public class Tile {

	// Which tile it is in the patternTable
	private int tileAddress;
	// Which palette to choose
	private int paletteNumber;
	// Which color from palette to choose (for the 8 pixels)
	private int[] paletteColor = new int[8];

	public Tile() {
	}

	public void setNametableAddress(int nametableOffset, int y) {
		// Tile address is H NNNN NNNN Pyyy, but P is for plane select (fetching colors)
		int tileNumber = Ppu.getInstance().fetchMemory(0x2000 | nametableOffset);
		int patternTableSelect = Ppu.getInstance().ppuInfo.backgroundPatternTableAddress * 0x1000;
		tileAddress = patternTableSelect | tileNumber << 4 | y;
	}

	public void setAttributeAddress(int attributeOffset, int coarseX, int coarseY) {
		int offset = 0;
		// Every 16 pixels, we change side
		if ((coarseX & 0b10) != 0) {
			offset += 2;
		}
		if ((coarseY & 0b10) != 0) {
			offset += 4;
		}
		
		int paletteByte = Ppu.getInstance().fetchMemory(0x23C0 | attributeOffset);
		paletteNumber = (paletteByte >> offset) & 0b11;
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
		int paletteIndex = paletteColor[Ppu.getInstance().ppuInfo.x];
		int paletteAddress = 0x3F00;
		if (paletteIndex != 0) {
			paletteAddress += 4 * paletteNumber + paletteIndex;
		}
		
		// Send pixel to screen
		int pixelColor = Ppu.getInstance().fetchMemory(paletteAddress);
		Ppu.getInstance().getScreen().setPixel(pixelColor);
	}
}
