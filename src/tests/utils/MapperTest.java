package utils;

import components.MemoryImpl;
import components.TranslatedAddress;
import components.bus.Bus;
import mapper.Mapper;

public class MapperTest extends Mapper {

	// Address of special register
	public static final int SPECIAL_REGISTER_ADDRESS = 0x1234;

	private static class CpuMapperTest extends Bus {

		private MemoryImpl memory;
		
		private int readCounter = 0;
		private int writeCounter = 0;

		public CpuMapperTest() {
			memory = new MemoryImpl(0xBFE0);
		}

		@Override
		protected boolean checkImpl() {
			// Not link to another component
			return true;
		}

		@Override
		protected TranslatedAddress translateAddress(int address) {
			if (address == SPECIAL_REGISTER_ADDRESS) {
				readCounter += 1;
			}
			
			return new TranslatedAddress(memory, address);
		}
	}

	private static class PpuMapperTest extends MemoryImpl {

		public PpuMapperTest() {
			super(0x2000);
		}

	}

	private CpuMapperTest cpuMapper;
//	private PpuMapperTest ppuMapper;
	
	public MapperTest() {
		super(new CpuMapperTest(), new PpuMapperTest());
		cpuMapper = (CpuMapperTest) super.getCpuBusMemory();
//		ppuMapper = (PpuMapperTest) super.getPpuBusMemory();
	}

	public int getReadCounter() {
		return cpuMapper.readCounter;
	}

	public int getWriteCounter() {
		return cpuMapper.writeCounter;
	}

	public void resetCounters() {
		cpuMapper.readCounter = 0;
		cpuMapper.writeCounter = 0;
	}

}
