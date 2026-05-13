package components.register;

import components.Component;
import components.Memory;
import components.dma.OamDma;

public class IoRegisters extends Component implements Memory {

	public static final int OAMDMA_ADDR = 0x4014;
	
	private OamDma oamDma;
	
	public IoRegisters() {
	}
	
	public void setOamDma(OamDma oamDma) {
		this.oamDma = oamDma;
	}
	
	@Override
	protected boolean checkImpl() {
		return oamDma != null;
	}
	
	@Override
	public int read(int address) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void write(int address, int value) {
		switch (address) {
		case 0x14:
			oamDma.setStartAddress(value);
			oamDma.startDma();
			break;

		default:
			System.out.println("Ignored address 0x40%02X".formatted(address));
			break;
		}
	}

}
