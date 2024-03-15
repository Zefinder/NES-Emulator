package components;

public class Ppu {

	public final PpuInfo ppuInfo = new PpuInfo();
	public final int[] oamMemory = new int[0x100];

	private int oddFrame = 0;
	private int scanlineNumber = -1;
	private int cycleNumber = 0;

	// Tiles here

	// Sprites
	private int[] spritesCurrentScanline = new int[0x20];
	private int[] spritesNextScanline = new int[0x20];

	private static final Ppu ppu = new Ppu();

	private Ppu() {
		// TODO Auto-generated constructor stub
	}

	public static Ppu getInstance() {
		return ppu;
	}

	/**
	 * <p>
	 * Exactly like the CPU's tick method, this method does not really represents 1
	 * tick per call. PPU rendering depend on the scanline number. There are 4
	 * cases:
	 * 
	 * <ul>
	 * <li>Scanline -1 - Pre-render
	 * <li>Scanline 0 to 238 - Rendering
	 * <li>Scanline 239 - Post-render
	 * <li>Scanline 240 to 309 - VBlank
	 * </ul>
	 * </p>
	 * 
	 * <h2>Pre-rendering</h2>
	 * <p>
	 * This scanline is like a rendering scanline, it will fetch as if it was a
	 * visible scanline, but it isn't. Nothing is displayed. Its purpose is to fetch
	 * the two first tiles for the first scanline (the number 0)
	 * </p>
	 * 
	 * <p>
	 * From cycle 280 to 304, vertical scroll bits are reloaded if rendering is
	 * enabled
	 * </p>
	 * 
	 * <p>
	 * This scanline uses the odd frame latch. If the frame is odd, then the last
	 * cycle is skipped.
	 * </p>
	 * 
	 * <h2>Rendering</h2>
	 * <p>
	 * Rendering is divided in 5 parts, depending on the cycle number:
	 * 
	 * <ul>
	 * <li>Cycle 0 - Nothing!
	 * <li>Cycles 1 to 256 - Fetching data for each tile (8 cycles per tile so 32
	 * tiles)
	 * <li>Cycles 257 to 320 - Fetching data for next scanline sprites
	 * <li>Cycles 321 to 336 - Fetching data for next scanline two first tiles
	 * <li>Cycles 337 to 340 - Fetching two times nametable but unknown purpose
	 * </ul>
	 * 
	 * Note that each fetching takes 2 PPU cycles
	 * </p>
	 * 
	 * <p>
	 * There are at most 8 sprites per scanline. Also, sprite 0 hit is only raised
	 * at cycle 2, and first pixel is rendered at cycle 4 (because of rendering
	 * pipeline).
	 * </p>
	 * 
	 * <h2>Post-rendering</h2>
	 * <p>
	 * The PPU does nothing here... Really nothing, so waiting for 341 cycles
	 * </p>
	 * 
	 * <h2>VBlank</h2>
	 * <p>
	 * In PAL systems, there are 70 vblank scanline (against 20 for NTSC). At cycle
	 * 1, the NMI is thrown. The rest is idling.
	 * </p>
	 * 
	 * <p>
	 * It can be a great idea to not tick for nothing. As each fetching is 2 PPU
	 * cycles, we can make some optimization doing the 2 cycles at a time. As the
	 * first VBlank tick is idling, we can wait for 342 (341+1) cycles after the
	 * last rendering scanline to set the NMI flag, and then wait for 340 + 69*341
	 * ticks until the pre-rendering scanline.
	 * </p>
	 * 
	 */
	public int tick() {
		int waitCycles;
		if (scanlineNumber < 0) {
			waitCycles = tickPreRendering();
		} else if (scanlineNumber < 239) {
			waitCycles = tickRendering();
		} else if (scanlineNumber == 240) {
			waitCycles = tickPostRendering();
		} else {
			waitCycles = tickVBlank();
		}

		return waitCycles;
	}

	private int tickPreRendering() {
		return 0;
	}

	private int tickRendering() {
		int waitCycles = 0;
		if (cycleNumber == 0) {
			// Nothing :D
			waitCycles = 1;
		} else if (cycleNumber < 257) {
			// Motion in 4 parts
			// TODO Nametable byte fetch
			if ((cycleNumber & 0b111) == 1) {

			}
			// TODO Attribute table byte fetch
			else if ((cycleNumber & 0b111) == 3) {

			}
			// TODO Pattern table tile low fetch
			else if ((cycleNumber & 0b111) == 5) {

			}
			// TODO Pattern table tile high fetch
			else {

			}

			waitCycles = 2;
		} else if (cycleNumber < 321) {
			// The first two cycles are ignored, nothing happens in there
			// TODO X and attributes for sprite fetch
			if ((cycleNumber & 0b111) == 3) {

			}
			// TODO Pattern table tile low fetch
			else if ((cycleNumber & 0b111) == 5) {

			}
			// TODO Pattern table tile high fetch
			else {

			}

			waitCycles = 2;
		} else if (cycleNumber < 337) {
			// Maybe make a function because it does it twice...
			// Motion in 4 parts
			// TODO Nametable byte fetch
			if ((cycleNumber & 0b111) == 1) {

			}
			// TODO Attribute table byte fetch
			else if ((cycleNumber & 0b111) == 3) {

			}
			// TODO Pattern table tile low fetch
			else if ((cycleNumber & 0b111) == 5) {

			}
			// TODO Pattern table tile high fetch
			else {

			}
			
			waitCycles = 2;
		} else {
			// Fetching nametable twice of third tile but unknown purpose...
			// So ignoring for now
			waitCycles = 4;
		}

		return waitCycles;
	}

	private int tickPostRendering() {
		// Set scanline to 240 for VBlank
		scanlineNumber = 240;

		// We are waiting for 341 cycles + the first VBlank cycle
		// No need to set cycleNumber since it's not used in VBlank
		return 342;
	}

	private int tickVBlank() {
		// TODO Raise NMI if set in PPU Controller

		// Set cycle to 0 for pre-rendering
		cycleNumber = 0;

		// Set scanline to -1 (pre-rendering)
		scanlineNumber = -1;

		// Set odd or even frame
		oddFrame = 1 - oddFrame;

		// This happens on the second cycle, so we wait for the remaining 340 cycles and
		// the 69 other scanlines
		return 340 + 69 * 341;
	}

}
