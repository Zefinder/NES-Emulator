package components;

import java.util.function.BiConsumer;

import components.cpu.Cpu;

public class DmaAction implements Runnable {

	// TODO Documentation

	private static final Cpu cpu = Cpu.getInstance();
	
	private int startAddress;
	private int size;
	private BiConsumer<Integer, Integer> dataConsumer;
	
	private int blockingTime;

	public DmaAction(int startAddress, int size, BiConsumer<Integer, Integer> dataConsumer) {
		this.startAddress = startAddress;
		this.size = size;
		this.dataConsumer = dataConsumer;
		
		blockingTime = 2 * size + 1;
	}

	public void startDma() {
		// Create a thread for the DMA
		Thread dmaThread = new Thread(this, "DMA Action [0x%04X]".formatted(startAddress));
		dmaThread.start();
	}

	public int getBlockingTime() {
		return blockingTime;
	}
	
	@Override
	public void run() {
		for (int address = startAddress; address < startAddress + size; address++) {
			dataConsumer.accept(address, cpu.fetchMemory(address));	
		}
	}
}
