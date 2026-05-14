package components.bus;

import components.NmiFlipFlop;
import components.TranslatedAddress;
import components.register.PpuRegisters;

/**
 * Not implemented as a bus to save time since all registers are on one address.
 * Also the PPU can access the values directly to save function calls.
 */
public class MmioBus extends Bus {

	public static final int PPUCTRL_ADDR = 0x2000;
	public static final int PPUMASK_ADDR = 0x2001;
	public static final int PPUSTATUS_ADDR = 0x2002;
	public static final int OAMADDR_ADDR = 0x2003;
	public static final int OAMDATA_ADDR = 0x2004;
	public static final int PPUSCROLL_ADDR = 0x2005;
	public static final int PPUADDR_ADDR = 0x2006;
	public static final int PPUDATA_ADDR = 0x2007;

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
	private int ppuDataBuffer;
	public int ppuData;

	private PpuRegisters ppuRegisters;
	private PpuBus ppuBus;
	private NmiFlipFlop nmiFlipFlop;

	public MmioBus() {
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

		this.ppuDataBuffer = 0;
		this.ppuData = 0;
	}

	public void setPpuRegisters(PpuRegisters ppuRegisters) {
		this.ppuRegisters = ppuRegisters;
	}

	public void setPpuBus(PpuBus ppuBus) {
		this.ppuBus = ppuBus;
	}

	public void setNmiFlipFlop(NmiFlipFlop nmiFlipFlop) {
		this.nmiFlipFlop = nmiFlipFlop;
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

		setNmi();
		ppuRegisters.setPpuController(baseNametableAddress);
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
		int value = verticalBlankStart << 7 | sprite0Hit << 6 | spriteOverflow << 5;

		// Reset vertical blank and address latch used by PPU Scroll and PPU Address
		verticalBlankStart = 0;
		ppuRegisters.w = 0;

		openBus = (openBus & 0b00011111) | value;

		return value;
	}

	public void setPpuStatus(int ppuStatus) {
		spriteOverflow = (ppuStatus >> 5) & 0b1;
		sprite0Hit = (ppuStatus >> 6) & 0b1;
		verticalBlankStart = (ppuStatus >> 7) & 0b1;

		setNmi();
	}

	public void setPpuScroll(int scrollValue) {
		ppuRegisters.setPpuScroll(scrollValue);
		
		// PPU Scroll is only the last one set
		ppuScroll = openBus = scrollValue;
	}

	public void setPpuAddress(int addressValue) {
		ppuRegisters.setPpuAddress(addressValue);
		
		// PPU Address is only the last one set
		ppuAddress = openBus = addressValue;
	}

	public int getOamData() {
		// Don't forget to set openbus
		return (openBus = ppuOamData);
	}

	public int getPpuData() {
		// Get value from buffer
		ppuData = openBus = ppuDataBuffer;

		// Increment PPU address register using the VRAM address increment flag
		ppuRegisters.t = (ppuRegisters.t + 1 + 31 * vramAddressIncrement) & 0x3FFF;

		// Get value from bus to buffer
		ppuDataBuffer = ppuBus.read(ppuRegisters.t);

		return ppuData;
	}

	public void setOamAddress(int value) {
		// Don't forget openbus
		ppuOamAddress = openBus = value;

		// Eager OAM Data update for read
		ppuOamData = ppuRegisters.oamMemory[value];
	}

	public void setOamData(int value) {
		// Don't forget openbus
		ppuOamData = openBus = value;

		// Update OAM memory
		ppuRegisters.oamMemory[ppuOamAddress] = value;

		// Increment 0x2003 register
		ppuOamAddress = (ppuOamAddress + 1) & 0xFF;
	}

	public void setPpuData(int value) {
		// Set PPU Data here does not affect anything since there is a read buffer
		ppuData = openBus = value;

		// Write to PPU memory
		ppuBus.write(ppuRegisters.t, value);

		// Increment PPU Address
		ppuAddress = (ppuRegisters.t + 1 + 31 * vramAddressIncrement) & 0x3FFF;
	}

	@Override
	protected boolean checkImpl() {
		return ppuRegisters != null && ppuBus != null && nmiFlipFlop != null;
	}

	@Override
	public int read(int address) {
		return switch (address) {
		case 2 -> getPpuStatus();
		case 4 -> getOamData();
		case 7 -> getPpuData();
		default -> openBus;
		};
	}

	@Override
	public void write(int address, int value) {
		switch (address) {
		case 0 -> setPpuController(value);
		case 1 -> setPpuMask(value);
		case 3 -> setOamAddress(value);
		case 4 -> setOamData(value);
		case 5 -> setPpuScroll(value);
		case 6 -> setPpuAddress(value);
		case 7 -> setPpuData(value);
		default -> openBus = value; // Should never happen
		}
	}

	@Override
	protected TranslatedAddress translateAddress(int address) {
		throw new UnsupportedOperationException("Should never be used since read and write are overriden");
	}

	private void setNmi() {
		nmiFlipFlop.setNmiState(generateNmi == 1 && verticalBlankStart == 1);
	}

}
