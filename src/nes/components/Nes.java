package components;

import java.io.IOException;

import components.bus.CpuBus;
import components.bus.MmioBus;
import components.bus.PpuBus;
import components.dma.OamDma;
import components.register.CpuInfo;
import components.register.IoRegisters;
import components.register.PpuInfo;
import exceptions.ComponentCheckException;
import exceptions.InstructionNotSupportedException;
import exceptions.NotNesFileException;

public class Nes {

	public Nes() {

	}

	public static void main(String[] args)
			throws NotNesFileException, IOException, InstructionNotSupportedException, ComponentCheckException {
		// Create all components
		PpuInfo ppuInfo = new PpuInfo();
		PpuBus ppuBus = new PpuBus();
		MmioBus mmioBus = new MmioBus();

		CpuInfo cpuInfo = new CpuInfo();
		CpuBus cpuBus = new CpuBus();

		IoRegisters ioRegisters = new IoRegisters();

		NmiFlipFlop nmiFlipFlop = new NmiFlipFlop();

		OamDma oamDma = new OamDma();

		Ppu ppu = new Ppu();
		Cpu cpu = new Cpu();

		// Link components
		cpu.setCpuInfo(cpuInfo);
		cpu.setBus(cpuBus);
		cpu.setNmiFlipFlop(nmiFlipFlop);
		cpuBus.setPpuMmioBus(mmioBus);
		cpuBus.setIoRegisters(ioRegisters);

		ppu.setPpuInfo(ppuInfo);
		ppu.setBus(ppuBus);
		ppu.setMmioBus(mmioBus);
		mmioBus.setPpuInfo(ppuInfo);
		mmioBus.setNmiFlipFlop(nmiFlipFlop);

		oamDma.setSource(cpuBus);
		oamDma.setDestination(cpuBus);

		ioRegisters.setOamDma(oamDma);

		// Check all components
		ppuInfo.check();
		ppuBus.check();
		mmioBus.check();
		cpuInfo.check();
		cpuBus.check();
		ioRegisters.check();
		nmiFlipFlop.check();
		oamDma.check();
		cpu.check();
		ppu.check();

		// OAM DMA test
		for (int address = 0; address < 0x800; address++) {
			cpu.writeMemory(address, ((address & 0xFF) * (address >> 8)) & 0xFF);
		}
		
		System.out.println("Should print only 0");
		cpu.writeMemory(IoRegisters.OAMDMA_ADDR, 0);
		while (oamDma.isRunning());
		
		
		System.out.println("\nShould print numbers 0 to 255");
		cpu.writeMemory(IoRegisters.OAMDMA_ADDR, 1);
		while (oamDma.isRunning());
		
		System.out.println("\nShould print numbers 2 by 2");
		cpu.writeMemory(IoRegisters.OAMDMA_ADDR, 2);
		while (oamDma.isRunning());
		
		// Create frame

//		Cartridge cartridge = new Cartridge("./Donkey Kong.nes");
//		cartridge.loadGame();
		// TODO Remove below when tests over
//		File nesFile = new File("./Donkey Kong.nes");
//		Disassembler disassembler = new Disassembler();
//		DisassemblyInfo info = disassembler.disassembleFile(nesFile);

//		final Cpu cpu = Cpu.getInstance();
//		final Ppu ppu = Ppu.getInstance();

//		Mapper mapper = new Mapper0(info.getPrgRom(), info.getChrRom());

//		cpu.setMapper(mapper);
//		Instruction[] romInstructions = info.getInstructions();
//		if (info.getInstructions().length != 0x8000) {
//			
//			Instruction[] instructions = new Instruction[0x8000];
//			for (int instructionNumber = 0; instructionNumber < 0x8000; instructionNumber++) {
//				instructions[instructionNumber] = romInstructions[instructionNumber % 0x4000];
//			}
//			cpu.setRomInstructions(instructions);
//		} else {			
//			cpu.setRomInstructions(romInstructions);
//		}
//		cpu.warmUp();

//		ppu.setMapper(mapper);
//		
//		Random r = new Random();
//		for (int address = 0; address < 0x800; address++) {
//			mapper.writeCpuBus(address, r.nextInt(0x100));
//		}

//		// Palette colors
//		cpu.storeMemory(0x2006, 0x3F);
//		cpu.storeMemory(0x2006, 0x00);
//
//		// Palette 0
//		cpu.storeMemory(0x2007, 0x03);
//		cpu.storeMemory(0x2007, 0x04);
//		cpu.storeMemory(0x2007, 0x05);
//		cpu.storeMemory(0x2007, 0x06);
//
//		// Palette 1
//		cpu.storeMemory(0x2007, 0x21);
//		cpu.storeMemory(0x2007, 0x17);
//		cpu.storeMemory(0x2007, 0x18);
//		cpu.storeMemory(0x2007, 0x19);
//
//		// Palette 2
//		cpu.storeMemory(0x2007, 0x0B);
//		cpu.storeMemory(0x2007, 0x1D);
//		cpu.storeMemory(0x2007, 0x2D);
//		cpu.storeMemory(0x2007, 0x3D);

//		GameFrame frame = new GameFrame(cartridge.getInstructions(), cartridge.getMapper());
//		ppu.setScreen(frame.getScreenPanel());
//		frame.initFrame(cartridge.getFileName());
	}
}
