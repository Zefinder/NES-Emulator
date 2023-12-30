package instructions;

import java.util.HashMap;

public class InstructionInfo {

	private static final byte NOCOD = (byte) 0xFF;

	public enum InstructionSet {

		// Ordre : Implicit, Accumulator, Immediate, ZP, ZPX, ZPY, Relative, Abs, AbsX,
		// AbsY, Indir, IndirX, IndirY
//		ADC(new byte[] { NOCOD, NOCOD, 0x69, 0x65, 0x75, NOCOD, NOCOD, 0x6D, 0x7D, 0x79, NOCOD, 0x61, 0x71 }),
		AND(new byte[] { NOCOD, NOCOD, 0x29, 0x25, 0x35, NOCOD, NOCOD, 0x2D, 0x3D, 0x39, NOCOD, 0x21, 0x31 }),
		ASL(new byte[] { NOCOD, 0x0A, NOCOD, 0x06, 0x16, NOCOD, NOCOD, 0x0E, 0x1E, NOCOD, NOCOD, NOCOD, NOCOD }),
		BCC(new byte[] { NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, (byte) 0x90, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD,
				NOCOD }),
		BCS(new byte[] { NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, (byte) 0xB0, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD,
				NOCOD }),
		BEQ(new byte[] { NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, (byte) 0xF0, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD,
				NOCOD }),
		BIT(new byte[] { NOCOD, NOCOD, NOCOD, 0x24, NOCOD, NOCOD, NOCOD, 0x2C, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD }),
		BMI(new byte[] { NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, 0x30, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD }),
		BNE(new byte[] { NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, (byte) 0xD0, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD,
				NOCOD }),
		BPL(new byte[] { NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, 0x10, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD }),
		BRK(new byte[] { 0x00, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD }),
		BVC(new byte[] { NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, 0x50, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD }),
		BVS(new byte[] { NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, 0x70, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD }),
		CLC(new byte[] { 0x18, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD }),
		CLD(new byte[] { (byte) 0xD8, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD,
				NOCOD }),
		CLI(new byte[] { 0x58, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD }),
		CLV(new byte[] { (byte) 0xB8, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD,
				NOCOD }),
		CMP(new byte[] { NOCOD, NOCOD, (byte) 0xC9, (byte) 0xC5, (byte) 0xD5, NOCOD, NOCOD, (byte) 0xCD, (byte) 0xDD,
				(byte) 0xD9, NOCOD, (byte) 0xC1, (byte) 0xD1 }),
		CPX(new byte[] { NOCOD, NOCOD, (byte) 0xE0, (byte) 0xE4, NOCOD, NOCOD, NOCOD, (byte) 0xEC, NOCOD, NOCOD, NOCOD,
				NOCOD, NOCOD }),
		CPY(new byte[] { NOCOD, NOCOD, (byte) 0xC0, (byte) 0xC4, NOCOD, NOCOD, NOCOD, (byte) 0xCC, NOCOD, NOCOD, NOCOD,
				NOCOD, NOCOD }),
		DEC(new byte[] { NOCOD, NOCOD, NOCOD, (byte) 0xC6, (byte) 0xD6, NOCOD, NOCOD, (byte) 0xCE, (byte) 0xDE, NOCOD,
				NOCOD, NOCOD, NOCOD }),
		DEX(new byte[] { (byte) 0xCA, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD,
				NOCOD }),
		DEY(new byte[] { (byte) 0x88, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD,
				NOCOD }),
		EOR(new byte[] { NOCOD, NOCOD, 0x49, 0x45, 0x55, NOCOD, NOCOD, 0x4D, 0x5D, 0x59, NOCOD, 0x41, 0x51 }),
		INC(new byte[] { NOCOD, NOCOD, NOCOD, (byte) 0xE6, (byte) 0xF6, NOCOD, NOCOD, (byte) 0xEE, (byte) 0xFE, NOCOD,
				NOCOD, NOCOD, NOCOD }),
		INX(new byte[] { (byte) 0xE8, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD,
				NOCOD }),
		INY(new byte[] { (byte) 0xC8, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD,
				NOCOD }),
		JMP(new byte[] { NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, 0x4C, NOCOD, NOCOD, 0x6C, NOCOD, NOCOD }),
		JSR(new byte[] { NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, 0x20, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD }),
		LDA(new byte[] { NOCOD, NOCOD, (byte) 0xA9, (byte) 0xA5, (byte) 0xB5, NOCOD, NOCOD, (byte) 0xAD, (byte) 0xBD,
				(byte) 0xB9, NOCOD, (byte) 0xA1, (byte) 0xB1 }),
		LDX(new byte[] { NOCOD, NOCOD, (byte) 0xA2, (byte) 0xA6, NOCOD, (byte) 0xB6, NOCOD, (byte) 0xAE, NOCOD,
				(byte) 0xBE, NOCOD, NOCOD, NOCOD }),
		LDY(new byte[] { NOCOD, NOCOD, (byte) 0xA0, (byte) 0xA4, (byte) 0xB4, NOCOD, NOCOD, (byte) 0xAC, (byte) 0xBC,
				NOCOD, NOCOD, NOCOD, NOCOD }),
		LSR(new byte[] { NOCOD, 0x4A, NOCOD, 0x46, 0x56, NOCOD, NOCOD, 0x4E, 0x5E, NOCOD, NOCOD, NOCOD, NOCOD }),
		NOP(new byte[] { (byte) 0xEA, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD,
				NOCOD }),
		ORA(new byte[] { NOCOD, NOCOD, 0x09, 0x05, 0x15, NOCOD, NOCOD, 0x0D, 0x1D, 0x19, NOCOD, 0x01, 0x11 }),
		PHA(new byte[] { 0x48, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD }),
		PHP(new byte[] { 0x08, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD }),
		PLA(new byte[] { 0x68, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD }),
		PLP(new byte[] { 0x28, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD }),
		ROL(new byte[] { NOCOD, 0x2A, NOCOD, 0x26, 0x36, NOCOD, NOCOD, 0x2E, 0x3E, NOCOD, NOCOD, NOCOD, NOCOD }),
		ROR(new byte[] { NOCOD, 0x6A, NOCOD, 0x66, 0x76, NOCOD, NOCOD, 0x6E, 0x7E, NOCOD, NOCOD, NOCOD, NOCOD }),
		RTI(new byte[] { 0x40, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD }),
		RTS(new byte[] { 0x60, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD }),
		SBC(new byte[] { NOCOD, NOCOD, (byte) 0xE9, (byte) 0xE5, (byte) 0xF5, NOCOD, NOCOD, (byte) 0xED, (byte) 0xFD,
				(byte) 0xF9, NOCOD, (byte) 0xE1, (byte) 0xF1 }),
		SEC(new byte[] { 0x38, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD }),
		SED(new byte[] { (byte) 0xF8, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD,
				NOCOD }),
		SEI(new byte[] { 0x78, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD }),
		STA(new byte[] { NOCOD, NOCOD, NOCOD, (byte) 0x85, (byte) 0x95, NOCOD, NOCOD, (byte) 0x8D, (byte) 0x9D,
				(byte) 0x99, NOCOD, (byte) 0x81, (byte) 0x91 }),
		STX(new byte[] { NOCOD, NOCOD, NOCOD, (byte) 0x86, NOCOD, (byte) 0x96, NOCOD, (byte) 0x8E, NOCOD, NOCOD, NOCOD,
				NOCOD, NOCOD }),
		STY(new byte[] { NOCOD, NOCOD, NOCOD, (byte) 0x84, (byte) 0x94, NOCOD, NOCOD, (byte) 0x8C, NOCOD, NOCOD, NOCOD,
				NOCOD, NOCOD }),
		TAX(new byte[] { (byte) 0xAA, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD,
				NOCOD }),
		TAY(new byte[] { (byte) 0xA8, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD,
				NOCOD }),
		TSX(new byte[] { (byte) 0xBA, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD,
				NOCOD }),
		TXA(new byte[] { (byte) 0x8A, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD,
				NOCOD }),
		TXS(new byte[] { (byte) 0x9A, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD,
				NOCOD }),
		TYA(new byte[] { (byte) 0x98, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD,
				NOCOD }),
		NMI(new byte[] { NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD, NOCOD });

		private byte[] opCodes;

		private InstructionSet(byte[] opCodes) {
			this.opCodes = opCodes;
		}

		public byte[] getOpCodes() {
			return this.opCodes;
		}
	}

	private static final InstructionInfo instance = new InstructionInfo();
	private static final HashMap<Integer, Instruction> instructionMap = new HashMap<Integer, Instruction>();

//	private InstructionSet instruction;
//	private AddressingMode addressingMode;
//	private int lsb, msb;

	private InstructionInfo() {
		// Init of the instruction map
		instructionMap.put(0x69, new ADCInstruction(AddressingMode.IMMEDIATE));
		instructionMap.put(0x65, new ADCInstruction(AddressingMode.ZEROPAGE));
		instructionMap.put(0x75, new ADCInstruction(AddressingMode.ZEROPAGE_X));
		instructionMap.put(0x6D, new ADCInstruction(AddressingMode.ABSOLUTE));
		instructionMap.put(0x7D, new ADCInstruction(AddressingMode.ABSOLUTE_X));
		instructionMap.put(0x79, new ADCInstruction(AddressingMode.ABSOLUTE_Y));
		instructionMap.put(0x61, new ADCInstruction(AddressingMode.INDIRECT_X));
		instructionMap.put(0x71, new ADCInstruction(AddressingMode.INDIRECT_Y));
		
	}
	
	public static InstructionInfo getInstance() {
		return instance;
	}
//
//	public InstructionSet getInstruction() {
//		return this.instruction;
//	}
//
//	public void setArgument(int lsb, int msb) {
//		this.lsb = lsb;
//		this.msb = msb;
//	}
//
//	public int getLsb() {
//		return this.lsb;
//	}
//
//	public int getAdress() {
//		int tmpLsb = (lsb < 0 ? lsb + 256 : lsb);
//		int tmpMsb = (msb < 0 ? msb + 256 : msb);
//		int address = (tmpMsb << 8) | tmpLsb;
//		return address;
//	}
//
//	public int getByteNumber() {
//		return this.byteNumber;
//	}
//
//	public AddressingMode getAddressingMode() {
//		return addressingMode;
//	}
//
//	@Override
//	public String toString() {
//		String suffix;
//		int n;
//
//		switch (addressingMode) {
//		case IMPLICIT:
//			suffix = "";
//			break;
//
//		case ACCUMULATOR:
//			suffix = "A";
//			break;
//
//		case IMMEDIATE:
//			n = (lsb >= 0 ? lsb : lsb + 256);
//			suffix = String.format("#%d", n);
//			break;
//
//		case ZEROPAGE:
//			suffix = String.format("$%02x", (byte) lsb);
//			break;
//
//		case ZEROPAGE_X:
//			suffix = String.format("$%02x, X", (byte) lsb);
//			break;
//
//		case ZEROPAGE_Y:
//			suffix = String.format("$%02x, Y", (byte) lsb);
//			break;
//
//		case RELATIVE:
//			int lol = lsb;
//			n = lol + 2;
//			if (n > 0)
//				suffix = String.format("*+%d", n);
//			else
//				suffix = String.format("*%d", n);
//			break;
//
//		case ABSOLUTE:
//			suffix = String.format("$%02x%02x", (byte) msb, (byte) lsb);
//			break;
//
//		case ABSOLUTE_X:
//			suffix = String.format("$%02x%02x, X", (byte) msb, (byte) lsb);
//			break;
//
//		case ABSOLUTE_Y:
//			suffix = String.format("$%02x%02x, Y", (byte) msb, (byte) lsb);
//			break;
//
//		case INDIRECT:
//			suffix = String.format("($%02x%02x)", (byte) msb, (byte) lsb);
//			break;
//
//		case INDIRECT_X:
//			suffix = String.format("($%02x, X)", (byte) lsb);
//			break;
//
//		case INDIRECT_Y:
//			suffix = String.format("($%02x, Y)", (byte) lsb);
//			break;
//
//		default:
//			suffix = "";
//			break;
//		}
//
//		return this.instruction.toString() + " " + suffix;
//	}
//
//	private void setInstruction(byte code) {
//		InstructionSet finalInstruction = InstructionSet.NOP;
//		AddressingMode addressingMode = AddressingMode.IMPLICIT;
//		boolean trouve = false;
//
//		if (code != NOCOD)
//			for (InstructionSet instruction : InstructionSet.values()) {
//				byte[] opCodes = instruction.getOpCodes();
//				for (int i = 0; i < opCodes.length; i++) {
//					if (code == opCodes[i]) {
//						finalInstruction = instruction;
//						addressingMode = AddressingMode.values()[i];
//						trouve = true;
//						break;
//					}
//					if (trouve)
//						break;
//				}
//			}
//
//		this.instruction = finalInstruction;
//		this.addressingMode = addressingMode;
//	}
//
//	private void setbyteNumber() {
//		int byteNumber;
//
//		switch (addressingMode) {
//		case IMPLICIT:
//			byteNumber = 1;
//			break;
//
//		case ACCUMULATOR:
//			byteNumber = 1;
//			break;
//
//		case IMMEDIATE:
//			byteNumber = 2;
//			break;
//
//		case ZEROPAGE:
//			byteNumber = 2;
//			break;
//
//		case ZEROPAGE_X:
//			byteNumber = 2;
//			break;
//
//		case ZEROPAGE_Y:
//			byteNumber = 2;
//			break;
//
//		case RELATIVE:
//			byteNumber = 2;
//			break;
//
//		case ABSOLUTE:
//			byteNumber = 3;
//			break;
//
//		case ABSOLUTE_X:
//			byteNumber = 3;
//			break;
//
//		case ABSOLUTE_Y:
//			byteNumber = 3;
//			break;
//
//		case INDIRECT:
//			byteNumber = 3;
//			break;
//
//		case INDIRECT_X:
//			byteNumber = 2;
//			break;
//
//		case INDIRECT_Y:
//			byteNumber = 2;
//			break;
//
//		default:
//			byteNumber = 1;
//			break;
//		}
//
//		this.byteNumber = byteNumber;
//	}

}
