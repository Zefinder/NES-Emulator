package nes.components.ppu;

import nes.components.Bus;
import nes.components.Component;
import nes.components.mapper.Mapper;
import nes.components.ppu.bus.PPUBus;
import nes.components.ppu.register.PPURegisters;
import nes.components.ppu.rendering.PPURender;
import nes.exceptions.AddressException;

public class PPU implements Component, Runnable {

	private Bus bus;
	private PPURegisters registres;
	private PPURender renderUnit;
	
	private Thread ppuThread;

	private byte[] patternTable1;
	private byte[] patternTable2;
	private byte[] nametable0Begin;
	private byte[] nametable0End;
	private byte[] nametable1Begin;
	private byte[] nametable1End;
	private byte[] nametable2Begin;
	private byte[] nametable2End;
	private byte[] nametable3Begin;
	private byte[] nametable3End;
	private byte[] paletteIndexes;

	private int scanline, cycle;
	private boolean oddTick;

	private volatile boolean readyForNext;

	public PPU() {
		registres = new PPURegisters();
		bus = new PPUBus(registres);
		renderUnit = new PPURender();

		patternTable1 = new byte[0x1000];
		patternTable2 = new byte[0x1000];

		nametable0Begin = new byte[0x300];
		nametable0End = new byte[0x100];

		nametable1Begin = new byte[0x300];
		nametable1End = new byte[0x100];

		nametable2Begin = new byte[0x300];
		nametable2End = new byte[0x100];

		nametable3Begin = new byte[0x300];
		nametable3End = new byte[0x100];

		paletteIndexes = new byte[0x20];


		ppuThread = new Thread(this);
		ppuThread.start();
		
		scanline = 310;
		oddTick = false;
		readyForNext = true;

		System.out.println("Fraise");
	}

	public void setHorizontalNametableMirroring() {
		nametable1Begin = nametable0Begin;
		nametable1End = nametable0End;

		nametable3Begin = nametable2Begin;
		nametable3End = nametable2End;
	}

	public void setVerticalNametableMirroring() {
		nametable3Begin = nametable1Begin;
		nametable3End = nametable1End;

		nametable2Begin = nametable0Begin;
		nametable2End = nametable0End;
	}

	@Override
	public void start() {

	}

	@Override
	public void reset() {

	}

	@Override
	public void tick() throws AddressException {
//		System.out.println("PPU tick! ");
		while (!readyForNext) {
			// J'ai peut être honte d'écrire ça, oui
			// J'aurais pu utiliser une variable condition, oui xD
		}
  		readyForNext = false;
		
	}

	@Override
	public void initMapping(Mapper mapper) throws AddressException {
		// Ajout des pattern tables
		bus.addToMemoryMap(patternTable1);
		bus.addToMemoryMap(patternTable2);
		
		boolean vertical = mapper.mapPPUMemory(bus);
		if (vertical)
			setVerticalNametableMirroring();
		else
			setHorizontalNametableMirroring();

		// Ajout des nametables
		bus.addToMemoryMap(nametable0Begin);
		bus.addToMemoryMap(nametable0End);
		bus.addToMemoryMap(nametable1Begin);
		bus.addToMemoryMap(nametable1End);
		bus.addToMemoryMap(nametable2Begin);
		bus.addToMemoryMap(nametable2End);
		bus.addToMemoryMap(nametable3Begin);
		bus.addToMemoryMap(nametable3End);

		// Miroirs des nametables
		bus.addToMemoryMap(nametable0Begin);
		bus.addToMemoryMap(nametable0End);
		bus.addToMemoryMap(nametable1Begin);
		bus.addToMemoryMap(nametable1End);
		bus.addToMemoryMap(nametable2Begin);
		bus.addToMemoryMap(nametable2End);
		bus.addToMemoryMap(nametable3Begin);

		// Ajout des palettes et miroirs
		for (int i = 0; i < 8; i++)
			bus.addToMemoryMap(paletteIndexes);
	}

	public PPURegisters getRegistres() {
		return registres;
	}
	
	public Bus getBus() {
		return bus;
	}

	public int getScanline() {
		return scanline;
	}

	public int getCycle() {
		return cycle;
	}

	@Override
	public void run() {
		while (true) {
			if (!readyForNext) {
				readyForNext = true;
				try {
					renderUnit.render(scanline, cycle, registres, bus, oddTick);
				} catch (AddressException e) {
					e.printStackTrace();
				}
				cycle++;
				oddTick = !oddTick;

				if (cycle == 341) {
					cycle = 0;
					scanline = (++scanline) % 311;
				}

				if (scanline == 310 && cycle == 341 && oddTick) {
					scanline = 0;
					cycle = 0;
				}

			}
		}
	}

}
