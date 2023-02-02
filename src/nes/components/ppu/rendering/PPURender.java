package components.ppu.rendering;

import components.Bus;
import components.ppu.register.PPURegisters;
import exceptions.AddressException;
import listener.EventManager;

public class PPURender {

	private int counter, spriteCounter, secondaryCounter;
	private boolean finOAM, tile2;
	private OAM tmpOAM;
	private OAM[] secondary, nextSecondary;

	private int frameCounter;

	private boolean spriteHit;

	public PPURender() {
		finOAM = false;
		tile2 = false;
		secondary = new OAM[8];
		nextSecondary = new OAM[8];
		for (int i = 0; i < 8; i++) {
			secondary[i] = new OAM();
			nextSecondary[i] = new OAM();
		}
		tmpOAM = new OAM();
		frameCounter = -1;
	}

	public void render(int scanline, int cycle, PPURegisters register, Bus bus, boolean oddTick)
			throws AddressException {
		if (scanline == 310) {
			preRender(scanline, cycle, register, bus);
		} else if (scanline < 239) {
			if (register.getExternalRegisters().doShowBg() && register.getExternalRegisters().doShowSprites()) {
				visible(scanline, cycle, register, bus);
			}
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
		}

	}

	private void preRender(int scanline, int cycle, PPURegisters register, Bus bus) throws AddressException {
		if (cycle == 1) {
			EventManager.getInstance().stopNMI();
			EventManager.getInstance().stopSpriteOverflow();
			EventManager.getInstance().stopSprite0Hit();
			spriteHit = false;

		} else if (cycle >= 280 && cycle <= 304 && register.getExternalRegisters().doShowBg()
				&& register.getExternalRegisters().doShowSprites()) {
			int v = register.getBackgroundRegisters().getV();
			int t = register.getBackgroundRegisters().getT();
			register.getBackgroundRegisters().setV((v & ~0x7BE0) | (t & 0x7BE0));
		} else if (cycle >= 321 && cycle <= 336 && register.getExternalRegisters().doShowBg()
				&& register.getExternalRegisters().doShowSprites()) {
			fetchNextTiles(register, bus);
		}
	}

	private void visible(int scanline, int cycle, PPURegisters register, Bus bus) throws AddressException {
		if (cycle == 0) {
			for (int i = 0; i < 8; i++) {
				secondary[i] = register.getSpritesRegisters().getSecondaryOAM()[i].clone();
			}

		} else if (cycle <= 256) {
			renderPixel(scanline, cycle, register, bus);
			// Attention, il faut prendre la PROCHAINE TUILE
			fetchNextTile(register.getBackgroundRegisters().getTile2(), register, bus,
					register.getBackgroundRegisters().getV() + 1);
			register.augX();
			if ((cycle & 0b111) == 0) { // Chaque 8 cycles, on a fini de fetch une tuile !
				register.getBackgroundRegisters().setTile1(register.getBackgroundRegisters().getTile2());
				register.getBackgroundRegisters().renewTile2();
			}
		} else if (cycle <= 320) {
			fetchNextSpriteData(scanline, register, bus);
		} else if (cycle <= 336) {
			fetchNextTiles(register, bus);
		}
	}

	private void spriteEvaluation(int scanline, int cycle, PPURegisters register, Bus bus, boolean oddTick) {
		if (cycle == 0) {
			spriteCounter = 0;
			secondaryCounter = 0;
		} else if (cycle >= 1 && cycle <= 64) {
			if (((cycle - 1) & 0b111) == 0) {
				nextSecondary[(cycle - 1) / 8] = new OAM();
			}

		} else if (cycle <= 256) {
			if (oddTick && !finOAM) {
				tmpOAM = register.getSpritesRegisters().getPrimaryOAM()[spriteCounter];

			} else if (!oddTick && !finOAM) {
				int size = (register.getExternalRegisters().getSpriteSize() ? 1 : 0);
				int y = (tmpOAM.getByte0() < 0 ? tmpOAM.getByte0() + 256 : tmpOAM.getByte0());
				if (scanline + 1 >= y && scanline + 1 < y + 8 + 8 * size) {
					if (secondaryCounter < 8) {
						try {
							int byte1 = tmpOAM.getByte1();
							byte1 = (byte1 < 0 ? byte1 + 256 : byte1);
							tmpOAM.setPaternTableData(getPatternTable(
									register.getExternalRegisters().getSpritePatternTableAddr() | (byte1 << 4), bus));
						} catch (AddressException e) {
							e.printStackTrace();
						}
						nextSecondary[secondaryCounter] = tmpOAM.clone();
						secondaryCounter++;
					} else {
						EventManager.getInstance().fireSpriteOverflow();
						finOAM = true;
					}

					finOAM = (++spriteCounter) >= 64 | finOAM;
				}
			}

			if (cycle == 256) {
				register.getSpritesRegisters().setSecondaryOAM(nextSecondary.clone());
			}
		} else if (cycle <= 320) {
			if (cycle == 257) {
				secondaryCounter = -1;
			}

			int tmp = (cycle - 1) & 0b111;
			switch (tmp) {
			case 0:
				tmpOAM = nextSecondary[++secondaryCounter];
				EventManager.getInstance().fireChanging2004(tmpOAM.getByte0());
				break;

			case 1:
				EventManager.getInstance().fireChanging2004(tmpOAM.getByte1());
				break;

			case 2:
				EventManager.getInstance().fireChanging2004(tmpOAM.getByte2());
				break;

			default:
				EventManager.getInstance().fireChanging2004(tmpOAM.getByte3());
				break;
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

	private void fetchNextTile(Tile tile, PPURegisters register, Bus bus, int v) throws AddressException {
		switch (counter) {
		case 0:
//			System.out.println("Adresse de la tuile : " + (0x2000 | (v & 0x0FFF)));
			tile.setNametable(bus.getByteFromMemory(0x2000 | (v & 0x0FFF)));
			break;

		case 2:
			tile.setAttributeTable(
					bus.getByteFromMemory(0x23C0 | (v & 0x0C00) | ((v >> 4) & 0x0038) | ((v >> 2) & 0x0007)));
			break;

		case 4:
			int nametable = (tile.getNametable() < 0 ? tile.getNametable() + 256 : tile.getNametable());
			tile.setPatternTable(
					getPatternTable(register.getExternalRegisters().getBgPatternTableAddr() | nametable << 4, bus));
			break;

		default:
			break;
		}

		if (counter == 8)
			counter = 0;
	}

	private void fetchNextTiles(PPURegisters register, Bus bus) throws AddressException {
		// TODO Attention au fetch, vérifier qu'il fetch bien le bon truc !
		if (!tile2) {
			fetchNextTile(register.getBackgroundRegisters().getTile1(), register, bus,
					register.getBackgroundRegisters().getV());
			if (counter == 0)
				tile2 = true;
		} else {
			fetchNextTile(register.getBackgroundRegisters().getTile2(), register, bus,
					register.getBackgroundRegisters().getV() + 1);
			if (counter == 0)
				tile2 = false;
		}
	}

	private void fetchNextSpriteData(int scanline, PPURegisters register, Bus bus) throws AddressException {
		OAM sprite = register.getSpritesRegisters().getSecondaryOAM()[counter / 8];
		if ((counter & 0b111) == 4) {
			if (!register.getExternalRegisters().getSpriteSize())
				sprite.setPaternTableData(getPatternTable(sprite.getTileAddress8x8(register), bus));
			else {
				if (scanline > sprite.getByte0() + 8)
					sprite.setPaternTableData(getPatternTable(sprite.getTileAddress8x16(), bus));
				else
					sprite.setPaternTableData(getPatternTable(sprite.getTileAddress8x16() + 0x10, bus));
			}
		}

		if (++counter == 64)
			counter = 0;
	}

	private void renderPixel(int scanline, int cycle, PPURegisters register, Bus bus) throws AddressException {
		NesColors universalBackground = NesColors.getColorCode(bus.getByteFromMemory(0x3F00));

		NesColors bgPixel = (register.getExternalRegisters().doShowBg() ? fetchBackgroundPixel(register, bus)
				: NesColors.X0D);

		// Attention, on démarre à écrire cycle 1
		NesColors spritePixel = (register.getExternalRegisters().doShowSprites()
				? fetchSpritePixel(scanline, cycle - 1, register, bus)
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
		int V = register.getBackgroundRegisters().getV();
		int x = (V << 3 | register.getBackgroundRegisters().getX()) & 0b11111111;
		int y = (V & 0b1111100000) >> 2 | ((V >> 12) & 0b111);
		Tile tile = register.getBackgroundRegisters().getTile1();
		int column = x / 8;
		int row = y / 8;

		int offset = 0;
//		if (x % 4 >= 2)
//			offset += 2;
//		if (y % 4 >= 2)
//			offset += 4;

		offset = (2 * ((column & 0b11) >> 1) + 4 * ((row & 0b11) >> 1));
		int paletteNumber = (tile.getAttributeTable() >> offset) & 0b00000011;
		int paletteAddress = 0x3F00 + 4 * paletteNumber;
		int pattern = tile.getPatternTable()[(V >> 12) & 0b111][register.getBackgroundRegisters().getX()];

		if (pattern == 0) {
			return NesColors.getColorCode(bus.getByteFromMemory(0x3F00));
		} else
			return NesColors.getColorCode(bus.getByteFromMemory(paletteAddress + pattern));
	}

	private NesColors fetchSpritePixel(int y, int x, PPURegisters register, Bus bus) throws AddressException {

		for (OAM oam : secondary) {
			int byte3 = (oam.getByte3() < 0 ? oam.getByte3() + 256 : oam.getByte3());
			int byte0 = (oam.getByte0() < 0 ? oam.getByte0() + 256 : oam.getByte0()) + 1;
			if (x >= byte3 && x < byte3 + 8 && y >= byte0 && y < byte0 + 8) {
				int paletteNumber = oam.getByte2() & 0b00000011;
				int paletteAddress = 0x3F10 + 4 * paletteNumber;
				int pattern = oam.getPaternTableData()[y - byte0][x - byte3];

				if (pattern == 0) {
					return NesColors.getColorCode(bus.getByteFromMemory(0x3F00));
				} else
					return NesColors.getColorCode(bus.getByteFromMemory(paletteAddress + pattern));
			}
		}

		return NesColors.getColorCode(bus.getByteFromMemory(0x3F00));
	}

	private byte[][] getPatternTable(int patternTableLow, Bus bus) throws AddressException {
		byte[][] patternTable = new byte[8][8];
		for (int row = 0; row < 8; row++) {
			byte lowPlan = bus.getByteFromMemory(patternTableLow + row);
			byte highPlan = bus.getByteFromMemory(patternTableLow + row + 8);

			for (int column = 0; column < 8; column++) {
				patternTable[row][7 - column] = (byte) ((lowPlan & 1) + 2 * (highPlan & 1));
				lowPlan >>= 1;
				highPlan >>= 1;
			}
		}

		return patternTable;
	}

}
