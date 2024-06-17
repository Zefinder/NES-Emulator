package instructions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static instructions.AddressingMode.*;

import components.cpu.Cpu;
import exceptions.InstructionNotSupportedException;
import instructions.alu.ADCInstruction;
import instructions.alu.DEXInstruction;
import instructions.alu.INYInstruction;
import instructions.branch.BNEInstruction;
import instructions.registermemory.LDAInstruction;
import instructions.registermemory.LDXInstruction;
import instructions.registermemory.STAInstruction;
import utils.MapperTest;

class TestInstructionIssues {

	static final Cpu cpu = Cpu.getInstance();

	@BeforeAll
	static void init() {
		// Registers at 0
		cpu.cpuInfo.A = 0;
		cpu.cpuInfo.X = 0;
		cpu.cpuInfo.Y = 0;
		cpu.cpuInfo.SP = 0xFD;
		cpu.cpuInfo.PC = 0;

		// Flags at 0
		cpu.cpuInfo.C = 0;
		cpu.cpuInfo.Z = 0;
		cpu.cpuInfo.I = 0;
		cpu.cpuInfo.D = 0;
		cpu.cpuInfo.B = 0;
		cpu.cpuInfo.V = 0;
		cpu.cpuInfo.N = 0;
	}

	/**
	 * While testing on SMB, I saw that if an instruction used memory for read or
	 * write, depended on the X or Y register and went on a loop, the address wasn't
	 * updated and thus the fetched data was the wrong one (and the stored data was
	 * at the wrong place)
	 */
	@Test
	public void testAddressingModeWithLoop() throws InstructionNotSupportedException {
		// Set mapper with custom instructions
		/*
		 * LDA #$20		; Set address $2020 in $00
		 * STA $00		
		 * STA $01
		 * LDA #$01		; A = 1
		 * STA $2020	; Set 1 to $2020
		 * LDA #$02		; A = 2
		 * STA $2021	; Set 1 to $2021
		 * LDX #$02		; X = 2
		 * 
		 * .loop:
		 * LDA #$01		; A = 1
		 * ADC ($00),Y	; A = 2 (1+1) at first loop and A = 3 at second loop
		 * INY			; Y += 1
		 * DEX			; X -= 1
		 * BNE .loop	; Loop if X != 0 (BNE *-6 ($F8)) (DON'T FORGET THAT IT IS -8+2)
		 * 
		 * STA $02		; Set A to $02, normally it'll be 3
		 */
//		int[] cpuInstructions = { 0xA9, 0x20, 0x85, 0x00, 0x85, 0x01, 0xA9, 0x01, 0x8D, 0x20, 0x20, 0xA9, 0x02, 0x8D,
//				0x21, 0x20, 0xA2, 0x02, 0xA9, 0x01, 0x71, 0x00, 0xC8, 0xCA, 0xD0, 0xF6, 0x85, 0x02 };
		MapperTest mapper = new MapperTest();
		cpu.setMapper(mapper);
		cpu.cpuInfo.PC = 0x8000;
		
		Instruction[] instructions = new Instruction[] {
			new LDAInstruction(IMMEDIATE, 0x20), null,
			new STAInstruction(ZEROPAGE, 0x00), null,
			new STAInstruction(ZEROPAGE, 0x01), null,
			new LDAInstruction(IMMEDIATE, 0x01), null,
			new STAInstruction(ABSOLUTE, 0x2020), null, null,
			new LDAInstruction(IMMEDIATE, 0x02), null,
			new STAInstruction(ABSOLUTE, 0x2021), null, null,
			new LDXInstruction(IMMEDIATE, 0x02), null,
			new LDAInstruction(IMMEDIATE, 0x01), null,
			new ADCInstruction(INDIRECT_Y, 0x00), null,
			new INYInstruction(IMPLICIT),
			new DEXInstruction(IMPLICIT),
			new BNEInstruction(RELATIVE, 0xF8), null,
			new STAInstruction(ZEROPAGE, 0x02)
		};
		cpu.setRomInstructions(instructions);
		
		// Execute 19 times
		for (int i = 0; i < 19; i++) {
			cpu.tick();
		}
		
		// Check if $02 has value 3 or 2
		int result = cpu.fetchMemory(0x02);
		assertEquals(0x03, result, "Result of ADC must be 3 since last loop Y changed to 1!");
	}
}
