package instructions;

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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import components.cpu.Cpu;
import exceptions.InstructionNotSupportedException;
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

class TestCyclesInstructions {

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

	@BeforeEach
	public void initMemory() {
		// Store address 0x00FF at 0xFE
		cpu.storeMemory(0xFE, 0xFF, 0x00);
	}

	private void testWorking(Class<?> instructionClass, int[] cycles, AddressingMode... addressingModes)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		for (int i = 0; i < cycles.length; i++) {
			Instruction instruction = (Instruction) instructionClass.getConstructor(AddressingMode.class)
					.newInstance(addressingModes[i]);
			assertEquals(cycles[i], instruction.getCycles());
		}
	}

	private void testWorkingPageCross(Class<?> instructionClass, int[] cycles, AddressingMode... addressingModes)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		for (int i = 0; i < cycles.length; i++) {
			cpu.cpuInfo.X = 0xFF;
			cpu.cpuInfo.Y = 0xFF;
			AddressingMode addressingMode = addressingModes[i];

			Instruction instruction = (Instruction) instructionClass.getConstructor(AddressingMode.class)
					.newInstance(addressingMode);

			// Normal cycles
			assertEquals(cycles[i], instruction.getCycles());

			// Update page crossed (to 1)
			instruction = instruction.newInstruction(0xFE);
			instruction.execute();

			// Cycles + 1
			assertEquals(cycles[i] + 1, instruction.getCycles());
		}
	}

	private void testWorkingRelative(Class<?> instructionClass, int cycles)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, InstructionNotSupportedException {
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
		assertEquals(cycles, instruction.getCycles());

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
		assertEquals(cycles + 1, instruction.getCycles());

		// Recreate branch instruction with page change (-128)
		instruction = instruction.newInstruction(0x80);
		instruction.execute();

		// Branched cycles with new page
		assertEquals(cycles + 1 + 2, instruction.getCycles());
	}

	private void testException(Class<?> instructionClass, AddressingMode... addressingModes)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		for (AddressingMode addressingMode : addressingModes) {
			Instruction instruction = (Instruction) instructionClass.getConstructor(AddressingMode.class)
					.newInstance(addressingMode);
			assertThrows(InstructionNotSupportedException.class, () -> instruction.getCycles());
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
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, InstructionNotSupportedException {
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
	@ValueSource(classes = { ASLInstruction.class, LSRInstruction.class, ROLInstruction.class, RORInstruction.class })
	void testCyclesAluGroup2(Class<?> clazz)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		// Working
		testWorking(clazz, new int[] { 2, 5, 6, 6, 7 }, ACCUMULATOR, ZEROPAGE, ZEROPAGE_X, ABSOLUTE, ABSOLUTE_X);

		// Others
		testException(clazz, IMPLICIT, IMMEDIATE, ZEROPAGE_Y, RELATIVE, ABSOLUTE_Y, INDIRECT, INDIRECT_X, INDIRECT_Y);
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
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		// Working
		testWorking(clazz, new int[] { 2, 3, 4 }, IMMEDIATE, ZEROPAGE, ABSOLUTE);

		// Others
		testException(clazz, IMPLICIT, ACCUMULATOR, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE, ABSOLUTE_X, ABSOLUTE_Y, INDIRECT,
				INDIRECT_X, INDIRECT_Y);
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
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		// Working
		testWorking(clazz, new int[] { 5, 6, 6, 7 }, ZEROPAGE, ZEROPAGE_X, ABSOLUTE, ABSOLUTE_X);

		// Others
		testException(clazz, IMPLICIT, ACCUMULATOR, IMMEDIATE, ZEROPAGE_Y, RELATIVE, ABSOLUTE_Y, INDIRECT, INDIRECT_X,
				INDIRECT_Y);
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
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		// Working
		testWorkingRelative(clazz, 2);

		// Others
		testException(clazz, IMPLICIT, ACCUMULATOR, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ZEROPAGE_Y, ABSOLUTE, ABSOLUTE_X,
				ABSOLUTE_Y, INDIRECT, INDIRECT_X, INDIRECT_Y);
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
	void testCyclesBIT(Class<?> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		// Working
		testWorking(clazz, new int[] { 3, 4 }, ZEROPAGE, ABSOLUTE);

		// Others
		testException(clazz, IMPLICIT, ACCUMULATOR, IMMEDIATE, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE, ABSOLUTE_X, ABSOLUTE_Y,
				INDIRECT, INDIRECT_X, INDIRECT_Y);
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
	void testCyclesBRK(Class<?> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		// Working
		testWorking(clazz, new int[] { 7 }, IMPLICIT);

		// Others
		testException(clazz, ACCUMULATOR, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE, ABSOLUTE, ABSOLUTE_X,
				ABSOLUTE_Y, INDIRECT, INDIRECT_X, INDIRECT_Y);
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
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		// Working
		testWorking(clazz, new int[] { 2 }, IMPLICIT);

		// Others
		testException(clazz, ACCUMULATOR, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE, ABSOLUTE, ABSOLUTE_X,
				ABSOLUTE_Y, INDIRECT, INDIRECT_X, INDIRECT_Y);
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
	void testCyclesJMP(Class<?> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		// Working
		testWorking(clazz, new int[] { 3, 5 }, ABSOLUTE, INDIRECT);

		// Others
		testException(clazz, IMPLICIT, ACCUMULATOR, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE, ABSOLUTE_X,
				ABSOLUTE_Y, INDIRECT_X, INDIRECT_Y);
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
	void testCyclesJSR(Class<?> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		// Working
		testWorking(clazz, new int[] { 6 }, ABSOLUTE);

		// Others
		testException(clazz, IMPLICIT, ACCUMULATOR, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE, ABSOLUTE_X,
				ABSOLUTE_Y, INDIRECT, INDIRECT_X, INDIRECT_Y);
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
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		// Working
		testWorking(clazz, new int[] { 2, 3, 4, 4 }, IMMEDIATE, ZEROPAGE, ZEROPAGE_Y, ABSOLUTE);
		testWorkingPageCross(clazz, new int[] { 4 }, ABSOLUTE_Y);

		// Others
		testException(clazz, IMPLICIT, ACCUMULATOR, ZEROPAGE_X, RELATIVE, ABSOLUTE_X, INDIRECT, INDIRECT_X, INDIRECT_Y);
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
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		// Working
		testWorking(clazz, new int[] { 2, 3, 4, 4 }, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ABSOLUTE);
		testWorkingPageCross(clazz, new int[] { 4 }, ABSOLUTE_X);

		// Others
		testException(clazz, IMPLICIT, ACCUMULATOR, ZEROPAGE_Y, RELATIVE, ABSOLUTE_Y, INDIRECT, INDIRECT_X, INDIRECT_Y);
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
	void testCyclesPush(Class<?> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		// Working
		testWorking(clazz, new int[] { 3 }, IMPLICIT);

		// Others
		testException(clazz, ACCUMULATOR, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE, ABSOLUTE, ABSOLUTE_X,
				ABSOLUTE_Y, INDIRECT, INDIRECT_X, INDIRECT_Y);
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
	void testCyclesPull(Class<?> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		// Working
		testWorking(clazz, new int[] { 4 }, IMPLICIT);

		// Others
		testException(clazz, ACCUMULATOR, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE, ABSOLUTE, ABSOLUTE_X,
				ABSOLUTE_Y, INDIRECT, INDIRECT_X, INDIRECT_Y);
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
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		// Working
		testWorking(clazz, new int[] { 6 }, IMPLICIT);

		// Others
		testException(clazz, ACCUMULATOR, IMMEDIATE, ZEROPAGE, ZEROPAGE_X, ZEROPAGE_Y, RELATIVE, ABSOLUTE, ABSOLUTE_X,
				ABSOLUTE_Y, INDIRECT, INDIRECT_X, INDIRECT_Y);
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
	void testCyclesSTA(Class<?> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		// Working
		testWorking(clazz, new int[] { 3, 4, 4, 5, 5, 6, 6 }, ZEROPAGE, ZEROPAGE_X, ABSOLUTE, ABSOLUTE_X, ABSOLUTE_Y,
				INDIRECT_X, INDIRECT_Y);

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
	void testCyclesSTX(Class<?> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		// Working
		testWorking(clazz, new int[] { 3, 4, 4 }, ZEROPAGE, ZEROPAGE_Y, ABSOLUTE);

		// Others
		testException(clazz, IMPLICIT, ACCUMULATOR, IMMEDIATE, ZEROPAGE_X, RELATIVE, ABSOLUTE_X, ABSOLUTE_Y, INDIRECT,
				INDIRECT_X, INDIRECT_Y);
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
	void testCyclesSTY(Class<?> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, InstructionNotSupportedException {
		// Working
		testWorking(clazz, new int[] { 3, 4, 4 }, ZEROPAGE, ZEROPAGE_X, ABSOLUTE);

		// Others
		testException(clazz, IMPLICIT, ACCUMULATOR, IMMEDIATE, ZEROPAGE_Y, RELATIVE, ABSOLUTE_X, ABSOLUTE_Y, INDIRECT,
				INDIRECT_X, INDIRECT_Y);
	}

}