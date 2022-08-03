package nes.listener;

import java.util.ArrayList;
import java.util.List;

import nes.components.ppu.rendering.NesColors;

public class EventManager {

	private static final EventManager instance = new EventManager();

	private List<RegisterListener> registerListenerList;
	private List<PPURenderListener> renderListenerList;

	private EventManager() {
		registerListenerList = new ArrayList<>();
		renderListenerList = new ArrayList<>();
	}

	public void addRegisterListener(RegisterListener listener) {
		registerListenerList.add(listener);
	}

	public void addRenderListener(PPURenderListener listener) {
		renderListenerList.add(listener);
	}
	
	public void firePixelChanged(NesColors pixel) {
		renderListenerList.forEach(listener -> listener.onPixelRendered(pixel));
	}

	public void fireWriting2000(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2000Written(newValue));
	}

	public void fireWriting2001(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2001Written(newValue));
	}

	public void fireWriting2002(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2002Written(newValue));
	}

	public void fireWriting2003(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2003Written(newValue));
	}

	public void fireWriting2004(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2004Written(newValue));
	}

	public void fireWriting2005(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2005Written(newValue));
	}

	public void fireWriting2006(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2006Written(newValue));
	}

	public void fireWriting2007(byte newValue) {
		registerListenerList.forEach(listener -> listener.on2007Written(newValue));
	}

	public void fireWriting4014(byte newValue) {
		registerListenerList.forEach(listener -> listener.on4014Written(newValue));
	}

	public void fireReading2000() {
		registerListenerList.forEach(RegisterListener::on2000Read);
	}
	
	public void fireReading2001() {
		registerListenerList.forEach(RegisterListener::on2001Read);
	}
	
	public void fireReading2002() {
		registerListenerList.forEach(RegisterListener::on2002Read);
	}
	
	public void fireReading2003() {
		registerListenerList.forEach(RegisterListener::on2003Read);
	}
	
	public void fireReading2004() {
		registerListenerList.forEach(RegisterListener::on2004Read);
	}
	
	public void fireReading2005() {
		registerListenerList.forEach(RegisterListener::on2005Read);
	}
	
	public void fireReading2006() {
		registerListenerList.forEach(RegisterListener::on2006Read);
	}
	
	public void fireReading2007() {
		registerListenerList.forEach(RegisterListener::on2007Read);
	}
	
	public void fireReading4014() {
		registerListenerList.forEach(RegisterListener::on4014Read);
	}
	
	public void fireNMI() {
		registerListenerList.forEach(RegisterListener::onNMIRaised);
	}
	
	public void fireSpriteOverflow() {
		registerListenerList.forEach(RegisterListener::onSpriteOverflowRaised);
	}
	
	public void fireSprite0Hit() {
		registerListenerList.forEach(RegisterListener::onSprite0HitRaised);
	}
	
	public void stopNMI() {
		registerListenerList.forEach(RegisterListener::onNMIOver);
	}
	
	public void stopSpriteOverflow() {
		registerListenerList.forEach(RegisterListener::onSpriteOverflowOver);
	}
	
	public void stopSprite0Hit() {
		registerListenerList.forEach(RegisterListener::onSprite0HitOver);
	}

	public void startOAMTransfer(byte[] oamData) {
		registerListenerList.forEach(listener -> listener.startOAMTransfer(oamData));
	}
		
	public static EventManager getInstance() {
		return instance;
	}

}
