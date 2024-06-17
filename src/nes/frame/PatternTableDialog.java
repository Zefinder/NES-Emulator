package frame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import components.ppu.Ppu;

public class PatternTableDialog extends JDialog {

	private static final Color NO_COLOR = new Color(0xffffff);
	private static final Color STEP_ONE_COLOR = new Color(0xaaaaaa);
	private static final Color STEP_TWO_COLOR = new Color(0x555555);
	private static final Color STEP_THREE_COLOR = new Color(0x000000);

	/**
	 * 
	 */
	private static final long serialVersionUID = 3452117888650018044L;

	public PatternTableDialog() {
		this.setTitle("Pattern table");
		this.setSize(800, 800);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		initPatternTab(tabbedPane, 0x0000, "Left table");
		initPatternTab(tabbedPane, 0x1000, "Right table");
		this.add(tabbedPane);
		
		this.setVisible(false);
	}

	private void initPatternTab(JTabbedPane tabbedPane, int baseAddress, String tabName) {
		JPanel tabPanel = new JPanel();
		tabPanel.setLayout(new GridLayout(16, 16));

		Ppu ppu = Ppu.getInstance();
		for (int address = 0; address <= 0xFF0; address += 0x10) {
			// Create the image
			BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);

			// Fill every pixel of the tile
			for (int y = 0; y < 0x8; y++) {

				// Fetch the low and high plans
				int lowPlanline = ppu.fetchMemory(baseAddress | address | y);
				int highPlanline = ppu.fetchMemory(baseAddress | address | 0x8 | y);

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
					
					// Setting color (TODO make constants)
					graphics.setPaint(pixelColor);
					graphics.fillRect(4 * index, 4 * y, 4, 4);
				}				
			}
			
			// Add image to panel
			tabPanel.add(new JLabel(new ImageIcon(image), JLabel.CENTER));
		}
		
		// Add panel to tabbed pane
		tabbedPane.add(tabPanel, tabName);
	}

	public void initDialog() {
		this.setVisible(true);
	}
}
