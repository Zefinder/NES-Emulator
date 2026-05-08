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

	public void setNametableAddress(PpuBus bus, int nametableOffset, int backgroundPatternTableAddress, int y) {
		// Tile address is H NNNN NNNN Pyyy, but P is for plane select (fetching colors)
		int tileNumber = bus.fetchAddress(0x2000 | nametableOffset);
		int patternTableSelect = backgroundPatternTableAddress * 0x1000;
		tileAddress = patternTableSelect | tileNumber << 4 | y;
	}

	public void setAttributeAddress(PpuBus bus, int attributeOffset, int coarseX, int coarseY) {
		int offset = 0;
		// Every 16 pixels, we change side
		if ((coarseX & 0b10) != 0) {
			offset += 2;
		}
		if ((coarseY & 0b10) != 0) {
			offset += 4;
		}
		
		int paletteByte = bus.fetchAddress(0x23C0 | attributeOffset);
		paletteNumber = (paletteByte >> offset) & 0b11;
	}

	public void fetchLowPatternTable(PpuBus bus) {
		// Low plane
		int lowPattern = bus.fetchAddress(tileAddress);
		for (int index = 0; index < 8; index++) {
			paletteColor[7 - index] = lowPattern & 0b1;
			lowPattern >>= 1;
		}
	}

	public void fetchHighPatternTable(PpuBus bus) {
		// High plane (+8)
		int highPattern = bus.fetchAddress(tileAddress + 8);
		for (int index = 0; index < 8; index++) {
			paletteColor[7 - index] += 2 * (highPattern & 0b1);
			highPattern >>= 1;
		}
	}

	public int drawPixel(PpuBus bus, int x) {
		int paletteIndex = paletteColor[x];
		int paletteAddress = 0x3F00;
		if (paletteIndex != 0) {
			paletteAddress += 4 * paletteNumber + paletteIndex;
		}
		
		// Send pixel to screen
		int pixelColor = bus.fetchAddress(paletteAddress);
		return pixelColor;
//		Ppu.getInstance().getScreen().setPixel(pixelColor);
	}
}
