package components.ppu.register;

public class PPUExternalRegisters {
	
	public enum Color {
		RED, GREEN, BLUE;
	}

	// Registres externes
	private byte PPUCTRL, PPUMASK, PPUSTATUS, OAMADDR, OAMDATA, PPUSCROLL, PPUADDR, PPUDATA, OAMDMA;

	public PPUExternalRegisters() {
		PPUCTRL = (byte) 0;
		PPUMASK = (byte) 0;
		PPUSTATUS = (byte) 0;
		OAMADDR = (byte) 0;
		OAMDATA = (byte) 0;
		PPUSCROLL = (byte) 0;
		PPUADDR = (byte) 0;
		PPUDATA = (byte) 0;
		OAMDMA = (byte) 0;
	}

	// PPUCTRL ($2000)
	public byte getPPUCTRL() {
		return PPUCTRL;
	}

	public void setPPUCTRL(byte PPUCTRL) {
		this.PPUCTRL = PPUCTRL;
	}

	public void enableNMI() {
		PPUCTRL |= 0b10000000;
	}

	public void disableNMI() {
		PPUCTRL &= 0b01111111;
	}

	public boolean hasNMI() {
		return (PPUCTRL & 0b10000000) == 0b10000000;
	}

	public void setMasterSlave() {
		PPUCTRL |= 0b01000000;
	}

	public void clearMasterSlave() {
		PPUCTRL &= 0b10111111;
	}

	public boolean hasMasterSlave() {
		return (PPUCTRL & 0b01000000) == 0b01000000;
	}

	public boolean getSpriteSize() {
		return (PPUCTRL & 0b00100000) == 0b00100000;
	}

	public void setSpriteSize(boolean large) {
		if (large)
			PPUCTRL |= 0b00100000;
		else
			PPUCTRL &= 0b11011111;
	}

	public int getBgPatternTableAddr() {
		if ((PPUCTRL & 0b00010000) == 0b00010000)
			return 0x1000;

		return 0x0000;
	}

	public void setBgPatternTableAddr(boolean right) {
		if (right)
			PPUCTRL |= 0b00010000;
		else
			PPUCTRL &= 0b11101111;
	}

	public int getSpritePatternTableAddr() {
		if ((PPUCTRL & 0b00001000) == 0b00001000)
			return 0x1000;

		return 0x0000;
	}

	public void setSpritePatternTableAddr(boolean right) {
		if (right)
			PPUCTRL |= 0b00001000;
		else
			PPUCTRL &= 0b11110111;
	}

	public int getVRAMIncrement() {
		if ((PPUCTRL & 0b00000100) == 0b00000100)
			return 32;

		return 1;
	}

	public void setVRAMIncrement(boolean further) {
		if (further)
			PPUCTRL |= 0b00000100;
		else
			PPUCTRL &= 0b11111011;
	}

	public int getNametableAddress() {
		byte addressNumber = (byte) (PPUCTRL | 0b00000011);
		return 0x2000 + 0x0400 * addressNumber;
	}

	public void setNametableAddress(byte addressNumber) {
		byte tmp = (byte) (addressNumber & 0b11);
		PPUCTRL |= tmp;
	}

	// PPUMASK ($2001)
	public byte getPPUMASK() {
		return PPUMASK;
	}

	public void setPPUMASK(byte PPUMASK) {
		this.PPUMASK = PPUMASK;
	}

	public boolean hasEmphasizedColor(Color color) {
		boolean res = false;
		switch (color) {
		case BLUE:
			if ((PPUMASK & 0b10000000) == 0b10000000)
				res = true;
			break;

		case RED:
			if ((PPUMASK & 0b01000000) == 0b01000000)
				res = true;
			break;

		case GREEN:
			if ((PPUMASK & 0b00100000) == 0b00100000)
				res = true;
			break;
		}

		return res;
	}

	public void setEmphasizedColor(Color color, boolean emphasized) {
		switch (color) {
		case BLUE:
			if (emphasized)
				PPUMASK |= 0b10000000;
			else
				PPUMASK &= 0b01111111;
			break;

		case RED:
			if (emphasized)
				PPUMASK |= 0b01000000;
			else
				PPUMASK &= 0b10111111;
			break;

		case GREEN:
			if (emphasized)
				PPUMASK |= 0b00100000;
			else
				PPUMASK &= 0b11011111;
			break;
		}
	}

	public boolean doShowSprites() {
		return (PPUMASK & 0b00010000) == 0b00010000;
	}

	public void showSprites() {
		PPUMASK |= 0b00010000;
	}

	public void hideSprites() {
		PPUMASK &= 0b11101111;
	}

	public boolean doShowBg() {
		return (PPUMASK & 0b00001000) == 0b00001000;
	}

	public void showBg() {
		PPUMASK |= 0b00001000;
	}

	public void hideBg() {
		PPUMASK &= 0b11110111;
	}

	public boolean doShowSpritesLeft() {
		return (PPUMASK & 0b00000100) == 0b00000100;
	}

	public void showSpritesLeft() {
		PPUMASK |= 0b00000100;
	}

	public void hideSpritesLeft() {
		PPUMASK &= 0b11111011;
	}

	public boolean doShowBgLeft() {
		return (PPUMASK & 0b00000010) == 0b00000010;
	}

	public void showBgLeft() {
		PPUMASK |= 0b00000010;
	}

	public void hideBgLeft() {
		PPUMASK &= 0b11111101;
	}

	public boolean getGreyscale() {
		return (PPUMASK & 0b00000001) == 0b00000001;
	}

	public void setGreyscale() {
		PPUMASK |= 0b00000001;
	}

	public void clearGreyscale() {
		PPUMASK &= 0b11111110;
	}

	// PPUSTATUS ($2002)
	public byte getPPUSTATUS() {
		return PPUSTATUS;
	}

	public void setPPUSTATUS(byte PPUSTATUS) {
		this.PPUSTATUS = PPUSTATUS;
	}

	public boolean isInNMI() {
		return (PPUSTATUS & 0b10000000) == 0b10000000;
	}

	public void setNMI() {
		PPUSTATUS |= 0b10000000;
	}

	public void clearNMI() {
		PPUSTATUS &= 0b01111111;
	}

	public boolean isSprite0Hit() {
		return (PPUSTATUS & 0b01000000) == 0b01000000;
	}

	public void setSprite0Hit() {
		PPUSTATUS |= 0b01000000;
	}

	public void clearSprite0Hit() {
		PPUSTATUS &= 0b10111111;
	}

	public boolean isSpriteOverflow() {
		return (PPUSTATUS & 0b00100000) == 0b00100000;
	}

	public void setSpriteOverflow() {
		PPUSTATUS |= 0b00100000;
	}

	public void clearSpriteOverflow() {
		PPUSTATUS &= 0b11011111;
	}

	// OAMADDR ($2003)
	public byte getOAMADDR() {
		return OAMADDR;
	}

	public void setOAMADDR(byte OAMADDR) {
		this.OAMADDR = OAMADDR;
	}

	// OAMDATA ($2004)
	public byte getOAMDATA() {
		return OAMDATA;
	}

	public void setOAMDATA(byte OAMDATA) {
		this.OAMDATA = OAMDATA;
	}

	// PPUSCROLL ($2005)
	public byte getPPUSCROLL() {
		return PPUSCROLL;
	}

	public void setPPUSCROLL(byte PPUSCROLL) {
		this.PPUSCROLL = PPUSCROLL;
	}

	// PPUADDR ($2006)
	public byte getPPUADDR() {
		return PPUADDR;
	}

	public void setPPUADDR(byte PPUADDR) {
		this.PPUADDR = PPUADDR;
	}

	// PPUDATA ($2007)
	public byte getPPUDATA() {
		return PPUDATA;
	}

	public void setPPUDATA(byte PPUDATA) {
		this.PPUDATA = PPUDATA;
	}

	// OAMDMA ($4014)
	public byte getOAMDMA() {
		return OAMDMA;
	}

	public void setOAMDMA(byte OAMDMA) {
		this.OAMDMA = OAMDMA;
	}

}
