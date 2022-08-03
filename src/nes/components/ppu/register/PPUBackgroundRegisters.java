package nes.components.ppu.register;

import nes.components.ppu.rendering.Tile;

public class PPUBackgroundRegisters {

	private int v, t;
	private byte x, w;
	
	private Tile tile1, tile2;

	public PPUBackgroundRegisters() {
		this.v = 0;
		this.t = 0;
		this.x = (byte) 0;
		this.w = (byte) 0;
		
		tile1 = new Tile();
		tile2 = new Tile();
	}

	public int getV() {
		return v;
	}

	public void setV(int v) {
		this.v = v;
	}

	public int getT() {
		return t;
	}

	public void setT(int t) {
		this.t = t;
	}

	public byte getX() {
		return x;
	}

	public void setX(byte x) {
		this.x = x;
	}

	public byte getW() {
		return w;
	}

	public void setW(byte w) {
		this.w = w;
	}

	public Tile getTile1() {
		return tile1;
	}

	public void setTile1(Tile tile1) {
		this.tile1 = tile1;
	}

	public Tile getTile2() {
		return tile2;
	}

	public void setTile2(Tile tile2) {
		this.tile2 = tile2;
	}

}
