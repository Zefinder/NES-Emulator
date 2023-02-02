package components;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import components.cpu.CPU;
import components.mapper.Mapper;
import components.mapper.Mapper0;
import components.ppu.PPU;
import decompile.Desassembler;
import exceptions.AddressException;
import exceptions.InstructionException;
import exceptions.MapperException;
import exceptions.NotNesFileException;
import instructions.Instruction;

public class NES implements Component {

	private final File romFile;

	private byte[] prgRom;
	private int mapperNumber;
	private int prgChunksNumber, chrChunksNumber;
	private int counter, cpuCounter;

	private Mapper mapper;
	private Component cpu, ppu;

	// A retirer après les tests
	private Map<Integer, Instruction> instructionMap;

	public NES(Component cpu, Component ppu, File romFile) {
		counter = 3;
		cpuCounter = 0;
		this.cpu = cpu;
		this.ppu = ppu;
		this.romFile = romFile;
	}

	@Override
	public void start()
			throws AddressException, IOException, InstructionException, NotNesFileException, MapperException {

		Desassembler des = new Desassembler(romFile);
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
	}

	@Override
	public void tick() throws AddressException {
		// Version PAL :
		// Un tick CPU après 16 ticks
		// Un tick PPU après 5 ticks

		// Donc un tick CPU après 3.2 ticks PPU
		// Soit un CPU après 3 PPU et 4 après 15

		ppu.tick();
		counter++;

		if (counter >= 3) {
			if (cpuCounter == 5)
				cpuCounter = 0;
			else {
				counter = 0;
				cpuCounter++;
				cpu.tick();
			}
		}
	}

	@Override
	public void reset() throws AddressException {
		cpu.reset();
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
		mapper = new Mapper0(prgRom, chrRom, instructionMap, verticalScrolling);
	}
	
	public Map<Integer, Instruction> getInstructionMap() {
		return instructionMap;
	}

	public static void main(String[] args)
			throws AddressException, IOException, InstructionException, NotNesFileException, MapperException {

		// Master cycle : 38ns
		// PPU cycle : 190ns
		// CPU cycle : 608ns

		CPU cpu = new CPU();
		PPU ppu = new PPU();

		NES nes = new NES(cpu, ppu, new File("./Super Mario Bros.nes"));
		nes.start();

		final ScheduledExecutorService sch = Executors.newScheduledThreadPool(1);

		sch.scheduleAtFixedRate(new Runnable() {

			private int counter = 0;
			private long debut = System.currentTimeMillis();
			private long fin;

			@Override
			public void run() {
				try {
					nes.tick();
					if (++counter == 5302550) {
						counter = 0;
						fin = System.currentTimeMillis();
						nes.framerate(debut, fin);
						debut = fin;
					}
				} catch (AddressException e) {
					e.printStackTrace();
				}
			}
		}, 0, 190, TimeUnit.NANOSECONDS);
	}

	private void framerate(long debut, long fin) {
		System.out.println("Framerate : " + 50000.0 / (fin - debut) + " tps");
	}

}
