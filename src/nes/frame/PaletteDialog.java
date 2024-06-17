package frame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import components.ppu.Ppu;

public class PaletteDialog extends InfoDialog {

	private static final int COLOR_PER_PALETTE = 4;

	/**
	 * 
	 */
	private static final long serialVersionUID = 6034325950554788318L;

	// Colored images
	private BufferedImage[] backgroundPalette0 = new BufferedImage[COLOR_PER_PALETTE];
	private BufferedImage[] backgroundPalette1 = new BufferedImage[COLOR_PER_PALETTE];
	private BufferedImage[] backgroundPalette2 = new BufferedImage[COLOR_PER_PALETTE];
	private BufferedImage[] backgroundPalette3 = new BufferedImage[COLOR_PER_PALETTE];
	
	private BufferedImage[] spritePalette0 = new BufferedImage[COLOR_PER_PALETTE];
	private BufferedImage[] spritePalette1 = new BufferedImage[COLOR_PER_PALETTE];
	private BufferedImage[] spritePalette2 = new BufferedImage[COLOR_PER_PALETTE];
	private BufferedImage[] spritePalette3 = new BufferedImage[COLOR_PER_PALETTE];

	public PaletteDialog(int posX, int posY, int sizeX, int sizeY) {
		super("Palettes", posX, posY, sizeX, sizeY);

		JPanel palettePanel = new JPanel();
		palettePanel.setLayout(new GridLayout(4, 1));
		
		JPanel backgroundPalettePanel = new JPanel();
		backgroundPalettePanel.setLayout(new GridLayout(1, 4));

		// Background panel 0
		JPanel backgroundPalette0Panel = new JPanel();
		backgroundPalette0Panel.setLayout(new GridLayout(4, 1));

		JPanel backgroundPalette1Panel = new JPanel();
		backgroundPalette1Panel.setLayout(new GridLayout(4, 1));

		JPanel backgroundPalette2Panel = new JPanel();
		backgroundPalette2Panel.setLayout(new GridLayout(4, 1));

		JPanel backgroundPalette3Panel = new JPanel();
		backgroundPalette3Panel.setLayout(new GridLayout(4, 1));

		for (int i = 0; i < COLOR_PER_PALETTE; i++) {
			backgroundPalette0[i] = new BufferedImage(25, 25, BufferedImage.TYPE_INT_RGB);
			backgroundPalette0Panel.add(new JLabel(new ImageIcon(backgroundPalette0[i]), JLabel.CENTER));

			backgroundPalette1[i] = new BufferedImage(25, 25, BufferedImage.TYPE_INT_RGB);
			backgroundPalette1Panel.add(new JLabel(new ImageIcon(backgroundPalette1[i]), JLabel.CENTER));

			backgroundPalette2[i] = new BufferedImage(25, 25, BufferedImage.TYPE_INT_RGB);
			backgroundPalette2Panel.add(new JLabel(new ImageIcon(backgroundPalette2[i]), JLabel.CENTER));

			backgroundPalette3[i] = new BufferedImage(25, 25, BufferedImage.TYPE_INT_RGB);
			backgroundPalette3Panel.add(new JLabel(new ImageIcon(backgroundPalette3[i]), JLabel.CENTER));
		}

		backgroundPalettePanel.add(backgroundPalette0Panel);
		backgroundPalettePanel.add(backgroundPalette1Panel);
		backgroundPalettePanel.add(backgroundPalette2Panel);
		backgroundPalettePanel.add(backgroundPalette3Panel);
		
		JPanel spritePalettePanel = new JPanel();
		spritePalettePanel.setLayout(new GridLayout(1, 4));

		// Background panel 0
		JPanel spritePalette0Panel = new JPanel();
		spritePalette0Panel.setLayout(new GridLayout(4, 1));

		JPanel spritePalette1Panel = new JPanel();
		spritePalette1Panel.setLayout(new GridLayout(4, 1));

		JPanel spritePalette2Panel = new JPanel();
		spritePalette2Panel.setLayout(new GridLayout(4, 1));

		JPanel spritePalette3Panel = new JPanel();
		spritePalette3Panel.setLayout(new GridLayout(4, 1));

		for (int i = 0; i < COLOR_PER_PALETTE; i++) {
			spritePalette0[i] = new BufferedImage(25, 25, BufferedImage.TYPE_INT_RGB);
			spritePalette0Panel.add(new JLabel(new ImageIcon(spritePalette0[i]), JLabel.CENTER));

			spritePalette1[i] = new BufferedImage(25, 25, BufferedImage.TYPE_INT_RGB);
			spritePalette1Panel.add(new JLabel(new ImageIcon(spritePalette1[i]), JLabel.CENTER));

			spritePalette2[i] = new BufferedImage(25, 25, BufferedImage.TYPE_INT_RGB);
			spritePalette2Panel.add(new JLabel(new ImageIcon(spritePalette2[i]), JLabel.CENTER));

			spritePalette3[i] = new BufferedImage(25, 25, BufferedImage.TYPE_INT_RGB);
			spritePalette3Panel.add(new JLabel(new ImageIcon(spritePalette3[i]), JLabel.CENTER));
		}
		
		spritePalettePanel.add(spritePalette0Panel);
		spritePalettePanel.add(spritePalette1Panel);
		spritePalettePanel.add(spritePalette2Panel);
		spritePalettePanel.add(spritePalette3Panel);

		palettePanel.add(new JLabel("Background palettes", JLabel.CENTER));
		palettePanel.add(backgroundPalettePanel);
		palettePanel.add(new JLabel("Sprite palettes", JLabel.CENTER));
		palettePanel.add(spritePalettePanel);
		
		this.add(palettePanel);
	}

	@Override
	protected void update() {
		Color backgroundColor = new Color(ColorPalette.values()[Ppu.getInstance().fetchMemory(0x3F00)].getColorValue());
		// These are actually unused colors since the 0th color of a palette reads the background
		Color backgroundColor1 = new Color(ColorPalette.values()[Ppu.getInstance().fetchMemory(0x3F04)].getColorValue());
		Color backgroundColor2 = new Color(ColorPalette.values()[Ppu.getInstance().fetchMemory(0x3F08)].getColorValue());
		Color backgroundColor3 = new Color(ColorPalette.values()[Ppu.getInstance().fetchMemory(0x3F0C)].getColorValue());

		Graphics2D graphics = backgroundPalette0[0].createGraphics();
		graphics.setPaint(backgroundColor);
		graphics.fillRect(0, 0, 25, 25);

		graphics = backgroundPalette1[0].createGraphics();
		graphics.setPaint(backgroundColor1);
		graphics.fillRect(0, 0, 25, 25);

		graphics = backgroundPalette2[0].createGraphics();
		graphics.setPaint(backgroundColor2);
		graphics.fillRect(0, 0, 25, 25);

		graphics = backgroundPalette3[0].createGraphics();
		graphics.setPaint(backgroundColor3);
		graphics.fillRect(0, 0, 25, 25);
		
		graphics = spritePalette0[0].createGraphics();
		graphics.setPaint(backgroundColor);
		graphics.fillRect(0, 0, 25, 25);

		graphics = spritePalette1[0].createGraphics();
		graphics.setPaint(backgroundColor1);
		graphics.fillRect(0, 0, 25, 25);

		graphics = spritePalette2[0].createGraphics();
		graphics.setPaint(backgroundColor2);
		graphics.fillRect(0, 0, 25, 25);

		graphics = spritePalette3[0].createGraphics();
		graphics.setPaint(backgroundColor3);
		graphics.fillRect(0, 0, 25, 25);

		for (int i = 1; i < COLOR_PER_PALETTE; i++) {
			Color color = new Color(ColorPalette.values()[Ppu.getInstance().fetchMemory(0x3F00 + i)].getColorValue());
			graphics = backgroundPalette0[i].createGraphics();
			graphics.setPaint(color);
			graphics.fillRect(0, 0, 25, 25);
		}

		for (int i = 1; i < COLOR_PER_PALETTE; i++) {
			Color color = new Color(ColorPalette.values()[Ppu.getInstance().fetchMemory(0x3F04 + i)].getColorValue());
			graphics = backgroundPalette1[i].createGraphics();
			graphics.setPaint(color);
			graphics.fillRect(0, 0, 25, 25);
		}

		for (int i = 1; i < COLOR_PER_PALETTE; i++) {
			Color color = new Color(ColorPalette.values()[Ppu.getInstance().fetchMemory(0x3F08 + i)].getColorValue());
			graphics = backgroundPalette2[i].createGraphics();
			graphics.setPaint(color);
			graphics.fillRect(0, 0, 25, 25);
		}

		for (int i = 1; i < COLOR_PER_PALETTE; i++) {
			Color color = new Color(ColorPalette.values()[Ppu.getInstance().fetchMemory(0x3F0C + i)].getColorValue());
			graphics = backgroundPalette3[i].createGraphics();
			graphics.setPaint(color);
			graphics.fillRect(0, 0, 25, 25);
		}
		
		for (int i = 1; i < COLOR_PER_PALETTE; i++) {
			Color color = new Color(ColorPalette.values()[Ppu.getInstance().fetchMemory(0x3F10 + i)].getColorValue());
			graphics = spritePalette0[i].createGraphics();
			graphics.setPaint(color);
			graphics.fillRect(0, 0, 25, 25);
		}

		for (int i = 1; i < COLOR_PER_PALETTE; i++) {
			Color color = new Color(ColorPalette.values()[Ppu.getInstance().fetchMemory(0x3F14 + i)].getColorValue());
			graphics = spritePalette1[i].createGraphics();
			graphics.setPaint(color);
			graphics.fillRect(0, 0, 25, 25);
		}

		for (int i = 1; i < COLOR_PER_PALETTE; i++) {
			Color color = new Color(ColorPalette.values()[Ppu.getInstance().fetchMemory(0x3F18 + i)].getColorValue());
			graphics = spritePalette2[i].createGraphics();
			graphics.setPaint(color);
			graphics.fillRect(0, 0, 25, 25);
		}

		for (int i = 1; i < COLOR_PER_PALETTE; i++) {
			Color color = new Color(ColorPalette.values()[Ppu.getInstance().fetchMemory(0x3F1C + i)].getColorValue());
			graphics = spritePalette3[i].createGraphics();
			graphics.setPaint(color);
			graphics.fillRect(0, 0, 25, 25);
		}
	}
}
