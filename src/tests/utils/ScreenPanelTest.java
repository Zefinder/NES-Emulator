package utils;

import frame.ScreenPanel;

public class ScreenPanelTest extends ScreenPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2652480170496620201L;
	
	private int[] pixelData;
	private int index = 0;
	
	public ScreenPanelTest(int length, int height) {
		super(length, height);
		pixelData = new int[length * height];
	}
	
	@Override
	public void setPixel(int pixelValue) {
		pixelData[index++] = pixelValue;
	}
	
	public int[] getPixelData() {
		return pixelData;
	}
	
	public void resetScreen() {
		pixelData = new int[pixelData.length];
	}

}
