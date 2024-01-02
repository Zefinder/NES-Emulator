package nes.cpu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import components.Cpu;
import nes.MapperTest;

class CpuStackTest {

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
		cpu.cpuInfo.SP = 0xFD;
	}

	@Test
	void testPush() {
		int expectedValue = 10;
		cpu.push(expectedValue);
		int gotValue = cpu.fetchMemory(0x1FD);

		assertEquals(expectedValue, gotValue, "Value must be in memory");
		assertEquals(0xFC, cpu.cpuInfo.SP, "SP must have decreased");
	}

	@Test
	void testPushPop() {
		int expectedValue = 10;
		cpu.push(expectedValue);
		int gotValue = cpu.pop();

		assertEquals(expectedValue, gotValue, "Value pushed must be value poped");
		assertEquals(0xFD, cpu.cpuInfo.SP, "SP must be at the initial state");
	}

	@Test
	void testStackOOBUp() {
		// SP is at FD, 3 pops must set it to 0x00
		cpu.pop();
		cpu.pop();
		cpu.pop();

		assertEquals(0x00, cpu.cpuInfo.SP, "SP must be 0 since wrapped");
	}

	@Test
	void testStackOOBDown() {
		// Push until 0xFF
		for (; cpu.cpuInfo.SP <= 0xFD;) {
			cpu.push(1);
		}

		assertEquals(0xFF, cpu.cpuInfo.SP, "SP must be 0xFF since wrapped");
	}
}
