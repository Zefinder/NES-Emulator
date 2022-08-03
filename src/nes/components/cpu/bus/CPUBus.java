package nes.components.cpu.bus;

import nes.components.Bus;
import nes.exceptions.AddressException;
import nes.listener.EventManager;

public class CPUBus extends Bus {

	private byte L;

	public CPUBus() {
		EventManager.getInstance().addRegisterListener(this);
	}

	// FIXME ATTENTION ATTENDRE LES CYCLES AVANT D'ECRIRE DANS CERTAINS REGISTRES

	@Override
	public synchronized byte getByteFromMemory(int address) throws AddressException {
		byte ret;
		switch (address) {
		case 0x2000:
			EventManager.getInstance().fireReading2000();
			ret = L;
			break;

		case 0x2001:
			EventManager.getInstance().fireReading2001();
			ret = L;
			break;

		case 0x2002:
			EventManager.getInstance().fireReading2002();
			ret = L;
			break;

		case 0x2003:
			EventManager.getInstance().fireReading2003();
			ret = L;
			break;

		case 0x2004:
			EventManager.getInstance().fireReading2004();
			ret = L;
			break;

		case 0x2005:
			EventManager.getInstance().fireReading2005();
			ret = L;
			break;

		case 0x2006:
			EventManager.getInstance().fireReading2006();
			ret = L;
			break;

		case 0x2007:
			EventManager.getInstance().fireReading2007();
			ret = L;
			break;

		case 0x4014:
			EventManager.getInstance().fireReading4014();
			ret = L;
			break;

		default:
//			System.out.println(String.format("0x%04X", address));
			ret = getByte(address);
			break;
		}
		return ret;
	}

	@Override
	public synchronized void setByteToMemory(int address, byte toSet) throws AddressException {
		switch (address) {
		case 0x2000:
			EventManager.getInstance().fireWriting2000(toSet);
			break;

		case 0x2001:
			EventManager.getInstance().fireWriting2001(toSet);
			break;

		case 0x2002:
			EventManager.getInstance().fireWriting2002(toSet);
			break;

		case 0x2003:
			EventManager.getInstance().fireWriting2003(toSet);
			break;

		case 0x2004:
			EventManager.getInstance().fireWriting2004(toSet);
			break;

		case 0x2005:
			EventManager.getInstance().fireWriting2005(toSet);
			break;

		case 0x2006:
			EventManager.getInstance().fireWriting2006(toSet);
			break;

		case 0x2007:
			EventManager.getInstance().fireWriting2007(toSet);
			break;

		case 0x4014:
			EventManager.getInstance().fireWriting4014(toSet);
			break;

		default:
			setByte(address, toSet);
			break;
		}

	}

	@Override
	public void on2000Written(byte newValue) {
		L = newValue;
		try {
			setByte(0x2000, newValue);
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void on2001Written(byte newValue) {
		L = newValue;
		try {
			setByte(0x2001, newValue);
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void on2002Written(byte newValue) {
		L = newValue;
	}

	@Override
	public void on2003Written(byte newValue) {
		L = newValue;
		try {
			setByte(0x2001, newValue);
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void on2004Written(byte newValue) {
		L = newValue;
		try {
			setByte(0x2001, newValue);
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void on2005Written(byte newValue) {
		L = newValue;
		try {
			setByte(0x2001, newValue);
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void on2006Written(byte newValue) {
		L = newValue;
		try {
			setByte(0x2001, newValue);
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void on2007Written(byte newValue) {
		L = newValue;
		try {
			setByte(0x2001, newValue);
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void on4014Written(byte newValue) {
		L = newValue;
		try {
			setByte(0x2001, newValue);
			byte[] oamData = new byte[0x100];
			int baseAddress = newValue << 8;
			for (int i = 0; i < oamData.length; i++) {
				oamData[i] = getByte(baseAddress | i);
			}

			EventManager.getInstance().startOAMTransfer(oamData);
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void on2000Read() {
		// Rien ici
	}

	@Override
	public void on2001Read() {
		// Rien ici
	}

	@Override
	public void on2002Read() {
		try {
			L = getByte(0x2002);
			setByte(0x2002, (byte) (getByte(0x2002) & 0b01111111));
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void on2003Read() {
		// Rien ici
	}

	@Override
	public void on2004Read() {
		try {
			L = getByte(0x2004);
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void on2005Read() {
		// Rien ici
	}

	@Override
	public void on2006Read() {
		// Rien ici
	}

	@Override
	public void on2007Read() {
		try {
			L = getByte(0x2007);
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void on4014Read() {
		// Rien ici
	}

	@Override
	public void onNMIRaised() {
		try {
			setByte(0x2002, (byte) (getByte(0x2002) | 0b10000000));
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSpriteOverflowRaised() {
		try {
			setByte(0x2002, (byte) (getByte(0x2002) | 0b00100000));
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSprite0HitRaised() {
		try {
			setByte(0x2002, (byte) (getByte(0x2002) | 0b01000000));
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onNMIOver() {
		try {
			setByte(0x2002, (byte) (getByte(0x2002) & ~0b10000000));
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSpriteOverflowOver() {
		try {
			setByte(0x2002, (byte) (getByte(0x2002) & ~0b00100000));
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSprite0HitOver() {
		try {
			setByte(0x2002, (byte) (getByte(0x2002) & ~0b01000000));
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startOAMTransfer(byte[] OAMdata) {
		// FIXME Faire attendre le CPU 250 cycles
		// Rien
	}
}
