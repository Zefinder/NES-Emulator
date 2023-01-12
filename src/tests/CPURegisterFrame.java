package tests;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import nes.components.NES;
import nes.components.cpu.CPU;
import nes.components.cpu.register.CPURegisters;
import nes.components.ppu.PPU;
import nes.exceptions.AddressException;
import nes.exceptions.InstructionException;
import nes.exceptions.MapperException;
import nes.exceptions.NotNesFileException;
import nes.instructions.Instruction;

public class CPURegisterFrame extends JFrame implements KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6222462540927450803L;
	private Map<Integer, Instruction> instructionMap;
	private NES nes;
	private CPURegisters registres;

	private JLabel n, v, b, d, i, z, c;
	private JLabel a, x, y, sp, pc;
	private JLabel ins1, ins2, ins3, ins4, ins5;

	public CPURegisterFrame(NES nes, CPURegisters registres) {
		this.nes = nes;
		this.registres = registres;
		this.instructionMap = nes.getInstructionMap();

		this.setTitle("NES");
		this.setSize(800, 800);
		this.setLocationRelativeTo(null);
		this.addKeyListener(this);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel flagPanel = buildFlagPanel();
		this.add(flagPanel, BorderLayout.NORTH);

		JPanel intructionAndRegPanel = buildInstructionAndRegPanel();
		this.add(intructionAndRegPanel, BorderLayout.EAST);

		this.setVisible(false);
	}

	private void update() {
		n.setText(String.valueOf((registres.getP() & 0b10000000) >> 7));
		v.setText(String.valueOf((registres.getP() & 0b01000000) >> 6));
		b.setText(String.valueOf((registres.getP() & 0b00110000) >> 4));
		d.setText(String.valueOf((registres.getP() & 0b00001000) >> 3));
		i.setText(String.valueOf((registres.getP() & 0b00000100) >> 2));
		z.setText(String.valueOf((registres.getP() & 0b00000010) >> 1));
		c.setText(String.valueOf(registres.getP() & 0b00000001));

		a.setText(String.format("0x%02X", registres.getA()));
		x.setText(String.format("0x%02X", registres.getX()));
		y.setText(String.format("0x%02X", registres.getY()));
		sp.setText(String.format("0x%04X", registres.getSp()));
		pc.setText(String.format("0x%04X", registres.getPc()));

		int index = 0;
		Instruction instruction = instructionMap.get(registres.getPc());
		for (int key : instructionMap.keySet()) {
			if (instructionMap.get(key) == instruction)
				break;

			++index;
		}
		Object[] instructions = instructionMap.values().toArray();

		if (instructions.length == 0 || index == instructions.length) {
			ins1.setText("NOP");
			ins2.setText("NOP");
			ins3.setText("NOP");
			ins4.setText("NOP");
			ins5.setText("NOP");

		} else if (index == 0) {
			ins1.setText("NOP");
			ins2.setText("NOP");
			ins3.setText(instructions[index].toString());
			ins4.setText(instructions[index + 1].toString());
			ins5.setText(instructions[index + 2].toString());

		} else if (index == 1) {
			ins1.setText("NOP");
			ins2.setText(instructions[index - 1].toString());
			ins3.setText(instructions[index].toString());
			ins4.setText(instructions[index + 1].toString());
			ins5.setText(instructions[index + 2].toString());

		} else if (index == instructions.length - 2) {
			ins1.setText(instructions[index - 2].toString());
			ins2.setText(instructions[index - 1].toString());
			ins3.setText(instructions[index].toString());
			ins4.setText(instructions[index + 1].toString());
			ins5.setText("NOP");

		} else if (index == instructions.length - 1) {
			ins1.setText(instructions[index - 2].toString());
			ins2.setText(instructions[index - 1].toString());
			ins3.setText(instructions[index].toString());
			ins4.setText("NOP");
			ins5.setText("NOP");
		} else {
			ins1.setText(instructions[index - 2].toString());
			ins2.setText(instructions[index - 1].toString());
			ins3.setText(instructions[index].toString());
			ins4.setText(instructions[index + 1].toString());
			ins5.setText(instructions[index + 2].toString());
		}
	}

	private void init() {
		update();
	}

	private void nextStep() throws AddressException {
		nes.tick();
		update();
	}

	private void nextInstruction() throws AddressException {
		nes.tick();
		nes.tick();
		nes.tick();
		update();
	}

	private JPanel buildFlagPanel() {
		JPanel flagPanel = new JPanel();
		flagPanel.setLayout(new GridLayout(2, 7, 10, 10));

		JLabel nLabel = new JLabel("N");
		nLabel.setHorizontalAlignment(JLabel.CENTER);
		nLabel.setVerticalAlignment(JLabel.CENTER);

		n = new JLabel("0");
		n.setHorizontalAlignment(JLabel.CENTER);
		n.setVerticalAlignment(JLabel.CENTER);

		JLabel vLabel = new JLabel("V");
		vLabel.setHorizontalAlignment(JLabel.CENTER);
		vLabel.setVerticalAlignment(JLabel.CENTER);

		v = new JLabel("0");
		v.setHorizontalAlignment(JLabel.CENTER);
		v.setVerticalAlignment(JLabel.CENTER);

		JLabel bLabel = new JLabel("B");
		bLabel.setHorizontalAlignment(JLabel.CENTER);
		bLabel.setVerticalAlignment(JLabel.CENTER);

		b = new JLabel("0");
		b.setHorizontalAlignment(JLabel.CENTER);
		b.setVerticalAlignment(JLabel.CENTER);

		JLabel dLabel = new JLabel("D");
		dLabel.setHorizontalAlignment(JLabel.CENTER);
		dLabel.setVerticalAlignment(JLabel.CENTER);

		d = new JLabel("0");
		d.setHorizontalAlignment(JLabel.CENTER);
		d.setVerticalAlignment(JLabel.CENTER);

		JLabel iLabel = new JLabel("I");
		iLabel.setHorizontalAlignment(JLabel.CENTER);
		iLabel.setVerticalAlignment(JLabel.CENTER);

		i = new JLabel("0");
		i.setHorizontalAlignment(JLabel.CENTER);
		i.setVerticalAlignment(JLabel.CENTER);

		JLabel zLabel = new JLabel("Z");
		zLabel.setHorizontalAlignment(JLabel.CENTER);
		zLabel.setVerticalAlignment(JLabel.CENTER);

		z = new JLabel("0");
		z.setHorizontalAlignment(JLabel.CENTER);
		z.setVerticalAlignment(JLabel.CENTER);

		JLabel cLabel = new JLabel("C");
		cLabel.setHorizontalAlignment(JLabel.CENTER);
		cLabel.setVerticalAlignment(JLabel.CENTER);

		c = new JLabel("0");
		c.setHorizontalAlignment(JLabel.CENTER);
		c.setVerticalAlignment(JLabel.CENTER);

		flagPanel.add(nLabel);
		flagPanel.add(vLabel);
		flagPanel.add(bLabel);
		flagPanel.add(dLabel);
		flagPanel.add(iLabel);
		flagPanel.add(zLabel);
		flagPanel.add(cLabel);

		flagPanel.add(n);
		flagPanel.add(v);
		flagPanel.add(b);
		flagPanel.add(d);
		flagPanel.add(i);
		flagPanel.add(z);
		flagPanel.add(c);
		return flagPanel;
	}

	private JPanel buildInstructionAndRegPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		JPanel registres = new JPanel();
		registres.setLayout(new GridLayout(5, 1, 5, 5));

		JLabel aLabel = new JLabel("A");
		registres.add(aLabel);
		a = new JLabel("0");
		registres.add(a);

		JLabel xLabel = new JLabel("X");
		registres.add(xLabel);
		x = new JLabel("0");
		registres.add(x);

		JLabel yLabel = new JLabel("Y");
		registres.add(yLabel);
		y = new JLabel("0");
		registres.add(y);

		JLabel spLabel = new JLabel("SP");
		registres.add(spLabel);
		sp = new JLabel("0");
		registres.add(sp);

		JLabel pcLabel = new JLabel("PC");
		registres.add(pcLabel);
		pc = new JLabel("0");
		registres.add(pc);

		registres.setBorder(BorderFactory.createTitledBorder("Registres"));

		JPanel instructionPanel = new JPanel();
		instructionPanel.setLayout(new GridLayout(5, 1));
		ins1 = new JLabel("NOP");
		ins2 = new JLabel("NOP");
		ins3 = new JLabel("NOP");
		ins3.setBackground(Color.LIGHT_GRAY);
		ins3.setOpaque(true);
		ins4 = new JLabel("NOP");
		ins5 = new JLabel("NOP");
		instructionPanel.add(ins1);
		instructionPanel.add(ins2);
		instructionPanel.add(ins3);
		instructionPanel.add(ins4);
		instructionPanel.add(ins5);

		instructionPanel.setBorder(BorderFactory.createTitledBorder("Instructions"));

		panel.add(registres);
		panel.add(instructionPanel);

		return panel;
	}

	public void showFrame() {
		init();
		this.setVisible(true);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_SPACE:
			try {
				nextStep();
			} catch (AddressException e1) {
				e1.printStackTrace();
			}
			break;

		case KeyEvent.VK_ENTER:
			try {
				nextInstruction();
			} catch (AddressException e1) {
				e1.printStackTrace();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	public static void main(String[] args)
			throws NotNesFileException, IOException, InstructionException, AddressException, MapperException {
		CPU cpu = new CPU();
		PPU ppu = new PPU();

		NES nes = new NES(cpu, ppu, new File("./Super Mario Bros.nes"));
		nes.start();

		CPURegisterFrame frame = new CPURegisterFrame(nes, cpu.getRegistres());
		frame.showFrame();

	}

}
