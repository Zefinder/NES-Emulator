package nes.components.ppu.register;

import nes.components.ppu.rendering.OAM;
import nes.listener.EventManager;
import nes.listener.RegisterListener;

public class PPURegisters implements RegisterListener {

	private PPUBackgroundRegisters backgroundRegisters;
	private PPUExternalRegisters externalRegisters;
	private PPUSpritesRegisters spritesRegisters;

	public PPURegisters() {
		backgroundRegisters = new PPUBackgroundRegisters();
		externalRegisters = new PPUExternalRegisters();
		spritesRegisters = new PPUSpritesRegisters();

		EventManager.getInstance().addRegisterListener(this);
	}

	public PPUBackgroundRegisters getBackgroundRegisters() {
		return backgroundRegisters;
	}

	public void setBackgroundRegisters(PPUBackgroundRegisters backgroundRegisters) {
		this.backgroundRegisters = backgroundRegisters;
	}

	public PPUExternalRegisters getExternalRegisters() {
		return externalRegisters;
	}

	public void setExternalRegisters(PPUExternalRegisters externalRegisters) {
		this.externalRegisters = externalRegisters;
	}

	public PPUSpritesRegisters getSpritesRegisters() {
		return spritesRegisters;
	}

	public void setSpritesRegisters(PPUSpritesRegisters spritesRegisters) {
		this.spritesRegisters = spritesRegisters;
	}

	public void augX() {
		byte x = backgroundRegisters.getX();

		if (x != 7) {
			x += 1;
		} else {
			x = 0;
			augCoarseX();
		}

		backgroundRegisters.setX(x);
	}

	private void augCoarseX() {
		int v = backgroundRegisters.getV();

		if ((v & 0x001F) == 31) {
			v &= ~0x001F;
			v ^= 0x0400;
		} else
			v += 1;

		backgroundRegisters.setV(v);
	}

	// v = yyy NN YYYYY XXXXX
	// yyy => déplacement fin
	// YYYYY => déplacement grossier
	public void augY() {
		int v = backgroundRegisters.getV();
		int y = 0; // Si on OR avec 0, c'est comme si on n'a rien fait !

		if ((v & 0x7000) != 0x7000) { // si le déplacement en y est inférieur à 7
			v += 0x1000; // Ben on ajoute 1 m'enfin
		} else {
			v &= ~0x7000; // Sinon on le met à 0 et là, c'est le drame...
			y = (v & 0x03E0) >> 5; // 0x03E0 = 0b000 00 11111 00000 (déplacement grossier)
			if (y == 0x1D) { // Soit 0b11101
				y = 0; // Le déplacement grossier est remis à 0 (parce que oui)
				v ^= 0x0800; // On change de nametable
			} else if (y == 31) { // Soit 0b11111
				y = 0; // On remet juste à 0
			} else { // SINON
				y += 1; // On augmente le déplacement
			}
		}

		backgroundRegisters.setV((v & ~0x03E0) | (y << 5)); // Et on a fini somehow
	}

	@Override
	public void on2000Written(byte newValue) {
		externalRegisters.setPPUCTRL(newValue);
		backgroundRegisters.setT((backgroundRegisters.getT() & ~0x0C00) | ((newValue & 0x03) << 10));
	}

	@Override
	public void on2001Written(byte newValue) {
		externalRegisters.setPPUMASK(newValue);
	}

	@Override
	public void on2002Written(byte newValue) {
		// Rien ici
	}

	@Override
	public void on2003Written(byte newValue) {
		externalRegisters.setOAMADDR(newValue);
	}

	@Override
	public void on2004Written(byte newValue) {
		externalRegisters.setOAMDATA(newValue);
	}

	@Override
	public void on2005Written(byte newValue) {
		externalRegisters.setPPUSCROLL(newValue);
		if (backgroundRegisters.getW() == 0) {
			backgroundRegisters.setX((byte) (newValue & 0x07));
			backgroundRegisters.setT((backgroundRegisters.getT() & ~0x001F) | (newValue >> 3));
			backgroundRegisters.setW((byte) 1);
		} else {
			backgroundRegisters
					.setT((backgroundRegisters.getT() & ~0x37E0) | ((newValue & 0x07) << 12) | (newValue >> 3) << 5);
			backgroundRegisters.setW((byte) 0);
		}
	}

	@Override
	public void on2006Written(byte newValue) {
		externalRegisters.setPPUADDR(newValue);
		int tmp = (newValue < 0 ? newValue + 256 : newValue);
		if (backgroundRegisters.getW() == 0) {
			backgroundRegisters.setT((backgroundRegisters.getT() & ~0xFF00) | ((tmp & 0x003F) << 8));
			backgroundRegisters.setW((byte) 1);
		} else {
			backgroundRegisters.setT((backgroundRegisters.getT() & ~0x00FF) | tmp);
			backgroundRegisters.setV(backgroundRegisters.getT());
			backgroundRegisters.setW((byte) 0);
		}
	}

	@Override
	public void on2007Written(byte newValue) {
		externalRegisters.setPPUDATA(newValue);
		backgroundRegisters.setV((backgroundRegisters.getV() + externalRegisters.getVRAMIncrement()) % 0x4000);

	}

	@Override
	public void on4014Written(byte newValue) {
		externalRegisters.setOAMDMA(newValue);
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
		backgroundRegisters.setW((byte) 0);
		externalRegisters.clearNMI();
	}

	@Override
	public void on2003Read() {
		// Rien ici
	}

	@Override
	public void on2004Read() {
		// Rien ici
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
		// Rien ici
	}

	@Override
	public void on4014Read() {
		// Rien ici
	}

	@Override
	public void on2000Changed(byte newValue) {
		externalRegisters.setPPUCTRL(newValue);
	}

	@Override
	public void on2001Changed(byte newValue) {
		externalRegisters.setPPUMASK(newValue);
	}

	@Override
	public void on2002Changed(byte newValue) {
		externalRegisters.setPPUSTATUS(newValue);
	}

	@Override
	public void on2003Changed(byte newValue) {
		externalRegisters.setOAMADDR(newValue);
	}

	@Override
	public void on2004Changed(byte newValue) {
		externalRegisters.setOAMDATA(newValue);
	}

	@Override
	public void on2005Changed(byte newValue) {
		externalRegisters.setPPUSCROLL(newValue);
	}

	@Override
	public void on2006Changed(byte newValue) {
		externalRegisters.setPPUADDR(newValue);
	}

	@Override
	public void on2007Changed(byte newValue) {
		externalRegisters.setPPUDATA(newValue);
	}

	@Override
	public void on4014Changed(byte newValue) {
		externalRegisters.setOAMDMA(newValue);
	}

	@Override
	public void onNMIRaised() {
		externalRegisters.setNMI();
		EventManager.getInstance().fireChanging2002(externalRegisters.getPPUSTATUS());
	}

	@Override
	public void onSpriteOverflowRaised() {
		externalRegisters.setSpriteOverflow();
		EventManager.getInstance().fireChanging2002(externalRegisters.getPPUSTATUS());
	}

	@Override
	public void onSprite0HitRaised() {
		externalRegisters.setSprite0Hit();
		EventManager.getInstance().fireChanging2002(externalRegisters.getPPUSTATUS());
	}

	@Override
	public void onNMIOver() {
		externalRegisters.clearNMI();
		EventManager.getInstance().fireChanging2002(externalRegisters.getPPUSTATUS());
	}

	@Override
	public void onSpriteOverflowOver() {
		externalRegisters.clearSpriteOverflow();
		EventManager.getInstance().fireChanging2002(externalRegisters.getPPUSTATUS());
	}

	@Override
	public void onSprite0HitOver() {
		externalRegisters.clearSprite0Hit();
		EventManager.getInstance().fireChanging2002(externalRegisters.getPPUSTATUS());
	}

	@Override
	public void startOAMTransfer(byte[] OAMdata) {
		for (int i = 0; i < OAMdata.length; i += 4) {
			spritesRegisters.getPrimaryOAM()[i / 4] = new OAM(OAMdata[i], OAMdata[i + 1], OAMdata[i + 2],
					OAMdata[i + 3]);
		}
	}

}
