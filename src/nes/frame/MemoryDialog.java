package frame;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import components.cpu.Cpu;
import components.ppu.Ppu;
import mapper.Mapper;

public class MemoryDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -123400624945240142L;
	private static final String[] COLUMN_NAMES = { "Address", "Value" };
	private static final int CPU_RAM_SIZE = 0x800;
	private static final int PPU_RAM_START = 0x2000;
	private static final int PPU_RAM_SIZE = 0x1000;
	private static final int PPU_PALETTES_START = 0x3F00;
	private static final int PPU_PALETTES_SIZE = 0x20;

	private final Cpu cpu = Cpu.getInstance();
	private final Ppu ppu = Ppu.getInstance();

	private Mapper mapper;

	// JTable for CPU and PPU memory
	private MemoryTableModel cpuMemoryModel;
	private MemoryTableModel ppuMemoryModel;

	private JTable cpuMemoryTable;
	private JTable ppuMemoryTable;

	public MemoryDialog(Mapper mapper) {
		this.setTitle("Memory dialog");
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.mapper = mapper;

		JPanel tablesPanel = new JPanel();
		tablesPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.BASELINE;
		c.gridheight = 1;
		c.gridwidth = 1;

		// Init CPU RAM table
		Object[][] cpuData = new Object[CPU_RAM_SIZE][2];
		for (int address = 0; address < CPU_RAM_SIZE; address++) {
			cpuData[address][0] = "$%04X".formatted(address);
			cpuData[address][1] = "$%02X".formatted(cpu.fetchMemory(address));
		}
		cpuMemoryModel = new MemoryTableModel(cpuData, COLUMN_NAMES);
		cpuMemoryTable = new JTable(cpuMemoryModel);
		JScrollPane cpuTablePanel = new JScrollPane(cpuMemoryTable);
		cpuTablePanel.setMinimumSize(cpuTablePanel.getPreferredSize());
		
		// Init PPU RAM table (nametables)
		Object[][] ppuData = new Object[PPU_RAM_SIZE + PPU_PALETTES_SIZE + 1][2];
		for (int address = 0; address < PPU_RAM_SIZE; address++) {
			ppuData[address][0] = "$%04X".formatted(address + PPU_RAM_START);
			ppuData[address][1] = "$%02X".formatted(ppu.fetchMemory(address + PPU_RAM_START));
		}
		// Insert empty line
		ppuData[PPU_RAM_SIZE][0] = "";
		ppuData[PPU_RAM_SIZE][1] = "";
		// Init PPU palettes table
		for (int address = 0; address < PPU_PALETTES_SIZE; address++) {
			ppuData[PPU_RAM_SIZE + address + 1][0] = "$%04X".formatted(address + PPU_PALETTES_START);
			ppuData[PPU_RAM_SIZE + address + 1][1] = "$%02X".formatted(ppu.fetchMemory(address + PPU_PALETTES_START));
		}

		ppuMemoryModel = new MemoryTableModel(ppuData, COLUMN_NAMES);
		ppuMemoryTable = new JTable(ppuMemoryModel);
		JScrollPane ppuTablePanel = new JScrollPane(ppuMemoryTable);
		ppuTablePanel.setMinimumSize(ppuTablePanel.getPreferredSize());
		
		c.gridx = 0;
		c.gridy = 0;
		tablesPanel.add(cpuTablePanel, c);
		c.gridx = 1;
		c.gridy = 0;
		tablesPanel.add(ppuTablePanel, c);

		this.setLayout(new GridBagLayout());
		this.add(tablesPanel);
		
		this.pack();
		this.setLocationRelativeTo(null);

		this.setVisible(false);
	}

	public void initDialog() {
		this.setVisible(true);
	}

	private static class MemoryTableModel extends AbstractTableModel {

		private Object[][] data;
		private String[] columnTitles;

		public MemoryTableModel(Object[][] data, String[] columnTitles) {
			this.data = data;
			this.columnTitles = columnTitles;
		}

		@Override
		public int getRowCount() {
			return data.length;
		}

		@Override
		public int getColumnCount() {
			return data[0].length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data[rowIndex][columnIndex];
		}

		@Override
		public String getColumnName(int column) {
			return columnTitles[column];
		}
	}
}
