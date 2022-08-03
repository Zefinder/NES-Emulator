package nes.listener;

public interface RegisterListener {

	void on2000Written(byte newValue);

	void on2001Written(byte newValue);

	void on2002Written(byte newValue);

	void on2003Written(byte newValue);

	void on2004Written(byte newValue);

	void on2005Written(byte newValue);

	void on2006Written(byte newValue);

	void on2007Written(byte newValue);

	void on4014Written(byte newValue);

	void on2000Read();

	void on2001Read();

	void on2002Read();

	void on2003Read();

	void on2004Read();

	void on2005Read();

	void on2006Read();

	void on2007Read();

	void on4014Read();

	void onNMIRaised();

	void onSpriteOverflowRaised();

	void onSprite0HitRaised();

	void onNMIOver();

	void onSpriteOverflowOver();

	void onSprite0HitOver();
	
	void startOAMTransfer(byte[] OAMdata);
}
