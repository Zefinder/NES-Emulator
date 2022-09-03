package nes.components.ppu.rendering;

import nes.components.ppu.register.PPURegisters;

public class OAM implements Cloneable {

	private byte byte0, byte1, byte2, byte3;
	private byte[][] patternTable;

	public OAM() {
		byte0 = (byte) 0xFF;
		byte1 = (byte) 0xFF;
		byte2 = (byte) 0xFF;
		byte3 = (byte) 0xFF;
		patternTable = new byte[8][8];
	}

	public OAM(byte byte0, byte byte1, byte byte2, byte byte3) {
		this.byte0 = byte0;
		this.byte1 = byte1;
		this.byte2 = byte2;
		this.byte3 = byte3;
		patternTable = new byte[8][8];
	}
	
	// Byte 0
	public byte getByte0() {
		return byte0;
	}

	public void setByte0(byte byte0) {
		this.byte0 = byte0;
	}

	// Byte 1
	public byte getByte1() {
		return byte1;
	}

	public void setByte1(byte byte1) {
		this.byte1 = byte1;
	}

	public int getTileAddress8x8(PPURegisters registres) {
		int tmp = (byte1 < 0 ? byte1 + 256 : byte1);
		return registres.getExternalRegisters().getSpritePatternTableAddr() + tmp;
	}

	public int getTileAddress8x16() {
		int tmp = (byte1 < 0 ? (byte1 & 0b11111110) + 256 : (byte1 & 0b11111110)) << 4;

		return tmp + (byte1 & 0x0000001) << 12;
	}

	// Byte 2
	public byte getByte2() {
		return byte2;
	}

	public void setByte2(byte byte2) {
		this.byte2 = byte2;
	}

	public int getPalette() {
		return 4 + byte2 & 0b00000011;
	}

	public void setPalette(int palette) {
		byte2 &= 0b11111100;
		byte2 |= palette;
	}

	public int getPriority() {
		return (byte2 & 0b00100000) >> 5;
	}

	public void setPriority(int priority) {
		priority %= 2;
		priority = priority << 5;
		byte2 &= 0b11011111;
		byte2 |= priority;
	}

	public boolean getFlip(boolean vertical) {
		if (vertical) {
			return (byte2 & 0b10000000) == 0b10000000;
		} else {
			return (byte2 & 0b01000000) == 0b01000000;
		}
	}

	public void setFlip(boolean vertical) {
		if (vertical) {
			byte2 |= 0b10000000;
		} else {
			byte2 |= 0b01000000;
		}
	}

	public void setUnflip(boolean vertical) {
		if (vertical) {
			byte2 &= 0b01111111;
		} else {
			byte2 &= 0b10111111;
		}
	}

	// Byte 3
	public byte getByte3() {
		return byte3;
	}

	public void setByte3(byte byte3) {
		this.byte3 = byte3;
	}

	public byte[][] getPaternTableData() {
		return patternTable;
	}

	public void setPaternTableData(byte[][] patternTable) {
		this.patternTable = patternTable;
	}
	
	@Override
	protected OAM clone() {
		OAM oam = new OAM();
		oam.setByte0(this.getByte0());
		oam.setByte1(this.getByte1());
		oam.setByte2(this.getByte2());
		oam.setByte3(this.getByte3());
		oam.setPaternTableData(this.getPaternTableData());
		
		return oam;
	}
	
	@Override
	public String toString() {
		return String.format("[%02X, %02X, %02X, %02X]", byte0, byte1, byte2, byte3);
	}

}
