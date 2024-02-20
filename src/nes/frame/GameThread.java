package frame;

import java.util.concurrent.atomic.AtomicBoolean;

import components.Cpu;
import exceptions.InstructionNotSupportedException;

// This is only for testing purposes
public class GameThread implements Runnable {

	// Speed 1: 2 Hz
	public static final long SPEED1 = 500000000;
	// Speed 2: 10 Hz
	public static final long SPEED2 = 10000000;
	// Speed 3: CPU speed
	public static final long SPEED3 = Cpu.CLOCK_SPEED;

	private AtomicBoolean running = new AtomicBoolean(false);
	private AtomicBoolean stopped = new AtomicBoolean(true);
	private Thread gameThread;

	// In ns
	private final long waitPeriod;

	public GameThread(long waitPeriod) {
		this.waitPeriod = waitPeriod;
		gameThread = new Thread(this);
		gameThread.setName("Game Thread (wait: " + waitPeriod + ")");
		gameThread.start();
	}

	public void interrupt() {
		running.set(false);
	}

	public boolean isStopped() {
		return stopped.get();
	}

	@Override
	public void run() {
		running.set(true);
		stopped.set(false);

		// Time for next activation (in ns)
		long nextTick = System.nanoTime();

		while (running.get()) {
			long currentTime = System.nanoTime();
			// If it's time to tick, we tick
			if (currentTime >= nextTick) {
				try {
					// Tick!
					int cycles;
					cycles = Cpu.getInstance().tick();

					// Setting next tick
					nextTick = currentTime + cycles * waitPeriod;
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}
			}
		}

		stopped.set(true);
	}

}
