package nes.listener;

import nes.components.ppu.rendering.NesColors;

public interface PPURenderListener {
	void onPixelRendered(NesColors pixel);
}
