package nes.instructions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import components.Cpu;
import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;
import instructions.alu.ASLInstruction;
import instructions.registermemory.LDAInstruction;
import instructions.registermemory.STAInstruction;
import nes.MapperTest;

class TestInstructionReadWrite {

	static final MapperTest mapper = new MapperTest();
	static final Cpu cpu = Cpu.getInstance();

	@BeforeEach
	public void resetCPU() {
		// Set mapper
		cpu.setMapper(mapper);

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

		// Reset counters
		mapper.resetCounters();
	}

	@Test
	void testReadWriteSpecialRegister() throws InstructionNotSupportedException {
		// Here we test if writing only writes and do not read, read only reads and do
		// not write, etc...
		// LDA reads but don't write
		LDAInstruction ldaInstruction = new LDAInstruction(AddressingMode.ABSOLUTE,
				MapperTest.SPECIAL_REGISTER_ADDRESS);
		ldaInstruction.execute();
		assertEquals(1, mapper.getReadCounter(), "LDA reads register!");
		assertEquals(0, mapper.getWriteCounter(), "LDA do not write register!");
		mapper.resetCounters();

		// STA writes but don't read
		STAInstruction staInstruction = new STAInstruction(AddressingMode.ABSOLUTE,
				MapperTest.SPECIAL_REGISTER_ADDRESS);
		staInstruction.execute();
		assertEquals(0, mapper.getReadCounter(), "STA do not read register!");
		assertEquals(1, mapper.getWriteCounter(), "STA writes register!");
		mapper.resetCounters();

		// ASL reads and writes
		ASLInstruction aslInstruction = new ASLInstruction(AddressingMode.ABSOLUTE,
				MapperTest.SPECIAL_REGISTER_ADDRESS);
		aslInstruction.execute();
		assertEquals(1, mapper.getReadCounter(), "ASL reads register!");
		assertEquals(1, mapper.getWriteCounter(), "ASL writes register!");
		mapper.resetCounters();
	}

	// If all addressing modes work, then each instruction must be tested with only
	// one addressing mode! (Like ASL)
	// Test to put 0x50 with all memory-based addressing modes
	@Test
	void testMemoryZeroPageAddressingMode() {
		for (int zeroPageAddress = 0; zeroPageAddress <= 0xFF; zeroPageAddress++) {
			Instruction instruction = new Instruction(AddressingMode.ZEROPAGE, zeroPageAddress) {
				@Override
				public Instruction newInstruction(int constant) {
					return null;
				}

				@Override
				public String getName() {
					return null;
				}

				@Override
				public int getCycle() throws InstructionNotSupportedException {
					return 0;
				}

				@Override
				public void execute() throws InstructionNotSupportedException {
					this.storeMemory(0x50);
				}
			};

			try {
				instruction.execute();
			} catch (InstructionNotSupportedException e) {
				e.printStackTrace();
			}

			assertEquals(0x50, cpu.fetchMemory(zeroPageAddress),
					"Value is not 0x50 for address 0x%04X".formatted(zeroPageAddress));
		}
	}

	@Test
	void testMemoryZeroPageXAddressingMode() {
		for (int zeroPageAddress = 0; zeroPageAddress <= 0xFF; zeroPageAddress++) {
			for (int xValue = 0; xValue <= 0xFF; xValue++) {
				cpu.cpuInfo.X = xValue;
				Instruction instruction = new Instruction(AddressingMode.ZEROPAGE_X, zeroPageAddress) {
					@Override
					public Instruction newInstruction(int constant) {
						return null;
					}

					@Override
					public String getName() {
						return null;
					}

					@Override
					public int getCycle() throws InstructionNotSupportedException {
						return 0;
					}

					@Override
					public void execute() throws InstructionNotSupportedException {
						this.storeMemory(0x50);
					}
				};

				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				assertEquals(0x50, cpu.fetchMemory((zeroPageAddress + xValue) & 0xFF),
						"Value is not 0x50 for address 0x%04X (X=0x%02X)".formatted(zeroPageAddress, xValue));
			}
		}
	}

	@Test
	void testMemoryZeroPageYAddressingMode() {
		for (int zeroPageAddress = 0; zeroPageAddress <= 0xFF; zeroPageAddress++) {
			for (int yValue = 0; yValue <= 0xFF; yValue++) {
				cpu.cpuInfo.Y = yValue;
				Instruction instruction = new Instruction(AddressingMode.ZEROPAGE_Y, zeroPageAddress) {
					@Override
					public Instruction newInstruction(int constant) {
						return null;
					}

					@Override
					public String getName() {
						return null;
					}

					@Override
					public int getCycle() throws InstructionNotSupportedException {
						return 0;
					}

					@Override
					public void execute() throws InstructionNotSupportedException {
						this.storeMemory(0x50);
					}
				};

				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				assertEquals(0x50, cpu.fetchMemory((zeroPageAddress + yValue) & 0xFF),
						"Value is not 0x50 for address 0x%04X (Y=0x%02X)".formatted(zeroPageAddress, yValue));
			}
		}
	}

	@Test
	void testMemoryAbsoluteAddressingMode() {
		for (int absoluteAddress = 0; absoluteAddress <= 0xFFFF; absoluteAddress++) {
			Instruction instruction = new Instruction(AddressingMode.ABSOLUTE, absoluteAddress) {
				@Override
				public Instruction newInstruction(int constant) {
					return null;
				}

				@Override
				public String getName() {
					return null;
				}

				@Override
				public int getCycle() throws InstructionNotSupportedException {
					return 0;
				}

				@Override
				public void execute() throws InstructionNotSupportedException {
					this.storeMemory(0x50);
				}
			};

			try {
				instruction.execute();
			} catch (InstructionNotSupportedException e) {
				e.printStackTrace();
			}

			assertEquals(0x50, cpu.fetchMemory(absoluteAddress),
					"Value is not 0x50 for address 0x%04X".formatted(absoluteAddress));
		}
	}

	@Test
	void testMemoryAbsoluteXAddressingMode() {
		for (int absoluteAddress = 0; absoluteAddress <= 0xFFFF; absoluteAddress++) {
			for (int xValue = 0; xValue <= 0xFF; xValue++) {
				cpu.cpuInfo.X = xValue;

				Instruction instruction = new Instruction(AddressingMode.ABSOLUTE_X, absoluteAddress) {
					@Override
					public Instruction newInstruction(int constant) {
						return null;
					}

					@Override
					public String getName() {
						return null;
					}

					@Override
					public int getCycle() throws InstructionNotSupportedException {
						return 0;
					}

					@Override
					public void execute() throws InstructionNotSupportedException {
						this.storeMemory(0x50);
					}
				};

				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				assertEquals(0x50, cpu.fetchMemory((absoluteAddress + xValue) & 0xFFFF),
						"Value is not 0x50 for address 0x%04X (X=0x%02X)".formatted(absoluteAddress, xValue));
			}
		}
	}

	@Test
	void testMemoryAbsoluteYAddressingMode() {
		for (int absoluteAddress = 0; absoluteAddress <= 0xFFFF; absoluteAddress++) {
			for (int yValue = 0; yValue <= 0xFF; yValue++) {
				cpu.cpuInfo.Y = yValue;

				Instruction instruction = new Instruction(AddressingMode.ABSOLUTE_Y, absoluteAddress) {
					@Override
					public Instruction newInstruction(int constant) {
						return null;
					}

					@Override
					public String getName() {
						return null;
					}

					@Override
					public int getCycle() throws InstructionNotSupportedException {
						return 0;
					}

					@Override
					public void execute() throws InstructionNotSupportedException {
						this.storeMemory(0x50);
					}
				};

				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				assertEquals(0x50, cpu.fetchMemory((absoluteAddress + yValue) & 0xFFFF),
						"Value is not 0x50 for address 0x%04X (Y=0x%02X)".formatted(absoluteAddress, yValue));
			}
		}
	}

	@Test
	void testMemoryIndirectXAddressingMode() {
		for (int zeroPageAddress = 0; zeroPageAddress <= 0xFF; zeroPageAddress++) {
			for (int xValue = 0; xValue <= 0xFF; xValue++) {
				// Set address to put the value in
				cpu.storeMemory((zeroPageAddress + xValue) & 0xFF, 0x34, 0x12);
				cpu.cpuInfo.X = xValue;

				Instruction instruction = new Instruction(AddressingMode.INDIRECT_X, zeroPageAddress) {
					@Override
					public Instruction newInstruction(int constant) {
						return null;
					}

					@Override
					public String getName() {
						return null;
					}

					@Override
					public int getCycle() throws InstructionNotSupportedException {
						return 0;
					}

					@Override
					public void execute() throws InstructionNotSupportedException {
						this.storeMemory(0x50);
					}
				};

				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				assertEquals(0x50, cpu.fetchMemory(0x1234),
						"Value is not 0x50 for address 0x1234 (ZP=0x%02X,X=0x%02X)".formatted(zeroPageAddress, xValue));
			}
		}
	}
	
	@Test
	void testMemoryIndirectYAddressingMode() {
		for (int zeroPageAddress = 0; zeroPageAddress <= 0xFF; zeroPageAddress++) {
			cpu.storeMemory(zeroPageAddress, 0x34, 0x12);
			for (int yValue = 0; yValue <= 0xFF; yValue++) {
				// Set address to put the value in
				cpu.cpuInfo.Y = yValue;

				Instruction instruction = new Instruction(AddressingMode.INDIRECT_Y, zeroPageAddress) {
					@Override
					public Instruction newInstruction(int constant) {
						return null;
					}

					@Override
					public String getName() {
						return null;
					}

					@Override
					public int getCycle() throws InstructionNotSupportedException {
						return 0;
					}

					@Override
					public void execute() throws InstructionNotSupportedException {
						this.storeMemory(0x50);
					}
				};

				try {
					instruction.execute();
				} catch (InstructionNotSupportedException e) {
					e.printStackTrace();
				}

				assertEquals(0x50, cpu.fetchMemory((0x1234 + yValue) & 0xFFFF),
						"Value is not 0x50 for address 0x12%02X (ZP=0x%02X)".formatted(yValue, zeroPageAddress));
			}
		}
	}
}
