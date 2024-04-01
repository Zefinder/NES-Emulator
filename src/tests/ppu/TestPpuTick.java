package ppu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import components.ppu.Ppu;
import mapper.Mapper0;
import utils.ScreenPanelTest;

@TestMethodOrder(OrderAnnotation.class)
public class TestPpuTick {

	private static final Ppu ppu = Ppu.getInstance();
	private static ScreenPanelTest screen;

	private int cycleNumber = 0;

	@BeforeAll
	static void init() {
		screen = new ScreenPanelTest(256, 240);

		// Set some tiles
		byte[] chrMem = new byte[0x2000];
		// First tile is 01230123 for each
		for (int i = 0; i < 0x8; i++) {
			chrMem[i] = 0b01010101;
			chrMem[i + 8] = 0b00010001;
		}

		ppu.setMapper(new Mapper0(new byte[0x4000], chrMem));
		ppu.setScreen(screen);
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
		// VBlank set cycle 1 (even if no rendering) and then... Nothing for 70 scanlines
		ppu.ppuInfo.setPpuStatus(0);
		ppu.tick(2);
		
		assertEquals(0b10000000, ppu.ppuInfo.getPpuStatus());
		
		ppu.tick(339 + 69*341);
		assertEquals(0, ppu.ppuInfo.v, "Nothing happens during VBlank"); 
	}
	
	private void assertBlankScreen() {
		for (int data: screen.getPixelData()) {
			assertEquals(0, data, "The screen must be blank!");
		}
	}

}
