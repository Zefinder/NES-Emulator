package listener;

import java.util.ArrayList;
import java.util.List;

import components.ppu.rendering.NesColors;

public class EventManager {

	private static final EventManager instance = new EventManager();

	private List<RegisterListener> registerListenerList;
	private List<PPURenderListener> renderListenerList;
	private List<BusListener> busListenerList;

	private EventManager() {
		registerListenerList = new ArrayList<>();
		renderListenerList = new ArrayList<>();
		busListenerList = new ArrayList<>();
	}

	public void addRegisterListener(RegisterListener listener) {
		registerListenerList.add(listener);
	}

	public void addRenderListener(PPURenderListener listener) {
		renderListenerList.add(listener);
	}
	
	public void addBusListener(BusListener listener) {
		busListenerList.add(listener);
	}
	
	public void fireValueChanged(boolean isCpuBus, int address, byte value) {
		busListenerList.forEach(listener -> listener.onValueChanged(isCpuBus, address, value));
	}
	
	/**
	 * Tells to the PPURenderListeners that a pixel has been rendered
	 * 
	 * @param pixel the pixel to render
	 * @see PPURenderListener
	 */
	public void firePixelChanged(NesColors pixel) {
		renderListenerList.forEach(listener -> listener.onPixelRendered(pixel));
	}

	/**
	 * Tells to the RegisterListeners that the PPUCTRL (0x2000) has been written by the CPU
	 *  
	 * @param newValue the byte given by the CPU
	 * @see RegisterListener
	 */
	public void fireWriting2000(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2000Written(newValue));
	}

	/**
	 * Tells to the RegisterListeners that the PPUMASK (0x2001) has been written by the CPU
	 *  
	 * @param newValue the byte given by the CPU
	 * @see RegisterListener
	 */
	public void fireWriting2001(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2001Written(newValue));
	}

	/**
	 * Tells to the RegisterListeners that the PPUSTATUS (0x2002) has been written by the CPU
	 *  
	 * @param newValue the byte given by the CPU
	 * @see RegisterListener
	 */
	public void fireWriting2002(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2002Written(newValue));
	}

	/**
	 * Tells to the RegisterListeners that the OAMADDR (0x2003) has been written by the CPU
	 *  
	 * @param newValue the byte given by the CPU
	 * @see RegisterListener
	 */
	public void fireWriting2003(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2003Written(newValue));
	}

	/**
	 * Tells to the RegisterListeners that the OAMDATA (0x2004) has been written by the CPU
	 *  
	 * @param newValue the byte given by the CPU
	 * @see RegisterListener
	 */
	public void fireWriting2004(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2004Written(newValue));
	}

	/**
	 * Tells to the RegisterListeners that the PPUSCROLL (0x2005) has been written by the CPU
	 *  
	 * @param newValue the byte given by the CPU
	 * @see RegisterListener
	 */
	public void fireWriting2005(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2005Written(newValue));
	}

	/**
	 * Tells to the RegisterListeners that the PPUADDR (0x2006) has been written by the CPU
	 *  
	 * @param newValue the byte given by the CPU
	 * @see RegisterListener
	 */
	public void fireWriting2006(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2006Written(newValue));
	}

	/**
	 * Tells to the RegisterListeners that the PPUDATA (0x2007) has been written by the CPU
	 *  
	 * @param newValue the byte given by the CPU
	 * @see RegisterListener
	 */
	public void fireWriting2007(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2007Written(newValue));
	}

	/**
	 * Tells to the RegisterListeners that the OAMDMA (0x4014) has been written by the CPU
	 *  
	 * @param newValue the byte given by the CPU
	 * @see RegisterListener
	 */
	public void fireWriting4014(byte newValue) {
		registerListenerList.forEach(listener -> listener.on4014Written(newValue));
	}

	/**
	 * Tells to the RegisterListeners that the PPUCTRL (0x2000) has been read by the CPU
	 *  
	 * @see RegisterListener
	 */
	public void fireReading2000() {
		registerListenerList.forEach(RegisterListener::on2000Read);
	}
	
	/**
	 * Tells to the RegisterListeners that the PPUMASK (0x2001) has been read by the CPU
	 *  
	 * @see RegisterListener
	 */
	public void fireReading2001() {
		registerListenerList.forEach(RegisterListener::on2001Read);
	}
	
	/**
	 * Tells to the RegisterListeners that the PPUSTATUS (0x2002) has been read by the CPU
	 *  
	 * @see RegisterListener
	 */
	public void fireReading2002() {
		registerListenerList.forEach(RegisterListener::on2002Read);
	}
	
	/**
	 * Tells to the RegisterListeners that the OAMADDR (0x2003) has been read by the CPU
	 *  
	 * @see RegisterListener
	 */
	public void fireReading2003() {
		registerListenerList.forEach(RegisterListener::on2003Read);
	}
	
	/**
	 * Tells to the RegisterListeners that the OAMDATA (0x2004) has been read by the CPU
	 *  
	 * @see RegisterListener
	 */
	public void fireReading2004() {
		registerListenerList.forEach(RegisterListener::on2004Read);
	}
	
	/**
	 * Tells to the RegisterListeners that the PPUSCROLL (0x2005) has been read by the CPU
	 *  
	 * @see RegisterListener
	 */
	public void fireReading2005() {
		registerListenerList.forEach(RegisterListener::on2005Read);
	}
	
	/**
	 * Tells to the RegisterListeners that the PPUADDR (0x2006) has been read by the CPU
	 *  
	 * @see RegisterListener
	 */
	public void fireReading2006() {
		registerListenerList.forEach(RegisterListener::on2006Read);
	}
	
	/**
	 * Tells to the RegisterListeners that the PPUDATA (0x2000) has been read by the CPU
	 *  
	 * @see RegisterListener
	 */
	public void fireReading2007() {
		registerListenerList.forEach(RegisterListener::on2007Read);
	}
	
	/**
	 * Tells to the RegisterListeners that the OAMDMA (0x4014) has been read by the CPU
	 *  
	 * @see RegisterListener
	 */
	public void fireReading4014() {
		registerListenerList.forEach(RegisterListener::on4014Read);
	}
	
	/**
	 * Tells to the RegisterListeners that the PPUCTRL (0x2000) has been written by the PPU
	 *  
	 * @param newValue the byte given by the PPU
	 * @see RegisterListener
	 */
	public void fireChanging2000(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2000Changed(newValue));
	}

	/**
	 * Tells to the RegisterListeners that the PPUMASK (0x2001) has been written by the PPU
	 *  
	 * @param newValue the byte given by the PPU
	 * @see RegisterListener
	 */
	public void fireChanging2001(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2001Changed(newValue));
	}

	/**
	 * Tells to the RegisterListeners that the PPUSTATUS (0x2002) has been written by the PPU
	 *  
	 * @param newValue the byte given by the PPU
	 * @see RegisterListener
	 */
	public void fireChanging2002(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2002Changed(newValue));
	}

	/**
	 * Tells to the RegisterListeners that the OAMADDR (0x2003) has been written by the PPU
	 *  
	 * @param newValue the byte given by the PPU
	 * @see RegisterListener
	 */
	public void fireChanging2003(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2003Changed(newValue));
	}

	/**
	 * Tells to the RegisterListeners that the OAMDATA (0x2004) has been written by the PPU
	 *  
	 * @param newValue the byte given by the PPU
	 * @see RegisterListener
	 */
	public void fireChanging2004(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2004Changed(newValue));
	}

	/**
	 * Tells to the RegisterListeners that the PPUSCROLL (0x2005) has been written by the PPU
	 *  
	 * @param newValue the byte given by the PPU
	 * @see RegisterListener
	 */
	public void fireChanging2005(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2005Changed(newValue));
	}

	/**
	 * Tells to the RegisterListeners that the PPUADDR (0x2006) has been written by the PPU
	 *  
	 * @param newValue the byte given by the PPU
	 * @see RegisterListener
	 */
	public void fireChanging2006(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2006Changed(newValue));
	}

	/**
	 * Tells to the RegisterListeners that the PPUDATA (0x2007) has been written by the PPU
	 *  
	 * @param newValue the byte given by the PPU
	 * @see RegisterListener
	 */
	public void fireChanging2007(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2007Changed(newValue));
	}

	/**
	 * Tells to the RegisterListeners that the OAMDMA (0x4014) has been written by the PPU
	 *  
	 * @param newValue the byte given by the PPU
	 * @see RegisterListener
	 */
	public void fireChanging4014(byte newValue) {
		registerListenerList.forEach(listener -> listener.on4014Changed(newValue));
	}
	
	/**
	 * Tells to the RegisterListeners that VBlank has started and NMI too
	 *  
	 * @see RegisterListener
	 */
	public void fireNMI() {
		registerListenerList.forEach(RegisterListener::onNMIRaised);
	}
	
	/**
	 * Tells to the RegisterListeners that Sprite Overflow has been fired during sprite evaluation
	 *  
	 * @see RegisterListener
	 */
	public void fireSpriteOverflow() {
		registerListenerList.forEach(RegisterListener::onSpriteOverflowRaised);
	}
	
	/**
	 * Tells to the RegisterListeners that the Sprite0 hit has been fired during rendering
	 *  
	 * @see RegisterListener
	 */
	public void fireSprite0Hit() {
		registerListenerList.forEach(RegisterListener::onSprite0HitRaised);
	}
	
	/**
	 * Tells to the RegisterListeners that NMI has been stopped
	 *  
	 * @see RegisterListener
	 */
	public void stopNMI() {
		registerListenerList.forEach(RegisterListener::onNMIOver);
	}
	
	/**
	 * Tells to the RegisterListeners that Sprite Overflow has been cleared
	 *  
	 * @see RegisterListener
	 */
	public void stopSpriteOverflow() {
		registerListenerList.forEach(RegisterListener::onSpriteOverflowOver);
	}
	
	/**
	 * Tells to the RegisterListeners that the Sprite0 hit has been cleared
	 * 
	 * @see RegisterListener
	 */
	public void stopSprite0Hit() {
		registerListenerList.forEach(RegisterListener::onSprite0HitOver);
	}

	/**
	 * Tells to the RegisterListeners that the OAM Transfer has begun
	 *  
	 * @see RegisterListener
	 */
	public void startOAMTransfer(byte[] oamData) {
		registerListenerList.forEach(listener -> listener.startOAMTransfer(oamData));
	}
		
	public static EventManager getInstance() {
		return instance;
	}

}
