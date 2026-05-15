package cpu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import components.Cpu;
import utils.MapperTest;

class TestCpuStack {

	static final Cpu cpu = Cpu.getInstance();

	@BeforeAll
	static void init() {
		cpu.setMapper(new MapperTest());
	}

	@BeforeEach
	public void resetCpu() {
		// Reset stack
		for (int SP = 0; SP <= 0xFF; SP++) {
			cpu.storeMemory(0x100 | SP, 0);
		}

		// SP at initial value
		cpuRegisters.SP = 0xFD;
	}

	@Test
	void testPush() {
		int expectedValue = 10;
		push(expectedValue);
		int gotValue = cpu.fetchMemory(0x1FD);

		assertEquals(expectedValue, gotValue, "Value must be in memory");
		assertEquals(0xFC, cpuRegisters.SP, "SP must have decreased");
	}

	@Test
	void testPushPop() {
		int expectedValue = 10;
		push(expectedValue);
		int gotValue = pop();

		assertEquals(expectedValue, gotValue, "Value pushed must be value poped");
		assertEquals(0xFD, cpuRegisters.SP, "SP must be at the initial state");
	}

	@Test
	void testStackOOBUp() {
		// SP is at FD, 3 pops must set it to 0x00
		pop();
		pop();
		pop();

		assertEquals(0x00, cpuRegisters.SP, "SP must be 0 since wrapped");
	}

	@Test
	void testStackOOBDown() {
		// Push until 0xFF
		for (; cpuRegisters.SP <= 0xFD;) {
			push(1);
		}

		assertEquals(0xFF, cpuRegisters.SP, "SP must be 0xFF since wrapped");
	}
}
