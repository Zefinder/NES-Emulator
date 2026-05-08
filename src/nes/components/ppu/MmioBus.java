package components.ppu;

import components.Memory;

/**
 * Not implemented as a bus to save time since all registers are on one address.
 * Also the PPU can access the values directly to save function calls.
 */
public class MmioBus implements Memory {

	// PPU Control (0x2000)
	int baseNametableAddress;
	int vramAddressIncrement;
	int spritePatternTableAddress;
	int backgroundPatternTableAddress;
	int spriteSize;
	int ppuMasterSlaveSelect;
	int generateNmi;

	// PPU Mask (0x2001)
	int greyScale;
	int showBackgroundInLeftmost;
	int showSpriteInLeftmost;
	int showBackground;
	int showSprites;
	int emphasizeGreen;
	int emphasizeRed;
	int emphasizeBlue;

	// PPU Status (0x2002)
	int spriteOverflow;
	int sprite0Hit;
	int verticalBlankStart;

	// PPU OAM Address (0x2003)
	int ppuOamAddress;

	// PPU OAM DATA (0x2004)
	int ppuOamData;

	// PPU Scroll (0x2005)
	int ppuScroll;

	// PPU Address (0x2006)
	int ppuAddress;

	// PPU Data (0x2007)
	int ppuData;

	// PPU OAM DMA (0x4014)
	int ppuOamDma;

	private final PpuInfo ppuInfo;

	public MmioBus(PpuInfo ppuInfo) {
		this.ppuInfo = ppuInfo; // Holds a reference for

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

	public int getPpuController() {
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

		ppuInfo.setPpuController(baseNametableAddress);
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
		ppuInfo.setPpuScroll(scrollValue);
		if (ppuInfo.w == 0) { // w has been reset after calling setPpuScroll
			// Update PPU Scroll
			ppuScroll = ppuInfo.t;
		}
	}

	public void setPpuAddress(int addressValue) {
		ppuInfo.setPpuAddress(addressValue);
		if (ppuInfo.w == 0) {
			ppuAddress = ppuInfo.t; // w has been reset after calling setPpuAddress
		}
	}

	@Override
	public int read(int address) {
		switch (address) {
		case 0:
		case 1:
		case 3:
		case 5:
		case 6:
			// TODO Openbus
			break;

		case 2:
			return getPpuStatus();

		case 4:
			return ppuOamData;

		case 7:
			return ppuData;

		default:
			// TODO Raise error
			break;
		}

		return 0;
	}

	// TODO Verify all writes (especially ppu data)
	@Override
	public void write(int address, int value) {
		switch (address) {
		case 0:
			setPpuController(value);
			break;

		case 1:
			setPpuMask(value);
			break;

		case 2:
			// TODO Openbus
			break;

		case 3:
			ppuOamAddress = value;
			break;

		case 4:
			ppuOamData = value;
			break;
		case 5:
			setPpuScroll(value);
			break;

		case 6:
			setPpuAddress(address);
			break;
			
		case 7:
			ppuData = value;

		default:
			// TODO Raise error
			break;
		}
	}

}
