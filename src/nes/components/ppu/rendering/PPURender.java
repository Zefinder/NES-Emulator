package nes.components.ppu.rendering;

import nes.components.Bus;
import nes.components.ppu.register.PPURegisters;
import nes.exceptions.AddressException;
import nes.listener.EventManager;

public class PPURender {

	private int counter, spriteCounter, secondaryCounter;
	private boolean finOAM;
	private OAM tmpOAM;
	private OAM[] secondary;

	private int frameCounter;

	private boolean spriteHit;

	public PPURender() {
		finOAM = false;
		secondary = new OAM[8];
		tmpOAM = new OAM();
		frameCounter = -1;
	}

	public void render(int scanline, int cycle, PPURegisters register, Bus bus, boolean oddTick)
			throws AddressException {
		if (scanline == 310) {
			preRender(scanline, cycle, register, bus);
		} else if (scanline < 239) {
			visible(scanline, cycle, register, bus);
			spriteEvaluation(scanline, cycle, register, bus, oddTick);
		} else if (scanline > 239) { // Rien à la scanline 239
			vblank(scanline, cycle, register);
		}

		if (register.getExternalRegisters().doShowBg() && register.getExternalRegisters().doShowSprites()) {
			if (cycle == 256) {
				register.augY();
			}

			if (cycle == 257) {
				int v = register.getBackgroundRegisters().getV();
				int t = register.getBackgroundRegisters().getT();
				register.getBackgroundRegisters().setV((v & ~0x041F) | (t & 0x41F));
			}

			if ((cycle <= 256 && cycle % 8 == 0) || cycle == 328 || cycle == 336) {
				register.augX();
			}
		}

	}

	private void preRender(int scanline, int cycle, PPURegisters register, Bus bus) throws AddressException {
		if (cycle == 1) {
			EventManager.getInstance().stopNMI();
			EventManager.getInstance().stopSpriteOverflow();
			EventManager.getInstance().stopSprite0Hit();

		} else if (cycle >= 280 && cycle <= 304 && register.getExternalRegisters().doShowBg()
				&& register.getExternalRegisters().doShowSprites()) {
			int v = register.getBackgroundRegisters().getV();
			int t = register.getBackgroundRegisters().getT();
			register.getBackgroundRegisters().setV((v & ~0x7BE0) | (t & 0x7BE0));
		} else if (cycle >= 321 && cycle <= 336) {
			fetchNextTiles(register, bus);
		}
	}

	private void visible(int scanline, int cycle, PPURegisters register, Bus bus) throws AddressException {
		if (cycle == 0) {
			for (int i = 0; i < 8; i++) {
				secondary[i] = register.getSpritesRegisters().getSecondaryOAM()[i].clone();
			}

		} else if (cycle <= 256) {
			renderPixel(cycle, cycle, register, bus);
			fetchNextTile(register.getBackgroundRegisters().getTile2(), register, bus);

			if (cycle % 8 == 0) { // Chaque 8 cycles, on a fini de fetch une tuile !
				register.getBackgroundRegisters().setTile1(register.getBackgroundRegisters().getTile2());
			}
		} else if (cycle <= 320) {
			fetchNextSpriteData(scanline, register, bus);
		} else if (cycle <= 336) {
			fetchNextTiles(register, bus);
		}
	}

	private void spriteEvaluation(int scanline, int cycle, PPURegisters register, Bus bus, boolean oddTick) {
		if (cycle >= 1 && cycle <= 64) {
			if ((cycle - 1) % 8 == 0) {
				register.getSpritesRegisters().getSecondaryOAM()[(cycle - 1) / 8] = new OAM();
			}

		} else if (cycle <= 256) {
			if (oddTick && !finOAM) {
				tmpOAM = register.getSpritesRegisters().getPrimaryOAM()[spriteCounter];

			} else if (!oddTick && !finOAM) {
				int size = (register.getExternalRegisters().getSpriteSize() ? 1 : 0);
				if (scanline + 1 >= tmpOAM.getByte0() && scanline + 1 <= tmpOAM.getByte0() + 8 + 8 * size) {
					if (secondaryCounter < 8) {
						secondary[secondaryCounter] = tmpOAM.clone();

					} else {
						EventManager.getInstance().fireSpriteOverflow();
						finOAM = true;
					}

					finOAM = (++spriteCounter) >= 64 | finOAM;
				}
			}

		} else if (cycle <= 320) {
			if (cycle == 257) {
				secondaryCounter = -1;
			}

			if ((cycle - 1) % 8 == 0) {
				tmpOAM = secondary[++secondaryCounter];
				EventManager.getInstance().fireChanging2004(tmpOAM.getByte0());
			}

			else if ((cycle - 1) % 8 == 1) {
				EventManager.getInstance().fireChanging2004(tmpOAM.getByte1());
			}

			else if ((cycle - 1) % 8 == 2) {
				EventManager.getInstance().fireChanging2004(tmpOAM.getByte2());

			} else {
				EventManager.getInstance().fireChanging2004(tmpOAM.getByte3());
			}
		}
	}

	private void vblank(int scanline, int cycle, PPURegisters register) {
		if (cycle == 1 && scanline == 240) {
			finOAM = false;
			secondaryCounter = 0;
			spriteCounter = 0;
			if (++frameCounter == 50)
				frameCounter = 0;

			EventManager.getInstance().fireNMI();
		}
	}

	private void fetchNextTile(Tile tile, PPURegisters register, Bus bus) throws AddressException {
		int v = register.getBackgroundRegisters().getV();
		switch (counter) {
		case 0:
			tile.setNametable(bus.getByteFromMemory(0x2000 | (v & 0x0FFF)));
			break;

		case 2:
			tile.setAttributeTable(
					bus.getByteFromMemory(0x23C0 | (v & 0x0C00) | ((v >> 4) & 0x0038) | ((v >> 2) & 0x0007)));
			break;

		case 4:
			tile.setPatternTable(getPatternTable(
					register.getExternalRegisters().getBgPatternTableAddr() | tile.getNametable() << 4, bus));
			break;

		default:
			break;
		}
		counter = (++counter) % 8;
	}

	private void fetchNextTiles(PPURegisters register, Bus bus) throws AddressException {
		if (counter < 8)
			fetchNextTile(register.getBackgroundRegisters().getTile1(), register, bus);
		else
			fetchNextTile(register.getBackgroundRegisters().getTile2(), register, bus);

		counter = (++counter) % 16;

	}

	private void fetchNextSpriteData(int scanline, PPURegisters register, Bus bus) throws AddressException {
		OAM sprite = register.getSpritesRegisters().getSecondaryOAM()[counter / 8];
		if (counter % 8 == 4) {
			if (!register.getExternalRegisters().getSpriteSize())
				sprite.setPaternTableData(getPatternTable(sprite.getTileAddress8x8(register), bus));
			else {
				if (scanline > sprite.getByte0() + 8)
					sprite.setPaternTableData(getPatternTable(sprite.getTileAddress8x16(), bus));
				else
					sprite.setPaternTableData(getPatternTable(sprite.getTileAddress8x16() + 0x10, bus));
			}
		}

		counter = (++counter) % 64;
	}

	private void renderPixel(int scanline, int cycle, PPURegisters register, Bus bus) throws AddressException {
		NesColors universalBackground = NesColors.getColorCode(bus.getByteFromMemory(0x3F00));

		NesColors bgPixel = (register.getExternalRegisters().doShowBg() ? fetchBackgroundPixel(register, bus)
				: NesColors.X0D);

		NesColors spritePixel = (register.getExternalRegisters().doShowSprites()
				? fetchSpritePixel(scanline, cycle, register, bus)
				: NesColors.X0D);

		if (bgPixel != universalBackground) {
			if (spritePixel != universalBackground) {
				if (!spriteHit && cycle != 256 && register.getExternalRegisters().doShowBg()
						&& register.getExternalRegisters().doShowSprites()) {
					spriteHit = !spriteHit;
					EventManager.getInstance().fireSprite0Hit();
				}

				if (tmpOAM.getPriority() == 0) { // 0 = foreground
					EventManager.getInstance().firePixelChanged(spritePixel);
				} else {
					EventManager.getInstance().firePixelChanged(bgPixel);
				}
			} else {
				EventManager.getInstance().firePixelChanged(bgPixel);
			}
		} else {
			if (spritePixel != universalBackground) {
				EventManager.getInstance().firePixelChanged(spritePixel);
			} else {
				EventManager.getInstance().firePixelChanged(universalBackground);
			}
		}

	}

	private NesColors fetchBackgroundPixel(PPURegisters register, Bus bus) throws AddressException {
		int x = register.getBackgroundRegisters().getV() << 3 | register.getBackgroundRegisters().getX();
		int y = (register.getBackgroundRegisters().getV() & 0b1111100000) >> 5
				| register.getBackgroundRegisters().getV() >> 12;
		Tile tile = register.getBackgroundRegisters().getTile1();

		int paletteNumber = tile.getAttributeTable() >> (2 * ((x / 16) % 2) + 4 * ((y / 16) % 2)) & 0b00000011;
		int paletteAddress = 0x3F01 + 4 * paletteNumber;
		int pattern = tile.getPatternTable()[register.getBackgroundRegisters().getV() >> 12][register
				.getBackgroundRegisters().getX()];

		if (pattern == 0) {
			return NesColors.getColorCode(bus.getByteFromMemory(0x3F00));
		} else
			return NesColors.getColorCode(bus.getByteFromMemory(paletteAddress + pattern));
	}

	private NesColors fetchSpritePixel(int scanline, int cycle, PPURegisters register, Bus bus)
			throws AddressException {
		int x = cycle;
		int y = scanline;

		for (OAM oam : secondary) {
			if (x >= oam.getByte3() && x < oam.getByte3() + 8) {
				tmpOAM = oam;
				int paletteNumber = oam.getByte2() & 0b00000011;
				int paletteAddress = 0x3F11 + 4 * paletteNumber;
				int pattern = oam.getPaternTableData()[y - oam.getByte0()][x - oam.getByte3()];

				if (pattern == 0) {
					return NesColors.getColorCode(bus.getByteFromMemory(0x3F00));
				} else
					return NesColors.getColorCode(bus.getByteFromMemory(paletteAddress + pattern));
			}
		}

		return NesColors.getColorCode(bus.getByteFromMemory(0x3F00));
	}

	private byte[][] getPatternTable(int patternTableLow, Bus bus) throws AddressException {
		// TODO vérifier que ça marche bien !
		byte[][] patternTable = new byte[8][8];

		for (int row = 0; row < 8; row++) {
			byte lowPlan = bus.getByteFromMemory(patternTableLow + row);
			byte highPlan = bus.getByteFromMemory(patternTableLow + row + 8);

			for (int column = 0; column < 8; column++) {
				patternTable[row][column] = (byte) ((lowPlan & 1) + 2 * (highPlan & 1));
				lowPlan >>= 1;
				highPlan >>= 1;
			}
		}

		return patternTable;
	}

}
