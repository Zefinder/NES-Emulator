package nes.components.ppu.bus;

import nes.components.Bus;
import nes.exceptions.AddressException;
import nes.listener.EventManager;

public class PPUBus extends Bus {

	private byte addrLow, addrHigh;
	private byte w;

	public PPUBus() {
		EventManager.getInstance().addRegisterListener(this);
	}

	@Override
	public synchronized byte getByteFromMemory(int address) throws AddressException {
		return getByte(address);
	}

	@Override
	public synchronized void setByteToMemory(int address, byte toSet) throws AddressException {
		setByte(address, toSet);
	}

	@Override
	public void on2000Written(byte newValue) {
	}

	@Override
	public void on2001Written(byte newValue) {
	}

	@Override
	public void on2002Written(byte newValue) {
	}

	@Override
	public void on2003Written(byte newValue) {
	}

	@Override
	public void on2004Written(byte newValue) {
	}

	@Override
	public void on2005Written(byte newValue) {
	}

	@Override
	public void on2006Written(byte newValue) {
		if (w == 0)
			addrHigh = newValue;
		else
			addrLow = newValue;
	}

	@Override
	public void on2007Written(byte newValue) {
		int lsb = (addrLow < 0 ? addrLow + 256 : addrLow);
		int msb = (addrHigh < 0 ? addrHigh + 256 : addrHigh) << 8;
		try {
			setByte(msb | lsb, newValue);
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void on4014Written(byte newValue) {
	}

	@Override
	public void on2000Read() {
	}

	@Override
	public void on2001Read() {
	}

	@Override
	public void on2002Read() {
		w = 0;
	}

	@Override
	public void on2003Read() {
	}

	@Override
	public void on2004Read() {
	}

	@Override
	public void on2005Read() {
	}

	@Override
	public void on2006Read() {
	}

	@Override
	public void on2007Read() {
	}

	@Override
	public void on4014Read() {
	}

	@Override
	public void onNMIRaised() {
	}

	@Override
	public void onSpriteOverflowRaised() {
	}

	@Override
	public void onSprite0HitRaised() {
	}

	@Override
	public void onNMIOver() {
	}

	@Override
	public void onSpriteOverflowOver() {
	}

	@Override
	public void onSprite0HitOver() {
	}

	@Override
	public void startOAMTransfer(byte[] OAMdata) {		
	}

}