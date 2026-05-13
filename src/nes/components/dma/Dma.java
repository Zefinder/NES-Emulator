package components.dma;

import java.util.concurrent.atomic.AtomicBoolean;

import components.Component;
import components.Memory;

public abstract class Dma extends Component implements Runnable {
	protected final String dmaName;
	private final AtomicBoolean started;

	protected Memory source;
	protected Memory destination;

	protected int blockingTime;

	public Dma(String dmaName) {
		this.dmaName = dmaName;
		this.started = new AtomicBoolean(false);
	}

	public void setSource(Memory source) {
		this.source = source;
	}

	public void setDestination(Memory destination) {
		this.destination = destination;
	}

	@Override
	protected boolean checkImpl() {
		return source != null && destination != null;
	}

	public void startDma() {
		// Starts DMA if not already started
		if (started.get()) {
			// TODO Throw error or log better!
			System.out.println("Error - DMA already started!");
			return;
		}

		// Create a thread for the DMA
		Thread dmaThread = new Thread(this, getName());
		started.set(true);
		dmaThread.start();
	}

	public int getBlockingTime() {
		return blockingTime;
	}
	
	public boolean isRunning() {
		return started.get();
	}
	
	protected abstract void dmaRoutine();
	
	protected abstract String getName();

	@Override
	public void run() {
		dmaRoutine();
		started.set(false);
	}
}
