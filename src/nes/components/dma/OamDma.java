package components.dma;

import components.bus.MmioBus;

public class OamDma extends Dma {

	private static final String DMA_NAME_FORMAT = "%s [0x%04X]";
	private static final int SIZE = 256;

	private int startAddress;

	public OamDma() {
		super("OAM DMA");
		this.blockingTime = 2 * SIZE + 1;
	}
	
	public void setStartAddress(int pageNumber) {
		this.startAddress = pageNumber << 8;
	}

	@Override
	protected String getName() {
		return DMA_NAME_FORMAT.formatted(this.dmaName, startAddress);
	}
	
	@Override
	protected void dmaRoutine() {
		for (int address = startAddress; address < startAddress + SIZE; address++) {
			destination.write(MmioBus.OAMDATA_ADDR, source.read(address));
		}
	}

}
