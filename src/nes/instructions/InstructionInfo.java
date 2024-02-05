package instructions;

import java.util.HashMap;

import instructions.alu.ADCInstruction;
import instructions.alu.ANDInstruction;
import instructions.alu.ASLInstruction;
import instructions.alu.BITInstruction;
import instructions.alu.CMPInstruction;
import instructions.alu.CPXInstruction;
import instructions.alu.CPYInstruction;
import instructions.alu.DECInstruction;
import instructions.alu.DEXInstruction;
import instructions.alu.DEYInstruction;
import instructions.alu.EORInstruction;
import instructions.alu.INCInstruction;
import instructions.alu.INXInstruction;
import instructions.alu.INYInstruction;
import instructions.alu.LSRInstruction;
import instructions.alu.ORAInstruction;
import instructions.alu.ROLInstruction;
import instructions.alu.RORInstruction;
import instructions.alu.SBCInstruction;
import instructions.branch.BCCInstruction;
import instructions.branch.BCSInstruction;
import instructions.branch.BEQInstruction;
import instructions.branch.BMIInstruction;
import instructions.branch.BNEInstruction;
import instructions.branch.BPLInstruction;
import instructions.branch.BVCInstruction;
import instructions.branch.BVSInstruction;
import instructions.flags.CLCInstruction;
import instructions.flags.CLDInstruction;
import instructions.flags.CLIInstruction;
import instructions.flags.CLVInstruction;
import instructions.flags.SECInstruction;
import instructions.flags.SEDInstruction;
import instructions.flags.SEIInstruction;
import instructions.jump.BRKInstruction;
import instructions.jump.JMPInstruction;
import instructions.jump.JSRInstruction;
import instructions.jump.RTIInstruction;
import instructions.jump.RTSInstruction;
import instructions.register.TAXInstruction;
import instructions.register.TAYInstruction;
import instructions.register.TSXInstruction;
import instructions.register.TXAInstruction;
import instructions.register.TXSInstruction;
import instructions.register.TYAInstruction;
import instructions.registermemory.LDAInstruction;
import instructions.registermemory.LDXInstruction;
import instructions.registermemory.LDYInstruction;
import instructions.registermemory.STAInstruction;
import instructions.registermemory.STXInstruction;
import instructions.registermemory.STYInstruction;
import instructions.stack.PHAInstruction;
import instructions.stack.PHPInstruction;
import instructions.stack.PLAInstruction;
import instructions.stack.PLPInstruction;

public class InstructionInfo {

	private static final HashMap<Integer, Instruction> instructionMap = new HashMap<Integer, Instruction>();

	private InstructionInfo() {
		// Init of the instruction map
		// ADC
		instructionMap.put(0x69, new ADCInstruction(AddressingMode.IMMEDIATE));
		instructionMap.put(0x65, new ADCInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0x75, new ADCInstruction(AddressingMode.ZEROPAGE_X));
		instructionMap.put(0x6D, new ADCInstruction(AddressingMode.ABSOLUTE));
		instructionMap.put(0x7D, new ADCInstruction(AddressingMode.ABSOLUTE_X));
		instructionMap.put(0x79, new ADCInstruction(AddressingMode.ABSOLUTE_Y));
		instructionMap.put(0x61, new ADCInstruction(AddressingMode.INDIRECT_X));
		instructionMap.put(0x71, new ADCInstruction(AddressingMode.INDIRECT_Y));

		// AND
		instructionMap.put(0x29, new ANDInstruction(AddressingMode.IMMEDIATE));
		instructionMap.put(0x25, new ANDInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0x35, new ANDInstruction(AddressingMode.ZEROPAGE_X));
		instructionMap.put(0x2D, new ANDInstruction(AddressingMode.ABSOLUTE));
		instructionMap.put(0x3D, new ANDInstruction(AddressingMode.ABSOLUTE_X));
		instructionMap.put(0x39, new ANDInstruction(AddressingMode.ABSOLUTE_Y));
		instructionMap.put(0x21, new ANDInstruction(AddressingMode.INDIRECT_X));
		instructionMap.put(0x31, new ANDInstruction(AddressingMode.INDIRECT_Y));

		// ASL
		instructionMap.put(0x0A, new ASLInstruction(AddressingMode.ACCUMULATOR));
		instructionMap.put(0x06, new ASLInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0x16, new ASLInstruction(AddressingMode.ZEROPAGE_X));
		instructionMap.put(0x0E, new ASLInstruction(AddressingMode.ABSOLUTE));
		instructionMap.put(0x1E, new ASLInstruction(AddressingMode.ABSOLUTE_X));

		// BCC
		instructionMap.put(0x90, new BCCInstruction(AddressingMode.RELATIVE));

		// BCS
		instructionMap.put(0xB0, new BCSInstruction(AddressingMode.RELATIVE));

		// BEQ
		instructionMap.put(0xF0, new BEQInstruction(AddressingMode.RELATIVE));

		// BIT
		instructionMap.put(0x24, new BITInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0x2C, new BITInstruction(AddressingMode.ABSOLUTE));

		// BMI
		instructionMap.put(0x30, new BMIInstruction(AddressingMode.RELATIVE));

		// BNE
		instructionMap.put(0xD0, new BNEInstruction(AddressingMode.RELATIVE));

		// BPL
		instructionMap.put(0x10, new BPLInstruction(AddressingMode.RELATIVE));

		// BRK
		instructionMap.put(0x00, new BRKInstruction(AddressingMode.IMPLICIT));

		// BVC
		instructionMap.put(0x50, new BVCInstruction(AddressingMode.RELATIVE));

		// BVS
		instructionMap.put(0x70, new BVSInstruction(AddressingMode.RELATIVE));

		// CLC
		instructionMap.put(0x18, new CLCInstruction(AddressingMode.IMPLICIT));

		// CLD
		instructionMap.put(0xD8, new CLDInstruction(AddressingMode.IMPLICIT));

		// CLI
		instructionMap.put(0x58, new CLIInstruction(AddressingMode.IMPLICIT));

		// CLV
		instructionMap.put(0xB8, new CLVInstruction(AddressingMode.IMPLICIT));

		// CMP
		instructionMap.put(0xC9, new CMPInstruction(AddressingMode.IMMEDIATE));
		instructionMap.put(0xC5, new CMPInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0xD5, new CMPInstruction(AddressingMode.ZEROPAGE_X));
		instructionMap.put(0xCD, new CMPInstruction(AddressingMode.ABSOLUTE));
		instructionMap.put(0xDD, new CMPInstruction(AddressingMode.ABSOLUTE_X));
		instructionMap.put(0xD9, new CMPInstruction(AddressingMode.ABSOLUTE_Y));
		instructionMap.put(0xC1, new CMPInstruction(AddressingMode.INDIRECT_X));
		instructionMap.put(0xD1, new CMPInstruction(AddressingMode.INDIRECT_Y));

		// CPX
		instructionMap.put(0xE0, new CPXInstruction(AddressingMode.IMMEDIATE));
		instructionMap.put(0xE4, new CPXInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0xEC, new CPXInstruction(AddressingMode.ABSOLUTE));

		// CPY
		instructionMap.put(0xC0, new CPYInstruction(AddressingMode.IMMEDIATE));
		instructionMap.put(0xC4, new CPYInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0xCC, new CPYInstruction(AddressingMode.ABSOLUTE));

		// DEC
		instructionMap.put(0xC6, new DECInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0xD6, new DECInstruction(AddressingMode.ZEROPAGE_X));
		instructionMap.put(0xCE, new DECInstruction(AddressingMode.ABSOLUTE));
		instructionMap.put(0xDE, new DECInstruction(AddressingMode.ABSOLUTE_X));

		// DEX
		instructionMap.put(0xCA, new DEXInstruction(AddressingMode.IMPLICIT));
		
		// DEY
		instructionMap.put(0x88, new DEYInstruction(AddressingMode.IMPLICIT));
		
		// EOR
		instructionMap.put(0x49, new EORInstruction(AddressingMode.IMMEDIATE));
		instructionMap.put(0x45, new EORInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0x55, new EORInstruction(AddressingMode.ZEROPAGE_X));
		instructionMap.put(0x4D, new EORInstruction(AddressingMode.ABSOLUTE));
		instructionMap.put(0x5D, new EORInstruction(AddressingMode.ABSOLUTE_X));
		instructionMap.put(0x59, new EORInstruction(AddressingMode.ABSOLUTE_Y));
		instructionMap.put(0x41, new EORInstruction(AddressingMode.INDIRECT_X));
		instructionMap.put(0x51, new EORInstruction(AddressingMode.INDIRECT_Y));
		
		// INC
		instructionMap.put(0xE6, new INCInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0xF6, new INCInstruction(AddressingMode.ZEROPAGE_X));
		instructionMap.put(0xEE, new INCInstruction(AddressingMode.ABSOLUTE));
		instructionMap.put(0xFE, new INCInstruction(AddressingMode.ABSOLUTE_X));
		
		// INX
		instructionMap.put(0xE8, new INXInstruction(AddressingMode.IMPLICIT));
		
		// INY
		instructionMap.put(0xC8, new INYInstruction(AddressingMode.IMPLICIT));
		
		// JMP
		instructionMap.put(0x4C, new JMPInstruction(AddressingMode.ABSOLUTE));
		instructionMap.put(0x6C, new JMPInstruction(AddressingMode.INDIRECT));
		
		// JSR
		instructionMap.put(0x20, new JSRInstruction(AddressingMode.ABSOLUTE));
		
		// LDA
		instructionMap.put(0xA9, new LDAInstruction(AddressingMode.IMMEDIATE));
		instructionMap.put(0xA5, new LDAInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0xB5, new LDAInstruction(AddressingMode.ZEROPAGE_X));
		instructionMap.put(0xAD, new LDAInstruction(AddressingMode.ABSOLUTE));
		instructionMap.put(0xBD, new LDAInstruction(AddressingMode.ABSOLUTE_X));
		instructionMap.put(0xB9, new LDAInstruction(AddressingMode.ABSOLUTE_Y));
		instructionMap.put(0xA1, new LDAInstruction(AddressingMode.INDIRECT_X));
		instructionMap.put(0xB1, new LDAInstruction(AddressingMode.INDIRECT_Y));
		
		// LDX
		instructionMap.put(0xA2, new LDXInstruction(AddressingMode.IMMEDIATE));
		instructionMap.put(0xA6, new LDXInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0xB6, new LDXInstruction(AddressingMode.ZEROPAGE_Y));
		instructionMap.put(0xAE, new LDXInstruction(AddressingMode.ABSOLUTE));
		instructionMap.put(0xBE, new LDXInstruction(AddressingMode.ABSOLUTE_Y));
		
		// LDY
		instructionMap.put(0xA0, new LDYInstruction(AddressingMode.IMMEDIATE));
		instructionMap.put(0xA4, new LDYInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0xB4, new LDYInstruction(AddressingMode.ZEROPAGE_X));
		instructionMap.put(0xAC, new LDYInstruction(AddressingMode.ABSOLUTE));
		instructionMap.put(0xBC, new LDYInstruction(AddressingMode.ABSOLUTE_X));
		
		// LSR
		instructionMap.put(0x4A, new LSRInstruction(AddressingMode.ACCUMULATOR));
		instructionMap.put(0x46, new LSRInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0x56, new LSRInstruction(AddressingMode.ZEROPAGE_X));
		instructionMap.put(0x4E, new LSRInstruction(AddressingMode.ABSOLUTE));
		instructionMap.put(0x5E, new LSRInstruction(AddressingMode.ABSOLUTE_X));
		
		// NOP
		instructionMap.put(0xEA, new NOPInstruction(AddressingMode.IMPLICIT));
		
		// ORA
		instructionMap.put(0x09, new ORAInstruction(AddressingMode.IMMEDIATE));
		instructionMap.put(0x05, new ORAInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0x15, new ORAInstruction(AddressingMode.ZEROPAGE_X));
		instructionMap.put(0x0D, new ORAInstruction(AddressingMode.ABSOLUTE));
		instructionMap.put(0x1D, new ORAInstruction(AddressingMode.ABSOLUTE_X));
		instructionMap.put(0x19, new ORAInstruction(AddressingMode.ABSOLUTE_Y));
		instructionMap.put(0x01, new ORAInstruction(AddressingMode.INDIRECT_X));
		instructionMap.put(0x11, new ORAInstruction(AddressingMode.INDIRECT_Y));
		
		// PHA
		instructionMap.put(0x48, new PHAInstruction(AddressingMode.IMPLICIT));
		
		// PHP
		instructionMap.put(0x08, new PHPInstruction(AddressingMode.IMPLICIT));
		
		// PLA
		instructionMap.put(0x68, new PLAInstruction(AddressingMode.IMPLICIT));
		
		// PLP
		instructionMap.put(0x28, new PLPInstruction(AddressingMode.IMPLICIT));
		
		// ROL
		instructionMap.put(0x2A, new ROLInstruction(AddressingMode.ACCUMULATOR));
		instructionMap.put(0x26, new ROLInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0x36, new ROLInstruction(AddressingMode.ZEROPAGE_X));
		instructionMap.put(0x2E, new ROLInstruction(AddressingMode.ABSOLUTE));
		instructionMap.put(0x3E, new ROLInstruction(AddressingMode.ABSOLUTE_X));
		
		// ROR
		instructionMap.put(0x6A, new RORInstruction(AddressingMode.ACCUMULATOR));
		instructionMap.put(0x66, new RORInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0x76, new RORInstruction(AddressingMode.ZEROPAGE_X));
		instructionMap.put(0x6E, new RORInstruction(AddressingMode.ABSOLUTE));
		instructionMap.put(0x7E, new RORInstruction(AddressingMode.ABSOLUTE_X));
		
		// RTI
		instructionMap.put(0x40, new RTIInstruction(AddressingMode.IMPLICIT));
		
		// RTS
		instructionMap.put(0x60, new RTSInstruction(AddressingMode.IMPLICIT));
		
		// SBC
		instructionMap.put(0xE9, new SBCInstruction(AddressingMode.IMMEDIATE));
		instructionMap.put(0xE5, new SBCInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0xF5, new SBCInstruction(AddressingMode.ZEROPAGE_X));
		instructionMap.put(0xED, new SBCInstruction(AddressingMode.ABSOLUTE));
		instructionMap.put(0xFD, new SBCInstruction(AddressingMode.ABSOLUTE_X));
		instructionMap.put(0xF9, new SBCInstruction(AddressingMode.ABSOLUTE_Y));
		instructionMap.put(0xE1, new SBCInstruction(AddressingMode.INDIRECT_X));
		instructionMap.put(0xF1, new SBCInstruction(AddressingMode.INDIRECT_Y));
		
		// SEC
		instructionMap.put(0x38, new SECInstruction(AddressingMode.IMPLICIT));
		
		// SED
		instructionMap.put(0xF8, new SEDInstruction(AddressingMode.IMPLICIT));
		
		// SEI
		instructionMap.put(0x78, new SEIInstruction(AddressingMode.IMPLICIT));
		
		// STA
		instructionMap.put(0x85, new STAInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0x95, new STAInstruction(AddressingMode.ZEROPAGE_X));
		instructionMap.put(0x8D, new STAInstruction(AddressingMode.ABSOLUTE));
		instructionMap.put(0x9D, new STAInstruction(AddressingMode.ABSOLUTE_X));
		instructionMap.put(0x99, new STAInstruction(AddressingMode.ABSOLUTE_Y));
		instructionMap.put(0x81, new STAInstruction(AddressingMode.INDIRECT_X));
		instructionMap.put(0x91, new STAInstruction(AddressingMode.INDIRECT_Y));
		
		// STX
		instructionMap.put(0x86, new STXInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0x96, new STXInstruction(AddressingMode.ZEROPAGE_Y));
		instructionMap.put(0x8E, new STXInstruction(AddressingMode.ABSOLUTE));
		
		// STY
		instructionMap.put(0x84, new STYInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0x94, new STYInstruction(AddressingMode.ZEROPAGE_X));
		instructionMap.put(0x8C, new STYInstruction(AddressingMode.ABSOLUTE));
		
		// TAX
		instructionMap.put(0xAA, new TAXInstruction(AddressingMode.IMPLICIT));
		
		// TAY
		instructionMap.put(0xA8, new TAYInstruction(AddressingMode.IMPLICIT));
		
		// TSX
		instructionMap.put(0xBA, new TSXInstruction(AddressingMode.IMPLICIT));
		
		// TXA
		instructionMap.put(0x8A, new TXAInstruction(AddressingMode.IMPLICIT));
		
		// TXS
		instructionMap.put(0x9A, new TXSInstruction(AddressingMode.IMPLICIT));
		
		// TYA
		instructionMap.put(0x98, new TYAInstruction(AddressingMode.IMPLICIT));
	}

	public static HashMap<Integer, Instruction> getInstructionMap() {
		return instructionMap;
	}

}
