package frame;

import java.util.concurrent.atomic.AtomicBoolean;

import components.cpu.Cpu;
import components.ppu.Ppu;
import exceptions.InstructionNotSupportedException;

// This is only for testing purposes
public class GameThread {

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
	private static final int PPU_FREQUENCY = 5320342;

	private AtomicBoolean running = new AtomicBoolean(false);
	private AtomicBoolean stopped = new AtomicBoolean(true);
	private Thread gameCpuThread;
	private Thread gamePpuThread;

	// In ns
	private long waitPeriod;
	private long cpuTickCounter;
	private int cpuTickNumber;

	private long ppuTickCounter;

	public GameThread() {
		cpuTickCounter = 0;

		// Creating timing check thread
		Thread timingCheckThread = new Thread(new Runnable() {
			@Override
			public void run() {
				long cpuCounter = 0;
				long ppuCounter = 0;
				long start = System.currentTimeMillis();
				while (true) {
					long end = System.currentTimeMillis();
					if (end > start + 1000) {
						long actualCpuTicks = cpuTickCounter;
						long totalCpuTicks = actualCpuTicks - cpuCounter;
						System.out.println(
								"CPU frequency: %d Hz (should be: %d Hz)".formatted(totalCpuTicks, CPU_FREQUENCY));

						long actualPpuTicks = ppuTickCounter;
						long totalPpuTicks = actualPpuTicks - ppuCounter;
						System.out.println(
								"PPU frequency: %d Hz (should be: %d Hz)\n".formatted(totalPpuTicks, PPU_FREQUENCY));

						cpuCounter = actualCpuTicks;
						ppuCounter = actualPpuTicks;
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

		gameCpuThread = new Thread(new CpuThread());
		gameCpuThread.setName("CPU Thread (wait: " + waitPeriod + ")");

		gamePpuThread = new Thread(new PpuThread());
		gamePpuThread.setName("PPU Thread");

		gameCpuThread.start();
		gamePpuThread.start();
	}

	public void interrupt() {
		running.set(false);
	}

	public boolean isStopped() {
		return stopped.get();
	}

	private class CpuThread implements Runnable {

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
							cpuTickCounter += cycles;

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

	private class PpuThread implements Runnable {

		@Override
		public void run() {
			running.set(true);
			// FIXME add a stop atomic value
			// Time for next activation (in ns)
			long nextTick = System.nanoTime();
			while (running.get()) {
				long currentTime = System.nanoTime();
				long cyclesToCatch = (currentTime - nextTick) / 188l;
				if (cyclesToCatch != 0) {
					ppuTickCounter += cyclesToCatch;
					Ppu.getInstance().tick(cyclesToCatch);
					nextTick = currentTime;
				}
			}
		}
	}
}
