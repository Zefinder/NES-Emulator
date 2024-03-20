package frame;

import java.util.concurrent.atomic.AtomicBoolean;

import components.cpu.Cpu;
import exceptions.InstructionNotSupportedException;

// This is only for testing purposes
public class GameThread implements Runnable {

	// Speed 1: 2 Hz
	public static final long SPEED1 = 500000000;
	// Speed 2: 100 Hz
	public static final long SPEED2 = 10000000;
	// Speed 3: CPU speed
	public static final long CPU_CLOCK_SPEED = 601;

	// 5 CPU ticks <=> 16 PPU ticks
	// TODO Change the way to time it, not precise...
	// Idea: while true, take time, count how much master clock passed and thus
	// count how much CPU and PPU clocks are needed
	public static final int CPU_TICK_PER_PERIOD = 25;
	public static final int PPU_TICK_PER_PERIOD = 16 * (CPU_TICK_PER_PERIOD / 5);

	// In Hz
	private static final int CPU_FREQUENCY = 1662607;

	private AtomicBoolean running = new AtomicBoolean(false);
	private AtomicBoolean stopped = new AtomicBoolean(true);
	private Thread gameThread;

	// In ns
	private long waitPeriod;
	private long tickCounter;
	private int cpuTickNumber;

	public GameThread() {
		tickCounter = 0;

		// Creating timing check thread
		Thread timingCheckThread = new Thread(new Runnable() {
			@Override
			public void run() {
				long counter = 0;
				long start = System.currentTimeMillis();
				while (true) {
					long end = System.currentTimeMillis();
					if (end > start + 1000) {
						long actualTicks = tickCounter;
						long totalTicks = actualTicks - counter;
						System.out.println(
								"CPU frequency: %d Hz (should be: %d Hz)".formatted(totalTicks, CPU_FREQUENCY));

						counter = actualTicks;
						start = end;
					}
				}
			}
		});
		timingCheckThread.start();
	}

	public void startThread(long waitPeriod, int cpuTickNumber) {
		this.waitPeriod = waitPeriod;
		this.cpuTickNumber = cpuTickNumber;

		gameThread = new Thread(this);
		gameThread.setName("Game Thread (wait: " + waitPeriod + ")");
//		gameThread.start();
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
		int cycles = 0;
		long nextTick = System.nanoTime();
		while (running.get()) {
			long currentTime = System.nanoTime();
			// If it's time to tick, we tick
			if (currentTime >= nextTick) {
				nextTick = currentTime;
				try {
					for (int tickNumber = 0; tickNumber < cpuTickNumber; tickNumber++) {
						// For timing
						tickCounter += cycles;

						// Tick!
						cycles = Cpu.getInstance().tick();

						// Setting next tick
						nextTick += cycles * waitPeriod;
					}
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}
			}
		}

		stopped.set(true);
	}

}
