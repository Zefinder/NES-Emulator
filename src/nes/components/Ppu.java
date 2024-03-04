package components;

public class Ppu {

	public final PpuInfo ppuInfo = new PpuInfo();
	public final int[] oamMemory = new int[64];
	
	private static final Ppu ppu = new Ppu();
	
	private Ppu() {
		// TODO Auto-generated constructor stub
	}
	
	public static Ppu getInstance() {
		return ppu;
	}
	
}
