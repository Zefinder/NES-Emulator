package nes.components.ppu.rendering;

public class Tile {

	private byte nametable, attributeTable;
	private byte[][] patternTable;
	
	public Tile() {
		patternTable = new byte[8][8];
	}

	public void setNametable(byte nametable) {
		this.nametable = nametable;
	}

	public void setAttributeTable(byte attributeTable) {
		this.attributeTable = attributeTable;
	}

	public void setPatternTable(byte[][] patternTable) {
		this.patternTable = patternTable;
	}

	public byte getNametable() {
		return nametable;
	}

	public byte getAttributeTable() {
		return attributeTable;
	}

	public byte[][] getPatternTable() {
		return patternTable;
	}
}
