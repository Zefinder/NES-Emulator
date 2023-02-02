package components.ppu.bus;

import components.Bus;
import exceptions.AddressException;
import listener.BusListener;

public class PPUBus extends Bus implements BusListener {

	public PPUBus(int memorySize) {
		super(memorySize, false);
//		EventManager.getInstance().addBusListener(this);
	}

	@Override
	public synchronized byte getByteFromMemory(int address) throws AddressException {
		return getByte(address);
	}

	@Override
	public synchronized void setByteToMemory(int address, byte toSet) throws AddressException {
		address = (address > 0x3F1F ? 0x3F00 + (address - 0x3F00) % 0x20 : address);

		switch (address) {
		case 0x3F00:
			setByte(0x3F10, toSet);
			break;

		case 0x3F04:
			setByte(0x3F14, toSet);
			break;

		case 0x3F08:
			setByte(0x3F18, toSet);
			break;

		case 0x3F0C:
			setByte(0x3F1C, toSet);
			break;

		case 0x3F10:
			setByte(0x3F00, toSet);
			break;

		case 0x3F14:
			setByte(0x3F04, toSet);
			break;

		case 0x3F18:
			setByte(0x3F08, toSet);
			break;

		case 0x3F1C:
			setByte(0x3F0C, toSet);
			break;

		default:
			break;
		}
		setByte(address, toSet);
	}

	@Override
	public void onValueChanged(boolean isCpuBus, int address, byte value) {
		if (!isCpuBus)
			try {
				setByteToMemory(address, value);
			} catch (AddressException e) {
				e.printStackTrace();
			}
	}

}