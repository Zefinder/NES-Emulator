package components;

public class PpuInfo {

	/* Registers */
	public int v;
	public int t;
	public int x;
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

	// Both of them: upper byte first
	public void setPpuScroll(int scrollValue) {
		// If w is 0 then put in t
		if (w == 0) {
			t = scrollValue;
		} else {
			// Else update PPU scroll register
			ppuScroll = t << 8 | scrollValue;
		}
		
		w = 1 - w;
	}

	public void setPpuAddress(int addressValue) {
		// If w is 0 then put in t
		if (w == 0) {
			t = addressValue;
		} else {
			// Else update PPU scroll register
			ppuAddress = t << 8 | addressValue;
		}
		
		w = 1 - w;
	}
}
