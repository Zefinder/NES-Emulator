package components.ppu.register;

import components.ppu.rendering.OAM;

public class PPUSpritesRegisters {

	OAM[] primaryOAM, secondaryOAM;

	public PPUSpritesRegisters() {
		primaryOAM = new OAM[64];
		for (int i = 0; i < 64; i++)
			primaryOAM[i] = new OAM();

		secondaryOAM = new OAM[8];
		for (int i = 0; i < 8; i++) {
			secondaryOAM[i] = new OAM();
		}
	}

	public OAM[] getPrimaryOAM() {
		return primaryOAM;
	}

	public void setPrimaryOAM(OAM[] primaryOAM) {
		this.primaryOAM = primaryOAM;
	}

	public OAM[] getSecondaryOAM() {
		return secondaryOAM;
	}

	public void setSecondaryOAM(OAM[] secondaryOAM) {
		this.secondaryOAM = secondaryOAM;
	}

}
