package components.register;

import components.Component;

public class PpuRegisters extends Component {

	/* Registers */
	/**
	 * Scroll position when rendering, VRAM address when not rendering v has the
	 * following form during rendering:
	 * 
	 * <pre>
	 * yyy NN YYYYY XXXXX
	 * </pre>
	 * 
	 * with
	 * <ul>
	 * <li><em>y</em> the fine Y scroll
	 * <li><em>N</em> the nametable select (0 to 3)
	 * <li><em>Y</em> the coarse Y scroll
	 * <li><em>X</em> the coarse X scroll
	 * </ul>
	 */
	public int v;
	/**
	 * Starting coarse-x scroll for the next scanline and the starting y scroll for
	 * the screen during rendering, temporary value when not rendering
	 */
	public int t;
	/** Fine-x position of the current scroll */
	public int x;
	/** Write latch for PPU Scroll and PPU Address */
	public int w;

	// PPU Pattern table address (TODO Understand...)
	public int fineYOffset;
	public int bitPlane;
	public int tileColumn;
	public int tileRow;
	public int patternTableHalf;
	
	// Oam memory is an internal memory, put with registers for now
	public int oamMemory[];

	public PpuRegisters() {
		this.v = 0;
		this.t = 0;
		this.x = 0;
		this.w = 0;
		
		this.oamMemory = new int[0x100];
	}

	public void setPpuController(int baseNametableAddress) {
		// Ppu controller base nametable address is in t
		// Reset these 2 bits
		t &= ~0b110000000000;

		// Update t
		t |= baseNametableAddress << 10;
	}
	
	public void setPpuScroll(int scrollValue) {
		// TODO Verify PPU Scroll (set 0 and 0 gives 0x0400...)
		// If w is 0 then put in t and x
		if (w == 0) {
			// Set fine X scroll
			x = scrollValue & 0b111;

			// Reset the 5 bits and update
			t &= ~0b11111;
			t |= scrollValue >> 3;
		} else {
			// Reset five following bits and update
			t &= ~0b1111100000;
			t |= (scrollValue >> 3) << 5;

			// Reset three last and update
			t &= ~0x7000;
			t |= (scrollValue & 0b111) << 12;
		}

		w = 1 - w;
	}

	public void setPpuAddress(int addressValue) {
		// If w is 0 then put in t
		if (w == 0) {
			// Reset last byte and update (two last bits ignored)
			t &= ~0x3F00;
			t |= (addressValue & 0b111111) << 8;
		} else {
			// Reset first byte and update
			t &= ~0b11111111;
			t |= addressValue;
			v = t;
		}

		w = 1 - w;
	}

	public void incrementCoarseX() {
		// if coarse X == 31
		if ((v & 0x001F) == 31) {
			// Set coarse X to 0 and switch nametable (horizontal) (TODO just do -31
			// to work faster?)
			v &= ~0x001F;
			v ^= 0x0400;
		} else {
			// Just increment coarse X
			v += 1;
		}
	}

	public void incrementY() {
		// As long as fine Y < 7, just increment
		if ((v & 0x7000) != 0x7000) {
			// Fine y is << 12, so 0x1000
			v += 0x1000;
		} else {
			// Reset Y (TODO isn't subtract 0x7000 just better?)
			v &= ~0x7000;

			// Get coarse Y
			int coarseY = (v & 0x03E0) >> 5;

			// There are 30 vertical tiles, at 29 we reset
			if (coarseY == 29) {
				coarseY = 0;

				// Switch nametable (vertical)
				v ^= 0x0800;
			} else if (coarseY == 31) {
				// If coarse Y is 30 or 31, "negative" tile so just reset Y
				coarseY = 0;
			} else {
				// Just increment (coarse Y is << 5, so 0x20)
				coarseY += 1;
			}
			
			// Put coarseY in v
			v &= ~0b1111100000;
			v |= coarseY << 5;
		}
	}
	
	public int getCurrentY() {
		return (v >> 5) & 0b11111;
	}
	
	@Override
	protected boolean checkImpl() {
		// Not link to another component
		return true;
	}
}
