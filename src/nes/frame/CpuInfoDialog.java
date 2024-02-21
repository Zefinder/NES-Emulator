package frame;

import java.awt.GridLayout;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import components.Cpu;
import components.CpuInfo;

public abstract class CpuInfoDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 594919851306084060L;

	private int index;

	private final JLabel[] nameLabels;
	private final JLabel[] valueLabels;

	protected final CpuInfo cpuInfo;

	public CpuInfoDialog(String title, int elementNumber) {
		cpuInfo = Cpu.getInstance().cpuInfo;

		// Creating the arrays and registering elements
		nameLabels = new JLabel[elementNumber];
		valueLabels = new JLabel[elementNumber];
		index = 0;
		registerElements();

		// Creating dialog
		this.setTitle(title);
		this.setSize(200, elementNumber * 50);
		this.add(createMainPanel());
		this.setResizable(false);
		this.setVisible(false);
	}

	private JPanel createMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(nameLabels.length, 2));

		for (int index = 0; index < nameLabels.length; index++) {
			valueLabels[index] = new JLabel("", SwingConstants.CENTER);
			panel.add(nameLabels[index]);
			panel.add(valueLabels[index]);
		}

		return panel;
	}

	/**
	 * This method registers all elements that will be displayed in the dialog and
	 * that were added using the {@link #addElement(int, String)} method.
	 */
	protected abstract void registerElements();

	/**
	 * <p>
	 * This method updates values that were registered. It's better not to forget a
	 * newly added element... See {@link #setElementValue(int, String)} to update
	 * the value of the selected row
	 * </p>
	 * 
	 * <p>
	 * This is called by an external thread and you should be careful when writing
	 * shared variables
	 * </p>
	 */
	protected abstract void update();

	/**
	 * This method adds an element in the list of elements. The value given by
	 * default is an empty string.
	 * 
	 * @param name the displayed name of the row
	 */
	protected void addElement(String name) {
		nameLabels[index++] = new JLabel(name, SwingConstants.CENTER);
	}

	/**
	 * This method modifies the value of a row. This is very useful for the
	 * {@link #update()} method.
	 * 
	 * @param index the index of the row to modify
	 * @param value the value of the row
	 */
	protected void setElementValue(int index, String value) {
		valueLabels[index].setText(value);
	}

	/**
	 * Inits the dialog and makes it update every 40ms (25Hz)
	 */
	public void initDialog() {
		this.setVisible(true);

		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> update(), 0, 40, TimeUnit.MILLISECONDS);
	}

}
