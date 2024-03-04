package instructions;

import static instructions.AddressingMode.ABSOLUTE;
import static instructions.AddressingMode.ACCUMULATOR;
import static instructions.AddressingMode.IMMEDIATE;
import static instructions.AddressingMode.IMPLICIT;
import static instructions.AddressingMode.RELATIVE;
import static instructions.AddressingMode.ZEROPAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import components.Cpu;
import components.CpuInfo;
import exceptions.InstructionNotSupportedException;
import instructions.alu.ADCInstruction;
import instructions.alu.ANDInstruction;
import instructions.alu.ASLInstruction;
import instructions.alu.AluInstruction;
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
import instructions.branch.BranchInstruction;
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
import instructions.register.TransferInstruction;
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
import utils.MapperTest;

@FunctionalInterface
interface TriIntFunction<T, U, V> {
	int apply(T t, U u, V v);
}

@FunctionalInterface
interface TesserIntFunction<T, U, V, W> {
	int apply(T t, U u, V v, W w);
}

class TestInstructions {

	static final Cpu cpu = Cpu.getInstance();

	@BeforeAll
	static void init() {
		// Set mapper
		cpu.setMapper(new MapperTest());

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

		// Reset all memory
		for (int address = 0; address <= 0xFFFF; address++) {
			cpu.storeMemory(address, 0);
		}
	}

	@Nested
	class TestAluInstructions {

		private void resetCpu() {
			// Registers at 0
			cpu.cpuInfo.A = 0;
			cpu.cpuInfo.X = 0;
			cpu.cpuInfo.Y = 0;
			cpu.cpuInfo.SP = 0;
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

		// From hardware:
		// V = not (((A7 NOR B7) and C6) NOR ((A7 NAND B7) NOR C6))
		private int evaluateADCV(int operand1, int operand2, int carry) {
			int A7 = (operand1 & 0b10000000) >> 7;
			int B7 = (operand2 & 0b10000000) >> 7;

			// To get the 6th carry, we compute as if the 7th bit was 0.
			// We use carry evaluation to say if carry or not...
			// But we need to shift to the left as if we had 8 bits!
			int C6 = ((operand1 & 0x7F) + (operand2 & 0x7F) + carry) >> 7;

			return (~(A7 | B7) & C6) | ~(~(A7 & B7) | C6);
		}

		private int evaluateSBCV(int operand1, int operand2, int carry) {
			return evaluateADCV(operand1, 255 - operand2, carry);
		}

		/**
		 * Used by instructions like ADC
		 */
		private Collection<DynamicTest> getTestsAluInstructionOverflow(AluInstruction instruction,
				TriIntFunction<Integer, Integer, Integer> function, IntFunction<Integer> carryEvaluation,
				TriIntFunction<Integer, Integer, Integer> overflowEvaluation) {
			List<DynamicTest> tests = new ArrayList<DynamicTest>();

			for (int operand1 = 0; operand1 <= 0xFF; operand1++) {
				for (int operand2 = 0; operand2 <= 0xFF; operand2++) {
					resetCpu();
					cpu.cpuInfo.A = operand1;
					instruction = instruction.newInstruction(operand2);

					// Without carry
					try {
						instruction.execute();
					} catch (InstructionNotSupportedException e) {
						e.printStackTrace();
					}

					int rawValue = function.apply(operand1, operand2, 0);
					int expectedValue = rawValue & 0xFF;
					int expectedC = carryEvaluation.apply(rawValue);
					int expectedZ = expectedValue == 0 ? 1 : 0;
					int expectedN = expectedValue >= 0x80 ? 1 : 0;
					int expectedV = overflowEvaluation.apply(operand1, operand2, 0);

					int gotValue = cpu.cpuInfo.A;
					int gotC = cpu.cpuInfo.C;
					int gotZ = cpu.cpuInfo.Z;
					int gotN = cpu.cpuInfo.N;
					int gotV = cpu.cpuInfo.V;

					// With carry (redo everything)
					cpu.cpuInfo.C = 1;
					cpu.cpuInfo.A = operand1;
					instruction = instruction.newInstruction(operand2);
					try {
						instruction.execute();
					} catch (InstructionNotSupportedException e) {
						e.printStackTrace();
					}

					int rawCarryValue = function.apply(operand1, operand2, 1);
					int expectedCarryValue = rawCarryValue & 0xFF;
					int expectedCarryC = carryEvaluation.apply(rawCarryValue);
					int expectedCarryZ = expectedCarryValue == 0 ? 1 : 0;
					int expectedCarryN = expectedCarryValue >= 0x80 ? 1 : 0;
					int expectedCarryV = overflowEvaluation.apply(operand1, operand2, 1);

					int gotCarryValue = cpu.cpuInfo.A;
					int gotCarryC = cpu.cpuInfo.C;
					int gotCarryZ = cpu.cpuInfo.Z;
					int gotCarryN = cpu.cpuInfo.N;
					int gotCarryV = cpu.cpuInfo.V;

					tests.add(DynamicTest.dynamicTest(String.format("0x%X 0x%X", operand1, operand2), () -> {
						// Without carry
						assertEquals(expectedValue, gotValue, "Value given by the CPU is wrong");
						assertEquals(expectedC, gotC, "Carry flag wrong");
						assertEquals(expectedZ, gotZ, "Zero flag wrong");
						assertEquals(expectedN, gotN, "Negative flag wrong");
						assertEquals(expectedV, gotV, "Overflow flag wrong");

						// With carry
						assertEquals(expectedCarryValue, gotCarryValue, "Value given by the CPU is wrong (carry)");
						assertEquals(expectedCarryC, gotCarryC, "Carry flag wrong (carry)");
						assertEquals(expectedCarryZ, gotCarryZ, "Zero flag wrong (carry)");
						assertEquals(expectedCarryN, gotCarryN, "Negative flag wrong (carry)");
						assertEquals(expectedCarryV, gotCarryV, "Overflow flag wrong (carry)");
					}));
				}
			}

			return tests;
		}

		/**
		 * Used by instructions like AND
		 */
		private Collection<DynamicTest> getTestsAluInstructionLogic(AluInstruction instruction,
				BiFunction<Integer, Integer, Integer> logicFunction) {
			List<DynamicTest> tests = new ArrayList<DynamicTest>();

			for (int operand1 = 0; operand1 <= 0xFF; operand1++) {
				for (int operand2 = 0; operand2 <= 0xFF; operand2++) {
					resetCpu();
					cpu.cpuInfo.A = operand1;
					instruction = instruction.newInstruction(operand2);

					try {
						instruction.execute();
					} catch (InstructionNotSupportedException e) {
						e.printStackTrace();
					}

					int rawValue = logicFunction.apply(operand1, operand2);
					int expectedValue = rawValue & 0xFF;
					int expectedZ = expectedValue == 0 ? 1 : 0;
					int expectedN = expectedValue >= 0x80 ? 1 : 0;

					int gotValue = cpu.cpuInfo.A;
					int gotZ = cpu.cpuInfo.Z;
					int gotN = cpu.cpuInfo.N;

					tests.add(DynamicTest.dynamicTest(String.format("0x%X 0x%X", operand1, operand2), () -> {
						assertEquals(expectedValue, gotValue, "Value given by the CPU is wrong");
						assertEquals(expectedZ, gotZ, "Zero flag wrong");
						assertEquals(expectedN, gotN, "Negative flag wrong");
					}));
				}
			}

			return tests;
		}

		/**
		 * Used by instructions like ASL
		 */
		private Collection<DynamicTest> getTestsAluInstructionAccumulator(AluInstruction instruction,
				Consumer<CpuInfo> flagSet, IntFunction<Integer> function, IntFunction<Integer> carryEvaluation) {
			List<DynamicTest> tests = new ArrayList<DynamicTest>();

			for (int operand1 = 0; operand1 <= 0xFF; operand1++) {
				resetCpu();
				cpu.cpuInfo.A = operand1;
				flagSet.accept(cpu.cpuInfo);

				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				int rawValue = function.apply(operand1);
				int expectedValue = rawValue & 0xFF;
				int expectedC = carryEvaluation.apply(operand1);
				int expectedZ = expectedValue == 0 ? 1 : 0;
				int expectedN = expectedValue >= 0x80 ? 1 : 0;

				int gotValue = cpu.cpuInfo.A;
				int gotC = cpu.cpuInfo.C;
				int gotZ = cpu.cpuInfo.Z;
				int gotN = cpu.cpuInfo.N;

				tests.add(DynamicTest.dynamicTest(String.format("0x%X", operand1), () -> {
					assertEquals(expectedValue, gotValue, "Value given by the CPU is wrong");
					assertEquals(expectedC, gotC, "Carry flag wrong");
					assertEquals(expectedZ, gotZ, "Zero flag wrong");
					assertEquals(expectedN, gotN, "Negative flag wrong");
				}));

			}

			return tests;
		}

		/**
		 * Used by CMP, CPX and CPY
		 */
		private Collection<DynamicTest> getTestsAluInstructionCompare(AluInstruction instruction,
				IntConsumer valueUpdate) {
			List<DynamicTest> tests = new ArrayList<DynamicTest>();

			for (int operand1 = 0; operand1 <= 0xFF; operand1++) {
				for (int operand2 = 0; operand2 <= 0xFF; operand2++) {
					resetCpu();
					valueUpdate.accept(operand1);

					instruction = instruction.newInstruction(operand2);
					try {
						instruction.execute();
					} catch (InstructionNotSupportedException e) {
						e.printStackTrace();
					}

					int value = operand1 - operand2;
					int expectedC = value >= 0 ? 1 : 0;
					int expectedZ = value == 0 ? 1 : 0;
					int expectedN = (value >= 0x80) || (value < 0) ? 1 : 0;

					int gotC = cpu.cpuInfo.C;
					int gotZ = cpu.cpuInfo.Z;
					int gotN = cpu.cpuInfo.N;

					tests.add(DynamicTest.dynamicTest(String.format("0x%X 0x%X", operand1, operand2), () -> {
						assertEquals(expectedC, gotC, "Carry flag wrong");
						assertEquals(expectedZ, gotZ, "Zero flag wrong");
						assertEquals(expectedN, gotN, "Negative flag wrong");
					}));

				}
			}

			return tests;
		}

		private Collection<DynamicTest> getTestsAluInstructionMemoryDecInc(AluInstruction instruction,
				IntFunction<Integer> function) {
			int zeroPageAddress = 0x10;
			// Reset a spot in zero page for test (0x10)
			cpu.storeMemory(zeroPageAddress, 0);

			List<DynamicTest> tests = new ArrayList<DynamicTest>();
			for (int value = 0; value <= 0xFF; value++) {
				resetCpu();
				cpu.storeMemory(zeroPageAddress, value);

				// Execute instruction
				instruction = instruction.newInstruction(zeroPageAddress);
				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				// Test flags
				int expectedResult = function.apply(value) & 0xFF;
				int expectedZ = expectedResult == 0 ? 1 : 0;
				int expectedN = (expectedResult >= 0x80) || (expectedResult < 0) ? 1 : 0;

				int gotResult = cpu.fetchMemory(zeroPageAddress);
				int gotZ = cpu.cpuInfo.Z;
				int gotN = cpu.cpuInfo.N;

				tests.add(DynamicTest.dynamicTest(String.format("0x%X", value), () -> {
					assertEquals(expectedResult, gotResult, "Results should be the same");
					assertEquals(expectedZ, gotZ, "Zero flag wrong");
					assertEquals(expectedN, gotN, "Negative flag wrong");
				}));
			}

			return tests;
		}

		private Collection<DynamicTest> getTestsAluInstructionRegisterDecInc(AluInstruction instruction,
				IntConsumer registerUpdate, IntFunction<Integer> function, IntSupplier registerSupplier) {
			List<DynamicTest> tests = new ArrayList<DynamicTest>();

			for (int value = 0; value <= 0xFF; value++) {
				resetCpu();

				// Update regiser
				registerUpdate.accept(value);

				// Execute instruction (implicit so no new)
				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				// Test flags
				int expectedResult = (function.apply(value)) & 0xFF;
				int expectedZ = expectedResult == 0 ? 1 : 0;
				int expectedN = (expectedResult >= 0x80) || (expectedResult < 0) ? 1 : 0;

				int gotResult = registerSupplier.getAsInt();
				int gotZ = cpu.cpuInfo.Z;
				int gotN = cpu.cpuInfo.N;

				tests.add(DynamicTest.dynamicTest(String.format("0x%X", value), () -> {
					assertEquals(expectedResult, gotResult, "Results should be the same");
					assertEquals(expectedZ, gotZ, "Zero flag wrong");
					assertEquals(expectedN, gotN, "Negative flag wrong");
				}));
			}

			return tests;
		}

		@TestFactory
		Collection<DynamicTest> ADC() {
			return getTestsAluInstructionOverflow(new ADCInstruction(IMMEDIATE), (a, b, C) -> a + b + C,
					value -> value > 0xFF ? 1 : 0, (a, b, C) -> evaluateADCV(a, b, C));
		}

		@TestFactory
		Collection<DynamicTest> AND() {
			return getTestsAluInstructionLogic(new ANDInstruction(IMMEDIATE), (a, b) -> a & b);
		}

		@TestFactory
		Collection<DynamicTest> ASL() {
			return getTestsAluInstructionAccumulator(new ASLInstruction(ACCUMULATOR), cpuInfo -> {
			}, a -> a << 1, a -> a << 1 > 0xFF ? 1 : 0);
		}

		@TestFactory
		Collection<DynamicTest> BIT() {
			int zeroPageAddress = 0x10;
			// Reset a spot in zero page for test (0x10)
			cpu.storeMemory(zeroPageAddress, 0);

			List<DynamicTest> tests = new ArrayList<DynamicTest>();
			for (int operand1 = 0; operand1 <= 0xFF; operand1++) {
				for (int operand2 = 0; operand2 <= 0xFF; operand2++) {
					resetCpu();

					cpu.cpuInfo.A = operand1;
					cpu.storeMemory(zeroPageAddress, operand2);

					// Execute instruction
					BITInstruction instruction = new BITInstruction(ZEROPAGE, zeroPageAddress);
					try {
						instruction.execute();
					} catch (InstructionNotSupportedException e) {
						e.printStackTrace();
					}

					// Test flags
					int result = operand1 & operand2;
					int expectedZ = result == 0 ? 1 : 0;
					int expectedV = (result & 0b01000000) != 0 ? 1 : 0;
					int expectedN = (result & 0b10000000) != 0 ? 1 : 0;

					int gotZ = cpu.cpuInfo.Z;
					int gotV = cpu.cpuInfo.V;
					int gotN = cpu.cpuInfo.N;

					tests.add(DynamicTest.dynamicTest(String.format("0x%X 0x%X", operand1, operand2), () -> {
						assertEquals(expectedZ, gotZ, "Zero flag wrong");
						assertEquals(expectedV, gotV, "Overflow flag wrong");
						assertEquals(expectedN, gotN, "Negative flag wrong");
					}));
				}
			}

			return tests;
		}

		@TestFactory
		Collection<DynamicTest> CMP() {
			return getTestsAluInstructionCompare(new CMPInstruction(IMMEDIATE), value -> cpu.cpuInfo.A = value);
		}

		@TestFactory
		Collection<DynamicTest> CPX() {
			return getTestsAluInstructionCompare(new CPXInstruction(IMMEDIATE), value -> cpu.cpuInfo.X = value);
		}

		@TestFactory
		Collection<DynamicTest> CPY() {
			return getTestsAluInstructionCompare(new CPYInstruction(IMMEDIATE), value -> cpu.cpuInfo.Y = value);
		}

		@TestFactory
		Collection<DynamicTest> DEC() {
			return getTestsAluInstructionMemoryDecInc(new DECInstruction(ZEROPAGE), value -> value - 1);
		}

		@TestFactory
		Collection<DynamicTest> DEX() {
			return getTestsAluInstructionRegisterDecInc(new DEXInstruction(IMPLICIT), value -> cpu.cpuInfo.X = value,
					value -> value - 1, () -> cpu.cpuInfo.X);
		}

		@TestFactory
		Collection<DynamicTest> DEY() {
			return getTestsAluInstructionRegisterDecInc(new DEYInstruction(IMPLICIT), value -> cpu.cpuInfo.Y = value,
					value -> value - 1, () -> cpu.cpuInfo.Y);
		}

		@TestFactory
		Collection<DynamicTest> EOR() {
			return getTestsAluInstructionLogic(new EORInstruction(IMMEDIATE), (a, b) -> a ^ b);
		}

		@TestFactory
		Collection<DynamicTest> INC() {
			return getTestsAluInstructionMemoryDecInc(new INCInstruction(ZEROPAGE), value -> value + 1);
		}

		@TestFactory
		Collection<DynamicTest> INX() {
			return getTestsAluInstructionRegisterDecInc(new INXInstruction(IMPLICIT), value -> cpu.cpuInfo.X = value,
					value -> value + 1, () -> cpu.cpuInfo.X);
		}

		@TestFactory
		Collection<DynamicTest> INY() {
			return getTestsAluInstructionRegisterDecInc(new INYInstruction(IMPLICIT), value -> cpu.cpuInfo.Y = value,
					value -> value + 1, () -> cpu.cpuInfo.Y);
		}

		@TestFactory
		Collection<DynamicTest> LSR() {
			return getTestsAluInstructionAccumulator(new LSRInstruction(ACCUMULATOR), cpuInfo -> {
			}, a -> a >> 1, a -> a & 1);
		}

		@TestFactory
		Collection<DynamicTest> ORA() {
			return getTestsAluInstructionLogic(new ORAInstruction(IMMEDIATE), (a, b) -> a | b);
		}

		@TestFactory
		Collection<DynamicTest> ROL() {
			return getTestsAluInstructionAccumulator(new ROLInstruction(ACCUMULATOR), cpuInfo -> cpuInfo.C = 1,
					a -> (a << 1) | 1, a -> a << 1 > 0xFF ? 1 : 0);
		}

		@TestFactory
		Collection<DynamicTest> ROR() {
			return getTestsAluInstructionAccumulator(new RORInstruction(ACCUMULATOR), cpuInfo -> cpuInfo.C = 1,
					a -> (1 << 7) | (a >> 1), a -> a & 1);
		}

		@TestFactory
		Collection<DynamicTest> SBC() {
			return getTestsAluInstructionOverflow(new SBCInstruction(IMMEDIATE), (a, b, C) -> a + (255 - b) + C,
					value -> value > 0xFF ? 0 : 1, (a, b, C) -> evaluateSBCV(a, b, C));
		}
	}

	@Nested
	class TestBranchInstructions {

		private Collection<DynamicTest> generateTestsBranchInstruction(BranchInstruction instruction,
				Consumer<CpuInfo> branchCondition, Consumer<CpuInfo> nonBranchCondition, int PC) {
			List<DynamicTest> tests = new ArrayList<DynamicTest>();

			for (int offset = 0; offset <= 0xFF; offset++) {
				// Reset PC
				cpu.cpuInfo.PC = PC;

				// Create instruction
				instruction = instruction.newInstruction(offset);

				/* NO BRANCH */

				// Force not branch condition
				nonBranchCondition.accept(cpu.cpuInfo);

				// Execute instruction
				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				// Got values
				int gotPC = cpu.cpuInfo.PC;

				// Adding test
				tests.add(DynamicTest.dynamicTest(String.format("0x%X no branch", offset),
						() -> assertEquals(PC, gotPC, "PC should not have been updated")));

				/* BRANCH */

				// Force branch condition
				branchCondition.accept(cpu.cpuInfo);

				// Execute instruction
				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				// Expected values
				int signedOffset = offset > 0x7F ? offset - 256 : offset;
				int expectedPC = (PC + signedOffset) & 0xFFFF;

				// Got values
				int gotBranchPC = cpu.cpuInfo.PC;

				// Adding test
				tests.add(DynamicTest.dynamicTest(String.format("0x%X branch", offset),
						() -> assertEquals(expectedPC, gotBranchPC, "PC should have been updated")));
			}

			return tests;
		}

		/**
		 * Branch testing. Testing 3 times : $0000, $FF80 and $8000.
		 * 
		 * @param instruction        the instruction to test
		 * @param branchCondition    the function that will enable branching
		 * @param nonBranchCondition the function that will disable branching
		 */
		private Collection<DynamicTest> getTestsBranchInstruction(BranchInstruction instruction,
				Consumer<CpuInfo> branchCondition, Consumer<CpuInfo> nonBranchCondition) {
			List<DynamicTest> tests = new ArrayList<DynamicTest>();
			tests.addAll(generateTestsBranchInstruction(instruction, branchCondition, nonBranchCondition, 0x0000));
			tests.addAll(generateTestsBranchInstruction(instruction, branchCondition, nonBranchCondition, 0x8000));
			tests.addAll(generateTestsBranchInstruction(instruction, branchCondition, nonBranchCondition, 0xFF80));

			return tests;
		}

		@TestFactory
		Collection<DynamicTest> BCC() {
			return getTestsBranchInstruction(new BCCInstruction(RELATIVE), cpuInfo -> cpuInfo.C = 0,
					cpuInfo -> cpuInfo.C = 1);
		}

		@TestFactory
		Collection<DynamicTest> BCS() {
			return getTestsBranchInstruction(new BCSInstruction(RELATIVE), cpuInfo -> cpuInfo.C = 1,
					cpuInfo -> cpuInfo.C = 0);
		}

		@TestFactory
		Collection<DynamicTest> BNE() {
			return getTestsBranchInstruction(new BNEInstruction(RELATIVE), cpuInfo -> cpuInfo.Z = 0,
					cpuInfo -> cpuInfo.Z = 1);
		}

		@TestFactory
		Collection<DynamicTest> BEQ() {
			return getTestsBranchInstruction(new BEQInstruction(RELATIVE), cpuInfo -> cpuInfo.Z = 1,
					cpuInfo -> cpuInfo.Z = 0);
		}

		@TestFactory
		Collection<DynamicTest> BPL() {
			return getTestsBranchInstruction(new BPLInstruction(RELATIVE), cpuInfo -> cpuInfo.N = 0,
					cpuInfo -> cpuInfo.N = 1);
		}

		@TestFactory
		Collection<DynamicTest> BMI() {
			return getTestsBranchInstruction(new BMIInstruction(RELATIVE), cpuInfo -> cpuInfo.N = 1,
					cpuInfo -> cpuInfo.N = 0);
		}

		@TestFactory
		Collection<DynamicTest> BVC() {
			return getTestsBranchInstruction(new BVCInstruction(RELATIVE), cpuInfo -> cpuInfo.V = 0,
					cpuInfo -> cpuInfo.V = 1);
		}

		@TestFactory
		Collection<DynamicTest> BVS() {
			return getTestsBranchInstruction(new BVSInstruction(RELATIVE), cpuInfo -> cpuInfo.V = 1,
					cpuInfo -> cpuInfo.V = 0);
		}
	}

	@Nested
	class TestFlagInstructions {

		@Test
		void CLC() {
			// Set C to 1
			cpu.cpuInfo.C = 1;

			// Execute CLC
			CLCInstruction instruction = new CLCInstruction(IMPLICIT);
			try {
				instruction.execute();
			} catch (InstructionNotSupportedException e) {
				e.printStackTrace();
			}

			// Test
			assertEquals(0, cpu.cpuInfo.C, "C must be 0");
		}

		@Test
		void CLD() {
			// Set D to 1
			cpu.cpuInfo.D = 1;

			// Execute CLD
			CLDInstruction instruction = new CLDInstruction(IMPLICIT);
			try {
				instruction.execute();
			} catch (InstructionNotSupportedException e) {
				e.printStackTrace();
			}

			// Test
			assertEquals(0, cpu.cpuInfo.D, "D must be 0");
		}

		@Test
		void CLI() {
			// Set I to 1
			cpu.cpuInfo.I = 1;

			// Execute CLI
			CLIInstruction instruction = new CLIInstruction(IMPLICIT);
			try {
				instruction.execute();
			} catch (InstructionNotSupportedException e) {
				e.printStackTrace();
			}

			// Test
			assertEquals(0, cpu.cpuInfo.I, "I must be 0");
		}

		@Test
		void CLV() {
			// Set V to 1
			cpu.cpuInfo.V = 1;

			// Execute CLV
			CLVInstruction instruction = new CLVInstruction(IMPLICIT);
			try {
				instruction.execute();
			} catch (InstructionNotSupportedException e) {
				e.printStackTrace();
			}

			// Test
			assertEquals(0, cpu.cpuInfo.V, "V must be 0");
		}

		@Test
		void SEC() {
			// Set C to 0
			cpu.cpuInfo.C = 0;

			// Execute CLC
			SECInstruction instruction = new SECInstruction(IMPLICIT);
			try {
				instruction.execute();
			} catch (InstructionNotSupportedException e) {
				e.printStackTrace();
			}

			// Test
			assertEquals(1, cpu.cpuInfo.C, "C must be 1");
		}

		@Test
		void SED() {
			// Set D to 0
			cpu.cpuInfo.D = 0;

			// Execute CLC
			SEDInstruction instruction = new SEDInstruction(IMPLICIT);
			try {
				instruction.execute();
			} catch (InstructionNotSupportedException e) {
				e.printStackTrace();
			}

			// Test
			assertEquals(1, cpu.cpuInfo.D, "D must be 1");
		}

		@Test
		void SEI() {
			// Set I to 0
			cpu.cpuInfo.I = 0;

			// Execute CLC
			SEIInstruction instruction = new SEIInstruction(IMPLICIT);
			try {
				instruction.execute();
			} catch (InstructionNotSupportedException e) {
				e.printStackTrace();
			}

			// Test
			assertEquals(1, cpu.cpuInfo.I, "I must be 1");
		}

	}

	@Nested
	class TestJumpInstructions {

		@Test
		void BRK() {
			int vectorAddress = 0xFFFE;

			// Reset stack
			cpu.cpuInfo.SP = 0xFD;
			for (int address = 0x100; address <= 0x1FF; address++) {
				cpu.storeMemory(address, 0);
			}

			for (int address = 0xFFFA; address <= 0xFFFF; address++) {
				cpu.storeMemory(address, 0);
			}

			// Set PC (0xBEEF for testing)
			cpu.cpuInfo.PC = 0xBEEF;

			// Set flags (Z = 1 and V = 1 for testing, P = 0x42)
			cpu.cpuInfo.C = 0;
			cpu.cpuInfo.Z = 1;
			cpu.cpuInfo.I = 0;
			cpu.cpuInfo.D = 0;
			cpu.cpuInfo.B = 0;
			cpu.cpuInfo.V = 1;
			cpu.cpuInfo.N = 0;

			// Put address in vector
			cpu.storeMemory(vectorAddress, new int[] { 0xAD, 0xDE });

			BRKInstruction instruction = new BRKInstruction(IMPLICIT);
			try {
				instruction.execute();
			} catch (InstructionNotSupportedException e) {
				e.printStackTrace();
			}

			// Test what is in stack
			assertEquals(0xBEEF - 1, cpu.fetchAddress(0x1FC), "Old PC should be in first stack position");
			assertEquals(0x42, cpu.fetchMemory(0x1FB), "Old flags should be in second stack position");

			// Test values now
			assertEquals(0xFA, cpu.cpuInfo.SP, "SP should have been decreased by 3");
			assertEquals(0xDEAD - 1, cpu.cpuInfo.PC, "PC should have been updated but removed 1");
			assertEquals(1, cpu.cpuInfo.B, "Break flag should be 1");
		}

		@TestFactory
		Collection<DynamicTest> JMP() {
			List<DynamicTest> tests = new ArrayList<DynamicTest>();

			for (int address = 0; address < 0xFFFF; address++) {
				// PC to 0
				cpu.cpuInfo.PC = 0;

				// Create and execute instruction
				JMPInstruction instruction = new JMPInstruction(ABSOLUTE, address);
				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				int expectedAddress = (address - 3) & 0xFFFF;
				int gotAddress = cpu.cpuInfo.PC;
				tests.add(DynamicTest.dynamicTest(String.format("0x%04X", address),
						() -> assertEquals(expectedAddress, gotAddress, "CPU should have jumped but removed 3")));
			}

			return tests;
		}

		@TestFactory
		Collection<DynamicTest> JSR() {
			List<DynamicTest> tests = new ArrayList<DynamicTest>();

			for (int address = 0; address < 0xFFFF; address++) {
				// PC to 0
				cpu.cpuInfo.PC = 0;
				// SP to 0xFD
				cpu.cpuInfo.SP = 0xFD;

				// Create and execute instruction
				JSRInstruction instruction = new JSRInstruction(ABSOLUTE, address);
				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				int expectedAddress = (address - 3) & 0xFFFF;
				int expectedSP = 0xFB;
				int expectedReturn = 0x0002;

				int gotAddress = cpu.cpuInfo.PC;
				int gotSP = cpu.cpuInfo.SP;
				int gotReturn = cpu.pop() | cpu.pop() << 8;

				tests.add(DynamicTest.dynamicTest(String.format("0x%04X", address), () -> {
					assertEquals(expectedAddress, gotAddress, "CPU should have jumped but removed 3");
					assertEquals(expectedSP, gotSP, "SP should have been decreased by 2");
					assertEquals(expectedReturn, gotReturn, "Return address should be 0x0002");
				}));
			}

			return tests;
		}

		@Test
		void RTI() {
			// Init and execute BRK
			BRK();

			// Create and execute instruction
			RTIInstruction instruction = new RTIInstruction(IMPLICIT);
			try {
				instruction.execute();
			} catch (InstructionNotSupportedException e) {
				e.printStackTrace();
			}

			// Test values
			assertEquals(0xBEEF - 1, cpu.cpuInfo.PC, "Old PC should be back");
			assertEquals(0x42, cpu.cpuInfo.getP(), "Old flags should be back");
		}

		@TestFactory
		Collection<DynamicTest> RTS() {
			List<DynamicTest> tests = new ArrayList<DynamicTest>();

			for (int address = 0; address < 0xFFFF; address++) {
				// PC to address
				cpu.cpuInfo.PC = address;
				// SP to 0xFD
				cpu.cpuInfo.SP = 0xFD;

				// Create and execute instruction
				JSRInstruction jumpInstruction = new JSRInstruction(ABSOLUTE, 0);
				try {
					jumpInstruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				// Go back
				RTSInstruction rtsInstruction = new RTSInstruction(IMPLICIT);
				try {
					rtsInstruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				int expectedAddress = (address + 3 - 1) & 0xFFFF;
				int expectedSP = 0xFD;

				int gotAddress = cpu.cpuInfo.PC;
				int gotSP = cpu.cpuInfo.SP;

				tests.add(DynamicTest.dynamicTest(String.format("0x%04X", address), () -> {
					assertEquals(expectedAddress, gotAddress,
							"CPU should have been back to initial + 3 from the JSR - 1 from the RTS");
					assertEquals(expectedSP, gotSP, "SP should have been back to initial");
				}));
			}

			return tests;
		}
	}

	@Nested
	class TestRegisterMemoryInstructions {

		private void resetCpu() {
			// Registers at 0
			cpu.cpuInfo.A = 0;
			cpu.cpuInfo.X = 0;
			cpu.cpuInfo.Y = 0;
			cpu.cpuInfo.SP = 0;
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

		private Collection<DynamicTest> getTestsLoadRegisterMemoryInstructions(AluInstruction instruction,
				IntSupplier registerValue) {
			List<DynamicTest> tests = new ArrayList<DynamicTest>();

			for (int value = 0; value <= 0xFF; value++) {
				resetCpu();
				instruction = instruction.newInstruction(value);

				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				int expectedValue = value;
				int expectedZ = value == 0 ? 1 : 0;
				int expectedN = value >= 0x80 ? 1 : 0;

				int gotValue = registerValue.getAsInt();
				int gotZ = cpu.cpuInfo.Z;
				int gotN = cpu.cpuInfo.N;

				tests.add(DynamicTest.dynamicTest(String.format("0x%X", value), () -> {
					assertEquals(expectedValue, gotValue, "Value given by the CPU is wrong");
					assertEquals(expectedZ, gotZ, "Zero flag wrong");
					assertEquals(expectedN, gotN, "Negative flag wrong");
				}));
			}

			return tests;
		}

		private Collection<DynamicTest> getTestsSetRegisterMemoryInstructions(Instruction instruction,
				IntConsumer registerUpdate) {
			List<DynamicTest> tests = new ArrayList<DynamicTest>();

			for (int value = 0; value <= 0xFF; value++) {
				// Reset CPU
				resetCpu();

				// Reset zeropage address 0x10
				cpu.storeMemory(0x10, 0);

				// Update register
				registerUpdate.accept(value);

				// Create and execute instruction
				instruction = instruction.newInstruction(0x10);
				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				// Test values
				int expectedValue = value;
				int gotValue = cpu.fetchMemory(0x10);

				tests.add(DynamicTest.dynamicTest(String.format("0x%02X", value),
						() -> assertEquals(expectedValue, gotValue, "Value should have been set to memory")));
			}

			return tests;
		}

		@TestFactory
		Collection<DynamicTest> LDA() {
			return getTestsLoadRegisterMemoryInstructions(new LDAInstruction(IMMEDIATE), () -> cpu.cpuInfo.A);
		}

		@TestFactory
		Collection<DynamicTest> LDX() {
			return getTestsLoadRegisterMemoryInstructions(new LDXInstruction(IMMEDIATE), () -> cpu.cpuInfo.X);
		}

		@TestFactory
		Collection<DynamicTest> LDY() {
			return getTestsLoadRegisterMemoryInstructions(new LDYInstruction(IMMEDIATE), () -> cpu.cpuInfo.Y);
		}

		@TestFactory
		Collection<DynamicTest> STA() {
			return getTestsSetRegisterMemoryInstructions(new STAInstruction(ZEROPAGE), value -> cpu.cpuInfo.A = value);
		}

		@TestFactory
		Collection<DynamicTest> STX() {
			return getTestsSetRegisterMemoryInstructions(new STXInstruction(ZEROPAGE), value -> cpu.cpuInfo.X = value);
		}

		@TestFactory
		Collection<DynamicTest> STY() {
			return getTestsSetRegisterMemoryInstructions(new STYInstruction(ZEROPAGE), value -> cpu.cpuInfo.Y = value);
		}

	}

	@Nested
	class TestStackInstructions {

		private void resetCpu() {
			// Reset stack
			for (int address = 0x100; address <= 0x1FF; address++) {
				cpu.storeMemory(address, 0);
			}

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

		private Collection<DynamicTest> getTestsStackPush(Instruction instruction, IntConsumer registerUpdate) {
			List<DynamicTest> tests = new ArrayList<DynamicTest>();

			for (int value = 0; value <= 0xFF; value++) {
				// Reset CPU registers and flags
				resetCpu();

				// Update register
				registerUpdate.accept(value);

				// Execute instruction
				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				// Test values
				int expectedValue = value;
				int gotValue = cpu.fetchMemory(0x1FD);

				tests.add(DynamicTest.dynamicTest(String.format("0x%X", value),
						() -> assertEquals(expectedValue, gotValue, "Value given by the CPU is wrong")));
			}

			return tests;
		}

		private Collection<DynamicTest> getTestsStackPop(Instruction instruction, IntSupplier registerValue,
				IntFunction<Integer> flagZUpdate) {
			List<DynamicTest> tests = new ArrayList<DynamicTest>();

			for (int value = 0; value <= 0xFF; value++) {
				// Reset CPU registers and flags
				resetCpu();

				// Push value
				cpu.push(value);

				// Execute instruction
				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				// Test values
				int expectedValue = value;
				int expectedZ = flagZUpdate.apply(value);
				int expectedN = value >= 0x80 ? 1 : 0;

				int gotValue = registerValue.getAsInt();
				int gotZ = cpu.cpuInfo.Z;
				int gotN = cpu.cpuInfo.N;

				tests.add(DynamicTest.dynamicTest(String.format("0x%X", value), () -> {
					assertEquals(expectedValue, gotValue, "Register value wrong");
					assertEquals(expectedZ, gotZ, "Zero flag wrong");
					assertEquals(expectedN, gotN, "Negative flag wrong");
				}));
			}

			return tests;
		}

		@TestFactory
		Collection<DynamicTest> PHA() {
			return getTestsStackPush(new PHAInstruction(IMPLICIT), value -> cpu.cpuInfo.A = value);
		}

		@TestFactory
		Collection<DynamicTest> PHP() {
			return getTestsStackPush(new PHPInstruction(IMPLICIT), value -> cpu.cpuInfo.setP(value));
		}

		@TestFactory
		Collection<DynamicTest> PLA() {
			return getTestsStackPop(new PLAInstruction(IMPLICIT), () -> cpu.cpuInfo.A, value -> value == 0 ? 1 : 0);
		}

		@TestFactory
		Collection<DynamicTest> PLP() {
			return getTestsStackPop(new PLPInstruction(IMPLICIT), () -> cpu.cpuInfo.getP(),
					value -> (value >> 1) & 0b1);
		}
	}

	@Nested
	class TestTransferInstructions {

		private void resetCpu() {
			// Registers at 0
			cpu.cpuInfo.A = 0;
			cpu.cpuInfo.X = 0;
			cpu.cpuInfo.Y = 0;
			cpu.cpuInfo.SP = 0;
		}

		private Collection<DynamicTest> getTestsTransferInstructions(TransferInstruction instruction,
				IntConsumer registerUpdate, IntSupplier registerValue) {
			List<DynamicTest> tests = new ArrayList<DynamicTest>();

			for (int value = 0; value <= 0xFF; value++) {
				// Reset CPU
				resetCpu();

				// Update register
				registerUpdate.accept(value);

				// Execute instruction
				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				int expectedValue = value;
				int expectedZ = value == 0 ? 1 : 0;
				int expectedN = value >= 0x80 ? 1 : 0;

				int gotValue = registerValue.getAsInt();
				int gotZ = cpu.cpuInfo.Z;
				int gotN = cpu.cpuInfo.N;

				tests.add(DynamicTest.dynamicTest(String.format("0x%X", value), () -> {
					assertEquals(expectedValue, gotValue, "Value given by the CPU is wrong");
					assertEquals(expectedZ, gotZ, "Zero flag wrong");
					assertEquals(expectedN, gotN, "Negative flag wrong");
				}));
			}

			return tests;
		}

		@TestFactory
		Collection<DynamicTest> TAX() {
			return getTestsTransferInstructions(new TAXInstruction(IMPLICIT), value -> cpu.cpuInfo.A = value,
					() -> cpu.cpuInfo.X);
		}

		@TestFactory
		Collection<DynamicTest> TAY() {
			return getTestsTransferInstructions(new TAYInstruction(IMPLICIT), value -> cpu.cpuInfo.A = value,
					() -> cpu.cpuInfo.Y);
		}

		@TestFactory
		Collection<DynamicTest> TSX() {
			return getTestsTransferInstructions(new TSXInstruction(IMPLICIT), value -> cpu.cpuInfo.SP = value,
					() -> cpu.cpuInfo.X);
		}

		@TestFactory
		Collection<DynamicTest> TXA() {
			return getTestsTransferInstructions(new TXAInstruction(IMPLICIT), value -> cpu.cpuInfo.X = value,
					() -> cpu.cpuInfo.A);
		}

		@TestFactory
		Collection<DynamicTest> TXS() {
			List<DynamicTest> tests = new ArrayList<DynamicTest>();
			Instruction instruction = new TXSInstruction(IMPLICIT);

			for (int value = 0; value <= 0xFF; value++) {
				// Reset CPU
				resetCpu();

				// Update register
				cpu.cpuInfo.X = value;

				// Execute instruction
				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				int expectedValue = value;
				int gotValue = cpu.cpuInfo.SP;

				tests.add(DynamicTest.dynamicTest(String.format("0x%X", value),
						() -> assertEquals(expectedValue, gotValue, "Value given by the CPU is wrong")));
			}

			return tests;
		}

		@TestFactory
		Collection<DynamicTest> TYA() {
			return getTestsTransferInstructions(new TYAInstruction(IMPLICIT), value -> cpu.cpuInfo.Y = value,
					() -> cpu.cpuInfo.A);
		}
	}
}
