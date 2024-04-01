package frame;

import java.awt.GridBagLayout;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import instructions.Instruction;

public class GameFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4153332469558642589L;

	// Dialogs
	private CpuRegisterDialog cpuRegisterDialog;
	private FlagDialog flagDialog;
	private InstructionDialog instructionDialog;
	
	private PpuRegisterDialog ppuRegisterDialog;
	private PaletteDialog paletteDialog;
	
	// Screen
	private ScreenPanel screenPanel = new ScreenPanel(256, 240);

	// Game Thread
	private GameThread gameThread;

	public GameFrame(Instruction[] romInstructions) {
		this.setTitle("NES Emulator");
		this.setSize(512, 480 + 30);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create CPU dialogs
		cpuRegisterDialog = new CpuRegisterDialog(126, 282);
		flagDialog = new FlagDialog(319, 157);
		instructionDialog = new InstructionDialog(romInstructions, 319, 517);

		// Create PPU dialogs
		ppuRegisterDialog = new PpuRegisterDialog(1016+193, 212);
		paletteDialog = new PaletteDialog(1016, 157, 200, 300);
		
		// Add everything to front when deiconify
		this.addWindowListener(new WindowActivatedListener() {
			@Override
			public void windowDeiconified(WindowEvent e) {
				cpuRegisterDialog.toFront();
				flagDialog.toFront();
				instructionDialog.toFront();
				ppuRegisterDialog.toFront();
				paletteDialog.toFront();
				toFront();
			}
		});
		
		// Add screen at the middle
		this.setLayout(new GridBagLayout());
		this.add(screenPanel);

		this.setVisible(false);
	}
	
	public ScreenPanel getScreenPanel() {
		return screenPanel;
	}

	public void initFrame(String gameName) {
		this.setTitle("NES Emulator - " + gameName);
		this.setVisible(true);

		// Init dialogs
		cpuRegisterDialog.initDialog();
		flagDialog.initDialog();
		instructionDialog.initDialog();
		ppuRegisterDialog.initDialog();
		paletteDialog.initDialog();
		toFront();
		
		// Create game thread and key listener
		gameThread = new GameThread();
		this.addKeyListener(new GameKeyListener(gameThread));
		gameThread.startThread(GameThread.CPU_CLOCK_SPEED, GameThread.CPU_TICK_PER_PERIOD);
	}

}
