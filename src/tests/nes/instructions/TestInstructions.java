package nes.instructions;

import static instructions.AddressingMode.IMMEDIATE;
import static instructions.AddressingMode.ACCUMULATOR;
import static instructions.AddressingMode.RELATIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;

import components.Cpu;
import components.CpuInfo;
import exceptions.InstructionNotSupportedException;
import instructions.alu.ADCInstruction;
import instructions.alu.ANDInstruction;
import instructions.alu.ASLInstruction;
import instructions.alu.AluInstruction;
import instructions.branch.BCCInstruction;
import instructions.branch.BranchInstruction;

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

	@Nested
	class TestAluInstructions {

		// TODO Add tests with carry
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

	}
}
