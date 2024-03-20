package components.ppu;

public class PpuInfo {

	/* Registers */
	/**
	 * Scroll position when rendering, VRAM address when not rendering v has the
	 * following form during rendering:
	 * 
	 * <pre>
	 * yyy NN YYYYY XXXXX
	 * </pre>
	 * 
	 * with
	 * <ul>
	 * <li><em>y</em> the fine Y scroll
	 * <li><em>N</em> the nametable select (0 to 3)
	 * <li><em>Y</em> the coarse Y scroll
	 * <li><em>X</em> the coarse X scroll
	 * </ul>
	 */
	public int v;
	/**
	 * Starting coarse-x scroll for the next scanline and the starting y scroll for
	 * the screen during rendering, temporary value when not rendering
	 */
	public int t;
	/** Fine-x position of the current scroll */
	public int x;
	/** Write latch for PPU Scroll and PPU Address */
	public int w;

	/* Shared Registers */
	// PPU Control (0x2000)
	public int baseNametableAddress;
	public int vramAddressIncrement;
	public int spritePatternTableAddress;
	public int backgroundPatternTableAddress;
	public int spriteSize;
	public int ppuMasterSlaveSelect;
	public int generateNmi;

	// PPU Mask (0x2001)
	public int greyScale;
	public int showBackgroundInLeftmost;
	public int showSpriteInLeftmost;
	public int showBackground;
	public int showSprites;
	public int emphasizeGreen;
	public int emphasizeRed;
	public int emphasizeBlue;

	// PPU Status (0x2002)
	public int spriteOverflow;
	public int sprite0Hit;
	public int verticalBlankStart;

	// PPU OAM Address (0x2003)
	public int ppuOamAddress;

	// PPU OAM DATA (0x2004)
	public int ppuOamData;

	// PPU Scroll (0x2005)
	public int ppuScroll;

	// PPU Address (0x2006)
	public int ppuAddress;

	// PPU Data (0x2007)
	public int ppuData;

	// PPU OAM DMA (0x4014)
	public int ppuOamDma;

	// PPU Pattern table address (TODO Understand...)
	public int fineYOffset;
	public int bitPlane;
	public int tileColumn;
	public int tileRow;
	public int patternTableHalf;

	public PpuInfo() {
		this.v = 0;
		this.t = 0;
		this.x = 0;
		this.w = 0;

		this.baseNametableAddress = 0;
		this.vramAddressIncrement = 0;
		this.spritePatternTableAddress = 0;
		this.backgroundPatternTableAddress = 0;
		this.spriteSize = 0;
		this.ppuMasterSlaveSelect = 0;
		this.generateNmi = 0;

		this.greyScale = 0;
		this.showBackgroundInLeftmost = 0;
		this.showSpriteInLeftmost = 0;
		this.showBackground = 0;
		this.showSprites = 0;
		this.emphasizeGreen = 0;
		this.emphasizeRed = 0;
		this.emphasizeBlue = 0;

		this.spriteOverflow = 0;
		this.sprite0Hit = 0;
		this.verticalBlankStart = 0;

		this.ppuOamAddress = 0;

		this.ppuOamData = 0;

		this.ppuScroll = 0;

		this.ppuAddress = 0;

		this.ppuData = 0;

		this.ppuOamDma = 0;
	}

	public int getPpuControl() {
		return generateNmi << 7 | ppuMasterSlaveSelect << 6 | spriteSize << 5 | backgroundPatternTableAddress << 4
				| spritePatternTableAddress << 3 | vramAddressIncrement << 2 | baseNametableAddress;
	}

	public void setPpuController(int ppuControl) {
		baseNametableAddress = ppuControl & 0b11;
		vramAddressIncrement = (ppuControl >> 2) & 0b1;
		spritePatternTableAddress = (ppuControl >> 3) & 0b1;
		backgroundPatternTableAddress = (ppuControl >> 4) & 0b1;
		spriteSize = (ppuControl >> 5) & 0b1;
		ppuMasterSlaveSelect = (ppuControl >> 6) & 0b1;
		generateNmi = (ppuControl >> 7) & 0b1;
	}

	public int getPpuMask() {
		return emphasizeBlue << 7 | emphasizeRed << 6 | emphasizeGreen << 5 | showSprites << 4 | showBackground << 3
				| showSpriteInLeftmost << 2 | showBackgroundInLeftmost << 1 | greyScale;
	}

	public void setPpuMask(int ppuMask) {
		greyScale = ppuMask & 0b1;
		showBackgroundInLeftmost = (ppuMask >> 1) & 0b1;
		showSpriteInLeftmost = (ppuMask >> 2) & 0b1;
		showBackground = (ppuMask >> 3) & 0b1;
		showSprites = (ppuMask >> 4) & 0b1;
		emphasizeGreen = (ppuMask >> 5) & 0b1;
		emphasizeRed = (ppuMask >> 6) & 0b1;
		emphasizeBlue = (ppuMask >> 7) & 0b1;
	}

	public int getPpuStatus() {
		return verticalBlankStart << 7 | sprite0Hit << 6 | spriteOverflow << 5;
	}

	public void setPpuStatus(int ppuStatus) {
		spriteOverflow = (ppuStatus >> 5) & 0b1;
		sprite0Hit = (ppuStatus >> 6) & 0b1;
		verticalBlankStart = (ppuStatus >> 7) & 0b1;
	}

	public void setPpuScroll(int scrollValue) {
		// If w is 0 then put in t and x
		if (w == 0) {
			// Set fine X scroll
			x = scrollValue & 0b111;

			// Reset the 5 bits and update
			t &= ~0b11111;
			t |= scrollValue >> 3;
		} else {
			// Reset five following bits and update
			t &= ~0b1111100000;
			t |= (scrollValue >> 3) << 5;

			// Reset three last and update
			t &= ~0x7000;
			t |= (scrollValue & 0b111) << 12;

			// Update PPU Scroll
			ppuScroll = t;
		}

		w = 1 - w;
	}

	public void setPpuAddress(int addressValue) {
		// If w is 0 then put in t
		if (w == 0) {
			// Reset last byte and update (two last bits ignored)
			t &= ~0xFF00;
			t |= (addressValue & 0b111111) << 8;
		} else {
			// Reset first byte and update
			t &= ~0b11111111;
			t |= addressValue;
			ppuAddress = v = t;
		}

		w = 1 - w;
	}

	public void incrementX() {
		// As long as x register < 7, just increment
		if (x != 7) {
			x += 1;
		} else {
			// Reset x register
			x = 0;
			
			// if coarse X == 31
			if ((v & 0x001F) == 31) {
				// Set coarse X to 0 and switch nametable (horizontal) (TODO just do -31 for it
				// to work faster?)
				v &= ~0x001F;
				v ^= 0x0400;
			} else {
				// Just increment coarse X
				v += 1;
			}
		}
	}

	public void incrementY() {
		// As long as fine Y < 7, just increment
		if ((v & 0x7000) != 0x7000) {
			// Fine y is << 12, so 0x1000
			v += 0x1000;
		} else {
			// Reset Y (TODO isn't subtract 0x7000 just better?)
			v &= ~0x7000;

			// Get coarse Y
			int coarseY = (v & 0x03E0) >> 5;

			// There are 29 vertical tiles, at 29 we reset
			if (coarseY == 29) {
				coarseY = 0;

				// Switch nametable (vertical)
				v ^= 0x0800;
			} else if (coarseY == 31) {
				// If coarse Y is 30 or 31, "negative" tile so just reset Y
				coarseY = 0;
			} else {
				// Just increment (coarse Y is << 5, so 0x20)
				coarseY += 0x20;
			}
		}
	}
}
