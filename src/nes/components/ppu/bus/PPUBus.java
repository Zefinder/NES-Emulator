package nes.components.ppu.bus;

import nes.components.Bus;
import nes.components.ppu.register.PPURegisters;
import nes.exceptions.AddressException;
import nes.listener.EventManager;

public class PPUBus extends Bus {

	private PPURegisters registers;

	public PPUBus(PPURegisters registers) {
		super();
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
		EventManager.getInstance().fireValueChanged(false, address, value);
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
	}

	@Override
	public void on2007Written(byte newValue) {
		try {
			setByteToMemory(registers.getBackgroundRegisters().getV(), newValue);
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
	public void on2000Changed(byte newValue) {
	}

	@Override
	public void on2001Changed(byte newValue) {
	}

	@Override
	public void on2002Changed(byte newValue) {
	}

	@Override
	public void on2003Changed(byte newValue) {
	}

	@Override
	public void on2004Changed(byte newValue) {
	}

	@Override
	public void on2005Changed(byte newValue) {
	}

	@Override
	public void on2006Changed(byte newValue) {
	}

	@Override
	public void on2007Changed(byte newValue) {
	}

	@Override
	public void on4014Changed(byte newValue) {
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