package nes.components;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import nes.components.cpu.CPU;
import nes.components.mapper.Mapper;
import nes.components.mapper.Mapper0;
import nes.components.ppu.PPU;
import nes.decompil.Desassembler;
import nes.exceptions.AddressException;
import nes.exceptions.InstructionException;
import nes.exceptions.MapperException;
import nes.exceptions.NotNesFileException;
import nes.instructions.Instruction;

public class NES implements Component {

	private final File romFile;

	private byte[] prgRom;
	private int mapperNumber;
	private int prgChunksNumber, chrChunksNumber;
	private int masterTick, frame;

	private long debut, fin;

	private Mapper mapper;
	private Component cpu, ppu;

	// A retirer après les tests
	private Map<Integer, Instruction> instructionMap;

	private static ScheduledExecutorService sch;

	public NES(Component cpu, Component ppu, File romFile) {
		masterTick = 1;
		this.cpu = cpu;
		this.ppu = ppu;
		this.romFile = romFile;
		frame = 0;
	}

	@Override
	public void start()
			throws AddressException, IOException, InstructionException, NotNesFileException, MapperException {

		Desassembler des = new Desassembler(romFile);

		des.disassembleHeaderFlags();
		setRomSpecs(des.getPrgChunksNumber(), des.getChrChunksNumber(), des.getMapper());

		System.out.println("Mapper to use: " + mapperNumber);

		des.disassemble(0x8000);
		instructionMap = des.getInstructionList();
		fillPrgRom(des.getPrgRom());

		setMapper(des.getPrgRom(), des.getChrRom(), des.getMirroring());
		if (getMapperNumber() != 0)
			throw new MapperException("Mapper isn't implemented yet!");

		initMapping(mapper);
		System.out.println("No problem with the ROM, all was mapped!");

		Screen screen = new Screen(romFile.getName());
		screen.connectScreen();
		
		cpu.start();
		ppu.start();

		debut = System.currentTimeMillis();
//		nano = System.nanoTime();

	}

	@Override
	public void tick() throws AddressException {
		// Version PAL :
		// Un tick CPU après 16 ticks
		// Un tick PPU après 5 ticks

//		System.out.printf("Master tick : %d ", masterTick);

		// FIXME Optimiser le CPU et le PPU
		if (masterTick % 16 == 0)
			cpu.tick();

		if (masterTick % 5 == 0)
			ppu.tick();

//		System.out.println();
//		masterTick = (++masterTick < 80 ? masterTick : 0);
//		masterTick++;
//		if (masterTick == 26512750) {
//			fin = System.currentTimeMillis();
//			System.out.println("Framerate : " + 50000.0 / (fin - debut) + " fps");
//			debut = System.currentTimeMillis();
//			masterTick = 0;
//		}

		if (++masterTick % 530255 == 0) {
			if (++frame == 50) {
				frame = 0;
				fin = System.currentTimeMillis();
				System.out.println("Framerate : " + 50000.0 / (fin - debut) + " fps");
				debut = System.currentTimeMillis();
			}
			masterTick = 0;
		}

	}

	@Override
	public void reset() throws AddressException {
		cpu.reset();
	}

	// A enlever après les tests
	public static void killNes() {
		sch.shutdown();
	}

	@Override
	public void initMapping(Mapper mapper) throws AddressException {
		cpu.initMapping(mapper);
		ppu.initMapping(mapper);
	}

	public void setRomSpecs(int prgChunksNumber, int chrChunksNumber, int mapper) {
		this.prgChunksNumber = prgChunksNumber;
		this.chrChunksNumber = chrChunksNumber;
		this.mapperNumber = mapper;
	}

	public int getMapperNumber() {
		return mapperNumber;
	}

	public int getPrgChunksNumber() {
		return prgChunksNumber;
	}

	public int getChrChunksNumber() {
		return chrChunksNumber;
	}

	public void fillPrgRom(byte[] prgRom) {
		this.prgRom = prgRom;
	}

	public byte[] getPrgRom() {
		return prgRom;
	}

	public Mapper getMapper() {
		return mapper;
	}

	public Component getCPU() {
		return cpu;
	}

	public void setMapper(byte[] prgRom, byte[] chrRom, boolean verticalScrolling) {
		// Pour l'instant un seul mapper
		mapper = new Mapper0(prgRom, chrRom, verticalScrolling);
	}

	public static void main(String[] args)
			throws AddressException, IOException, InstructionException, NotNesFileException, MapperException {
		CPU cpu = new CPU();
		PPU ppu = new PPU();

		NES nes = new NES(cpu, ppu, new File("./Super Mario Bros.nes"));
		nes.start();

//		final ScheduledExecutorService sch = Executors.newScheduledThreadPool(1);
//		sch = Executors.newScheduledThreadPool(1);
//		sch.scheduleAtFixedRate(new Runnable() {
//
////			private int counter = 0;
//
//			@Override
//			public void run() {
//				try {
//					nes.tick();
//				} catch (AddressException e) {
//					e.printStackTrace();
//				}
//			}
//		}, 0, 10, TimeUnit.NANOSECONDS);
//		byte a = (byte) 0x80;
//		byte b = (byte) 0xFF;
//		byte c = (byte) (a + b);
//		System.out.println(String.format("0x%04x", c));

		while (true) {
//			if(System.nanoTime() - nano >= 32) {
			nes.tick();
//				nano = System.nanoTime();
//			}

		}
	}

	// A SUPPRIMER APRES LES TESTS
	public void nextCPUTick() throws AddressException {
		do {
			tick();
		} while (masterTick % 16 != 0);
			tick();
	}

	public void nextPPUTick() throws AddressException {
		do {
			tick();
		} while (masterTick % 5 != 0);
		tick();
	}

	public Map<Integer, Instruction> getInstructionMap() {
		return instructionMap;
	}

}
