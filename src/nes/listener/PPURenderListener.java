package listener;

import components.ppu.rendering.NesColors;

public interface PPURenderListener {
	void onPixelRendered(NesColors pixel);
}
