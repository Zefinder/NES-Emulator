package nes.instructions;

import static components.Cpu.BREAK_VECTOR;
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
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
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
import instructions.BRKInstruction;
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
import instructions.jump.JMPInstruction;
import instructions.jump.JSRInstruction;
import nes.MapperTest;

@FunctionalInterface
interface TriFunction<T, U, V, R> {

	R apply(T t, U u, V v);

	default <K> TriFunction<T, U, V, K> andThen(Function<? super R, ? extends K> after) {
		Objects.requireNonNull(after);
		return (T t, U u, V v) -> after.apply(apply(t, u, v));
	}
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

		// TODO Add tests with carry (for those who modifies it and those who do not)
		// TODO Add tests for ALU instructions (like ASL) that directly modify memory
		/**
		 * Used by instructions like ADC
		 */
		private Collection<DynamicTest> getTestsAluInstructionOverflow(AluInstruction instruction,
				BiFunction<Integer, Integer, Integer> function,
				TriFunction<Integer, Integer, Integer, Integer> overflowEvaluation) {
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

					int rawValue = function.apply(operand1, operand2);
					int expectedValue = rawValue & 0xFF;
					int expectedC = rawValue > 0xFF ? 1 : 0;
					int expectedZ = expectedValue == 0 ? 1 : 0;
					int expectedN = expectedValue >= 0x80 ? 1 : 0;
					int expectedV = overflowEvaluation.apply(expectedValue, operand1, operand2);

					int gotValue = cpu.cpuInfo.A;
					int gotC = cpu.cpuInfo.C;
					int gotZ = cpu.cpuInfo.Z;
					int gotN = cpu.cpuInfo.N;
					int gotV = cpu.cpuInfo.V;

					tests.add(DynamicTest.dynamicTest(String.format("0x%X 0x%X", operand1, operand2), () -> {
						assertEquals(expectedValue, gotValue, "Value given by the CPU is wrong");
						assertEquals(expectedC, gotC, "Carry flag wrong");
						assertEquals(expectedZ, gotZ, "Zero flag wrong");
						assertEquals(expectedN, gotN, "Negative flag wrong");
						assertEquals(expectedV, gotV, "Overflow flag wrong");
					}));
				}
			}

			return tests;
		}

		/**
		 * Used by instructions like AND
		 */
		private Collection<DynamicTest> getTestsAluInstructionLogic(AluInstruction instruction,
				BiFunction<Integer, Integer, Integer> function) {
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

					int rawValue = function.apply(operand1, operand2);
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
				Function<Integer, Integer> function) {
			List<DynamicTest> tests = new ArrayList<DynamicTest>();

			for (int operand1 = 0; operand1 <= 0xFF; operand1++) {
				resetCpu();
				cpu.cpuInfo.A = operand1;

				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				int rawValue = function.apply(operand1);
				int expectedValue = rawValue & 0xFF;
				int expectedC = rawValue > 0xFF ? 1 : 0;
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
				Consumer<Integer> valueUpdate) {
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
			return getTestsAluInstructionOverflow(new ADCInstruction(IMMEDIATE), (a, b) -> a + b,
					(expectedValue, operand1, operand2) -> {
						boolean greater = operand1 > operand2;
						boolean lesser = operand1 < operand2;
						boolean equal = operand1.equals(operand2);

						boolean positive1 = operand1 <= 0x7F && operand1 != 0;
						boolean positive2 = operand2 <= 0x7F && operand2 != 0;
						boolean negative1 = operand1 >= 0x80;
						boolean negative2 = operand2 >= 0x80;
						boolean zero1 = operand1 == 0;
						boolean zero2 = operand2 == 0;

						return expectedValue == 0 // If 0 then both zero or they cancel each other out
								? (zero1 && zero2) || equal && ((positive1 && negative2) || (negative1 && positive2))
										? 0
										: 1
								: expectedValue >= 0x80 // If negative, biggest negative wins or both negative
										? (lesser && (negative2 || (negative1 && zero2)))
												|| (greater && (negative1 || (zero1 && negative2)))
												|| (equal && negative1 && negative2) ? 0 : 1
										: (lesser && (positive2 || (positive1 && zero2))) // Same for positive
												|| (greater && (positive1 || (zero1 && positive2)))
												|| (equal && positive1 && positive2) ? 0 : 1;
					});
		}

		@TestFactory
		Collection<DynamicTest> AND() {
			return getTestsAluInstructionLogic(new ANDInstruction(IMMEDIATE), (a, b) -> a & b);
		}

		@TestFactory
		Collection<DynamicTest> ASL() {
			return getTestsAluInstructionAccumulator(new ASLInstruction(ACCUMULATOR), a -> a << 1);
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

	}

	@Nested
	class TestInterruptionInstructions {

		private static final int[] ADDRESS = { 0xAD, 0xDE };
		private static final int PC_BEFORE = 0xBEEF;
		private static final int P_BEFORE = 0x42;
		private static final int SP_BEFORE = 0xFD;
		private static final int SP_AFTER = (SP_BEFORE - 3) & 0xFF;

		private void prepareCpu(int vectorAddress) {
			// Reset stack
			cpu.cpuInfo.SP = SP_BEFORE;
			for (int address = 0x100; address <= 0x1FF; address++) {
				cpu.storeMemory(address, 0);
			}

			for (int address = 0xFFFA; address <= 0xFFFF; address++) {
				cpu.storeMemory(address, 0);
			}

			// Set PC (0xBEEF for testing)
			cpu.cpuInfo.PC = PC_BEFORE;

			// Set flags (Z = 1 and V = 1 for testing, P = 0x42)
			cpu.cpuInfo.C = 0;
			cpu.cpuInfo.Z = 1;
			cpu.cpuInfo.I = 0;
			cpu.cpuInfo.D = 0;
			cpu.cpuInfo.B = 0;
			cpu.cpuInfo.V = 1;
			cpu.cpuInfo.N = 0;

			// Put address in vector
			cpu.storeMemory(vectorAddress, ADDRESS);
		}

		@Test
		void BRK() {
			prepareCpu(BREAK_VECTOR);
			BRKInstruction instruction = new BRKInstruction(IMPLICIT);
			try {
				instruction.execute();
			} catch (InstructionNotSupportedException e) {
				e.printStackTrace();
			}

			// Test what is in stack
			assertEquals(PC_BEFORE, cpu.fetchAddress(0x100 | SP_BEFORE - 1), "Old PC should be in first stack position");
			assertEquals(P_BEFORE, cpu.fetchMemory(0x100 | SP_BEFORE - 2),
					"Old flags should be in second stack position");

			// Test values now
			assertEquals(SP_AFTER, cpu.cpuInfo.SP, "SP should have been decreased by 3");
			assertEquals(0xDEAD, cpu.cpuInfo.PC, "PC shouls have been updated");
			assertEquals(1, cpu.cpuInfo.B, "Break flag should be 1");
		}
	}

	@Nested
	class TestJumpInstructions {

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

				int expectedAddress = address;
				int gotAddress = cpu.cpuInfo.PC;
				tests.add(DynamicTest.dynamicTest(String.format("0x%04X", address),
						() -> assertEquals(expectedAddress, gotAddress, "CPU should have jumped")));
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

				int expectedAddress = address;
				int expectedSP = 0xFB;
				int expectedReturn = 0xFFFF;

				int gotAddress = cpu.cpuInfo.PC;
				int gotSP = cpu.cpuInfo.SP;
				int gotReturn = cpu.pop() | cpu.pop() << 8;
				
				tests.add(DynamicTest.dynamicTest(String.format("0x%04X", address), () -> {
					assertEquals(expectedAddress, gotAddress, "CPU should have jumped");
					assertEquals(expectedSP, gotSP, "SP should have been decreased by 2");
					assertEquals(expectedReturn, gotReturn, "Return address should be 0xFFFF");
				}));
			}

			return tests;
		}
	}
}
