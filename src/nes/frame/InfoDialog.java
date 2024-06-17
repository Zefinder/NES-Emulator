package frame;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JDialog;

import components.cpu.Cpu;
import components.cpu.CpuInfo;
import components.ppu.Ppu;
import components.ppu.PpuInfo;

public abstract class InfoDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3595150045454482981L;
	
	protected final CpuInfo cpuInfo;
	protected final PpuInfo ppuInfo;

	public InfoDialog(String title, int posX, int posY, int sizeX, int sizeY) {
		this.setTitle(title);
		this.setLocation(posX, posY);
		this.setSize(sizeX, sizeY);
		
		cpuInfo = Cpu.getInstance().cpuInfo;
		ppuInfo = Ppu.getInstance().ppuInfo;
	}

	/**
	 * <p>
	 * This method updates values of the dialog. It is called by an external thread
	 * and you should be careful when writing shared variables
	 * </p>
	 */
	protected abstract void update();

	/**
	 * Inits the dialog and makes it update every 40ms (25Hz)
	 */
	public void initDialog() {
		this.setVisible(true);

		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> update(), 0, 40, TimeUnit.MILLISECONDS);
	}
}
