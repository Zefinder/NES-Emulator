package nes.components.ppu.bus;

import nes.components.Bus;
import nes.components.ppu.register.PPURegisters;
import nes.exceptions.AddressException;
import nes.listener.BusListener;
import nes.listener.EventManager;

public class PPUBus extends Bus implements BusListener {

	private PPURegisters registers;

	public PPUBus(PPURegisters registers) {
		super();
		EventManager.getInstance().addBusListener(this);
		this.registers = registers;
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
	protected synchronized void setByte(int address, byte value) throws AddressException {
		super.setByte(address, value);
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