package nes.instructions;

import static instructions.AddressingMode.ABSOLUTE;
import static instructions.AddressingMode.ABSOLUTE_X;
import static instructions.AddressingMode.ABSOLUTE_Y;
import static instructions.AddressingMode.ACCUMULATOR;
import static instructions.AddressingMode.IMMEDIATE;
import static instructions.AddressingMode.IMPLICIT;
import static instructions.AddressingMode.INDIRECT;
import static instructions.AddressingMode.INDIRECT_X;
import static instructions.AddressingMode.INDIRECT_Y;
import static instructions.AddressingMode.RELATIVE;
import static instructions.AddressingMode.ZEROPAGE;
import static instructions.AddressingMode.ZEROPAGE_X;
import static instructions.AddressingMode.ZEROPAGE_Y;
import static org.junit.Assume.assumeFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import components.Cpu;
import components.CpuInfo;
import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;
import instructions.NOPInstruction;
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
import nes.MapperTest;

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

		// TODO Add tests for ALU instructions (like ASL) that directly modify memory
		/**
		 * Used by instructions like ADC
		 */
		private Collection<DynamicTest> getTestsAluInstructionOverflow(AluInstruction instruction,
				TriIntFunction<Integer, Integer, Integer> function,
				TesserIntFunction<Integer, Integer, Integer, Integer> overflowEvaluation) {
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
					int expectedC = rawValue > 0xFF ? 1 : 0;
					int expectedZ = expectedValue == 0 ? 1 : 0;
					int expectedN = expectedValue >= 0x80 ? 1 : 0;
					int expectedV = overflowEvaluation.apply(expectedValue, expectedValue, operand1, operand2);

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
					int expectedCarryC = rawCarryValue > 0xFF ? 1 : 0;
					int expectedCarryZ = expectedCarryValue == 0 ? 1 : 0;
					int expectedCarryN = expectedCarryValue >= 0x80 ? 1 : 0;
					int expectedCarryV = overflowEvaluation.apply(expectedCarryValue, expectedValue, operand1,
							operand2);

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
					(expectedValue, beforeCarry, operand1, operand2) -> {
						int signed1 = operand1 >= 0x80 ? operand1 - 256 : operand1;
						int signed2 = operand2 >= 0x80 ? operand2 - 256 : operand2;
						boolean greater = Math.abs(signed1) > Math.abs(signed2);
						boolean lesser = Math.abs(signed1) < Math.abs(signed2);
						boolean equal = Math.abs(signed1) == Math.abs(signed2);

						boolean positive1 = operand1 <= 0x7F && operand1 != 0;
						boolean positive2 = operand2 <= 0x7F && operand2 != 0;
						boolean negative1 = operand1 >= 0x80;
						boolean negative2 = operand2 >= 0x80;
						boolean zero1 = operand1 == 0;
						boolean zero2 = operand2 == 0;

						boolean expectedBeforePositive = (beforeCarry & 0xFF) <= 0x7F;
						boolean expectedValuePositive = (expectedValue & 0xFF) <= 0x7F;

						int V = beforeCarry == 0 // If 0 then both zero or they cancel each other out
								? (zero1 && zero2) || equal && ((positive1 && negative2) || (negative1 && positive2))
										? 0
										: 1
								: !expectedBeforePositive // If negative, biggest negative wins or both negative
										? (lesser && (negative2 || (negative1 && zero2)))
												|| (greater && (negative1 || (zero1 && negative2)))
												|| (equal && negative1 && negative2) ? 0 : 1
										: (lesser && (positive2 || (positive1 && zero2))) // Same for positive
												|| (greater && (positive1 || (zero1 && positive2)))
												|| (equal && positive1 && positive2) ? 0 : 1;

						if (V == 0 && !expectedValue.equals(beforeCarry)) {
							// If no overflow before and carry is on, then we need to check
							// Adding a carry (1) to a number can lead to the same sign IF positive
							// (positive + 1 is positive)
							// If negative, then if zero given then overflow
							// If we consider 0 as positive, then it will detect positive -> negative
							// And negative -> zero (since zero is considered as positive!)
							// Thus if same sign then no overflow, else overflow
							V = expectedValuePositive == expectedBeforePositive ? 0 : 1;
						}

						return V;
					});
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
			assumeFalse(true);

			// TODO Change V lambda
			return getTestsAluInstructionOverflow(new SBCInstruction(IMMEDIATE), (a, b, C) -> a - b - (1 - C),
					(expectedValue, operand1, operand2, C) -> {
						int signed1 = operand1 >= 0x80 ? operand1 - 256 : operand1;
						int signed2 = operand2 >= 0x80 ? operand2 - 256 : operand2;
						boolean greater = Math.abs(signed1) > Math.abs(signed2);
						boolean lesser = Math.abs(signed1) < Math.abs(signed2);
						boolean equal = Math.abs(signed1) == Math.abs(signed2);

						boolean positive1 = operand1 <= 0x7F && operand1 != 0;
						boolean positive2 = operand2 <= 0x7F && operand2 != 0;
						boolean negative1 = operand1 >= 0x80;
						boolean negative2 = operand2 >= 0x80;
						boolean zero1 = operand1 == 0;
						boolean zero2 = operand2 == 0;

						int V = expectedValue == 0 // If 0 then both zero or they cancel each other out
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

						return V;
					});
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
			assertEquals(0xBEEF, cpu.fetchAddress(0x1FC), "Old PC should be in first stack position");
			assertEquals(0x42, cpu.fetchMemory(0x1FB), "Old flags should be in second stack position");

			// Test values now
			assertEquals(0xFA, cpu.cpuInfo.SP, "SP should have been decreased by 3");
			assertEquals(0xDEAD, cpu.cpuInfo.PC, "PC shouls have been updated");
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
			assertEquals(0xBEEF, cpu.cpuInfo.PC, "Old PC should be back");
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

				int expectedAddress = (address - 1) & 0xFFFF;
				int expectedSP = 0xFD;

				int gotAddress = cpu.cpuInfo.PC;
				int gotSP = cpu.cpuInfo.SP;

				tests.add(DynamicTest.dynamicTest(String.format("0x%04X", address), () -> {
					assertEquals(expectedAddress, gotAddress, "CPU should have been back to initial");
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

	@Nested
	class TestCyclesInstructions {

		@BeforeEach
		public void initMemory() {
			// Store address 0x00FF at 0xFE
			cpu.storeMemory(0xFE, 0xFF, 0x00);
		}

		private void testWorking(Class<?> instructionClass, int[] cycles, AddressingMode... addressingModes)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			for (int i = 0; i < cycles.length; i++) {
				Instruction instruction = (Instruction) instructionClass.getConstructor(AddressingMode.class)
						.newInstance(addressingModes[i]);
				assertEquals(cycles[i], instruction.getCycle());
			}
		}

		private void testWorkingPageCross(Class<?> instructionClass, int[] cycles, AddressingMode... addressingModes)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			for (int i = 0; i < cycles.length; i++) {
				cpu.cpuInfo.X = 0xFF;
				cpu.cpuInfo.Y = 0xFF;
				AddressingMode addressingMode = addressingModes[i];

				Instruction instruction = (Instruction) instructionClass.getConstructor(AddressingMode.class)
						.newInstance(addressingMode);

				// Normal cycles
				assertEquals(cycles[i], instruction.getCycle());

				// Update page crossed (to 1)
				instruction = instruction.newInstruction(0xFE);

				// Cycles + 1
				assertEquals(cycles[i] + 1, instruction.getCycle());
			}
		}

		private void testWorkingRelative(Class<?> instructionClass, int cycles)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Flags at 0
			cpu.cpuInfo.C = 0;
			cpu.cpuInfo.Z = 0;
			cpu.cpuInfo.I = 0;
			cpu.cpuInfo.D = 0;
			cpu.cpuInfo.B = 0;
			cpu.cpuInfo.V = 0;
			cpu.cpuInfo.N = 0;

			// PC at 0
			cpu.cpuInfo.PC = 0;

			BranchInstruction instruction = (BranchInstruction) instructionClass.getConstructor(AddressingMode.class)
					.newInstance(RELATIVE);

			instruction = instruction.newInstruction(2);

			// Normal cycles
			assertEquals(cycles, instruction.getCycle());

			// Execute instruction, if jumped then begin tests, else change flags to 1
			instruction.execute();

			if (cpu.cpuInfo.PC == 0) {
				// Flags at 1
				cpu.cpuInfo.C = 1;
				cpu.cpuInfo.Z = 1;
				cpu.cpuInfo.I = 1;
				cpu.cpuInfo.D = 1;
				cpu.cpuInfo.B = 1;
				cpu.cpuInfo.V = 1;
				cpu.cpuInfo.N = 1;

				// Execute again
				instruction.execute();
			}

			// Branched cycles
			assertEquals(cycles + 1, instruction.getCycle());

			// Recreate branch instruction with page change (-128)
			instruction = instruction.newInstruction(0x80);
			instruction.execute();

			// Branched cycles with new page
			assertEquals(cycles + 1 + 2, instruction.getCycle());
		}

		private void testException(Class<?> instructionClass, AddressingMode... addressingModes)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException {
			for (AddressingMode addressingMode : addressingModes) {
				Instruction instruction = (Instruction) instructionClass.getConstructor(AddressingMode.class)
						.newInstance(addressingMode);
				assertThrows(InstructionNotSupportedException.class, () -> instruction.getCycle());
			}
		}

		/**
		 * <ul>
		 * <li>Immediate - 2
		 * <li>Zeropage - 3
		 * <li>ZeropageX - 4
		 * <li>Absolute - 4
		 * <li>AbsoluteX - 4+
		 * <li>AbsoluteY - 4+
		 * <li>IndirectX - 6
		 * <li>IndirectY - 5+
		 * </ul>
		 * 
		 * @param clazz the instruction class
		 * @throws InstructionNotSupportedException
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 */
		@ParameterizedTest
		@ValueSource(classes = { ADCInstruction.class, ANDInstruction.class, CMPInstruction.class, EORInstruction.class,
				LDAInstruction.class, ORAInstruction.class, SBCInstruction.class })
		void testCyclesAluGroup1(Class<?> clazz)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Working
			testWorking(clazz, new int[] { 2, 3, 4, 4, 6 }, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ABSOLUTE, INDIRECT_X);
			testWorkingPageCross(clazz, new int[] { 4, 4, 5 }, ABSOLUTE_X, ABSOLUTE_Y, INDIRECT_Y);

			// Others
			testException(clazz, IMPLICIT, ACCUMULATOR, ZEROPAGE_Y, RELATIVE, INDIRECT);
		}

		/**
		 * <ul>
		 * <li>Accumulator - 2
		 * <li>Zeropage - 5
		 * <li>ZeropageX - 6
		 * <li>Absolute - 6
		 * <li>AbsoluteX - 7
		 * </ul>
		 * 
		 * @param clazz the instruction class
		 * @throws InstructionNotSupportedException
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 */
		@ParameterizedTest
		@ValueSource(classes = { ASLInstruction.class, LSRInstruction.class, ROLInstruction.class,
				RORInstruction.class })
		void testCyclesAluGroup2(Class<?> clazz)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Working
			testWorking(clazz, new int[] { 2, 5, 6, 6, 7 }, ACCUMULATOR, ZEROPAGE, ZEROPAGE_X, ABSOLUTE, ABSOLUTE_X);

			// Others
			testException(clazz, IMPLICIT, IMMEDIATE, ZEROPAGE_Y, RELATIVE, ABSOLUTE_Y, INDIRECT, INDIRECT_X,
					INDIRECT_Y);
		}

		/**
		 * <ul>
		 * <li>Immediate - 2
		 * <li>Zeropage - 3
		 * <li>Absolute - 4
		 * </ul>
		 * 
		 * @param clazz the instruction class
		 * @throws InstructionNotSupportedException
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 */
		@ParameterizedTest
		@ValueSource(classes = { CPXInstruction.class, CPYInstruction.class })
		void testCyclesAluGroup3(Class<?> clazz)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Working
			testWorking(clazz, new int[] { 2, 3, 4 }, IMMEDIATE, ZEROPAGE, ABSOLUTE);

			// Others
			testException(clazz, IMPLICIT, ACCUMULATOR, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE, ABSOLUTE_X, ABSOLUTE_Y,
					INDIRECT, INDIRECT_X, INDIRECT_Y);
		}

		/**
		 * <ul>
		 * <li>Zeropage - 5
		 * <li>ZeropageX - 6
		 * <li>Absolute - 6
		 * <li>AbsoluteX - 7
		 * </ul>
		 * 
		 * @param clazz the instruction class
		 * @throws InstructionNotSupportedException
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 */
		@ParameterizedTest
		@ValueSource(classes = { DECInstruction.class, INCInstruction.class })
		void testCyclesAluGroup4(Class<?> clazz)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Working
			testWorking(clazz, new int[] { 5, 6, 6, 7 }, ZEROPAGE, ZEROPAGE_X, ABSOLUTE, ABSOLUTE_X);

			// Others
			testException(clazz, IMPLICIT, ACCUMULATOR, IMMEDIATE, ZEROPAGE_Y, RELATIVE, ABSOLUTE_Y, INDIRECT,
					INDIRECT_X, INDIRECT_Y);
		}

		/**
		 * <ul>
		 * <li>Relative - 2++
		 * </ul>
		 * 
		 * @param clazz the instruction class
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 * @throws InstructionNotSupportedException
		 */
		@ParameterizedTest
		@ValueSource(classes = { BCCInstruction.class, BCSInstruction.class, BEQInstruction.class, BMIInstruction.class,
				BNEInstruction.class, BPLInstruction.class, BVCInstruction.class, BVSInstruction.class })
		void testCyclesBranch(Class<?> clazz)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Working
			testWorkingRelative(clazz, 2);

			// Others
			testException(clazz, IMPLICIT, ACCUMULATOR, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ZEROPAGE_Y, ABSOLUTE,
					ABSOLUTE_X, ABSOLUTE_Y, INDIRECT, INDIRECT_X, INDIRECT_Y);
		}

		/**
		 * <ul>
		 * <li>Zeropage - 3
		 * <li>Absolute - 4
		 * </ul>
		 * 
		 * @param clazz the instruction class
		 * @throws InstructionNotSupportedException
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 */
		@ParameterizedTest
		@ValueSource(classes = { BITInstruction.class })
		void testCyclesBIT(Class<?> clazz)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Working
			testWorking(clazz, new int[] { 3, 4 }, ZEROPAGE, ABSOLUTE);

			// Others
			testException(clazz, IMPLICIT, ACCUMULATOR, IMMEDIATE, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE, ABSOLUTE_X,
					ABSOLUTE_Y, INDIRECT, INDIRECT_X, INDIRECT_Y);
		}

		/**
		 * <ul>
		 * <li>Implied - 7
		 * </ul>
		 * 
		 * @param clazz the instruction class
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 * @throws InstructionNotSupportedException
		 */
		@ParameterizedTest
		@ValueSource(classes = { BRKInstruction.class })
		void testCyclesBRK(Class<?> clazz)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Working
			testWorking(clazz, new int[] { 7 }, IMPLICIT);

			// Others
			testException(clazz, ACCUMULATOR, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE, ABSOLUTE,
					ABSOLUTE_X, ABSOLUTE_Y, INDIRECT, INDIRECT_X, INDIRECT_Y);
		}

		/**
		 * <ul>
		 * <li>Implied - 2
		 * </ul>
		 * 
		 * @param clazz the instruction class
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 * @throws InstructionNotSupportedException
		 */
		@ParameterizedTest
		@ValueSource(classes = { CLCInstruction.class, CLDInstruction.class, CLIInstruction.class, CLVInstruction.class,
				DEXInstruction.class, DEYInstruction.class, INXInstruction.class, INYInstruction.class,
				NOPInstruction.class, SECInstruction.class, SEDInstruction.class, SEIInstruction.class,
				TAXInstruction.class, TAYInstruction.class, TSXInstruction.class, TXAInstruction.class,
				TXSInstruction.class, TYAInstruction.class })
		void testCyclesImplied(Class<?> clazz)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Working
			testWorking(clazz, new int[] { 2 }, IMPLICIT);

			// Others
			testException(clazz, ACCUMULATOR, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE, ABSOLUTE,
					ABSOLUTE_X, ABSOLUTE_Y, INDIRECT, INDIRECT_X, INDIRECT_Y);
		}

		/**
		 * <ul>
		 * <li>Absolute - 3
		 * <li>Indirect - 5
		 * </ul>
		 * 
		 * @param clazz the instruction class
		 * @throws InstructionNotSupportedException
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 */
		@ParameterizedTest
		@ValueSource(classes = { JMPInstruction.class })
		void testCyclesJMP(Class<?> clazz)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Working
			testWorking(clazz, new int[] { 3, 5 }, ABSOLUTE, INDIRECT);

			// Others
			testException(clazz, IMPLICIT, ACCUMULATOR, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE,
					ABSOLUTE_X, ABSOLUTE_Y, INDIRECT_X, INDIRECT_Y);
		}

		/**
		 * <ul>
		 * <li>Absolute - 6
		 * </ul>
		 * 
		 * @param clazz the instruction class
		 * @throws InstructionNotSupportedException
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 */
		@ParameterizedTest
		@ValueSource(classes = { JSRInstruction.class })
		void testCyclesJSR(Class<?> clazz)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Working
			testWorking(clazz, new int[] { 6 }, ABSOLUTE);

			// Others
			testException(clazz, IMPLICIT, ACCUMULATOR, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE,
					ABSOLUTE_X, ABSOLUTE_Y, INDIRECT, INDIRECT_X, INDIRECT_Y);
		}

		/**
		 * <ul>
		 * <li>Immediate - 2
		 * <li>Zeropage - 3
		 * <li>ZeropageY - 4
		 * <li>Absolute - 4
		 * <li>AbsoluteY - 4+
		 * </ul>
		 * 
		 * @param clazz the instruction class
		 * @throws InstructionNotSupportedException
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 */
		@ParameterizedTest
		@ValueSource(classes = { LDXInstruction.class })
		void testCyclesLoadRegisterX(Class<?> clazz)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Working
			testWorking(clazz, new int[] { 2, 3, 4, 4 }, IMMEDIATE, ZEROPAGE, ZEROPAGE_Y, ABSOLUTE);
			testWorkingPageCross(clazz, new int[] { 4 }, ABSOLUTE_Y);

			// Others
			testException(clazz, IMPLICIT, ACCUMULATOR, ZEROPAGE_X, RELATIVE, ABSOLUTE_X, INDIRECT, INDIRECT_X,
					INDIRECT_Y);
		}

		/**
		 * <ul>
		 * <li>Immediate - 2
		 * <li>Zeropage - 3
		 * <li>ZeropageX - 4
		 * <li>Absolute - 4
		 * <li>AbsoluteX - 4+
		 * </ul>
		 * 
		 * @param clazz the instruction class
		 * @throws InstructionNotSupportedException
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 */
		@ParameterizedTest
		@ValueSource(classes = { LDYInstruction.class })
		void testCyclesLoadRegisterY(Class<?> clazz)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Working
			testWorking(clazz, new int[] { 2, 3, 4, 4 }, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ABSOLUTE);
			testWorkingPageCross(clazz, new int[] { 4 }, ABSOLUTE_X);

			// Others
			testException(clazz, IMPLICIT, ACCUMULATOR, ZEROPAGE_Y, RELATIVE, ABSOLUTE_Y, INDIRECT, INDIRECT_X,
					INDIRECT_Y);
		}

		/**
		 * <ul>
		 * <li>Implied - 3
		 * </ul>
		 * 
		 * @param clazz the instruction class
		 * @throws InstructionNotSupportedException
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 */
		@ParameterizedTest
		@ValueSource(classes = { PHAInstruction.class, PHPInstruction.class })
		void testCyclesPush(Class<?> clazz)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Working
			testWorking(clazz, new int[] { 3 }, IMPLICIT);

			// Others
			testException(clazz, ACCUMULATOR, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE, ABSOLUTE,
					ABSOLUTE_X, ABSOLUTE_Y, INDIRECT, INDIRECT_X, INDIRECT_Y);
		}

		/**
		 * <ul>
		 * <li>Implied - 4
		 * </ul>
		 * 
		 * @param clazz the instruction class
		 * @throws InstructionNotSupportedException
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 */
		@ParameterizedTest
		@ValueSource(classes = { PLAInstruction.class, PLPInstruction.class })
		void testCyclesPull(Class<?> clazz)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Working
			testWorking(clazz, new int[] { 4 }, IMPLICIT);

			// Others
			testException(clazz, ACCUMULATOR, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE, ABSOLUTE,
					ABSOLUTE_X, ABSOLUTE_Y, INDIRECT, INDIRECT_X, INDIRECT_Y);
		}

		/**
		 * <ul>
		 * <li>Implied - 6
		 * </ul>
		 * 
		 * @param clazz the instruction class
		 * @throws InstructionNotSupportedException
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 */
		@ParameterizedTest
		@ValueSource(classes = { RTIInstruction.class, RTSInstruction.class })
		void testCyclesReturn(Class<?> clazz)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Working
			testWorking(clazz, new int[] { 6 }, IMPLICIT);

			// Others
			testException(clazz, ACCUMULATOR, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE, ABSOLUTE,
					ABSOLUTE_X, ABSOLUTE_Y, INDIRECT, INDIRECT_X, INDIRECT_Y);
		}

		/**
		 * <ul>
		 * <li>Zeropage - 3
		 * <li>ZeropageX - 4
		 * <li>Absolute - 4
		 * <li>AbsoluteX - 5
		 * <li>AbsoluteY - 5
		 * <li>IndirectX - 6
		 * <li>IndirectY - 6
		 * </ul>
		 * 
		 * @param clazz the instruction class
		 * @throws InstructionNotSupportedException
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 */
		@ParameterizedTest
		@ValueSource(classes = { STAInstruction.class })
		void testCyclesSTA(Class<?> clazz)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Working
			testWorking(clazz, new int[] { 3, 4, 4, 5, 5, 6, 6 }, ZEROPAGE, ZEROPAGE_X, ABSOLUTE, ABSOLUTE_X,
					ABSOLUTE_Y, INDIRECT_X, INDIRECT_Y);

			// Others
			testException(clazz, IMPLICIT, ACCUMULATOR, IMMEDIATE, ZEROPAGE_Y, RELATIVE, INDIRECT);
		}

		/**
		 * <ul>
		 * <li>Zeropage - 3
		 * <li>ZeropageY - 4
		 * <li>Absolute - 4
		 * </ul>
		 * 
		 * @param clazz the instruction class
		 * @throws InstructionNotSupportedException
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 */
		@ParameterizedTest
		@ValueSource(classes = { STXInstruction.class })
		void testCyclesSTX(Class<?> clazz)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Working
			testWorking(clazz, new int[] { 3, 4, 4 }, ZEROPAGE, ZEROPAGE_Y, ABSOLUTE);

			// Others
			testException(clazz, IMPLICIT, ACCUMULATOR, IMMEDIATE, ZEROPAGE_X, RELATIVE, ABSOLUTE_X, ABSOLUTE_Y,
					INDIRECT, INDIRECT_X, INDIRECT_Y);
		}

		/**
		 * <ul>
		 * <li>Zeropage - 3
		 * <li>ZeropageX - 4
		 * <li>Absolute - 4
		 * </ul>
		 * 
		 * @param clazz the instruction class
		 * @throws InstructionNotSupportedException
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 */
		@ParameterizedTest
		@ValueSource(classes = { STYInstruction.class })
		void testCyclesSTY(Class<?> clazz)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
			// Working
			testWorking(clazz, new int[] { 3, 4, 4 }, ZEROPAGE, ZEROPAGE_X, ABSOLUTE);

			// Others
			testException(clazz, IMPLICIT, ACCUMULATOR, IMMEDIATE, ZEROPAGE_Y, RELATIVE, ABSOLUTE_X, ABSOLUTE_Y,
					INDIRECT, INDIRECT_X, INDIRECT_Y);
		}

	}
}
