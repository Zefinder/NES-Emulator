package components.ppu;

import frame.ScreenPanel;
import mapper.Mapper;

public class Ppu {

	public final PpuInfo ppuInfo = new PpuInfo();
	public final int[] oamMemory = new int[0x100];

	/* Frame variables */
	// Begins in an even frame BUT pre-render will inverse it at cycle 0
	private int oddFrame = 1;
	private int scanlineNumber = -1;
	private int cycleNumber = 0;

	/* Mapper */
	private Mapper mapper;

	/* Tiles */
	private Tile currentTile = new Tile();
	private Tile nextFirstTile = new Tile();
	private Tile nextSecondTile = new Tile();

	/* Sprites */
	// TODO Sprites...
	private int[] spritesCurrentScanline = new int[0x20];
	private int[] spritesNextScanline = new int[0x20];

	/* Screen linked to the PPU */
	private ScreenPanel screen;

	private static final Ppu ppu = new Ppu();

	private Ppu() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Sets the mapper for the CPU. Do not change while running
	 * 
	 * @param mapper the mapper to use
	 */
	public void setMapper(Mapper mapper) {
		this.mapper = mapper;
	}

	public void setScreen(ScreenPanel screen) {
		this.screen = screen;
	}

	public ScreenPanel getScreen() {
		return screen;
	}

	/**
	 * Fetches a value in memory
	 * 
	 * @param address the address to look for the value
	 * @return a value
	 */
	public int fetchMemory(int address) {
		return mapper.readPpuBus(address);
	}

	// TODO Look at the PPU scrolling for register copying...
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
	public int tick(long ticksToCatch) {
		// TODO Make the cycle logic

		int waitCycles = 0;
		boolean rendering = ppuInfo.showBackground + ppuInfo.showSprites != 0;
		for (long i = 0; i < ticksToCatch; i++) {
			if (scanlineNumber < 0) {
				waitCycles = tickPreRendering();
			} else if (scanlineNumber < 239) {
				// First draw pixels and then tick (to not skip a tile when x = 0)
				if (rendering && cycleNumber != 0 && cycleNumber <= 256) {
					// Draw pixel
					currentTile.drawPixel();

					// Update x
					if (ppuInfo.x == 7) {
						ppuInfo.x = 0;

						// Change tile!
						currentTile = nextFirstTile;
					} else {
						ppuInfo.x = (ppuInfo.x + 1) & 0b111;
					}
				}

				waitCycles = tickRendering();
			} else if (scanlineNumber == 239) {
				waitCycles = tickPostRendering();
			} else {
				waitCycles = tickVBlank();
			}

			// Rendering if background or sprites shown (or both)
			if (rendering) {
				// Last pixel rendered, we can increment Y
				if (cycleNumber == 256) {
					ppuInfo.incrementY();
				}
				// Copy value from t to v for horizontal position
				else if (cycleNumber == 257) {
					ppuInfo.v &= ~(0x041F);
					ppuInfo.v |= (ppuInfo.t & 0x41F);
				}

				if (scanlineNumber == -1) {
					// Copy value from t to v for vertical position
					if (cycleNumber >= 280 && cycleNumber <= 304) {
						ppuInfo.v &= ~(0x7BE0);
						ppuInfo.v |= (ppuInfo.t & 0x7BE0);
					}
				}

				// Increment X if rendering enabled
				if (cycleNumber != 0 && (cycleNumber <= 256 || cycleNumber >= 328) && (cycleNumber & 0x7) == 0) {
					ppuInfo.incrementCoarseX();
				}
			}

			// Cycles from 0 to 340
			if (++cycleNumber > 340) {
				cycleNumber = 0;
				if (++scanlineNumber == 310) {
					scanlineNumber = -1;
				}
			}
		}

		return waitCycles;
	}

	private int tickPreRendering() {
		if (cycleNumber == 0) {
			oddFrame = 1 - oddFrame;
		} else if (cycleNumber == 1) {
			ppu.ppuInfo.setPpuStatus(0);
		} else if (cycleNumber > 320 && cycleNumber < 337) {
			int cyclePart = (cycleNumber & 0b111);
			// Here we put for the two next tiles
			// Tile selection
			Tile tileSelected = (cycleNumber & 0x8) == 0 ? nextFirstTile : nextSecondTile;

			// Nametable byte fetch
			if (cyclePart == 1) {
				// Fine y has been incremented at dot 256
				tileSelected.setNametableAddress(ppuInfo.v & 0x0FFF, (ppuInfo.v >> 12) & 0b111);
			}
			// Attribute table byte fetch
			else if (cyclePart == 3) {
				tileSelected.setAttributeAddress(
						(ppuInfo.v & 0x0C00) | ((ppuInfo.v >> 4) & 0x38) | ((ppuInfo.v >> 2) & 0x07),
						ppuInfo.v & 0b11111, (ppuInfo.v >> 5) & 0b11111);
			}
			// Pattern table tile low fetch
			else if (cyclePart == 5) {
				tileSelected.fetchLowPatternTable();
			}
			// Pattern table tile high fetch
			else if (cyclePart == 7) {
				tileSelected.fetchHighPatternTable();
			}
		}

		// Odd and even frame
		if (cycleNumber == 339 && oddFrame == 1) {
			cycleNumber++;
		}

		return 0;
	}

	private int tickRendering() {
		int waitCycles = 0;
		int cyclePart = (cycleNumber & 0b111);
		if (cycleNumber == 0) {
			// Nothing :D
			waitCycles = 1;

			// Load next tiles for rendering
			currentTile = nextFirstTile;
			nextFirstTile = nextSecondTile;
			nextSecondTile = new Tile();
		} else if (cycleNumber < 257) {
			// Motion in 5 parts
			waitCycles = 2;

			// Time to switch tiles
			if (cyclePart == 0) {
				nextFirstTile = nextSecondTile;
				nextSecondTile = new Tile();
				waitCycles = 1;
			}
			// Nametable byte fetch
			else if (cyclePart == 1) {
				nextSecondTile.setNametableAddress(ppuInfo.v & 0x0FFF, ppuInfo.v >> 12);
			}
			// Attribute table byte fetch
			else if (cyclePart == 3) {
				nextSecondTile.setAttributeAddress(
						(ppuInfo.v & 0x0C00) | ((ppuInfo.v >> 4) & 0x38) | ((ppuInfo.v >> 2) & 0x07),
						ppuInfo.v & 0b11111, (ppuInfo.v >> 5) & 0b11111);
			}
			// Pattern table tile low fetch
			else if (cyclePart == 5) {
				nextSecondTile.fetchLowPatternTable();
			}
			// Pattern table tile high fetch
			else if (cyclePart == 7) {
				nextSecondTile.fetchHighPatternTable();
				waitCycles = 1;
			}

		} else if (cycleNumber < 321) {
			// The first two cycles are ignored, nothing happens in there
			// TODO X and attributes for sprite fetch
			if (cyclePart == 3) {

			}
			// TODO Pattern table tile low fetch
			else if (cyclePart == 5) {

			}
			// TODO Pattern table tile high fetch
			else if (cyclePart == 7) {

			}

			waitCycles = 2;
		} else if (cycleNumber < 337) {
			// Here we put for the two next tiles
			waitCycles = 2;

			// Tile selection
			Tile tileSelected = (cycleNumber & 0x8) == 0 ? nextFirstTile : nextSecondTile;

			// Nametable byte fetch
			if (cyclePart == 1) {
				// Fine y has been incremented at dot 256
				tileSelected.setNametableAddress(ppuInfo.v & 0x0FFF, ppuInfo.v >> 12);
			}
			// Attribute table byte fetch
			else if (cyclePart == 3) {
				tileSelected.setAttributeAddress(
						(ppuInfo.v & 0x0C00) | ((ppuInfo.v >> 4) & 0x38) | ((ppuInfo.v >> 2) & 0x07),
						ppuInfo.v & 0b11111, (ppuInfo.v >> 5) & 0b11111);
			}
			// Pattern table tile low fetch
			else if (cyclePart == 5) {
				tileSelected.fetchLowPatternTable();
			}
			// Pattern table tile high fetch
			else if (cyclePart == 7) {
				tileSelected.fetchHighPatternTable();
			}

		} else {
			// Fetching nametable twice of third tile but unknown purpose...
			// So ignoring for now
			waitCycles = 4;
		}

		return waitCycles;
	}

	private int tickPostRendering() {
		// Set scanline to 240 for VBlank
//		scanlineNumber = 240;

		// We are waiting for 341 cycles + the first VBlank cycle
		// No need to set cycleNumber since it's not used in VBlank
		return 342;
	}

	private int tickVBlank() {
		// Raise NMI if set in PPU Controller (TODO Actually if generateNMI and NMI set,
		// interrupt... Not only in that case... )
		if (cycleNumber == 1 && scanlineNumber == 240) {
			ppuInfo.verticalBlankStart = 1;
		}

		// This happens on the second cycle, so we wait for the remaining 340 cycles and
		// the 69 other scanlines
		return 340 + 69 * 341;
	}

	public static Ppu getInstance() {
		return ppu;
	}

}
