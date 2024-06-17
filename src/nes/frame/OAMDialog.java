package frame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import components.ppu.Ppu;

public class OAMDialog extends JDialog {

	private static final Color NO_COLOR = new Color(0xffffff);
	private static final Color STEP_ONE_COLOR = new Color(0xaaaaaa);
	private static final Color STEP_TWO_COLOR = new Color(0x555555);
	private static final Color STEP_THREE_COLOR = new Color(0x000000);
	private static final Ppu ppu = Ppu.getInstance();

	/**
	 * 
	 */
	private static final long serialVersionUID = -7726452427861431518L;

	public OAMDialog() {
		this.setTitle("OAM Dialog");
		this.setSize(800, 800);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.add(buildOAMPanel());

		this.setVisible(false);
	}

	private JPanel buildOAMPanel() {
		JPanel oamPanel = new JPanel();
		oamPanel.setLayout(new GridLayout(8, 8));
		
		int[] oamMemory = ppu.oamMemory;
		int baseAddress = -1;
		if (ppu.ppuInfo.spriteSize == 0) {
			baseAddress = 0x1000 * ppu.ppuInfo.spritePatternTableAddress;
		}
		
		for (int spriteNumber = 0; spriteNumber < 64; spriteNumber++) {
			oamPanel.add(buildSpritePanel(oamMemory, baseAddress, spriteNumber));
		}
		
		return oamPanel;
	}

	private JPanel buildSpritePanel(int[] oamMemory, int baseAddress, int spriteNumber) {
		JPanel spritePanel = new JPanel();
		spritePanel.setLayout(new GridBagLayout());

		int spriteAddress = oamMemory[4 * spriteNumber + 1];
		if (baseAddress == -1) {
			// If baseAddress is -1 then we are in 8x16 mode
			baseAddress = 0x1000 * (spriteAddress & 0b1);
			spriteAddress &= 0b11111110; // Remove bit 0 
		} 		
		spriteAddress <<= 4;

		// For each sprite, display the tile, X/Y position and attributes
		// Draw the tile in black and white for now
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 1;
		c.gridheight = 1;

		// ImageIcon of the tile
		c.gridx = 0;
		c.gridy = 0;

		BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < 0x8; y++) {
			// Fetch the low and high plans
			int lowPlanline = ppu.fetchMemory(baseAddress | spriteAddress | y);
			int highPlanline = ppu.fetchMemory(baseAddress | spriteAddress | 0x8 | y);

			// Create an array of pattern and fill it
			int[] patterns = new int[8];
			for (int index = 0; index < patterns.length; index++) {
				patterns[patterns.length - 1 - index] = (lowPlanline & 0b1) + 2 * (highPlanline & 0b1);
				lowPlanline >>= 1;
				highPlanline >>= 1;
			}

			// Create graphics and fill rectangles
			Graphics2D graphics = image.createGraphics();
			for (int index = 0; index < patterns.length; index++) {
				int pattern = patterns[index];
				Color pixelColor;

				// Choosing color with pattern
				switch (pattern) {
				case 0:
					pixelColor = NO_COLOR;
					break;
				case 1:
					pixelColor = STEP_ONE_COLOR;
					break;
				case 2:
					pixelColor = STEP_TWO_COLOR;
					break;
				case 3:
					pixelColor = STEP_THREE_COLOR;
					break;

				default:
					pixelColor = NO_COLOR;
					break;
				}

				graphics.setPaint(pixelColor);
				graphics.fillRect(4 * index, 4 * y, 4, 4);
			}
		}
		
		// Add image to panel
		spritePanel.add(new JLabel(new ImageIcon(image), JLabel.CENTER), c);
		
		c.gridx = 1;
		c.gridy = 0;
		JPanel xyPanel = new JPanel();
		xyPanel.setLayout(new BoxLayout(xyPanel, BoxLayout.PAGE_AXIS));
		xyPanel.add(new JLabel("Y=$%02X".formatted(oamMemory[4 * spriteNumber]), SwingConstants.CENTER));
		xyPanel.add(new JLabel("X=$%02X".formatted(oamMemory[4 * spriteNumber + 3]), SwingConstants.CENTER));
		spritePanel.add(xyPanel, c);
		
		spritePanel.setBorder(BorderFactory.createTitledBorder("Sprite " + spriteNumber));

		return spritePanel;
	}
	
	public void initDialog() {
		this.setVisible(true);
	}
}
