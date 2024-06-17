package frame;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ScreenPanel extends JPanel {

	private static final long serialVersionUID = -801936700422382058L;

	private final int length;
	private final int bufferSize;
	private int bufferIndex;
	private int[] pixelData;

	private BufferedImage screen;

	public ScreenPanel(int length, int height) {
		this.length = length;
		bufferSize = length * height;
		bufferIndex = 0;
		pixelData = new int[bufferSize];

		screen = new BufferedImage(length, height, BufferedImage.TYPE_INT_RGB);

		this.add(new JLabel(new ImageIcon(screen)));
	}

	/**
	 * Sets a pixel in the buffer, the pixel value is the NES color and not the RGB
	 * one. The transformation is done when the screen is updated
	 * 
	 * @param pixelValue
	 */
	public void setPixel(int pixelValue) {
		pixelData[bufferIndex] = pixelValue;
		// If we are the end of the buffer we draw the frame
		if (++bufferIndex == bufferSize) {
			drawScreen();
			bufferIndex = 0;
		}
	}

	// The frame is drawn asynchronously to not have too many cycles to catch up for
	// the PPU
	private void drawScreen() {
		int[] screenToDraw = pixelData;
		pixelData = new int[bufferSize];

		// Launch thread to draw
		Thread screenDrawerThread = new Thread(new ScreenDrawer(this, length, screenToDraw, screen));
		screenDrawerThread.setName("Screen Drawer");
		screenDrawerThread.start();
	}

	private static class ScreenDrawer implements Runnable {

		private final JPanel container;
		private final int width;
		private final int[] pixelBuffer;
		private BufferedImage screen;

		public ScreenDrawer(JPanel container, int width, int[] pixelBuffer, BufferedImage screen) {
			this.container = container;
			this.width = width;
			this.pixelBuffer = pixelBuffer;
			this.screen = screen;
		}

		@Override
		public void run() {
			int x = 0;
			int y = 0;

			for (int pixelIndex = 0; pixelIndex < pixelBuffer.length; pixelIndex++) {
				int nesPixel = pixelBuffer[pixelIndex];
				screen.setRGB(x, y, ColorPalette.values()[nesPixel].getColorValue());
				if (++x == width) {
					x = 0;
					y++;
				}
			}
			
			container.repaint();
		}
	}
}
