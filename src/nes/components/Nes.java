package components;

import java.io.IOException;

import components.bus.CpuBus;
import components.bus.MmioBus;
import components.bus.PpuBus;
import components.dma.OamDma;
import components.register.CpuRegisters;
import components.register.IoRegisters;
import components.register.PpuRegisters;
import exceptions.ComponentCheckException;
import exceptions.InstructionNotSupportedException;
import exceptions.NotNesFileException;

public class Nes {

	public Nes() {

	}

	public static void main(String[] args)
			throws NotNesFileException, IOException, InstructionNotSupportedException, ComponentCheckException {
		// Create all components
		PpuRegisters ppuRegisters = new PpuRegisters();
		PpuBus ppuBus = new PpuBus();
		MmioBus mmioBus = new MmioBus();

		CpuRegisters cpuRegisters = new CpuRegisters();
		CpuBus cpuBus = new CpuBus();

		IoRegisters ioRegisters = new IoRegisters();

		NmiFlipFlop nmiFlipFlop = new NmiFlipFlop();

		OamDma oamDma = new OamDma();

		Ppu ppu = new Ppu();
		Cpu cpu = new Cpu();

		// Link components
		cpu.setCpuRegisters(cpuRegisters);
		cpu.setBus(cpuBus);
		cpu.setNmiFlipFlop(nmiFlipFlop);
		cpuBus.setPpuMmioBus(mmioBus);
		cpuBus.setIoRegisters(ioRegisters);

		ppu.setPpuRegisters(ppuRegisters);
		ppu.setBus(ppuBus);
		ppu.setMmioBus(mmioBus);
		
		mmioBus.setPpuBus(ppuBus);
		mmioBus.setPpuRegisters(ppuRegisters);
		mmioBus.setNmiFlipFlop(nmiFlipFlop);

		oamDma.setSource(cpuBus);
		oamDma.setDestination(cpuBus);

		ioRegisters.setOamDma(oamDma);

		// Check all components
		ppuRegisters.check();
		ppuBus.check();
		mmioBus.check();
		cpuRegisters.check();
		cpuBus.check();
		ioRegisters.check();
		nmiFlipFlop.check();
		oamDma.check();
		cpu.check();
		ppu.check();

		// MMIO Bus test (in palette because does not require cartridge)
		cpu.writeMemory(MmioBus.PPUADDR_ADDR, 0x3F);
		cpu.writeMemory(MmioBus.PPUADDR_ADDR, 0x02);
		cpu.writeMemory(MmioBus.PPUDATA_ADDR, 0x03);
		
		// Should output 0x03
		System.out.println("0x%02X".formatted(ppu.fetchMemory(0x3F02)));
		
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
