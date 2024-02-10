package nes.instructions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import instructions.AddressingMode;
import instructions.Instruction;
import instructions.InstructionInfo;
import instructions.NOPInstruction;
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

public class InstructionInfoTest {

	private static Map<Integer, Instruction> instructionMap = InstructionInfo.getInstance().getInstructionMap();

	@ParameterizedTest
	@CsvSource(value = { "IMMEDIATE, 0x69", "ZEROPAGE, 0x65", "ZEROPAGE_X, 0x75", "ABSOLUTE, 0x6D", "ABSOLUTE_X, 0x7D",
			"ABSOLUTE_Y, 0x79", "INDIRECT_X, 0x61", "INDIRECT_Y, 0x71" })
	void testInfoADC(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new ADCInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMMEDIATE, 0x29", "ZEROPAGE, 0x25", "ZEROPAGE_X, 0x35", "ABSOLUTE, 0x2D", "ABSOLUTE_X, 0x3D",
			"ABSOLUTE_Y, 0x39", "INDIRECT_X, 0x21", "INDIRECT_Y, 0x31" })
	void testInfoAND(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new ANDInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "ACCUMULATOR, 0x0A", "ZEROPAGE, 0x06", "ZEROPAGE_X, 0x16", "ABSOLUTE, 0x0E",
			"ABSOLUTE_X, 0x1E" })
	void testInfoASL(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new ASLInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "RELATIVE, 0x90" })
	void testInfoBCC(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new BCCInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "RELATIVE, 0xB0" })
	void testInfoBCS(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new BCSInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "RELATIVE, 0xF0" })
	void testInfoBEQ(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new BEQInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "ZEROPAGE, 0x24", "ABSOLUTE, 0x2C" })
	void testInfoBIT(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new BITInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "RELATIVE, 0x30" })
	void testInfoBMI(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new BMIInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "RELATIVE, 0xD0" })
	void testInfoBNE(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new BNEInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "RELATIVE, 0x10" })
	void testInfoBPL(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new BPLInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0x00" })
	void testInfoBRK(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new BRKInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "RELATIVE, 0x50" })
	void testInfoBVC(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new BVCInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "RELATIVE, 0x70" })
	void testInfoBVS(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new BVSInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0x18" })
	void testInfoCLC(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new CLCInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0xD8" })
	void testInfoCLD(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new CLDInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0x58" })
	void testInfoCLI(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new CLIInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0xB8" })
	void testInfoCLV(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new CLVInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMMEDIATE, 0xC9", "ZEROPAGE, 0xC5", "ZEROPAGE_X, 0xD5", "ABSOLUTE, 0xCD", "ABSOLUTE_X, 0xDD",
			"ABSOLUTE_Y, 0xD9", "INDIRECT_X, 0xC1", "INDIRECT_Y, 0xD1" })
	void testInfoCMP(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new CMPInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMMEDIATE, 0xE0", "ZEROPAGE, 0xE4", "ABSOLUTE, 0xEC" })
	void testInfoCPX(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new CPXInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMMEDIATE, 0xC0", "ZEROPAGE, 0xC4", "ABSOLUTE, 0xCC" })
	void testInfoCPY(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new CPYInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "ZEROPAGE, 0xC6", "ZEROPAGE_X, 0xD6", "ABSOLUTE, 0xCE", "ABSOLUTE_X, 0xDE" })
	void testInfoDEC(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new DECInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0xCA" })
	void testInfoDEX(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new DEXInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0x88" })
	void testInfoDEY(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new DEYInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMMEDIATE, 0x49", "ZEROPAGE, 0x45", "ZEROPAGE_X, 0x55", "ABSOLUTE, 0x4D", "ABSOLUTE_X, 0x5D",
			"ABSOLUTE_Y, 0x59", "INDIRECT_X, 0x41", "INDIRECT_Y, 0x51" })
	void testInfoEOR(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new EORInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "ZEROPAGE, 0xE6", "ZEROPAGE_X, 0xF6", "ABSOLUTE, 0xEE", "ABSOLUTE_X, 0xFE" })
	void testInfoINC(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new INCInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0xE8" })
	void testInfoINX(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new INXInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0xC8" })
	void testInfoINY(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new INYInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "ABSOLUTE, 0x4C", "INDIRECT, 0x6C" })
	void testInfoJMP(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new JMPInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "ABSOLUTE, 0x20" })
	void testInfoJSR(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new JSRInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMMEDIATE, 0xA9", "ZEROPAGE, 0xA5", "ZEROPAGE_X, 0xB5", "ABSOLUTE, 0xAD", "ABSOLUTE_X, 0xBD",
			"ABSOLUTE_Y, 0xB9", "INDIRECT_X, 0xA1", "INDIRECT_Y, 0xB1" })
	void testInfoLDA(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new LDAInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMMEDIATE, 0xA2", "ZEROPAGE, 0xA6", "ZEROPAGE_Y, 0xB6", "ABSOLUTE, 0xAE",
			"ABSOLUTE_Y, 0xBE" })
	void testInfoLDX(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new LDXInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMMEDIATE, 0xA0", "ZEROPAGE, 0xA4", "ZEROPAGE_X, 0xB4", "ABSOLUTE, 0xAC",
			"ABSOLUTE_X, 0xBC" })
	void testInfoLDY(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new LDYInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "ACCUMULATOR, 0x4A", "ZEROPAGE, 0x46", "ZEROPAGE_X, 0x56", "ABSOLUTE, 0x4E",
			"ABSOLUTE_X, 0x5E" })
	void testInfoLSR(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new LSRInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0xEA" })
	void testInfoNOP(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new NOPInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMMEDIATE, 0x09", "ZEROPAGE, 0x05", "ZEROPAGE_X, 0x15", "ABSOLUTE, 0x0D", "ABSOLUTE_X, 0x1D",
			"ABSOLUTE_Y, 0x19", "INDIRECT_X, 0x01", "INDIRECT_Y, 0x11" })
	void testInfoORA(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new ORAInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0x48" })
	void testInfoPHA(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new PHAInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0x08" })
	void testInfoPHP(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new PHPInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0x68" })
	void testInfoPLA(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new PLAInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0x28" })
	void testInfoPLP(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new PLPInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "ACCUMULATOR, 0x2A", "ZEROPAGE, 0x26", "ZEROPAGE_X, 0x36", "ABSOLUTE, 0x2E",
			"ABSOLUTE_X, 0x3E" })
	void testInfoROL(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new ROLInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "ACCUMULATOR, 0x6A", "ZEROPAGE, 0x66", "ZEROPAGE_X, 0x76", "ABSOLUTE, 0x6E",
			"ABSOLUTE_X, 0x7E" })
	void testInfoROR(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new RORInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0x40" })
	void testInfoRTI(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new RTIInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0x60" })
	void testInfoRTS(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new RTSInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMMEDIATE, 0xE9", "ZEROPAGE, 0xE5", "ZEROPAGE_X, 0xF5", "ABSOLUTE, 0xED", "ABSOLUTE_X, 0xFD",
			"ABSOLUTE_Y, 0xF9", "INDIRECT_X, 0xE1", "INDIRECT_Y, 0xF1" })
	void testInfoSBC(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new SBCInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0x38" })
	void testInfoSEC(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new SECInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0xF8" })
	void testInfoSED(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new SEDInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0x78" })
	void testInfoSEI(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new SEIInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "ZEROPAGE, 0x85", "ZEROPAGE_X, 0x95", "ABSOLUTE, 0x8D", "ABSOLUTE_X, 0x9D", "ABSOLUTE_Y, 0x99",
			"INDIRECT_X, 0x81", "INDIRECT_Y, 0x91" })
	void testInfoSTA(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new STAInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "ZEROPAGE, 0x86", "ZEROPAGE_Y, 0x96", "ABSOLUTE, 0x8E" })
	void testInfoSTX(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new STXInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "ZEROPAGE, 0x84", "ZEROPAGE_X, 0x94", "ABSOLUTE, 0x8C" })
	void testInfoSTY(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new STYInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0xAA" })
	void testInfoTAX(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new TAXInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0xA8" })
	void testInfoTAY(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new TAYInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0xBA" })
	void testInfoTSX(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new TSXInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0x8A" })
	void testInfoTXA(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new TXAInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0x9A" })
	void testInfoTXS(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new TXSInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}

	@ParameterizedTest
	@CsvSource(value = { "IMPLICIT, 0x98" })
	void testInfoTYA(AddressingMode mode, int opcode) {
		Instruction expectedInstruction = new TYAInstruction(mode);
		Instruction gotInstruction = instructionMap.get(opcode);

		assertEquals(expectedInstruction, gotInstruction);
	}
}
