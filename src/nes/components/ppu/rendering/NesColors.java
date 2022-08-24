package nes.components.ppu.rendering;

import java.awt.Color;

public enum NesColors {
	// Ligne 0
	X00(84, 84, 84), X01(0, 30, 116), X02(8, 16, 144), X03(48, 0, 136), X04(68, 0, 100), X05(92, 0, 48), X06(84, 4, 0),
	X07(60, 24, 0), X08(32, 42, 0), X09(8, 58, 0), X0A(0, 64, 0), X0B(0, 60, 0), X0C(0, 50, 60), X0D(0, 0, 0),
	X0E(0, 0, 0), X0F(0, 0, 0),

	// Ligne 1
	X10(152, 150, 152), X11(8, 76, 196), X12(48, 50, 236), X13(92, 30, 228), X14(136, 20, 176), X15(160, 20, 100),
	X16(152, 34, 32), X17(120, 60, 0), X18(84, 90, 0), X19(40, 114, 0), X1A(8, 124, 0), X1B(0, 118, 40),
	X1C(0, 102, 120), X1D(0, 0, 0), X1E(0, 0, 0), X1F(0, 0, 0),

	// Ligne 2
	X20(238, 236, 238), X21(76, 154, 236), X22(120, 124, 236), X23(176, 98, 236), X24(228, 84, 236), X25(236, 88, 180),
	X26(236, 106, 100), X27(212, 136, 32), X28(160, 170, 0), X29(116, 196, 0), X2A(76, 208, 32), X2B(56, 204, 108),
	X2C(56, 180, 204), X2D(60, 60, 60), X2E(0, 0, 0), X2F(0, 0, 0),

	// Ligne 3
	X30(236, 238, 236), X31(168, 204, 236), X32(188, 188, 236), X33(212, 178, 236), X34(236, 174, 236),
	X35(236, 174, 212), X36(236, 180, 176), X37(228, 196, 144), X38(204, 210, 120), X39(180, 222, 120),
	X3A(168, 226, 144), X3B(152, 226, 180), X3C(160, 214, 228), X3D(160, 162, 160), X3E(0, 0, 0), X3F(0, 0, 0);

	private int r, g, b;

	private NesColors(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public Color getColorFromCode() {
		return new Color(r, g, b);
	}
	
	public int getRGBFromCode() {
		return getColorFromCode().getRGB();
	}

	public static NesColors getColorCode(int code) {
		for (NesColors palette : NesColors.values()) {
			if (Integer.parseInt(palette.name().substring(1), 16) == code)
				return palette;
		}
		return X0D;
	}
}
