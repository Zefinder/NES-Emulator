package nes.instructions;

import static instructions.AddressingMode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import instructions.AddressingMode;

class TestAddressingMode {

	private Collection<DynamicTest> generateAddressingModeTests(int expectedByteNumber,
			AddressingMode... addressingModes) {
		List<DynamicTest> tests = new ArrayList<DynamicTest>();

		for (AddressingMode mode : addressingModes) {
			tests.add(DynamicTest.dynamicTest(mode.name(), () -> assertEquals(expectedByteNumber, mode.getByteNumber(),
					String.format("Addressing mode should be on %d byte only!", expectedByteNumber))));
		}

		return tests;
	}

	@TestFactory
	Collection<DynamicTest> testOneByteAddressingMode() {
		return generateAddressingModeTests(1, IMPLICIT, ACCUMULATOR);
	}

	@TestFactory
	Collection<DynamicTest> testTwoByteAddressingMode() {
		return generateAddressingModeTests(2, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE, INDIRECT_X,
				INDIRECT_Y);
	}

	@TestFactory
	Collection<DynamicTest> testThreeByteAddressingMode() {
		return generateAddressingModeTests(3, ABSOLUTE, ABSOLUTE_X, ABSOLUTE_Y, INDIRECT);
	}

}
