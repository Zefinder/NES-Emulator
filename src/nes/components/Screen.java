package nes.components;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;

import nes.components.ppu.rendering.NesColors;
import nes.listener.EventManager;
import nes.listener.PPURenderListener;

public class Screen extends JFrame implements PPURenderListener, Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5485315281389127140L;
	private static final int SCREENX_SIZE = 32, SCREENY_SIZE = 30;
	private static final int PIXEL_SIZE = 3;

	private BlockingQueue<NesColors> pixelQueue;

	private BufferedImage screen;
	private JPanel screenPanel;
	private int x, y;

	public Screen(String name) {
		EventManager.getInstance().addRenderListener(this);

		this.setTitle(name);
		this.setSize(820, 795);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		pixelQueue = new LinkedBlockingQueue<>();
		x = 0;
		y = 0;
		screenPanel = buildScreen();

		this.add(screenPanel);
		this.setVisible(false);
	}

	private JPanel buildScreen() {
		screen = new BufferedImage(SCREENX_SIZE * 8 * 3, SCREENY_SIZE * 8 * 3, BufferedImage.TYPE_3BYTE_BGR);
		JPanel panel = new ImagePanel(screen);

		return panel;
	}

	@Override
	public void onPixelRendered(NesColors pixel) {
		try {
			pixelQueue.put(pixel);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true)
			try {
				NesColors pixel = pixelQueue.take();
				drawPixel(pixel.getRGBFromCode());
				screenPanel.repaint();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	private void drawPixel(int pixel) {
		int pixelSqr = PIXEL_SIZE * PIXEL_SIZE;
		int[] toDraw = new int[pixelSqr];

		for (int i = 0; i < pixelSqr; i++) {
			toDraw[i] = pixel;
		}

		screen.setRGB(x*PIXEL_SIZE, y*PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE, toDraw, 0, 0);
		if (++x >= 256) {
			x = 0;
			if (++y >= 239)
				y = 0;
		}
	}

	public void connectScreen() {
		this.setVisible(true);
		Thread t = new Thread(this);
		t.setName("Screen");
		t.start();
	}

	private class ImagePanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3757478389543862468L;
		private BufferedImage image;

		public ImagePanel(BufferedImage image) {
			this.image = image;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(image, 19, 19, this);
		}

	}
}
