package components.ppu;

public class Tile {

	// Which tile it is in the patternTable
	private int tileAddress;
	// Which palette to choose
	private int paletteByte;
	// Which color from palette to choose (for the 8 pixels)
	private int[] paletteColor;

	public Tile() {
	}

	public void setNametableAddress(int nametableOffset, int y) {
		// Tile address is H NNNN NNNN Pyyy, but P is for plane select (fetching colors)
		int tileNumber = Ppu.getInstance().fetchMemory(0x2000 | nametableOffset);
		int patternTableSelect = Ppu.getInstance().ppuInfo.spritePatternTableAddress * 0x1000;
		tileAddress = patternTableSelect | tileNumber << 4 | y;
	}

	public void setAttributeAddress(int attributeOffset) {
		paletteByte = Ppu.getInstance().fetchMemory(0x23C0 | attributeOffset);
	}

	public void fetchLowPatternTable() {
		// Low plane
		int lowPattern = Ppu.getInstance().fetchMemory(tileAddress);
		for (int index = 0; index < 8; index++) {
			paletteColor[index] = lowPattern & 0b1;
			lowPattern >>= 1;
		}
	}

	public void fetchHighPatternTable() {
		// High plane (+8)
		int highPattern = Ppu.getInstance().fetchMemory(tileAddress + 8);
		for (int index = 0; index < 8; index++) {
			paletteColor[index] += highPattern & 0b1;
			highPattern >>= 1;
		}
	}

	public void drawPixel(int coarseX, int coarseY) {
		int offset = 0;
		// Every 16 pixels, we change side
		if ((coarseX & 0x10) == 1) {
			offset += 2;
		}
		if ((coarseY & 0x10) == 1) {
			offset += 4;
		}

		int paletteIndex = paletteColor[Ppu.getInstance().ppuInfo.x];
		int paletteAddress = 0x3F00;
		if (paletteIndex != 0) {
			paletteAddress += 4 * ((paletteByte >> offset) & 0b11);
		}
		
		// TODO Send pixel to screen
		int pixelColor = Ppu.getInstance().fetchMemory(paletteAddress);
	}
}
