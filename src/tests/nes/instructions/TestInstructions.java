package nes.instructions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;

import components.Cpu;
import exceptions.InstructionNotSupportedException;
import instructions.ADCInstruction;
import instructions.AddressingMode;

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

	@Nested
	class TestAluInstructions {

		void resetCpu() {
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

		private Collection<DynamicTest> getTestsAluInstruction1(BiFunction<Integer, Integer, Integer> function,
				TriFunction<Integer, Integer, Integer, Integer> overflowEvaluation) {
			List<DynamicTest> tests = new ArrayList<DynamicTest>();

			for (int operand1 = 0; operand1 <= 0xFF; operand1++) {
				for (int operand2 = 0; operand2 <= 0xFF; operand2++) {
					resetCpu();
					cpu.cpuInfo.A = operand1;
					ADCInstruction instruction = new ADCInstruction(AddressingMode.IMMEDIATE, operand2);

					// Without carry
					try {
						instruction.execute();
					} catch (InstructionNotSupportedException e) {
						e.printStackTrace();
					}

					int rawValue = function.apply(operand1, operand2);
					int expectedC = rawValue > 0xFF ? 1 : 0;
					int expectedValue = rawValue & 0xFF;
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

		@TestFactory
		Collection<DynamicTest> ADC() {
			return getTestsAluInstruction1((a, b) -> a + b, (expectedValue, operand1, operand2) -> {
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
						? (zero1 && zero2) || equal && ((positive1 && negative2) || (negative1 && positive2)) ? 0 : 1
						: expectedValue >= 0x80 // If negative, biggest negative wins or both negative
								? (lesser && (negative2 || (negative1 && zero2)))
										|| (greater && (negative1 || (zero1 && negative2)))
										|| (equal && negative1 && negative2) ? 0 : 1
								: (lesser && (positive2 || (positive1 && zero2))) // Same for positive
										|| (greater && (positive1 || (zero1 && positive2)))
										|| (equal && positive1 && positive2) ? 0 : 1;
			});
		}
	}
}
