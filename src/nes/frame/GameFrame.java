package frame;

import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import components.Cpu;
import components.CpuInfo;
import instructions.Instruction;

public class GameFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4153332469558642589L;

	private CpuInfo cpuInfo;
	
	// Dialogs
	private RegisterDialog registerDialog;
	private FlagDialog flagDialog;
	private InstructionDialog instructionDialog;

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
		
		this.addKeyListener(new GameKeyListener());
		
		this.setVisible(false);		
	}

	public void initFrame(String gameName) {
		cpuInfo = Cpu.getInstance().cpuInfo;

		// Must be set by the mapper or by some init function
		cpuInfo.PC = 0x8000;

		this.setTitle("NES Emulator - " + gameName);
		this.setVisible(true);
		
		registerDialog.initDialog();
		flagDialog.initDialog();
		instructionDialog.initDialog();
	}

}
