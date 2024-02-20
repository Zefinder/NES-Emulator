package frame;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import components.Cpu;
import exceptions.InstructionNotSupportedException;

// This is only for testing purposes
public class GameKeyListener implements KeyListener {

	private final Cpu cpu = Cpu.getInstance();
	private int currentMode = 0;
	private GameThread gameThread;

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int newMode = currentMode;

		switch (e.getKeyCode()) {
		// No running threads, pause mode
		case KeyEvent.VK_0:
		case KeyEvent.VK_NUMPAD0:
			newMode = 0;
			break;

		// First speed, 2 instructions per second
		case KeyEvent.VK_1:
		case KeyEvent.VK_NUMPAD1:
			newMode = 1;
			break;

		// Second speed, 10 instructions per second
		case KeyEvent.VK_2:
		case KeyEvent.VK_NUMPAD2:
			newMode = 2;
			break;

		// Third speed, normal CPU rate
		case KeyEvent.VK_3:
		case KeyEvent.VK_NUMPAD3:
			newMode = 3;
			break;

		// If in pause mode to tick
		case KeyEvent.VK_ENTER:
			try {
				if (currentMode == 0) {
					cpu.tick();
				}
			} catch (InstructionNotSupportedException e1) {
				e1.printStackTrace();
			}
			break;

		default:
			break;
		}
		
		// If changing mode then kill the thread if living and create a new one
		if (newMode != currentMode) {
			currentMode = newMode;
			
			if (gameThread != null) {
				gameThread.interrupt();
				// Waiting for thread to stop
				while (!gameThread.isStopped());
			}
			
			if (currentMode == 1) {
				gameThread = new GameThread(GameThread.SPEED1);
			} else if (currentMode == 2) {
				gameThread = new GameThread(GameThread.SPEED2);
			} else if (currentMode == 3) {
				gameThread = new GameThread(GameThread.SPEED3);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
