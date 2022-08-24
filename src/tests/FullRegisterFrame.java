package tests;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import nes.components.NES;
import nes.components.cpu.CPU;
import nes.components.cpu.register.CPURegisters;
import nes.components.ppu.PPU;
import nes.components.ppu.register.PPURegisters;
import nes.components.ppu.rendering.NesColors;
import nes.components.ppu.rendering.Tile;
import nes.exceptions.AddressException;
import nes.exceptions.InstructionException;
import nes.exceptions.MapperException;
import nes.exceptions.NotNesFileException;
import nes.instructions.Instruction;
import nes.listener.BusListener;
import nes.listener.EventManager;

public class FullRegisterFrame extends JFrame implements KeyListener, BusListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6222462540927450803L;
	private Map<Integer, Instruction> instructionMap;
	private NES nes;
	private CPU cpu;
	private PPU ppu;
	private CPURegisters cpuRegistres;
	private PPURegisters ppuRegistres;

	private JLabel n, v, b, d, i, z, c;
	private JLabel a, x, y, sp, pc;
	private JLabel PPUCTRL, PPUMASK, PPUSTATUS, OAMADDR, OAMDATA, PPUSCROLL, PPUADDR, PPUDATA, OAMDMA;
	private JLabel ppuV, ppuT, ppuX, ppuW, scanline, cycle;
	private JLabel ins1, ins2, ins3, ins4, ins5;
	private JLabel tileLabel1, tileLabel2;

	private JTable cpuTable, ppuTable;
	private BufferedImage tile1, tile2;

	private String[][] cpuBusContent, ppuBusContent;

	private boolean auto1, auto2, auto3, auto4, auto5;

	public FullRegisterFrame(NES nes, CPU cpu, PPU ppu) {
		this.nes = nes;
		this.cpu = cpu;
		this.ppu = ppu;
		this.cpuRegistres = cpu.getRegistres();
		this.ppuRegistres = ppu.getRegistres();
		this.instructionMap = nes.getInstructionMap();

		this.cpuBusContent = new String[cpu.getBus().getSize()][2];
		this.ppuBusContent = new String[ppu.getBus().getSize()][2];
		EventManager.getInstance().addBusListener(this);

		this.setTitle("NES");
		this.setSize(800, 800);
		this.setLocationRelativeTo(null);
		this.addKeyListener(this);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel flagPanel = buildFlagPanel();
		this.add(flagPanel, BorderLayout.NORTH);

		JPanel intructionAndRegPanel = buildInstructionAndRegPanel();
		this.add(intructionAndRegPanel, BorderLayout.EAST);

		JPanel ppuRegistersPanel = buildPPURegistersPanel();
		this.add(ppuRegistersPanel, BorderLayout.SOUTH);

		JPanel busValuesPanel = buildBusPanel();
		this.add(busValuesPanel, BorderLayout.WEST);

		final ScheduledExecutorService schAuto1 = Executors.newScheduledThreadPool(1);
		schAuto1.scheduleAtFixedRate(new Runnable() {

			private int counter = 0;

			@Override
			public void run() {
				try {
					if (auto1) {
						nes.tick();
						counter = ++counter % 5;
						if (counter == 0)
							update();
					}
				} catch (AddressException e) {
					e.printStackTrace();
				}
			}
		}, 0, 31250, TimeUnit.MICROSECONDS);

		final ScheduledExecutorService schAuto2 = Executors.newScheduledThreadPool(1);
		schAuto2.scheduleAtFixedRate(new Runnable() {

			private int counter = 0;

			@Override
			public void run() {
				try {
					if (auto2) {
						nes.tick();
						counter = ++counter % 5;
						if (counter == 0)
							update();
					}
				} catch (AddressException e) {
					e.printStackTrace();
				}
			}
		}, 0, 7812, TimeUnit.MICROSECONDS);

		final ScheduledExecutorService schAuto3 = Executors.newScheduledThreadPool(1);
		schAuto3.scheduleAtFixedRate(new Runnable() {

			private int counter = 0;

			@Override
			public void run() {
				try {
					if (auto3) {
						nes.tick();
						counter = ++counter % 5;
						if (counter == 0)
							update();
					}
				} catch (AddressException e) {
					e.printStackTrace();
				}
			}
		}, 0, 1953125, TimeUnit.NANOSECONDS);

		final ScheduledExecutorService schAuto4 = Executors.newScheduledThreadPool(1);
		schAuto4.scheduleAtFixedRate(new Runnable() {

			private int counter = 0;

			@Override
			public void run() {
				try {
					if (auto4) {
						nes.tick();
						counter = ++counter % 5;
						if (counter == 0)
							update();
					}
				} catch (AddressException e) {
					e.printStackTrace();
				}
			}
		}, 0, 488281, TimeUnit.NANOSECONDS);

		final ScheduledExecutorService schAuto5 = Executors.newScheduledThreadPool(1);
		schAuto5.scheduleAtFixedRate(new Runnable() {

			private int counter = 0;

			@Override
			public void run() {
				try {
					if (auto5) {
						nes.tick();
						counter = ++counter % 150000;
						if (counter == 0)
							update();
					}
				} catch (AddressException e) {
					e.printStackTrace();
				}
			}
		}, 0, 37, TimeUnit.NANOSECONDS);

		this.setVisible(false);
	}

	private void update() throws AddressException {

		/***** Flags *****/

		n.setText(String.valueOf((cpuRegistres.getP() & 0b10000000) >> 7));
		v.setText(String.valueOf((cpuRegistres.getP() & 0b01000000) >> 6));
		b.setText(String.valueOf((cpuRegistres.getP() & 0b00110000) >> 4));
		d.setText(String.valueOf((cpuRegistres.getP() & 0b00001000) >> 3));
		i.setText(String.valueOf((cpuRegistres.getP() & 0b00000100) >> 2));
		z.setText(String.valueOf((cpuRegistres.getP() & 0b00000010) >> 1));
		c.setText(String.valueOf(cpuRegistres.getP() & 0b00000001));

		/***** CPU registers *****/

		a.setText(String.format("0x%02X", cpuRegistres.getA()));
		x.setText(String.format("0x%02X", cpuRegistres.getX()));
		y.setText(String.format("0x%02X", cpuRegistres.getY()));
		sp.setText(String.format("0x%04X", cpuRegistres.getSp()));
		pc.setText(String.format("0x%04X", cpuRegistres.getPc()));

		/***** External registers *****/

		PPUCTRL.setText(
				String.format("0b%8s", Integer.toBinaryString(ppuRegistres.getExternalRegisters().getPPUCTRL() & 0xFF))
						.replace(' ', '0'));
		PPUMASK.setText(
				String.format("0b%8s", Integer.toBinaryString(ppuRegistres.getExternalRegisters().getPPUMASK() & 0xFF))
						.replace(' ', '0'));
		PPUSTATUS.setText(String
				.format("0b%8s", Integer.toBinaryString(ppuRegistres.getExternalRegisters().getPPUSTATUS() & 0xFF))
				.replace(' ', '0'));
		OAMADDR.setText(
				String.format("0b%8s", Integer.toBinaryString(ppuRegistres.getExternalRegisters().getOAMADDR() & 0xFF))
						.replace(' ', '0'));
		OAMDATA.setText(
				String.format("0b%8s", Integer.toBinaryString(ppuRegistres.getExternalRegisters().getOAMDATA() & 0xFF))
						.replace(' ', '0'));
		PPUSCROLL.setText(String
				.format("0b%8s", Integer.toBinaryString(ppuRegistres.getExternalRegisters().getPPUSCROLL() & 0xFF))
				.replace(' ', '0'));
		PPUADDR.setText(
				String.format("0b%8s", Integer.toBinaryString(ppuRegistres.getExternalRegisters().getPPUADDR() & 0xFF))
						.replace(' ', '0'));
		PPUDATA.setText(
				String.format("0b%8s", Integer.toBinaryString(ppuRegistres.getExternalRegisters().getPPUDATA() & 0xFF))
						.replace(' ', '0'));
		OAMDMA.setText(
				String.format("0b%8s", Integer.toBinaryString(ppuRegistres.getExternalRegisters().getOAMDMA() & 0xFF))
						.replace(' ', '0'));

		/***** Background registers *****/

		ppuV.setText(
				String.format("0b%15s", Integer.toBinaryString(ppuRegistres.getBackgroundRegisters().getV() & 0xFFFF))
						.replace(' ', '0'));
		ppuT.setText(
				String.format("0b%15s", Integer.toBinaryString(ppuRegistres.getBackgroundRegisters().getT() & 0xFFFF))
						.replace(' ', '0'));

		ppuX.setText(String.format("0b%3s", Integer.toBinaryString(ppuRegistres.getBackgroundRegisters().getX() & 0x3))
				.replace(' ', '0'));

		ppuW.setText(String.format("0b%1s", Integer.toBinaryString(ppuRegistres.getBackgroundRegisters().getW() & 0x1))
				.replace(' ', '0'));

		scanline.setText(String.format("%d", ppu.getScanline()));
		cycle.setText(String.format("%d", ppu.getCycle()));

		/***** Next Tiles *****/

		int x = ppuRegistres.getBackgroundRegisters().getV() << 3 | ppuRegistres.getBackgroundRegisters().getX();
		int y = (ppuRegistres.getBackgroundRegisters().getV() & 0b1111100000) >> 5
				| ppuRegistres.getBackgroundRegisters().getV() >> 12;

		int[] colors = new int[64];
		int universalColor = NesColors.getColorCode(ppu.getBus().getByteFromMemory(0x3F00)).getRGBFromCode();
		Tile tile = ppuRegistres.getBackgroundRegisters().getTile1();

		int paletteNumber = tile.getAttributeTable() >> (2 * ((x / 16) % 2) + 4 * ((y / 16) % 2)) & 0b00000011;
		int paletteAddress = 0x3F01 + 4 * paletteNumber;

		for (int row = 0; row < 8; row++) {
			for (int column = 0; column < 8; column++) {
				int pattern = tile.getPatternTable()[row][column];
				if (pattern == 0)
					for (int i = 0; i < 64; i++)
						colors[i] = universalColor;

				else
					for (int i = 0; i < 64; i++)
						colors[i] = NesColors.getColorCode(ppu.getBus().getByteFromMemory(paletteAddress + pattern))
								.getRGBFromCode();

				tile1.setRGB(column * 8, row * 8, 8, 8, colors, 0, 0);
			}
		}

		/***** CPU instructions *****/

		int index = 0;
		Instruction instruction = instructionMap.get(cpuRegistres.getPc());
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
		
		cpuTable.repaint();
		ppuTable.repaint();
	}

	private void init() throws AddressException {
		update();
	}

	private void nextPPUStep() throws AddressException {
		nes.nextPPUTick();
	}

	private void nextCPUStep() throws AddressException {
		nes.nextCPUTick();
	}

	private void nextVBlank() throws AddressException {
		System.out.println("To next VBLANK...");
		while (ppuRegistres.getExternalRegisters().getPPUSTATUS() >= 0) {
			nes.tick();
		}
		update();
		System.out.println("VBLANK reached");
	}

	private void manual() throws InterruptedException {
		auto1 = false;
		auto2 = false;
		auto3 = false;
		auto4 = false;
		auto5 = false;
	}

	private void automatic(int speed) throws InterruptedException {
		switch (speed) {
		case 1:
			auto1 = true;
			auto2 = false;
			auto3 = false;
			auto4 = false;
			auto5 = false;
			break;

		case 2:
			auto1 = false;
			auto2 = true;
			auto3 = false;
			auto4 = false;
			auto5 = false;
			break;

		case 3:
			auto1 = false;
			auto2 = false;
			auto3 = true;
			auto4 = false;
			auto5 = false;
			break;

		case 4:
			auto1 = false;
			auto2 = false;
			auto3 = false;
			auto4 = true;
			auto5 = false;
			break;

		default:
			auto1 = false;
			auto2 = false;
			auto3 = false;
			auto4 = false;
			auto5 = true;
			break;
		}
	}

	private JPanel buildFlagPanel() {

		/***** Flags *****/

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

		flagPanel.setBorder(BorderFactory.createTitledBorder("Flags"));

		return flagPanel;
	}

	private JPanel buildInstructionAndRegPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		/***** CPU registers *****/

		JPanel cpuRegistres = new JPanel();
		cpuRegistres.setLayout(new GridLayout(5, 1, 5, 5));

		JLabel aLabel = new JLabel("A");
		cpuRegistres.add(aLabel);
		a = new JLabel("0");
		cpuRegistres.add(a);

		JLabel xLabel = new JLabel("X");
		cpuRegistres.add(xLabel);
		x = new JLabel("0");
		cpuRegistres.add(x);

		JLabel yLabel = new JLabel("Y");
		cpuRegistres.add(yLabel);
		y = new JLabel("0");
		cpuRegistres.add(y);

		JLabel spLabel = new JLabel("SP");
		cpuRegistres.add(spLabel);
		sp = new JLabel("0");
		cpuRegistres.add(sp);

		JLabel pcLabel = new JLabel("PC");
		cpuRegistres.add(pcLabel);
		pc = new JLabel("0");
		cpuRegistres.add(pc);

		cpuRegistres.setBorder(BorderFactory.createTitledBorder("cpuRegistres"));

		/***** CPU instructions *****/

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

		panel.add(cpuRegistres);
		panel.add(instructionPanel);

		return panel;
	}

	private JPanel buildPPURegistersPanel() {
		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		/***** External registers *****/

		JPanel external = new JPanel();
		external.setLayout(new GridLayout(9, 2));

		JLabel label2000 = new JLabel("$2000");
		PPUCTRL = new JLabel("0");
		external.add(label2000);
		external.add(PPUCTRL);

		JLabel label2001 = new JLabel("$2001");
		PPUMASK = new JLabel("0");
		external.add(label2001);
		external.add(PPUMASK);

		JLabel label2002 = new JLabel("$2002");
		PPUSTATUS = new JLabel("0");
		external.add(label2002);
		external.add(PPUSTATUS);

		JLabel label2003 = new JLabel("$2003");
		OAMADDR = new JLabel("0");
		external.add(label2003);
		external.add(OAMADDR);

		JLabel label2004 = new JLabel("$2004");
		OAMDATA = new JLabel("0");
		external.add(label2004);
		external.add(OAMDATA);

		JLabel label2005 = new JLabel("$2005");
		PPUSCROLL = new JLabel("0");
		external.add(label2005);
		external.add(PPUSCROLL);

		JLabel label2006 = new JLabel("$2006");
		PPUADDR = new JLabel("0");
		external.add(label2006);
		external.add(PPUADDR);

		JLabel label2007 = new JLabel("$2007");
		PPUDATA = new JLabel("0");
		external.add(label2007);
		external.add(PPUDATA);

		JLabel label4014 = new JLabel("$4014");
		OAMDMA = new JLabel("0");
		external.add(label4014);
		external.add(OAMDMA);

		external.setBorder(BorderFactory.createTitledBorder("External Registers"));

		/***** Background registers *****/

		JPanel background = new JPanel();
		background.setLayout(new GridLayout(6, 2));

		JLabel v = new JLabel("V");
		ppuV = new JLabel("0");
		background.add(v);
		background.add(ppuV);

		JLabel t = new JLabel("T");
		ppuT = new JLabel("0");
		background.add(t);
		background.add(ppuT);

		JLabel x = new JLabel("X");
		ppuX = new JLabel("0");
		background.add(x);
		background.add(ppuX);

		JLabel w = new JLabel("W");
		ppuW = new JLabel("0");
		background.add(w);
		background.add(ppuW);

		JLabel scanLabel = new JLabel("Scanline");
		scanline = new JLabel("0");
		background.add(scanLabel);
		background.add(scanline);

		JLabel cycleLabel = new JLabel("Cycle");
		cycle = new JLabel("0");
		background.add(cycleLabel);
		background.add(cycle);

		/***** Tiles *****/

		JPanel tile = new JPanel();
		tile.setLayout(new GridLayout(2, 2));

		JLabel titleTile1 = new JLabel("Tile 1");
		tile1 = new BufferedImage(64, 64, BufferedImage.TYPE_3BYTE_BGR);
		tileLabel1 = new JLabel(new ImageIcon(tile1));

		JLabel titleTile2 = new JLabel("Tile 2");
		tile2 = new BufferedImage(64, 64, BufferedImage.TYPE_3BYTE_BGR);
		tileLabel2 = new JLabel(new ImageIcon(tile2));

		tile.add(titleTile1);
		tile.add(tileLabel1);

		tile.add(titleTile2);
		tile.add(tileLabel2);

		background.setBorder(BorderFactory.createTitledBorder("Background Registers"));
		tile.setBorder(BorderFactory.createTitledBorder("Next tiles"));

		panel.add(external);
		panel.add(background);
		panel.add(tile);

		JPanel palettes = new JPanel();
		palettes.setLayout(new GridLayout(2, 4));

		return panel;
	}

	private JPanel buildBusPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		/***** CPU bus *****/
		JPanel cpuPanel = new JPanel();

		cpuBusContent = new String[cpu.getBus().getSize()][2];
		byte[] data = cpu.getBus().getValues();
		for (int row = 0; row < data.length; row++) {
			cpuBusContent[row][0] = String.format("0x%04X", row);
			cpuBusContent[row][1] = String.format("0x%02X", data[row]);
		}
		cpuTable = new JTable(cpuBusContent, new String[] { "Address", "Value" });
		cpuTable.setMinimumSize(cpuTable.getPreferredSize());
		cpuTable.setEnabled(false);
		cpuPanel.add(cpuTable);

		JScrollPane cpuScrollPanel = new JScrollPane(cpuPanel);
		cpuScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		cpuScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		cpuScrollPanel.getVerticalScrollBar().setUnitIncrement(32);

		/***** PPU bus *****/

		JPanel ppuPanel = new JPanel();

		ppuBusContent = new String[ppu.getBus().getSize()][2];
		data = ppu.getBus().getValues();
		for (int row = 0; row < data.length; row++) {
			ppuBusContent[row][0] = String.format("0x%04X", row);
			ppuBusContent[row][1] = String.format("0x%02X", data[row]);
		}
		ppuTable = new JTable(ppuBusContent, new String[] { "Address", "Value" });
		ppuTable.setEnabled(false);
		ppuPanel.add(ppuTable);

		JScrollPane ppuScrollPanel = new JScrollPane(ppuPanel);
		ppuScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		ppuScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		ppuScrollPanel.getVerticalScrollBar().setUnitIncrement(32);

		panel.add(cpuScrollPanel);
		panel.add(ppuScrollPanel);
		panel.setBorder(BorderFactory.createTitledBorder("Components' buses"));
		return panel;
	}

	public void showFrame() throws AddressException {
		init();
		this.setVisible(true);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_SPACE:
			try {
				nextPPUStep();
			} catch (AddressException e1) {
				e1.printStackTrace();
			}
			break;

		case KeyEvent.VK_ENTER:
			try {
				nextCPUStep();
			} catch (AddressException e1) {
				e1.printStackTrace();
			}
			break;

		case KeyEvent.VK_V:
			try {
				nextVBlank();
			} catch (AddressException e1) {
				e1.printStackTrace();
			}
			break;

		case KeyEvent.VK_NUMPAD0:
		case KeyEvent.VK_0:
			try {
				manual();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			break;

		case KeyEvent.VK_NUMPAD1:
		case KeyEvent.VK_1:
			try {
				automatic(1);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			break;

		case KeyEvent.VK_NUMPAD2:
		case KeyEvent.VK_2:
			try {
				automatic(2);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			break;

		case KeyEvent.VK_NUMPAD3:
		case KeyEvent.VK_3:
			try {
				automatic(3);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			break;

		case KeyEvent.VK_NUMPAD4:
		case KeyEvent.VK_4:
			try {
				automatic(4);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			break;

		case KeyEvent.VK_NUMPAD5:
		case KeyEvent.VK_5:
			try {
				automatic(5);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			break;
		default:
			break;
		}
		try {
			update();
		} catch (AddressException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void onValueChanged(boolean isCpuBus, int address, byte value) {
		if (isCpuBus)
			cpuBusContent[address][1] = String.format("0x%02X", value);
		else
			ppuBusContent[address][1] = String.format("0x%02X", value);

	}

	public static void main(String[] args)
			throws NotNesFileException, IOException, InstructionException, AddressException, MapperException {
		CPU cpu = new CPU();
		PPU ppu = new PPU();

		NES nes = new NES(cpu, ppu, new File("./Super Mario Bros.nes"));
		nes.start();

		FullRegisterFrame frame = new FullRegisterFrame(nes, cpu, ppu);
		frame.showFrame();

	}

}
