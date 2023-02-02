package listener;

/**
 * Listener used to tell the {@link CPU} and the {@link PPU} that something
 * changed in the PPU's external registers. The listener is divided into 4 parts
 * :
 * <ul>
 * <li>The CPU wrote in the external registers
 * <li>The CPU read in the external registers
 * <li>The PPU changed the value in one external register
 * <li>Timing events that can be used by games (NMI, Sprite0 hit and sprite
 * overflow
 * </ul>
 * 
 * All these functions are called by the {@link EventManager}
 * 
 * @author Zefinder
 * 
 * @see EventManager
 */
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

	void on2000Changed(byte newValue);

	void on2001Changed(byte newValue);

	void on2002Changed(byte newValue);

	void on2003Changed(byte newValue);

	void on2004Changed(byte newValue);

	void on2005Changed(byte newValue);

	void on2006Changed(byte newValue);

	void on2007Changed(byte newValue);

	void on4014Changed(byte newValue);

	void onNMIRaised();

	void onSpriteOverflowRaised();

	void onSprite0HitRaised();

	void onNMIOver();

	void onSpriteOverflowOver();

	void onSprite0HitOver();

	void startOAMTransfer(byte[] OAMdata);
}
