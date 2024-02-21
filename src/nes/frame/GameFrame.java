package frame;

import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import instructions.Instruction;

public class GameFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4153332469558642589L;
	
	// Dialogs
	private RegisterDialog registerDialog;
	private FlagDialog flagDialog;
	private InstructionDialog instructionDialog;
	
	// Game Thread
	private GameThread gameThread;

	public GameFrame(Instruction[] romInstructions) {
		this.setTitle("NES Emulator");
		this.setSize(500, 500);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create dialogs
		registerDialog = new RegisterDialog();
		flagDialog = new FlagDialog();
		instructionDialog = new InstructionDialog(romInstructions);

		// Add everything to front when deiconify
		this.addWindowListener(new WindowActivatedListener() {
			@Override
			public void windowDeiconified(WindowEvent e) {
				registerDialog.toFront();
				flagDialog.toFront();
				toFront();
			}
		});
		
		this.setVisible(false);		
	}

	public void initFrame(String gameName) {
		this.setTitle("NES Emulator - " + gameName);
		this.setVisible(true);
		
		// Init dialogs
		registerDialog.initDialog();
		flagDialog.initDialog();
		instructionDialog.initDialog();
		
		// Create game thread and key listener
		gameThread = new GameThread();
		this.addKeyListener(new GameKeyListener(gameThread));
		gameThread.startThread(GameThread.CPU_CLOCK_SPEED, GameThread.CPU_TICK_PER_PERIOD);
	}

}
