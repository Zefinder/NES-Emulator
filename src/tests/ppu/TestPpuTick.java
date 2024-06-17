package ppu;

import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import components.cpu.Cpu;
import components.ppu.Ppu;
import mapper.Mapper;
import mapper.Mapper0;
import utils.ScreenPanelTest;

@TestMethodOrder(OrderAnnotation.class)
public class TestPpuTick {

	private static final Ppu ppu = Ppu.getInstance();
	private static ScreenPanelTest screen;

	private int[] tile0Palette0 = { 0x03, 0x04, 0x05, 0x06, 0x03, 0x04, 0x05, 0x06 };
	private int[] tile1Palette0 = { 0x03, 0x03, 0x04, 0x04, 0x05, 0x05, 0x06, 0x06 };
	private int[] tile2Palette0 = { 0x03, 0x04, 0x06, 0x05, 0x05, 0x06, 0x04, 0x03 };
	private int[] tile3Palette0 = { 0x06, 0x05, 0x04, 0x03, 0x03, 0x04, 0x05, 0x06 };
	private int[] tile0Palette1 = { 0x03, 0x17, 0x18, 0x19, 0x03, 0x17, 0x18, 0x19 };
	private int[] tile0Palette2 = { 0x03, 0x1D, 0x2D, 0x3D, 0x03, 0x1D, 0x2D, 0x3D };
	private int[] tile1Palette1 = { 0x03, 0x03, 0x17, 0x17, 0x18, 0x18, 0x19, 0x19 };
	private int[] tile2Palette2 = { 0x03, 0x1D, 0x3D, 0x2D, 0x2D, 0x3D, 0x1D, 0x03 };
	private int[] tile3Palette2 = { 0x3D, 0x2D, 0x1D, 0x03, 0x03, 0x1D, 0x2D, 0x3D };

	@BeforeAll
	static void init() {
		screen = new ScreenPanelTest(256, 240);

		// Set some tiles
		byte[] chrMem = new byte[0x2000];
		// First tile is 01230123 for each line
		for (int i = 0; i < 0x8; i++) {
			chrMem[i] = 0b01010101;
			chrMem[i + 8] = 0b00110011;
		}
		// Second tile is 00112233 for each line
		for (int i = 0x10; i < 0x18; i++) {
			chrMem[i] = 0b00110011;
			chrMem[i + 8] = 0b00001111;
		}
		// Third tile is 01322310
		for (int i = 0x20; i < 0x28; i++) {
			chrMem[i] = 0b01100110;
			chrMem[i + 8] = 0b00111100;
		}
		// Fourth tile is 32100123
		for (int i = 0x30; i < 0x38; i++) {
			chrMem[i] = (byte) 0b10100101;
			chrMem[i + 8] = (byte) 0b11000011;
		}

		Mapper mapper0 = new Mapper0(new byte[0x4000], chrMem);

		ppu.setMapper(mapper0);
		ppu.setScreen(screen);

		Cpu cpu = Cpu.getInstance();
		cpu.setMapper(mapper0);

		// Set tile in nametable (0x2000)
		cpu.storeMemory(0x2006, 0x20);
		cpu.storeMemory(0x2006, 0x00);

		// First 4 tiles : 03, 01, 02, 03
		// If coarseX begins at 1, first tile is 01 !
		cpu.storeMemory(0x2007, 0x03);
		cpu.storeMemory(0x2007, 0x01);
		cpu.storeMemory(0x2007, 0x02);
		cpu.storeMemory(0x2007, 0x03);

		cpu.storeMemory(0x2007, 0x00);
		cpu.storeMemory(0x2007, 0x01);
		cpu.storeMemory(0x2007, 0x02);
		cpu.storeMemory(0x2007, 0x03);

		// Set attribute table
		cpu.storeMemory(0x2006, 0x23);
		cpu.storeMemory(0x2006, 0xC0);

		// Attribute table stores in 4x4
		// Use palette 1 for first 2 tiles
		// Use palette 2 for 2 next
		// Use palette 0 for the rest
		cpu.storeMemory(0x2007, (2 << 2) | (1 << 0));
		cpu.storeMemory(0x2007, 0);

		// Palette colors
		cpu.storeMemory(0x2006, 0x3F);
		cpu.storeMemory(0x2006, 0x00);

		// Palette 0
		cpu.storeMemory(0x2007, 0x03);
		cpu.storeMemory(0x2007, 0x04);
		cpu.storeMemory(0x2007, 0x05);
		cpu.storeMemory(0x2007, 0x06);

		// Palette 1
		cpu.storeMemory(0x2007, 0x03);
		cpu.storeMemory(0x2007, 0x17);
		cpu.storeMemory(0x2007, 0x18);
		cpu.storeMemory(0x2007, 0x19);

		// Palette 2
		cpu.storeMemory(0x2007, 0x03);
		cpu.storeMemory(0x2007, 0x1D);
		cpu.storeMemory(0x2007, 0x2D);
		cpu.storeMemory(0x2007, 0x3D);
	}

	// We know that the PPU starts at scanline -1 (pre-render), cycle 0

	@Test
	@Order(1)
	void testPreRenderScanlineNoRender() {
		// Cycles 280 to 304: copy vertical position of t to v IF RENDERING
		for (int cycleNumber = 0; cycleNumber <= 340; cycleNumber++) {
			ppu.ppuInfo.t = 0x7FFF;
			ppu.ppuInfo.v = 0;
			ppu.ppuInfo.setPpuStatus(0b11100000);

			ppu.tick(1);

			// Cycle 1 resets PPU Status
			if (cycleNumber == 1) {
				assertEquals(0, ppu.ppuInfo.getPpuStatus());
			}

			// Cycle 256 increment v if rendering enabled (no)
			if (cycleNumber == 256) {
				assertEquals(0, ppu.ppuInfo.v, "V should not be incremented since rendering disabled (Y)");
			}

			// Cycle 257 copies horizontal t to v if rendering enabled (no)
			if (cycleNumber == 257) {
				assertEquals(0, ppu.ppuInfo.v, "V should not be updated from t since rendering disabled (horizontal)");
			}

			// Cycles 280 to 304 copies vertical t to v if rendering enabled (no)
			if (cycleNumber >= 280 && cycleNumber <= 304) {
				assertEquals(0, ppu.ppuInfo.v, "V should not be updated from t since rendering disabled (vertical)");
			}

			// Every 8 cycles from 8 to 256 plus 328 and 336, increment coarse X from v v if
			// rendering enabled (no)
			if ((cycleNumber & 0b111) == 0 && ((cycleNumber >= 8 && cycleNumber <= 256) || cycleNumber >= 328)) {
				assertEquals(0, ppu.ppuInfo.v, "V should not be incremented since rendering disabled (coarse X)");
			}

			// Cycles 321 to 336 is fetching, but not possible to verify it... (render
			// disabled)
		}
	}

	@Test
	@Order(2)
	void testRenderScanlineNoRender() {
		// No rendering so just verify that the screen is blank at the end of the
		// scanlines
		ppu.ppuInfo.v = 0;

		// 239 lines
		for (int scanline = 0; scanline <= 238; scanline++) {
			for (int cycleNumber = 0; cycleNumber <= 340; cycleNumber++) {
				// Well nothing interesting here, just checking register and screen at the end
				ppu.tick(1);

				// Cycle 256 increment v if rendering enabled (no)
				if (cycleNumber == 256) {
					assertEquals(0, ppu.ppuInfo.v, "V should not be incremented since rendering disabled (Y)");
				}

				// Cycle 257 copies horizontal t to v if rendering enabled (no)
				if (cycleNumber == 257) {
					ppu.ppuInfo.t = 0x7FFF;
					assertEquals(0, ppu.ppuInfo.v,
							"V should not be updated from t since rendering disabled (horizontal)");
				}

				// Every 8 cycles from 8 to 256 plus 328 and 336, increment coarse X from v v if
				// rendering enabled (no)
				if ((cycleNumber & 0b111) == 0 && ((cycleNumber >= 8 && cycleNumber <= 256) || cycleNumber >= 328)) {
					assertEquals(0, ppu.ppuInfo.v, "V should not be incremented since rendering disabled (coarse X)");
				}
			}
		}

		// Verify that screen has not been drawn
		assertBlankScreen();
	}

	@Test
	@Order(3)
	void testPostRenderScanlineNoRender() {
		// Just nothing here, like really nothing
		ppu.ppuInfo.v = 0;
		ppu.ppuInfo.t = 0;

		ppu.tick(341);

		assertEquals(0, ppu.ppuInfo.v, "Nothing happens during post rendering");
	}

	@Test
	@Order(4)
	void testVBlankScanlineNoRender() {
		// VBlank set cycle 1 (even if no rendering) and then... Nothing for 70
		// scanlines
		ppu.ppuInfo.setPpuStatus(0);
		ppu.tick(2);

		assertEquals(0b10000000, ppu.ppuInfo.getPpuStatus());

		ppu.tick(339 + 69 * 341);
		assertEquals(0, ppu.ppuInfo.v, "Nothing happens during VBlank");
	}

	@Test
	@Order(5)
	void testPreRenderScanlineRender() {
		int coarseX = 1;
		int fineY = 4;
		int coarseY = 0;
		int nametable = 0;
		ppu.ppuInfo.v = (fineY << 12) | (nametable << 10) | (coarseY << 5) | coarseX;
		ppu.ppuInfo.t = (fineY << 12) | (nametable << 10) | (coarseY << 5) | coarseX;

		// Enable rendering (background is enough)
		ppu.ppuInfo.showBackground = 1;

		// This is an odd frame, so one tick less
		for (int cycleNumber = 0; cycleNumber <= 339; cycleNumber++) {
			ppu.ppuInfo.setPpuStatus(0b11100000);
			ppu.tick(1);

			// Cycle 1 resets PPU Status
			if (cycleNumber == 1) {
				assertEquals(0, ppu.ppuInfo.getPpuStatus());
			}

			// Cycle 256 increment vertical v if rendering enabled (yes)
			else if (cycleNumber == 256) {
				if (++fineY == 8) {
					fineY = 0;
					if (++coarseY == 30) {
						coarseY = 0;
						nametable ^= 0b10;
					}
				}

				assertEquals(fineY, (ppu.ppuInfo.v >> 12) & 0b111, "V should be incremented (fineY)");
				assertEquals(coarseY, (ppu.ppuInfo.v >> 5) & 0b11111, "V should be incremented (coarseY)");
				assertEquals(nametable, (ppu.ppuInfo.v >> 10) & 0b11, "Nametable should be updated");
			}

			// Cycle 257 copies horizontal t to v if rendering enabled (yes)
			else if (cycleNumber == 257) {
				coarseX = ppu.ppuInfo.t & 0b11111;
				nametable &= ~0b1;
				nametable |= (ppu.ppuInfo.t >> 10) & 0b1;

				assertEquals((fineY << 12) | (nametable << 10) | (coarseY << 5) | coarseX, ppu.ppuInfo.v,
						"V should be updated from t (horizontal)");
			}

			// Cycles 280 to 304 copies vertical t to v if rendering enabled (yes)
			else if (cycleNumber >= 280 && cycleNumber <= 304) {
				coarseY = (ppu.ppuInfo.t >> 5) & 0b11111;
				fineY = (ppu.ppuInfo.t >> 12) & 0b111;
				nametable &= ~0b10;
				nametable |= (ppu.ppuInfo.t >> 10) & 0b10;
				assertEquals((fineY << 12) | (nametable << 10) | (coarseY << 5) | coarseX, ppu.ppuInfo.v,
						"V should be updated from t (vertical)");
			}

			// Every 8 cycles from 8 to 256 plus 328 and 336, increment coarse X from v if
			// rendering enabled (yes)
			if ((cycleNumber & 0b111) == 0 && ((cycleNumber >= 8 && cycleNumber <= 256) || cycleNumber >= 328)) {
				if (++coarseX == 32) {
					coarseX = 0;
					nametable ^= 0b01;
				}

				assertEquals((fineY << 12) | (nametable << 10) | (coarseY << 5) | coarseX, ppu.ppuInfo.v,
						"V should be incremented (coarse X) (cycle %d)".formatted(cycleNumber));
			}

			// Verify tiles fetched when rendering
		}
	}

	@Test
	@Order(6)
	void testRenderScanlineRender() {
		int coarseX = 1 + 2; // + 2 since we fetched the first 2 tiles
		int fineY = 4;
		int coarseY = 0;
		int nametable = 0;

		// We assume that all latter tests are correct
		assumeTrue("V does not have the same value than the precedent scanline, abort...",
				ppu.ppuInfo.v == ((fineY << 12) | (nametable << 10) | (coarseY << 5) | coarseX));
		assumeTrue("T does not have the same value than the precedent scanline, abort...",
				ppu.ppuInfo.t == ((fineY << 12) | (nametable << 10) | (coarseY << 5) | coarseX - 2));

		for (int scanline = 0; scanline <= 238; scanline++) {
			for (int cycleNumber = 0; cycleNumber <= 340; cycleNumber++) {
				ppu.tick(1);

				// Cycle 256 increment vertical v if rendering enabled (yes)
				if (cycleNumber == 256) {
					if (++fineY == 8) {
						fineY = 0;
						if (++coarseY == 30) {
							coarseY = 0;
							nametable ^= 0b10;
						}
					}

					assertEquals(fineY, (ppu.ppuInfo.v >> 12) & 0b111, "V should be incremented (fineY)");
					assertEquals(coarseY, (ppu.ppuInfo.v >> 5) & 0b11111,
							"V should be incremented (coarseY) (scanline %d)".formatted(scanline));
					assertEquals(nametable, (ppu.ppuInfo.v >> 10) & 0b11, "Nametable should be updated");
				}

				// Cycle 257 copies horizontal t to v if rendering enabled (yes)
				else if (cycleNumber == 257) {
					coarseX = ppu.ppuInfo.t & 0b11111;
					nametable &= ~0b1;
					nametable |= (ppu.ppuInfo.t >> 10) & 0b1;

					assertEquals((fineY << 12) | (nametable << 10) | (coarseY << 5) | coarseX, ppu.ppuInfo.v,
							"V should be updated from t (horizontal)");
				}

				// Every 8 cycles from 8 to 256 plus 328 and 336, increment coarse X from v if
				// rendering enabled (yes)
				if ((cycleNumber & 0b111) == 0 && ((cycleNumber >= 8 && cycleNumber <= 256) || cycleNumber >= 328)) {
					if (++coarseX == 32) {
						coarseX = 0;
						nametable ^= 0b01;
					}

					assertEquals((fineY << 12) | (nametable << 10) | (coarseY << 5) | coarseX, ppu.ppuInfo.v,
							"V should be incremented (coarse X) (cycle %d)".formatted(cycleNumber));
				}
			}
		}

		// Verify that screen has been drawn
		assertScreenSet();
	}

	private void assertBlankScreen() {
		for (int data : screen.getPixelData()) {
			assertEquals(0, data, "The screen must be blank!");
		}
	}

	private void assertScreenSet() {
		// The fineY is at 4, so 4 lines of the first tiles pattern
		int[] pixelData = screen.getPixelData();
		int coarseX = 1;
		int fineY = 4;
		int screenWidth = 256;
		int tileWidth = 8;
		int tileHeight = 8;
		int tilesPerLines = screenWidth / tileWidth;
		for (int pixelIndex = 0; pixelIndex < pixelData.length; pixelIndex++) {
			int tileX = (pixelIndex % screenWidth) / tileWidth;
			int tileY = ((pixelIndex / screenWidth) + fineY) / tileHeight;
			int address = 0x2000 + coarseX + tileX + tilesPerLines * tileY;

			// We ignore the wrapping
			if (address >= 0x23C0) {
				continue;
			}

			int[] pixelsPattern = tile0Palette0;
			if (tileX == 0 && tileY == 0) {
				pixelsPattern = tile1Palette1;
			} else if (tileX == 1 && tileY == 0) {
				pixelsPattern = tile2Palette2;
			} else if (tileX == 2 && tileY == 0) {
				pixelsPattern = tile3Palette2;
			} else if (tileX == 4 && tileY == 0) {
				pixelsPattern = tile1Palette0;
			} else if (tileX == 5 && tileY == 0) {
				pixelsPattern = tile2Palette0;
			} else if (tileX == 6 && tileY == 0) {
				pixelsPattern = tile3Palette0;
			} else if (tileX == 0 && tileY == 1) {
				pixelsPattern = tile0Palette1;
			} else if ((tileX == 1 || tileX == 2) && tileY == 1) {
				pixelsPattern = tile0Palette2;
			}

			int pixelInTile = pixelsPattern[pixelIndex % tileWidth];
			assertEquals(pixelInTile, pixelData[pixelIndex],
					"pixel=%d, tileX=%d, tileY=%d".formatted(pixelIndex, tileX, tileY));
		}
	}
}
