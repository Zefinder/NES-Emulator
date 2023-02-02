package components.ppu;

import components.Bus;
import components.Component;
import components.mapper.Mapper;
import components.ppu.bus.PPUBus;
import components.ppu.register.PPURegisters;
import components.ppu.rendering.PPURender;
import exceptions.AddressException;

public class PPU implements Component, Runnable {

	private Bus bus;
	private PPURegisters registres;
	private PPURender renderUnit;

	private Thread ppuThread;

	private int scanline, cycle;
	private boolean oddTick;

	private volatile boolean readyForNext;

	public PPU() {
		registres = new PPURegisters();
		bus = new PPUBus(0x4000);
		renderUnit = new PPURender();

		ppuThread = new Thread(this);
		ppuThread.setName("Thread PPU");
		ppuThread.start();

		scanline = 310;
		oddTick = false;
		readyForNext = true;
	}

	public void setHorizontalNametableMirroring() {
		// TODO Dire au bus que c'est en horizontal nametable
	}

	public void setVerticalNametableMirroring() {
		// TODO Dire au bus que c'est en vertical nametable
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
		boolean vertical = mapper.mapPPUMemory(bus);
		if (vertical)
			setVerticalNametableMirroring();
		else
			setHorizontalNametableMirroring();

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
					if (++scanline == 311)
						scanline = 0;
				}

				if (scanline == 310 && cycle == 341 && oddTick) {
					scanline = 0;
					cycle = 0;
				}

			}
		}
	}

}
