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
//		screen.setRGB(10, 10, Color.CYAN.getRGB());
//		screen.setRGB(11, 10, Color.CYAN.getRGB());
//		screen.setRGB(12, 10, Color.CYAN.getRGB());
//		screen.setRGB(10, 11, Color.CYAN.getRGB());
//		screen.setRGB(11, 11, Color.CYAN.getRGB());
//		screen.setRGB(12, 11, Color.CYAN.getRGB());
//		screen.setRGB(10, 12, Color.CYAN.getRGB());
//		screen.setRGB(11, 12, Color.CYAN.getRGB());
//		screen.setRGB(12, 12, Color.CYAN.getRGB());

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

		// TODO Launch thread to draw
	}

	private static class ScreenDrawer implements Runnable {

		private final int width;
		private final int[] pixelBuffer;
		private BufferedImage screen;

		public ScreenDrawer(int width, int[] pixelBuffer, BufferedImage screen) {
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
				// TODO Transform NES pixel into color
				
				screen.setRGB(x, y, nesPixel);
			}
		}

	}

}
