package nes.components.cpu.register;

import nes.listener.EventManager;
import nes.listener.RegisterListener;

public class CPURegisters implements RegisterListener {

	// Statut processeur (flags)
	private byte P;

	// Registres de données
	private byte A, X, Y;

	// Program counter
	private int pc;

	// Stack pointer
	private int sp;

	private boolean nmi, nmiAllowed;

	public CPURegisters() {
		EventManager.getInstance().addRegisterListener(this);
	}

	public byte getP() {
		return P;
	}

	public void setP(byte p) {
		P = p;
	}

	public byte getA() {
		return A;
	}

	public void setA(byte a) {
		A = a;
	}

	public byte getX() {
		return X;
	}

	public void setX(byte x) {
		X = x;
	}

	public byte getY() {
		return Y;
	}

	public void setY(byte y) {
		Y = y;
	}

	public int getPc() {
		return pc;
	}

	public void setPc(int pc) {
		this.pc = pc;
	}

	public int getSp() {
		return sp;
	}

	public void setSp(int sp) {
		this.sp = sp;
	}

	public boolean hasNMI() {
		if (nmi && nmiAllowed) {
			nmi = false;
			return true;
		}
		return false;
	}

	public void stopNMI() {
		EventManager.getInstance().stopNMI();
	}

	@Override
	public void on2000Written(byte newValue) {
		if (newValue < 0)
			nmiAllowed = true;
		else
			nmiAllowed = false;
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
		nmi = true;
	}

	@Override
	public void onSpriteOverflowRaised() {
	}

	@Override
	public void onSprite0HitRaised() {
	}

	@Override
	public void onNMIOver() {
		nmi = false;
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
