package frame;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import components.cpu.Cpu;
import exceptions.InstructionNotSupportedException;

// This is only for testing purposes
public class GameKeyListener implements KeyListener {

	private final Cpu cpu = Cpu.getInstance();
	private int currentMode = 3;
	private GameThread gameThread;

	public GameKeyListener(GameThread gameThread) {
		this.gameThread = gameThread;
	}

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

		// If in pause mode to tick (deprecated)
		case KeyEvent.VK_ENTER:
			try {
				if (currentMode == 0) {
					cpu.tick();
				}
			} catch (InstructionNotSupportedException e1) {
				e1.printStackTrace();
			}
			break;

		// To show the content of the pattern table
		case KeyEvent.VK_P:
			PatternTableDialog patternDialog = new PatternTableDialog();
			patternDialog.initDialog();
			break;
			
		case KeyEvent.VK_M:
			gameThread.interrupt();
			MemoryDialog memoryDialog = new MemoryDialog(null);
			memoryDialog.initDialog();
			changeMode(currentMode);
			break;
			
		case KeyEvent.VK_O:
			gameThread.interrupt();
			OAMDialog oamDialog = new OAMDialog();
			oamDialog.initDialog();
			changeMode(currentMode);
			break;
			
		default:
			break;
		}

		// If changing mode then kill the thread if living and create a new one
		if (newMode != currentMode) {
			currentMode = newMode;

			gameThread.interrupt();
			// Waiting for thread to stop
			while (!gameThread.isStopped());
			changeMode(newMode);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
	
	private void changeMode(int mode) {
		if (mode == 1) {
			gameThread.startThread(GameThread.SPEED1, 1);
		} else if (mode == 2) {
			gameThread.startThread(GameThread.SPEED2, 1);
		} else if (mode == 3) {
			gameThread.startThread(GameThread.CPU_CLOCK_SPEED, GameThread.CPU_TICK_PER_PERIOD);
		}
	}

}
