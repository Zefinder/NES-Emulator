package tests;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nes.components.Bus;
import nes.components.cpu.bus.CPUBus;
import nes.components.cpu.register.CPURegisters;
import nes.exceptions.AddressException;
import nes.instructions.Instruction;
import nes.instructions.Instruction.AddressingMode;
import nes.instructions.Instruction.InstructionSet;
import nes.instructions.InstructionReader;

class InstructionsTest {

	private static CPURegisters registres;

	// Liste des constituants du bus
	private static byte[] memory;
	private static byte[] ppuRegisters;
	private static byte[] apuIORegisters;
	private static byte[] testMode;
	private static byte[] catridge;

	private static Bus bus;
	private static InstructionReader instructionReader;

	private Instruction instruction;

	@BeforeAll
	public static void init() throws AddressException {
		registres = new CPURegisters();

		memory = new byte[0x800];
		ppuRegisters = new byte[0x08];
		apuIORegisters = new byte[0x18];
		testMode = new byte[0x08];
		catridge = new byte[0xBFE0];

		bus = new CPUBus();

		// Mémoire et ses réflexions
		bus.addToMemoryMap(memory);
		bus.addToMemoryMap(memory);
		bus.addToMemoryMap(memory);
		bus.addToMemoryMap(memory);

		// Registres PPU et ses réflexions
		for (int i = 0x2007; i <= 0x3FFF; i = i + 0x08) {
			bus.addToMemoryMap(ppuRegisters);
		}

		// APU et les registres IO
		bus.addToMemoryMap(apuIORegisters);

		// APU et Registres IO non utilisés (test mode)
		bus.addToMemoryMap(testMode);

		// Cartouche
		bus.addToMemoryMap(catridge);

		instructionReader = new InstructionReader(bus);

	}

	@BeforeEach
	public void initRegisters() throws AddressException {
		registres.setA((byte) 0);
		registres.setY((byte) 0);
		registres.setX((byte) 0);
		registres.setP((byte) 0);
		registres.setSp(0x1FF);
		registres.setPc(0);

		// On modifie la mémoire
		bus.setByteToMemory(0x40, (byte) 0x06);

		bus.setByteToMemory(0x86, (byte) 0x4A);
		bus.setByteToMemory(0x87, (byte) 0x26);

		bus.setByteToMemory(0xAB, (byte) 0x01);

		bus.setByteToMemory(0x4FEE, (byte) 0xFF);
		bus.setByteToMemory(0x4FEF, (byte) 0x26);

		bus.setByteToMemory(0x264A, (byte) 0x40);
		bus.setByteToMemory(0x26FF, (byte) 0x10);

		bus.setByteToMemory(0xFFFE, (byte) 0x66);
		bus.setByteToMemory(0xFFFF, (byte) 0xF6);
	}

	// Adressages

	@Test
	public void accumulatorAddressing() throws AddressException {
		registres.setA((byte) 10);
		Instruction instruction = new Instruction(InstructionSet.NOP, AddressingMode.ACCUMULATOR);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 10);

		registres.setA((byte) 0x50);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x50);

		registres.setA((byte) 0xFE);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0xFE);
	}

	@Test
	public void immediateAddressing() throws AddressException {
		Instruction instruction = new Instruction(InstructionSet.NOP, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x10, (byte) 0);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x10);

		instruction.setArgument((byte) 0x80, (byte) 0);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x80);

		instruction.setArgument((byte) 0xFF, (byte) 0);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0xFF);
	}

	@Test
	public void zeropageAddressing() throws AddressException {
		Instruction instruction = new Instruction(InstructionSet.NOP, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x06);

		instruction.setArgument((byte) 0x86, (byte) 0);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x4A);

		instruction.setArgument((byte) 0x87, (byte) 0);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x26);
	}

	@Test
	public void zeropageXAddressing() throws AddressException {
		Instruction instruction = new Instruction(InstructionSet.NOP, AddressingMode.ZEROPAGE_X);
		registres.setX((byte) 0xFF);
		instruction.setArgument((byte) 0x41, (byte) 0); // 0x41 + 0xFF = 0x140 => 0x40
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x06);

		registres.setX((byte) 0x10);
		instruction.setArgument((byte) 0x76, (byte) 0);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x4A);

		registres.setX((byte) 0x20);
		instruction.setArgument((byte) 0x67, (byte) 0);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x26);
	}

	@Test
	public void zeropageYAddressing() throws AddressException {
		Instruction instruction = new Instruction(InstructionSet.NOP, AddressingMode.ZEROPAGE_Y);
		registres.setY((byte) 0xFF);
		instruction.setArgument((byte) 0x41, (byte) 0); // 0x41 + 0xFF = 0x140 => 0x40
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x06);

		registres.setY((byte) 0x10);
		instruction.setArgument((byte) 0x76, (byte) 0);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x4A);

		registres.setY((byte) 0x20);
		instruction.setArgument((byte) 0x67, (byte) 0);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x26);
	}

	@Test
	public void relativeAddressing() throws AddressException {
		Instruction instruction = new Instruction(InstructionSet.NOP, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0x10, (byte) 0);
		registres.setPc(0x4010);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[1] == 0x4020);

		instruction.setArgument((byte) 0x80, (byte) 0);
		registres.setPc(0x4020);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[1] == 0x3FA0);

		instruction.setArgument((byte) 0xFF, (byte) 0);
		registres.setPc(0x3FA0);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[1] == 0x3F9F);
	}

	@Test
	public void absoluteAddressing() throws AddressException {
		Instruction instruction = new Instruction(InstructionSet.NOP, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0xEE, (byte) 0x4F);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0xFF);

		instruction.setArgument((byte) 0xEF, (byte) 0x4F);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x26);

		instruction.setArgument((byte) 0x4A, (byte) 0x26);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x40);

		instruction.setArgument((byte) 0xFF, (byte) 0x26);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x10);
	}

	@Test
	public void absoluteXAddressing() throws AddressException {
		Instruction instruction = new Instruction(InstructionSet.NOP, AddressingMode.ABSOLUTE_X);
		registres.setX((byte) 0x10);
		instruction.setArgument((byte) 0xDE, (byte) 0x4F);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0xFF);

		registres.setX((byte) 0x2F);
		instruction.setArgument((byte) 0xC0, (byte) 0x4F);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x26);

		registres.setX((byte) 0xFF);
		instruction.setArgument((byte) 0x4B, (byte) 0x25); // 0x254B + 0xFF = 0x264A
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x40);

		registres.setX((byte) 0xF0);
		instruction.setArgument((byte) 0x0F, (byte) 0x26);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x10);
	}

	@Test
	public void absoluteYAddressing() throws AddressException {
		Instruction instruction = new Instruction(InstructionSet.NOP, AddressingMode.ABSOLUTE_Y);
		registres.setY((byte) 0x10);
		instruction.setArgument((byte) 0xDE, (byte) 0x4F);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0xFF);

		registres.setY((byte) 0x2F);
		instruction.setArgument((byte) 0xC0, (byte) 0x4F);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x26);

		registres.setY((byte) 0xFF);
		instruction.setArgument((byte) 0x4B, (byte) 0x25); // 0x254B + 0xFF = 0x264A
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x40);

		registres.setY((byte) 0xF0);
		instruction.setArgument((byte) 0x0F, (byte) 0x26);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x10);
	}

	@Test
	public void indirectAddressing() throws AddressException {
		Instruction instruction = new Instruction(InstructionSet.NOP, AddressingMode.INDIRECT);
		instruction.setArgument((byte) 0x86, (byte) 0x00);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x40);

		instruction.setArgument((byte) 0xEE, (byte) 0x4F);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x10);
	}

	@Test
	public void indirectXAddressing() throws AddressException {
		Instruction instruction = new Instruction(InstructionSet.NOP, AddressingMode.INDIRECT_X);
		registres.setX((byte) 0x10);
		instruction.setArgument((byte) 0x76, (byte) 0x00);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x40);

		registres.setX((byte) 0xFF);
		instruction.setArgument((byte) 0x87, (byte) 0x00);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x40);

	}

	@Test
	public void indirectYAddressing() throws AddressException {
		Instruction instruction = new Instruction(InstructionSet.NOP, AddressingMode.INDIRECT_Y);
		registres.setY((byte) 0xB5);
		instruction.setArgument((byte) 0x86, (byte) 0x00);
		assertTrue(instructionReader.getOperandAndAddress(instruction, registres.getA(), registres.getX(),
				registres.getY(), registres.getPc())[0] == (byte) 0x10);
	}

	// ADC
	@Test
	public void adcImmediate() throws AddressException {
		registres.setA((byte) 20);

		instruction = new Instruction(InstructionSet.ADC, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 20, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 40);

		instruction.setArgument((byte) 20, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 60);
	}

	@Test
	public void adcZeropage() throws AddressException {
		registres.setA((byte) 20);

		instruction = new Instruction(InstructionSet.ADC, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 26);

		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 32);
	}

	@Test
	public void adcZeropageX() throws AddressException {
		registres.setA((byte) 20);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.ADC, AddressingMode.ZEROPAGE_X);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 26);

		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 32);
	}

	@Test
	public void adcAbsolute() throws AddressException {
		registres.setA((byte) 0x20);

		instruction = new Instruction(InstructionSet.ADC, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0x4A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x60);

		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x66);
	}

	@Test
	public void adcAbsoluteX() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.ADC, AddressingMode.ABSOLUTE_X);
		instruction.setArgument((byte) 0xEF, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x30);

		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x36);
	}

	@Test
	public void adcAbsoluteY() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setY((byte) 0x10);

		instruction = new Instruction(InstructionSet.ADC, AddressingMode.ABSOLUTE_Y);
		instruction.setArgument((byte) 0xEF, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x30);

		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x36);
	}

	@Test
	public void adcIndirectX() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.ADC, AddressingMode.INDIRECT_X);
		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x60);

		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0xA0);
	}

	@Test
	public void adcIndirectY() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setY((byte) 0xB5);

		instruction = new Instruction(InstructionSet.ADC, AddressingMode.INDIRECT_Y);
		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x30);

		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x40);
	}

	@Test
	public void adcCFlag() throws AddressException {
		registres.setA((byte) 0x80);

		instruction = new Instruction(InstructionSet.ADC, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x81, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x01);
		assertTrue(registres.getP() == 0b00000001);
	}

	@Test
	public void adcZFlag() throws AddressException {
		registres.setA((byte) 0x00);

		instruction = new Instruction(InstructionSet.ADC, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x00, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x00);
		assertTrue(registres.getP() == (byte) 0b00000010);
	}

	@Test
	public void adcNFlag() throws AddressException {
		registres.setA((byte) 0x80);

		instruction = new Instruction(InstructionSet.ADC, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x00, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x80);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void andImmediate() throws AddressException {
		registres.setA((byte) 20);

		instruction = new Instruction(InstructionSet.AND, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 20, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 20);

		instruction.setArgument((byte) 4, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 4);
	}

	@Test
	public void andZeropage() throws AddressException {
		registres.setA((byte) 20);

		instruction = new Instruction(InstructionSet.AND, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 4);

		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 0);
	}

	@Test
	public void andZeropageX() throws AddressException {
		registres.setA((byte) 20);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.AND, AddressingMode.ZEROPAGE_X);
		instruction.setArgument((byte) 0x77, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 4);

		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 4);
	}

	@Test
	public void andAbsolute() throws AddressException {
		registres.setA((byte) 0x20);

		instruction = new Instruction(InstructionSet.AND, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0x4A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0);

		registres.setA((byte) 0x20);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0);
	}

	@Test
	public void andAbsoluteX() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.AND, AddressingMode.ABSOLUTE_X);
		instruction.setArgument((byte) 0xEF, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0);

		registres.setA((byte) 0x20);
		instruction.setArgument((byte) 0xDE, (byte) 0x4F);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x20);
	}

	@Test
	public void andAbsoluteY() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setY((byte) 0x10);

		instruction = new Instruction(InstructionSet.AND, AddressingMode.ABSOLUTE_Y);
		instruction.setArgument((byte) 0xEF, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0);

		registres.setA((byte) 0x20);
		instruction.setArgument((byte) 0xDE, (byte) 0x4F);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x20);
	}

	@Test
	public void andIndirectX() throws AddressException {
		registres.setA((byte) 0x50);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.AND, AddressingMode.INDIRECT_X);
		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x40);

		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x40);
	}

	@Test
	public void andIndirectY() throws AddressException {
		registres.setA((byte) 0xF0);
		registres.setY((byte) 0xB5);

		instruction = new Instruction(InstructionSet.AND, AddressingMode.INDIRECT_Y);
		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x10);

		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x10);
	}

	@Test
	public void andZFlag() throws AddressException {
		registres.setA((byte) 0x00);

		instruction = new Instruction(InstructionSet.AND, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x00, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x00);
		assertTrue(registres.getP() == (byte) 0b00000010);
	}

	@Test
	public void andNFlag() throws AddressException {
		registres.setA((byte) 0xF0);

		instruction = new Instruction(InstructionSet.AND, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x80, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x80);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	// ASL
	@Test
	public void aslAccumulator() throws AddressException {
		registres.setA((byte) 20);

		instruction = new Instruction(InstructionSet.ASL, AddressingMode.ACCUMULATOR);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 40);

		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 80);
	}

	@Test
	public void aslZeropage() throws AddressException {
		instruction = new Instruction(InstructionSet.ASL, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == 12);

		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x86) == (byte) 0x94);
	}

	@Test
	public void aslZeropageX() throws AddressException {
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.ASL, AddressingMode.ZEROPAGE_X);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == 12);

		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x86) == (byte) 0x94);
	}

	@Test
	public void aslAbsolute() throws AddressException {
		instruction = new Instruction(InstructionSet.ASL, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0x4A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x264A) == (byte) 0x80);

		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == (byte) 12);
	}

	@Test
	public void aslAbsoluteX() throws AddressException {
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.ASL, AddressingMode.ABSOLUTE_X);
		instruction.setArgument((byte) 0x3A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x264A) == (byte) 0x80);

		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == (byte) 12);
	}

	@Test
	public void aslCFlag() throws AddressException {
		registres.setA((byte) 0x80);
		instruction = new Instruction(InstructionSet.ASL, AddressingMode.ACCUMULATOR);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x00);
		assertTrue(registres.getP() == (byte) 0b00000011);
	}

	@Test
	public void aslZFlag() throws AddressException {
		instruction = new Instruction(InstructionSet.ASL, AddressingMode.ACCUMULATOR);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x00);
		assertTrue(registres.getP() == (byte) 0b00000010);
	}

	@Test
	public void aslNFlag() throws AddressException {
		instruction = new Instruction(InstructionSet.ASL, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x86) == (byte) 0x94);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	// Branchements

	@Test
	public void bccFail() throws AddressException {
		registres.setP((byte) 0x1);
		instruction = new Instruction(InstructionSet.BCC, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);
		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x0);
		assertTrue(cycles == 2);
	}

	@Test
	public void bccSucceed() throws AddressException {
		instruction = new Instruction(InstructionSet.BCC, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);

		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0xE);
		assertTrue(cycles == 3);
	}

	@Test
	public void bccNewPage() throws AddressException {
		registres.setPc(0xFE);
		instruction = new Instruction(InstructionSet.BCC, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);

		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x10C);
		assertTrue(cycles == 5);
	}

	@Test
	public void bcsFail() throws AddressException {
		instruction = new Instruction(InstructionSet.BCS, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);
		
		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x0);
		assertTrue(cycles == 2);
	}

	@Test
	public void bcsSucceed() throws AddressException {
		registres.setP((byte) 0x1);
		instruction = new Instruction(InstructionSet.BCS, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);

		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0xE);
		assertTrue(cycles == 3);
	}

	@Test
	public void bcsNewPage() throws AddressException {
		registres.setP((byte) 0x1);
		registres.setPc(0xFE);
		instruction = new Instruction(InstructionSet.BCS, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);

		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x10C);
		assertTrue(cycles == 5);
	}

	@Test
	public void beqFail() throws AddressException {
		instruction = new Instruction(InstructionSet.BEQ, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);
		
		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x0);
		assertTrue(cycles == 2);
	}

	@Test
	public void beqSucceed() throws AddressException {
		registres.setP((byte) 0b00000010);
		instruction = new Instruction(InstructionSet.BEQ, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);

		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0xE);
		assertTrue(cycles == 3);
	}

	@Test
	public void beqNewPage() throws AddressException {
		registres.setP((byte) 0b00000010);
		registres.setPc(0xFE);
		instruction = new Instruction(InstructionSet.BEQ, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);

		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x10C);
		assertTrue(cycles == 5);
	}

	// BIT (juste tester les flags car 0 modifications de valeur)
	@Test
	public void bitZeropage() throws AddressException {
		registres.setA((byte) 20);

		instruction = new Instruction(InstructionSet.BIT, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == 0b01000010);
	}

	@Test
	public void bitAbsolute() throws AddressException {
		registres.setA((byte) 20);

		instruction = new Instruction(InstructionSet.BIT, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0xEE, (byte) 0x4F);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b11000000);
	}

	// Branchements le retour

	@Test
	public void bmiFail() throws AddressException {
		instruction = new Instruction(InstructionSet.BMI, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);
		
		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x0);
		assertTrue(cycles == 2);
	}

	@Test
	public void bmiSucceed() throws AddressException {
		registres.setP((byte) 0b10000000);
		instruction = new Instruction(InstructionSet.BMI, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);

		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0xE);
		assertTrue(cycles == 3);
	}

	@Test
	public void bmiNewPage() throws AddressException {
		registres.setP((byte) 0b10000000);
		registres.setPc(0xFE);
		instruction = new Instruction(InstructionSet.BMI, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);

		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x10C);
		assertTrue(cycles == 5);
	}

	@Test
	public void bneFail() throws AddressException {
		registres.setP((byte) 0b00000010);
		instruction = new Instruction(InstructionSet.BNE, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);
		
		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x0);
		assertTrue(cycles == 2);
	}

	@Test
	public void bneSucceed() throws AddressException {
		instruction = new Instruction(InstructionSet.BNE, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);

		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0xE);
		assertTrue(cycles == 3);
	}

	@Test
	public void bneNewPage() throws AddressException {
		registres.setPc(0xFE);
		instruction = new Instruction(InstructionSet.BNE, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);

		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x10C);
		assertTrue(cycles == 5);
	}

	@Test
	public void bplFail() throws AddressException {
		registres.setP((byte) 0b10000000);
		instruction = new Instruction(InstructionSet.BPL, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);
		
		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x0);
		assertTrue(cycles == 2);
	}

	@Test
	public void bplSucceed() throws AddressException {
		instruction = new Instruction(InstructionSet.BPL, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);

		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0xE);
		assertTrue(cycles == 3);
	}

	@Test
	public void bplNewPage() throws AddressException {
		registres.setPc(0xFE);
		instruction = new Instruction(InstructionSet.BPL, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);

		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x10C);
		assertTrue(cycles == 5);
	}

	// BRK
	@Test
	public void brk() throws AddressException {
		registres.setPc(0x4FEE);
		registres.setP((byte) 0b10000010);
		instruction = new Instruction(InstructionSet.BRK, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0xF666);
		assertTrue(registres.getSp() == 0x1FC);
		assertTrue(registres.getP() == (byte) 0b10010010);
		assertTrue(bus.getByteFromMemory(registres.getSp() + 1) == (byte) 0b10000010);
		assertTrue(bus.getByteFromMemory(registres.getSp() + 3) == (byte) 0x4F);
		assertTrue(bus.getByteFromMemory(registres.getSp() + 2) == (byte) 0xEE);

		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0xF666);
		assertTrue(registres.getSp() == 0x1F9);
		assertTrue(registres.getP() == (byte) 0b10010010);
		assertTrue(bus.getByteFromMemory(registres.getSp() + 1) == (byte) 0b10010010);
		assertTrue(bus.getByteFromMemory(registres.getSp() + 3) == (byte) 0xF6);
		assertTrue(bus.getByteFromMemory(registres.getSp() + 2) == (byte) 0x66);

	}

	// Branchements partie 3

	@Test
	public void bvcFail() throws AddressException {
		registres.setP((byte) 0b01000000);
		instruction = new Instruction(InstructionSet.BVC, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);
		
		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x0);
		assertTrue(cycles == 2);
	}

	@Test
	public void bvcSucceed() throws AddressException {
		instruction = new Instruction(InstructionSet.BVC, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);

		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0xE);
		assertTrue(cycles == 3);
	}

	@Test
	public void bvcNewPage() throws AddressException {
		registres.setPc(0xFE);
		instruction = new Instruction(InstructionSet.BVC, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);

		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x10C);
		assertTrue(cycles == 5);
	}

	@Test
	public void bvsFail() throws AddressException {
		instruction = new Instruction(InstructionSet.BVS, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);
		
		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x0);
		assertTrue(cycles == 2);
	}

	@Test
	public void bvsSucceed() throws AddressException {
		registres.setP((byte) 0b01000000);
		instruction = new Instruction(InstructionSet.BVS, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);

		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0xE);
		assertTrue(cycles == 3);
	}

	@Test
	public void bvsNewPage() throws AddressException {
		registres.setP((byte) 0b01000000);
		registres.setPc(0xFE);
		instruction = new Instruction(InstructionSet.BVS, AddressingMode.RELATIVE);
		instruction.setArgument((byte) 0xE, (byte) 0);

		int cycles = instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x10C);
		assertTrue(cycles == 5);
	}

	// Enlevage des flags
	@Test
	public void clc() throws AddressException {
		registres.setP((byte) 0b00000001);
		instruction = new Instruction(InstructionSet.CLC, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0);
	}

	@Test
	public void cld() throws AddressException {
		registres.setP((byte) 0b00001000);
		instruction = new Instruction(InstructionSet.CLD, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0);
	}

	@Test
	public void cli() throws AddressException {
		registres.setP((byte) 0b00000100);
		instruction = new Instruction(InstructionSet.CLI, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0);
	}

	@Test
	public void clv() throws AddressException {
		registres.setP((byte) 0b01000000);
		instruction = new Instruction(InstructionSet.CLV, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0);
	}

	// CMP
	@Test
	public void cmpImmediate() throws AddressException {
		registres.setA((byte) 20);

		instruction = new Instruction(InstructionSet.CMP, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 20, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000011);

		instruction.setArgument((byte) 10, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000001);

		instruction.setArgument((byte) 30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void cmpZeropage() throws AddressException {
		registres.setA((byte) 20);

		instruction = new Instruction(InstructionSet.CMP, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000001);

		instruction.setArgument((byte) 0x87, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void cmpZeropageX() throws AddressException {
		registres.setA((byte) 20);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.CMP, AddressingMode.ZEROPAGE_X);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000001);

		instruction.setArgument((byte) 0x77, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void cmpAbsolute() throws AddressException {
		registres.setA((byte) 0x20);

		instruction = new Instruction(InstructionSet.CMP, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000001);

		instruction.setArgument((byte) 0x87, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void cmpAbsoluteX() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.CMP, AddressingMode.ABSOLUTE_X);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000001);

		instruction.setArgument((byte) 0x77, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void cmpAbsoluteY() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setY((byte) 0x10);

		instruction = new Instruction(InstructionSet.CMP, AddressingMode.ABSOLUTE_Y);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000001);

		instruction.setArgument((byte) 0x77, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void cmpIndirectX() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.CMP, AddressingMode.INDIRECT_X);
		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b10000000);

		instruction.setArgument((byte) 0x20, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000001);
	}

	@Test
	public void cmpIndirectY() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setY((byte) 0xB5);

		instruction = new Instruction(InstructionSet.CMP, AddressingMode.INDIRECT_Y);
		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000001);

		instruction.setArgument((byte) 0x20, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000001);
	}

	// CPX
	@Test
	public void cpxImmediate() throws AddressException {
		registres.setX((byte) 20);

		instruction = new Instruction(InstructionSet.CPX, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 20, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000011);

		instruction.setArgument((byte) 10, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000001);

		instruction.setArgument((byte) 30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void cpxZeropage() throws AddressException {
		registres.setX((byte) 20);

		instruction = new Instruction(InstructionSet.CPX, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000001);

		instruction.setArgument((byte) 0x87, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void cpxAbsolute() throws AddressException {
		registres.setX((byte) 0x20);

		instruction = new Instruction(InstructionSet.CPX, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000001);

		instruction.setArgument((byte) 0x87, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	// CPY
	@Test
	public void cpyImmediate() throws AddressException {
		registres.setY((byte) 20);

		instruction = new Instruction(InstructionSet.CPY, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 20, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000011);

		instruction.setArgument((byte) 10, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000001);

		instruction.setArgument((byte) 30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void cpyZeropage() throws AddressException {
		registres.setY((byte) 20);

		instruction = new Instruction(InstructionSet.CPY, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000001);

		instruction.setArgument((byte) 0x87, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void cpyAbsolute() throws AddressException {
		registres.setY((byte) 0x20);

		instruction = new Instruction(InstructionSet.CPY, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000001);

		instruction.setArgument((byte) 0x87, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void decZeropage() throws AddressException {
		instruction = new Instruction(InstructionSet.DEC, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == (byte) 0x05);
	}

	@Test
	public void decZeropageX() throws AddressException {
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.DEC, AddressingMode.ZEROPAGE_X);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == (byte) 0x05);
	}

	@Test
	public void decAbsolute() throws AddressException {
		instruction = new Instruction(InstructionSet.DEC, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0xFF, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x26FF) == (byte) 0x0F);
	}

	@Test
	public void decAbsoluteX() throws AddressException {
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.DEC, AddressingMode.ABSOLUTE_X);
		instruction.setArgument((byte) 0xDE, (byte) 0x4F);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x4FEE) == (byte) 0xFE);
	}

	@Test
	public void decZFlag() throws AddressException {
		instruction = new Instruction(InstructionSet.DEC, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0xAB, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0xAB) == (byte) 0x00);
		assertTrue(registres.getP() == (byte) 0b00000010);
	}

	@Test
	public void decNFlag() throws AddressException {
		instruction = new Instruction(InstructionSet.DEC, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0xAB, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0xAB) == (byte) 0xFF);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	// DEX (On teste tout en une fonction)
	@Test
	public void dex() throws AddressException {
		registres.setX((byte) 0x02);

		instruction = new Instruction(InstructionSet.DEX, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0x01);
		assertTrue(registres.getP() == (byte) 0b00000000);

		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0x00);
		assertTrue(registres.getP() == (byte) 0b00000010);

		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0xFF);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	// DEY (On teste tout en une fonction)
	@Test
	public void dey() throws AddressException {
		registres.setY((byte) 0x02);

		instruction = new Instruction(InstructionSet.DEY, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0x01);
		assertTrue(registres.getP() == (byte) 0b00000000);

		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0x00);
		assertTrue(registres.getP() == (byte) 0b00000010);

		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0xFF);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	// EOR
	@Test
	public void eorImmediate() throws AddressException {
		registres.setA((byte) 20);

		instruction = new Instruction(InstructionSet.EOR, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 20, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 0);

		registres.setA((byte) 20);

		instruction.setArgument((byte) 4, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 16);
	}

	@Test
	public void eorZeropage() throws AddressException {
		registres.setA((byte) 20);

		instruction = new Instruction(InstructionSet.EOR, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 18);

		registres.setA((byte) 20);

		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x5E);
	}

	@Test
	public void eorZeropageX() throws AddressException {
		registres.setA((byte) 20);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.EOR, AddressingMode.ZEROPAGE_X);
		instruction.setArgument((byte) 0x77, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x32);

		registres.setA((byte) 20);

		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 18);
	}

	@Test
	public void eorAbsolute() throws AddressException {
		registres.setA((byte) 0x20);

		instruction = new Instruction(InstructionSet.EOR, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0x4A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x60);

		registres.setA((byte) 0x20);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x26);
	}

	@Test
	public void eorAbsoluteX() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.EOR, AddressingMode.ABSOLUTE_X);
		instruction.setArgument((byte) 0x3A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x60);

		registres.setA((byte) 0x20);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x26);
	}

	@Test
	public void eorAbsoluteY() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setY((byte) 0x10);

		instruction = new Instruction(InstructionSet.EOR, AddressingMode.ABSOLUTE_Y);
		instruction.setArgument((byte) 0x3A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x60);

		registres.setA((byte) 0x20);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x26);
	}

	@Test
	public void eorIndirectX() throws AddressException {
		registres.setA((byte) 0x50);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.EOR, AddressingMode.INDIRECT_X);
		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x10);

		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x50);
	}

	@Test
	public void eorIndirectY() throws AddressException {
		registres.setA((byte) 0xF0);
		registres.setY((byte) 0xB5);

		instruction = new Instruction(InstructionSet.EOR, AddressingMode.INDIRECT_Y);
		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0xE0);

		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0xF0);
	}

	@Test
	public void eorZFlag() throws AddressException {
		registres.setA((byte) 0x20);

		instruction = new Instruction(InstructionSet.EOR, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x20, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x00);
		assertTrue(registres.getP() == (byte) 0b00000010);
	}

	@Test
	public void eorNFlag() throws AddressException {
		registres.setA((byte) 0x08);

		instruction = new Instruction(InstructionSet.EOR, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x80, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x88);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	// INC
	@Test
	public void incZeropage() throws AddressException {
		instruction = new Instruction(InstructionSet.INC, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == (byte) 0x07);
	}

	@Test
	public void incZeropageX() throws AddressException {
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.INC, AddressingMode.ZEROPAGE_X);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == (byte) 0x07);
	}

	@Test
	public void incAbsolute() throws AddressException {
		instruction = new Instruction(InstructionSet.INC, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0xFF, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x26FF) == (byte) 0x11);
	}

	@Test
	public void incAbsoluteX() throws AddressException {
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.INC, AddressingMode.ABSOLUTE_X);
		instruction.setArgument((byte) 0xDE, (byte) 0x4F);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x4FEE) == (byte) 0x00);
	}

	@Test
	public void incZFlag() throws AddressException {
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.INC, AddressingMode.ABSOLUTE_X);
		instruction.setArgument((byte) 0xDE, (byte) 0x4F);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x4FEE) == (byte) 0x00);
		assertTrue(registres.getP() == (byte) 0b00000010);
	}

	@Test
	public void incNFlag() throws AddressException {
		instruction = new Instruction(InstructionSet.INC, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0xFF, (byte) 0xFF);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0xFFFF) == (byte) 0xF7);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	// INX (On teste tout en une fonction)
	@Test
	public void inx() throws AddressException {
		registres.setX((byte) 0xFE);

		instruction = new Instruction(InstructionSet.INX, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0xFF);
		assertTrue(registres.getP() == (byte) 0b10000000);

		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0x00);
		assertTrue(registres.getP() == (byte) 0b00000010);

		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0x01);
		assertTrue(registres.getP() == (byte) 0b00000000);
	}

	// INY (On teste tout en une fonction)
	@Test
	public void iny() throws AddressException {
		registres.setY((byte) 0xFE);

		instruction = new Instruction(InstructionSet.INY, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0xFF);
		assertTrue(registres.getP() == (byte) 0b10000000);

		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0x00);
		assertTrue(registres.getP() == (byte) 0b00000010);

		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0x01);
		assertTrue(registres.getP() == (byte) 0b00000000);
	}

	// JMP (Il y a juste à tester que pc est bien changé)
	@Test
	public void jumpAbsolute() throws AddressException {
		instruction = new Instruction(InstructionSet.JMP, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0xEE, (byte) 0x4F);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x4FEE - 3);
	}

	@Test
	public void jumpIndirect() throws AddressException {
		instruction = new Instruction(InstructionSet.JMP, AddressingMode.INDIRECT);
		instruction.setArgument((byte) 0xEE, (byte) 0x4F);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x26FF - 3);

		instruction.setArgument((byte) 0xFF, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x10 - 3);
	}

	// JSR
	@Test
	public void jsr() throws AddressException {
		registres.setPc(0x2266);
		instruction = new Instruction(InstructionSet.JSR, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0xEE, (byte) 0x4F);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x4FEB);
		assertTrue(bus.getByteFromMemory(registres.getSp() + 2) == (byte) 0x22);
		assertTrue(bus.getByteFromMemory(registres.getSp() + 1) == (byte) 0x68);
		assertTrue(registres.getSp() == 0x1FD);
	}

	// LDA
	@Test
	public void ldaImmediate() throws AddressException {
		instruction = new Instruction(InstructionSet.LDA, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x40);

		instruction.setArgument((byte) 0x60, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x60);
	}

	@Test
	public void ldaZeropage() throws AddressException {
		instruction = new Instruction(InstructionSet.LDA, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x6);

		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x4A);
	}

	@Test
	public void ldaZeropageX() throws AddressException {
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.LDA, AddressingMode.ZEROPAGE_X);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x6);

		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x4A);
	}

	@Test
	public void ldaAbsolute() throws AddressException {
		instruction = new Instruction(InstructionSet.LDA, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0x4A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x40);

		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x4A);
	}

	@Test
	public void ldaAbsoluteX() throws AddressException {
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.LDA, AddressingMode.ABSOLUTE_X);
		instruction.setArgument((byte) 0x3A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x40);

		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x4A);
	}

	@Test
	public void ldaAbsoluteY() throws AddressException {
		registres.setY((byte) 0x10);

		instruction = new Instruction(InstructionSet.LDA, AddressingMode.ABSOLUTE_Y);
		instruction.setArgument((byte) 0x3A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x40);

		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x4A);
	}

	@Test
	public void ldaIndirectX() throws AddressException {
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.LDA, AddressingMode.INDIRECT_X);
		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x40);
	}

	@Test
	public void ldaIndirectY() throws AddressException {
		registres.setY((byte) 0xB5);

		instruction = new Instruction(InstructionSet.LDA, AddressingMode.INDIRECT_Y);
		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x10);
	}

	@Test
	public void ldaZFlag() throws AddressException {
		instruction = new Instruction(InstructionSet.LDA, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x00, (byte) 0x00);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x00);
		assertTrue(registres.getP() == (byte) 0b00000010);
	}

	@Test
	public void ldaNFlag() throws AddressException {
		instruction = new Instruction(InstructionSet.LDA, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x86);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	// LDX
	@Test
	public void ldxImmediate() throws AddressException {
		instruction = new Instruction(InstructionSet.LDX, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0x40);

		instruction.setArgument((byte) 0x60, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0x60);
	}

	@Test
	public void ldxZeropage() throws AddressException {
		instruction = new Instruction(InstructionSet.LDX, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0x6);

		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0x4A);
	}

	@Test
	public void ldxZeropageY() throws AddressException {
		registres.setY((byte) 0x10);

		instruction = new Instruction(InstructionSet.LDX, AddressingMode.ZEROPAGE_Y);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0x6);

		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0x4A);
	}

	@Test
	public void ldxAbsolute() throws AddressException {
		instruction = new Instruction(InstructionSet.LDX, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0x4A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0x40);

		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0x4A);
	}

	@Test
	public void ldxAbsoluteY() throws AddressException {
		registres.setY((byte) 0x10);

		instruction = new Instruction(InstructionSet.LDX, AddressingMode.ABSOLUTE_Y);
		instruction.setArgument((byte) 0x3A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0x40);

		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0x4A);
	}

	@Test
	public void ldxZFlag() throws AddressException {
		instruction = new Instruction(InstructionSet.LDX, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x00, (byte) 0x00);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0x00);
		assertTrue(registres.getP() == (byte) 0b00000010);
	}

	@Test
	public void ldxNFlag() throws AddressException {
		instruction = new Instruction(InstructionSet.LDX, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0x86);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	// LDY
	@Test
	public void ldyImmediate() throws AddressException {
		instruction = new Instruction(InstructionSet.LDY, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0x40);

		instruction.setArgument((byte) 0x60, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0x60);
	}

	@Test
	public void ldyZeropage() throws AddressException {
		instruction = new Instruction(InstructionSet.LDY, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0x6);

		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0x4A);
	}

	@Test
	public void ldyZeropageX() throws AddressException {
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.LDY, AddressingMode.ZEROPAGE_X);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0x6);

		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0x4A);
	}

	@Test
	public void ldyAbsolute() throws AddressException {
		instruction = new Instruction(InstructionSet.LDY, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0x4A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0x40);

		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0x4A);
	}

	@Test
	public void ldyAbsoluteX() throws AddressException {
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.LDY, AddressingMode.ABSOLUTE_X);
		instruction.setArgument((byte) 0x3A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0x40);

		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0x4A);
	}

	@Test
	public void ldyZFlag() throws AddressException {
		instruction = new Instruction(InstructionSet.LDY, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x00, (byte) 0x00);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0x00);
		assertTrue(registres.getP() == (byte) 0b00000010);
	}

	@Test
	public void ldyNFlag() throws AddressException {
		instruction = new Instruction(InstructionSet.LDY, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0x86);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	// LSR
	@Test
	public void lsrAccumulator() throws AddressException {
		registres.setA((byte) 20);

		instruction = new Instruction(InstructionSet.LSR, AddressingMode.ACCUMULATOR);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 10);

		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 5);
	}

	@Test
	public void lsrZeropage() throws AddressException {
		instruction = new Instruction(InstructionSet.LSR, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == 3);

		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x86) == (byte) 0x25);
	}

	@Test
	public void lsrZeropageX() throws AddressException {
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.LSR, AddressingMode.ZEROPAGE_X);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == 3);

		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x86) == (byte) 0x25);
	}

	@Test
	public void lsrAbsolute() throws AddressException {
		instruction = new Instruction(InstructionSet.LSR, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0x4A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x264A) == (byte) 0x20);

		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == (byte) 3);
	}

	@Test
	public void lsrAbsoluteX() throws AddressException {
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.LSR, AddressingMode.ABSOLUTE_X);
		instruction.setArgument((byte) 0x3A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x264A) == (byte) 0x20);

		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == (byte) 3);
	}

	@Test
	public void lsrCFlag() throws AddressException {
		registres.setA((byte) 1);

		instruction = new Instruction(InstructionSet.LSR, AddressingMode.ACCUMULATOR);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 0);
		assertTrue(registres.getP() == (byte) 0b00000011);
	}

	@Test
	public void lsrZFlag() throws AddressException {
		registres.setA((byte) 0);

		instruction = new Instruction(InstructionSet.LSR, AddressingMode.ACCUMULATOR);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 0);
		assertTrue(registres.getP() == (byte) 0b00000010);
	}

	// NOP (rien à faire)
	@Test
	public void nop() throws AddressException {
		instruction = new Instruction(InstructionSet.NOP, AddressingMode.IMPLICIT);
		int cycles = instructionReader.getInstructionCycles(instruction, registres);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(cycles == 2);
	}

	// ORA
	@Test
	public void oraImmediate() throws AddressException {
		registres.setA((byte) 20);

		instruction = new Instruction(InstructionSet.ORA, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 20, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 20);

		registres.setA((byte) 20);
		instruction.setArgument((byte) 4, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 20);
	}

	@Test
	public void oraZeropage() throws AddressException {
		registres.setA((byte) 20);

		instruction = new Instruction(InstructionSet.ORA, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 22);

		registres.setA((byte) 20);
		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 0x5E);
	}

	@Test
	public void oraZeropageX() throws AddressException {
		registres.setA((byte) 20);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.ORA, AddressingMode.ZEROPAGE_X);
		instruction.setArgument((byte) 0x77, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 0x36);

		registres.setA((byte) 20);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 22);
	}

	@Test
	public void oraAbsolute() throws AddressException {
		registres.setA((byte) 0x20);

		instruction = new Instruction(InstructionSet.ORA, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0x4A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x60);

		registres.setA((byte) 0x20);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x26);
	}

	@Test
	public void oraAbsoluteX() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.ORA, AddressingMode.ABSOLUTE_X);
		instruction.setArgument((byte) 0xEF, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x30);

		registres.setA((byte) 0x20);
		instruction.setArgument((byte) 0xDE, (byte) 0x4F);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0xFF);
	}

	@Test
	public void oraAbsoluteY() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setY((byte) 0x10);

		instruction = new Instruction(InstructionSet.ORA, AddressingMode.ABSOLUTE_Y);
		instruction.setArgument((byte) 0xEF, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x30);

		registres.setA((byte) 0x20);
		instruction.setArgument((byte) 0xDE, (byte) 0x4F);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0xFF);
	}

	@Test
	public void oraIndirectX() throws AddressException {
		registres.setA((byte) 0x50);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.ORA, AddressingMode.INDIRECT_X);
		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x50);
	}

	@Test
	public void oraIndirectY() throws AddressException {
		registres.setA((byte) 0xF0);
		registres.setY((byte) 0xB5);

		instruction = new Instruction(InstructionSet.ORA, AddressingMode.INDIRECT_Y);
		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0xF0);
	}

	@Test
	public void oraZFlag() throws AddressException {
		registres.setA((byte) 0x00);

		instruction = new Instruction(InstructionSet.ORA, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x00, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x00);
		assertTrue(registres.getP() == (byte) 0b00000010);
	}

	@Test
	public void oraNFlag() throws AddressException {
		registres.setA((byte) 0xF0);

		instruction = new Instruction(InstructionSet.ORA, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x00, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0xF0);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	// PHA et PLA
	@Test
	public void phaPla() throws AddressException {
		registres.setA((byte) 0x20);

		instruction = new Instruction(InstructionSet.PHA, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x1FF) == (byte) 0x20);
		assertTrue(registres.getSp() == 0x1FE);

		registres.setA((byte) 0x00);

		instruction = new Instruction(InstructionSet.PLA, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 0x20);
		assertTrue(registres.getSp() == 0x1FF);

		registres.setSp(0x100);

	}

	@Test
	public void phaPlaReloop() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setSp(0x100);

		instruction = new Instruction(InstructionSet.PHA, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x100) == (byte) 0x20);
		assertTrue(registres.getSp() == 0x1FF);

		registres.setA((byte) 0x00);

		instruction = new Instruction(InstructionSet.PLA, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 0x20);
		assertTrue(registres.getSp() == 0x100);
	}

	// PHP et PLP
	@Test
	public void phpPlp() throws AddressException {
		registres.setP((byte) 0x20);

		instruction = new Instruction(InstructionSet.PHP, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x1FF) == (byte) 0x20);
		assertTrue(registres.getSp() == 0x1FE);

		registres.setP((byte) 0x00);

		instruction = new Instruction(InstructionSet.PLP, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == 0x20);
		assertTrue(registres.getSp() == 0x1FF);
	}

	@Test
	public void phpPlpReloop() throws AddressException {
		registres.setP((byte) 0x20);
		registres.setSp(0x100);

		instruction = new Instruction(InstructionSet.PHP, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x100) == (byte) 0x20);
		assertTrue(registres.getSp() == 0x1FF);

		registres.setP((byte) 0x00);

		instruction = new Instruction(InstructionSet.PLP, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == 0x20);
		assertTrue(registres.getSp() == 0x100);
	}

	// ROL
	@Test
	public void rolAccumulator() throws AddressException {
		registres.setA((byte) 20);

		instruction = new Instruction(InstructionSet.ROL, AddressingMode.ACCUMULATOR);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 40);
		assertTrue(registres.getP() == (byte) 0b00000000);

		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 80);
		assertTrue(registres.getP() == (byte) 0b00000000);

		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == -96);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void rolZeropage() throws AddressException {
		instruction = new Instruction(InstructionSet.ROL, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == 12);
		assertTrue(registres.getP() == (byte) 0b00000000);

		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x86) == (byte) 0x94);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void rolZeropageX() throws AddressException {
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.ROL, AddressingMode.ZEROPAGE_X);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == 12);
		assertTrue(registres.getP() == (byte) 0b00000000);

		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x86) == (byte) 0x94);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void rolAbsolute() throws AddressException {
		instruction = new Instruction(InstructionSet.ROL, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0x4A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x264A) == (byte) 0x80);
		assertTrue(registres.getP() == (byte) 0b10000000);

		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == (byte) 12);
		assertTrue(registres.getP() == (byte) 0b00000000);
	}

	@Test
	public void rolAbsoluteX() throws AddressException {
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.ROL, AddressingMode.ABSOLUTE_X);
		instruction.setArgument((byte) 0x3A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x264A) == (byte) 0x80);
		assertTrue(registres.getP() == (byte) 0b10000000);

		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == (byte) 12);
		assertTrue(registres.getP() == (byte) 0b00000000);
	}

	@Test
	public void rolZFlag() throws AddressException {
		registres.setA((byte) 0x80);

		instruction = new Instruction(InstructionSet.ROL, AddressingMode.ACCUMULATOR);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0);
		assertTrue(registres.getP() == (byte) 0b00000011);
	}

	@Test
	public void rolNFlag() throws AddressException {
		registres.setA((byte) 0x40);
		registres.setP((byte) 0b00000001);

		instruction = new Instruction(InstructionSet.ROL, AddressingMode.ACCUMULATOR);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x81);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	// ROR
	@Test
	public void rorAccumulator() throws AddressException {
		registres.setA((byte) 20);

		instruction = new Instruction(InstructionSet.ROR, AddressingMode.ACCUMULATOR);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 10);
		assertTrue(registres.getP() == (byte) 0b00000000);

		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 5);
		assertTrue(registres.getP() == (byte) 0b00000000);

		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 2);
		assertTrue(registres.getP() == (byte) 0b00000001);

		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == -127);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void rorZeropage() throws AddressException {
		instruction = new Instruction(InstructionSet.ROR, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == 3);
		assertTrue(registres.getP() == (byte) 0b00000000);

		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x86) == (byte) 0x25);
		assertTrue(registres.getP() == (byte) 0b00000000);
	}

	@Test
	public void rorZeropageX() throws AddressException {
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.ROR, AddressingMode.ZEROPAGE_X);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == 3);
		assertTrue(registres.getP() == (byte) 0b00000000);

		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x86) == (byte) 0x25);
		assertTrue(registres.getP() == (byte) 0b00000000);
	}

	@Test
	public void rorAbsolute() throws AddressException {
		instruction = new Instruction(InstructionSet.ROR, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0x4A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x264A) == (byte) 0x20);
		assertTrue(registres.getP() == (byte) 0b00000000);

		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == (byte) 3);
		assertTrue(registres.getP() == (byte) 0b00000000);
	}

	@Test
	public void rorAbsoluteX() throws AddressException {
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.ROR, AddressingMode.ABSOLUTE_X);
		instruction.setArgument((byte) 0x3A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x264A) == (byte) 0x20);
		assertTrue(registres.getP() == (byte) 0b00000000);

		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == (byte) 3);
		assertTrue(registres.getP() == (byte) 0b00000000);
	}

	@Test
	public void rorZFlag() throws AddressException {
		registres.setA((byte) 1);

		instruction = new Instruction(InstructionSet.ROR, AddressingMode.ACCUMULATOR);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 0);
		assertTrue(registres.getP() == (byte) 0b00000011);
	}

	@Test
	public void rorNFlag() throws AddressException {
		registres.setA((byte) 0);
		registres.setP((byte) 0b00000001);

		instruction = new Instruction(InstructionSet.ROR, AddressingMode.ACCUMULATOR);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x80);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	// RTI
	@Test
	public void rti() throws AddressException {
		// Break
		registres.setPc(0x4FEE);
		registres.setP((byte) 0b10000010);
		instruction = new Instruction(InstructionSet.NMI, AddressingMode.NMI);
		instructionReader.processInstruction(instruction, registres);

		instruction = new Instruction(InstructionSet.RTI, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x4FED);
		assertTrue(registres.getSp() == 0x1FF);
		assertTrue(registres.getP() == (byte) 0b10000010);
		
		instruction = new Instruction(InstructionSet.BRK, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);

		instruction = new Instruction(InstructionSet.RTI, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x4FED);
		assertTrue(registres.getSp() == 0x1FF);
		assertTrue(registres.getP() == (byte) 0b10000010);
	}

	// RTI
	@Test
	public void rts() throws AddressException {
		// Jump
		registres.setPc(0x4FEE);
		instruction = new Instruction(InstructionSet.JSR, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);

		instruction = new Instruction(InstructionSet.RTS, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getPc() == 0x4FF0);
		assertTrue(registres.getSp() == 0x1FF);
	}

	// SBC
	@Test
	public void sbcImmediate() throws AddressException {
		registres.setA((byte) 20);

		instruction = new Instruction(InstructionSet.SBC, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 20, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == 0);

		instruction.setArgument((byte) 20, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) -20);
	}

	@Test
	public void sbcZeropage() throws AddressException {
		registres.setA((byte) 20);

		instruction = new Instruction(InstructionSet.SBC, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x0E);

		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x08);
	}

	@Test
	public void sbcZeropageX() throws AddressException {
		registres.setA((byte) 20);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.SBC, AddressingMode.ZEROPAGE_X);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x0E);

		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x08);
	}

	@Test
	public void sbcAbsolute() throws AddressException {
		registres.setA((byte) 0x20);

		instruction = new Instruction(InstructionSet.SBC, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0x4A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0xE0);

		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0xDA);
	}

	@Test
	public void sbcAbsoluteX() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.SBC, AddressingMode.ABSOLUTE_X);
		instruction.setArgument((byte) 0xEF, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x10);

		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x0A);
	}

	@Test
	public void sbcAbsoluteY() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setY((byte) 0x10);

		instruction = new Instruction(InstructionSet.SBC, AddressingMode.ABSOLUTE_Y);
		instruction.setArgument((byte) 0xEF, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x10);

		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x0A);
	}

	@Test
	public void sbcIndirectX() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.SBC, AddressingMode.INDIRECT_X);
		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0xE0);

		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0xA0);
	}

	@Test
	public void sbcIndirectY() throws AddressException {
		registres.setA((byte) 0x20);
		registres.setY((byte) 0xB5);

		instruction = new Instruction(InstructionSet.SBC, AddressingMode.INDIRECT_Y);
		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x10);

		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x00);
	}

	@Test
	public void sbcCFlag() throws AddressException {
		registres.setA((byte) 0x80);

		instruction = new Instruction(InstructionSet.SBC, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x79, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x07);
		assertTrue(registres.getP() == 0b00000001);
	}

	@Test
	public void sbcZFlag() throws AddressException {
		registres.setA((byte) 0x00);

		instruction = new Instruction(InstructionSet.SBC, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x00, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x00);
		assertTrue(registres.getP() == (byte) 0b00000010);
	}

	@Test
	public void sbcNFlag() throws AddressException {
		registres.setA((byte) 0x80);

		instruction = new Instruction(InstructionSet.SBC, AddressingMode.IMMEDIATE);
		instruction.setArgument((byte) 0x00, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x80);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	// Mise Carry
	@Test
	public void sec() throws AddressException {
		instruction = new Instruction(InstructionSet.SEC, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000001);
	}

	@Test
	public void sed() throws AddressException {
		instruction = new Instruction(InstructionSet.SED, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00001000);
	}

	@Test
	public void sei() throws AddressException {
		instruction = new Instruction(InstructionSet.SEI, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getP() == (byte) 0b00000100);
	}

	// STA
	@Test
	public void staZeropage() throws AddressException {
		registres.setA((byte) 0x66);

		instruction = new Instruction(InstructionSet.STA, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == (byte) 0x66);
	}

	@Test
	public void staZeropageX() throws AddressException {
		registres.setA((byte) 0x66);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.STA, AddressingMode.ZEROPAGE_X);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == (byte) 0x66);
	}

	@Test
	public void staAbsolute() throws AddressException {
		registres.setA((byte) 0x66);

		instruction = new Instruction(InstructionSet.STA, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0x4A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x264A) == (byte) 0x66);
	}

	@Test
	public void staAbsoluteX() throws AddressException {
		registres.setA((byte) 0x66);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.STA, AddressingMode.ABSOLUTE_X);
		instruction.setArgument((byte) 0x3A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x264A) == (byte) 0x66);
	}

	@Test
	public void staAbsoluteY() throws AddressException {
		registres.setA((byte) 0x66);
		registres.setY((byte) 0x10);

		instruction = new Instruction(InstructionSet.STA, AddressingMode.ABSOLUTE_Y);
		instruction.setArgument((byte) 0x3A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x264A) == (byte) 0x66);
	}

	@Test
	public void staIndirectX() throws AddressException {
		registres.setA((byte) 0x66);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.STA, AddressingMode.INDIRECT_X);
		instruction.setArgument((byte) 0x76, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x264A) == (byte) 0x66);
	}

	@Test
	public void staIndirectY() throws AddressException {
		registres.setA((byte) 0x66);
		registres.setY((byte) 0xB5);

		instruction = new Instruction(InstructionSet.STA, AddressingMode.INDIRECT_Y);
		instruction.setArgument((byte) 0x86, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x26FF) == (byte) 0x66);
	}

	// STX
	@Test
	public void stxZeropage() throws AddressException {
		registres.setX((byte) 0x66);

		instruction = new Instruction(InstructionSet.STX, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == (byte) 0x66);
	}

	@Test
	public void stxZeropageY() throws AddressException {
		registres.setX((byte) 0x66);
		registres.setY((byte) 0x10);

		instruction = new Instruction(InstructionSet.STX, AddressingMode.ZEROPAGE_Y);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == (byte) 0x66);
	}

	@Test
	public void stxAbsolute() throws AddressException {
		registres.setX((byte) 0x66);

		instruction = new Instruction(InstructionSet.STX, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0x4A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x264A) == (byte) 0x66);
	}

	// STY
	@Test
	public void styZeropage() throws AddressException {
		registres.setY((byte) 0x66);

		instruction = new Instruction(InstructionSet.STY, AddressingMode.ZEROPAGE);
		instruction.setArgument((byte) 0x40, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == (byte) 0x66);
	}

	@Test
	public void styZeropageX() throws AddressException {
		registres.setY((byte) 0x66);
		registres.setX((byte) 0x10);

		instruction = new Instruction(InstructionSet.STY, AddressingMode.ZEROPAGE_X);
		instruction.setArgument((byte) 0x30, (byte) 0);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x40) == (byte) 0x66);
	}

	@Test
	public void styAbsolute() throws AddressException {
		registres.setY((byte) 0x66);

		instruction = new Instruction(InstructionSet.STY, AddressingMode.ABSOLUTE);
		instruction.setArgument((byte) 0x4A, (byte) 0x26);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(bus.getByteFromMemory(0x264A) == (byte) 0x66);
	}

	// Les transfers
	@Test
	public void tax() throws AddressException {
		registres.setA((byte) 0x66);

		instruction = new Instruction(InstructionSet.TAX, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0x66);
	}

	@Test
	public void taxZFlag() throws AddressException {
		registres.setA((byte) 0);

		instruction = new Instruction(InstructionSet.TAX, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0);
		assertTrue(registres.getP() == (byte) 0b00000010);
	}

	@Test
	public void taxNFlag() throws AddressException {
		registres.setA((byte) 0x86);

		instruction = new Instruction(InstructionSet.TAX, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0x86);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void tay() throws AddressException {
		registres.setA((byte) 0x66);

		instruction = new Instruction(InstructionSet.TAY, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0x66);
	}

	@Test
	public void tayZFlag() throws AddressException {
		registres.setA((byte) 0);

		instruction = new Instruction(InstructionSet.TAY, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0);
		assertTrue(registres.getP() == (byte) 0b00000010);
	}

	@Test
	public void tayNFlag() throws AddressException {
		registres.setA((byte) 0x86);

		instruction = new Instruction(InstructionSet.TAY, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getY() == (byte) 0x86);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void tsx() throws AddressException {
		registres.setSp(0x1FE);

		instruction = new Instruction(InstructionSet.TSX, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0xFE);
	}

	@Test
	public void tsxZFlag() throws AddressException {
		registres.setSp(0x100);

		instruction = new Instruction(InstructionSet.TSX, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0);
		assertTrue(registres.getP() == (byte) 0b00000010);
	}

	@Test
	public void tsxNFlag() throws AddressException {
		registres.setSp(0x1FE);

		instruction = new Instruction(InstructionSet.TSX, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getX() == (byte) 0xFE);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void txa() throws AddressException {
		registres.setX((byte) 0x66);

		instruction = new Instruction(InstructionSet.TXA, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x66);
	}

	@Test
	public void txaZFlag() throws AddressException {
		registres.setX((byte) 0);

		instruction = new Instruction(InstructionSet.TXA, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0);
		assertTrue(registres.getP() == (byte) 0b00000010);
	}

	@Test
	public void txaNFlag() throws AddressException {
		registres.setX((byte) 0x86);

		instruction = new Instruction(InstructionSet.TXA, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x86);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}

	@Test
	public void txs() throws AddressException {
		registres.setX((byte) 0x66);

		instruction = new Instruction(InstructionSet.TXS, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getSp() == 0x166);

		registres.setX((byte) 0xFF);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getSp() == 0x1FF);
	}

	@Test
	public void tya() throws AddressException {
		registres.setY((byte) 0x66);

		instruction = new Instruction(InstructionSet.TYA, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x66);
	}

	@Test
	public void tyaZFlag() throws AddressException {
		registres.setY((byte) 0);

		instruction = new Instruction(InstructionSet.TYA, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0);
		assertTrue(registres.getP() == (byte) 0b00000010);
	}

	@Test
	public void tyaNFlag() throws AddressException {
		registres.setY((byte) 0x86);

		instruction = new Instruction(InstructionSet.TYA, AddressingMode.IMPLICIT);
		instructionReader.processInstruction(instruction, registres);
		assertTrue(registres.getA() == (byte) 0x86);
		assertTrue(registres.getP() == (byte) 0b10000000);
	}
}
