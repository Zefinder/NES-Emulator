package nes.instructions;

import nes.components.Bus;
import nes.components.cpu.register.CPURegisters;
import nes.exceptions.AddressException;

public class InstructionReader {

	private Bus bus;
	@SuppressWarnings("unused")
	private byte C, Z, I, D, B, V, N;
	private final byte C_MASK = 0b00000001;
	private final byte Z_MASK = 0b00000010;
	private final byte I_MASK = 0b00000100;
	private final byte D_MASK = 0b00001000;
	private final byte B_MASK = 0b00110000;
	private final byte V_MASK = 0b01000000;
	private final byte N_MASK = (byte) 0b10000000;

	private byte operand2;
	private int address;
//	private void cycles;

	public InstructionReader(Bus bus) {
		this.bus = bus;
	}

	public int getInstructionCycles(Instruction processing, CPURegisters registres) throws AddressException {
		int cycles = 0;
		switch (processing.getInstruction()) {
		case ADC:
			switch (processing.getAddressingMode()) {
			case IMMEDIATE:
				cycles += 2;
				break;

			case ZEROPAGE:
				cycles += 3;
				break;

			case ZEROPAGE_X:
				cycles += 4;
				break;

			case ABSOLUTE:
				cycles += 4;
				break;

			case ABSOLUTE_X:
				cycles += 4;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			case ABSOLUTE_Y:
				cycles += 4;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			case INDIRECT_X:
				cycles += 6;
				break;

			case INDIRECT_Y:
				cycles += 5;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			default:
				break;
			}
			break;

		case AND:
			switch (processing.getAddressingMode()) {
			case IMMEDIATE:
				cycles += 2;
				break;

			case ZEROPAGE:
				cycles += 3;
				break;

			case ZEROPAGE_X:
				cycles += 4;
				break;

			case ABSOLUTE:
				cycles += 4;
				break;

			case ABSOLUTE_X:
				cycles += 4;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			case ABSOLUTE_Y:
				cycles += 4;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			case INDIRECT_X:
				cycles += 6;
				break;

			case INDIRECT_Y:
				cycles += 5;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			default:
				break;
			}
			break;

		case ASL:
			switch (processing.getAddressingMode()) {
			case ACCUMULATOR:
				cycles += 2;
				break;

			case ZEROPAGE:
				cycles += 5;
				break;

			case ZEROPAGE_X:
				cycles += 6;
				break;

			case ABSOLUTE:
				cycles += 6;
				break;

			case ABSOLUTE_X:
				cycles += 7;
				break;

			default:
				break;
			}
			break;

		case BCC:
			cycles += 2;
			if (C == 0) {
				cycles += 1;
				if (isNewPage(registres.getPc()))
					cycles += 2;
			}
			break;

		case BCS:
			cycles += 2;
			if (C == 1) {
				cycles += 1;
				if (isNewPage(registres.getPc()))
					cycles += 2;
			}
			break;

		case BEQ:
			cycles += 2;
			if (Z == 1) {
				cycles += 1;
				if (isNewPage(registres.getPc()))
					cycles += 2;
			}
			break;

		case BIT:
			switch (processing.getAddressingMode()) {
			case ZEROPAGE:
				cycles += 3;
				break;

			case ABSOLUTE:
				cycles += 4;
			default:
				break;
			}
			break;

		case BMI:
			cycles += 2;
			if (N == 1) {
				cycles += 1;
				if (isNewPage(registres.getPc()))
					cycles += 2;
			}
			break;

		case BNE:
			cycles += 2;
			if (Z == 0) {
				cycles += 1;
				if (isNewPage(registres.getPc()))
					cycles += 2;
			}
			break;

		case BPL:
			cycles += 2;
			if (N == 0) {
				cycles += 1;
				if (isNewPage(registres.getPc()))
					cycles += 2;
			}
			break;

		case BRK:
			cycles += 7;
			break;

		case BVC:
			cycles += 2;
			if (V == 0) {
				cycles += 1;
				if (isNewPage(registres.getPc()))
					cycles += 2;
			}
			break;

		case BVS:
			cycles += 2;
			if (V == 1) {
				cycles += 1;
				if (isNewPage(registres.getPc()))
					cycles += 2;
			}
			break;

		case CLC:
			cycles += 2;
			break;

		case CLD:
			cycles += 2;
			break;

		case CLI:
			cycles += 2;
			break;

		case CLV:
			cycles += 2;
			break;

		case CMP:
			switch (processing.getAddressingMode()) {
			case IMMEDIATE:
				cycles += 2;
				break;

			case ZEROPAGE:
				cycles += 3;
				break;

			case ZEROPAGE_X:
				cycles += 4;
				break;

			case ABSOLUTE:
				cycles += 4;
				break;

			case ABSOLUTE_X:
				cycles += 4;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			case ABSOLUTE_Y:
				cycles += 4;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			case INDIRECT_X:
				cycles += 6;
				break;

			case INDIRECT_Y:
				cycles += 5;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			default:
				break;
			}
			break;

		case CPX:
			switch (processing.getAddressingMode()) {
			case IMMEDIATE:
				cycles += 2;
				break;

			case ZEROPAGE:
				cycles += 3;
				break;

			case ABSOLUTE:
				cycles += 4;
				break;

			default:
				break;
			}
			break;

		case CPY:
			switch (processing.getAddressingMode()) {
			case IMMEDIATE:
				cycles += 2;
				break;

			case ZEROPAGE:
				cycles += 3;
				break;

			case ABSOLUTE:
				cycles += 4;
				break;

			default:
				break;
			}
			break;

		case DEC:
			switch (processing.getAddressingMode()) {
			case ZEROPAGE:
				cycles += 5;
				break;

			case ZEROPAGE_X:
				cycles += 6;
				break;

			case ABSOLUTE:
				cycles += 6;
				break;

			case ABSOLUTE_X:
				cycles += 7;
				break;

			default:
				break;
			}
			break;

		case DEX:
			cycles += 2;
			break;

		case DEY:
			cycles += 2;
			break;

		case EOR:

			switch (processing.getAddressingMode()) {
			case IMMEDIATE:
				cycles += 2;
				break;

			case ZEROPAGE:
				cycles += 3;
				break;

			case ZEROPAGE_X:
				cycles += 4;
				break;

			case ABSOLUTE:
				cycles += 4;
				break;

			case ABSOLUTE_X:
				cycles += 4;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			case ABSOLUTE_Y:
				cycles += 4;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			case INDIRECT_X:
				cycles += 6;
				break;

			case INDIRECT_Y:
				cycles += 5;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			default:
				break;
			}
			break;

		case INC:
			switch (processing.getAddressingMode()) {
			case ZEROPAGE:
				cycles += 5;
				break;

			case ZEROPAGE_X:
				cycles += 6;
				break;

			case ABSOLUTE:
				cycles += 6;
				break;

			case ABSOLUTE_X:
				cycles += 7;
				break;

			default:
				break;
			}
			break;

		case INX:
			cycles += 2;
			break;

		case INY:
			cycles += 2;
			break;

		case JMP:
			switch (processing.getAddressingMode()) {
			case ABSOLUTE:
				cycles += 3;
				break;

			case INDIRECT:
				cycles += 5;

			default:
				break;
			}
			break;

		case JSR:
			cycles += 6;
			break;

		case LDA:
			switch (processing.getAddressingMode()) {
			case IMMEDIATE:
				cycles += 2;
				break;

			case ZEROPAGE:
				cycles += 3;
				break;

			case ZEROPAGE_X:
				cycles += 4;
				break;

			case ABSOLUTE:
				cycles += 4;
				break;

			case ABSOLUTE_X:
				cycles += 4;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			case ABSOLUTE_Y:
				cycles += 4;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			case INDIRECT_X:
				cycles += 6;
				break;

			case INDIRECT_Y:
				cycles += 5;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			default:
				break;
			}
			break;

		case LDX:
			switch (processing.getAddressingMode()) {
			case IMMEDIATE:
				cycles += 2;
				break;

			case ZEROPAGE:
				cycles += 3;
				break;

			case ZEROPAGE_Y:
				cycles += 4;
				break;

			case ABSOLUTE:
				cycles += 4;
				break;

			case ABSOLUTE_Y:
				cycles += 4;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			default:
				break;
			}
			break;

		case LDY:

			switch (processing.getAddressingMode()) {
			case IMMEDIATE:
				cycles += 2;
				break;

			case ZEROPAGE:
				cycles += 3;
				break;

			case ZEROPAGE_X:
				cycles += 4;
				break;

			case ABSOLUTE:
				cycles += 4;
				break;

			case ABSOLUTE_X:
				cycles += 4;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			default:
				break;
			}
			break;

		case LSR:

			switch (processing.getAddressingMode()) {
			case ACCUMULATOR:
				cycles += 2;
				break;

			case ZEROPAGE:
				cycles += 3;
				break;

			case ZEROPAGE_X:
				cycles += 4;
				break;

			case ABSOLUTE:
				cycles += 4;
				break;

			case ABSOLUTE_X:
				cycles += 4;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			default:
				break;
			}
			break;

		case NOP:
			cycles += 2;
			break;

		case ORA:
			switch (processing.getAddressingMode()) {
			case IMMEDIATE:
				cycles += 2;
				break;

			case ZEROPAGE:
				cycles += 3;
				break;

			case ZEROPAGE_X:
				cycles += 4;
				break;

			case ABSOLUTE:
				cycles += 4;
				break;

			case ABSOLUTE_X:
				cycles += 4;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			case ABSOLUTE_Y:
				cycles += 4;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			case INDIRECT_X:
				cycles += 6;
				break;

			case INDIRECT_Y:
				cycles += 5;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			default:
				break;
			}
			break;

		case PHA:
			cycles += 3;
			break;

		case PHP:
			cycles += 3;
			break;

		case PLA:
			cycles += 4;
			break;

		case PLP:
			cycles += 4;
			break;

		case ROL:
			switch (processing.getAddressingMode()) {
			case ACCUMULATOR:
				cycles += 2;
				break;

			case ZEROPAGE:
				cycles += 5;
				break;

			case ZEROPAGE_X:
				cycles += 6;
				break;

			case ABSOLUTE:
				cycles += 6;
				break;

			case ABSOLUTE_X:
				cycles += 7;
				break;

			default:
				break;
			}
			break;

		case ROR:
			switch (processing.getAddressingMode()) {
			case ACCUMULATOR:
				cycles += 2;
				break;

			case ZEROPAGE:
				cycles += 5;
				break;

			case ZEROPAGE_X:
				cycles += 6;
				break;

			case ABSOLUTE:
				cycles += 6;
				break;

			case ABSOLUTE_X:
				cycles += 7;
				break;

			default:
				break;
			}
			break;

		case RTI:
			cycles += 6;
			break;

		case RTS:
			cycles += 6;
			break;

		case SBC:
			switch (processing.getAddressingMode()) {
			case IMMEDIATE:
				cycles += 2;
				break;

			case ZEROPAGE:
				cycles += 3;
				break;

			case ZEROPAGE_X:
				cycles += 4;
				break;

			case ABSOLUTE:
				cycles += 4;
				break;

			case ABSOLUTE_X:
				cycles += 4;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			case ABSOLUTE_Y:
				cycles += 4;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			case INDIRECT_X:
				cycles += 6;
				break;

			case INDIRECT_Y:
				cycles += 5;
				if (isNewPage(registres.getPc())) {
					cycles += 1;
				}
				break;

			default:
				break;
			}
			break;

		case SEC:
			cycles += 2;
			break;

		case SED:
			cycles += 2;
			break;

		case SEI:
			cycles += 2;
			break;

		case STA:
			switch (processing.getAddressingMode()) {
			case ZEROPAGE:
				cycles += 3;
				break;

			case ZEROPAGE_X:
				cycles += 4;
				break;

			case ABSOLUTE:
				cycles += 4;
				break;

			case ABSOLUTE_X:
				cycles += 5;
				break;

			case ABSOLUTE_Y:
				cycles += 5;
				break;

			case INDIRECT_X:
				cycles += 6;
				break;

			case INDIRECT_Y:
				cycles += 6;
				break;

			default:
				break;
			}
			break;

		case STX:
			switch (processing.getAddressingMode()) {
			case ZEROPAGE:
				cycles += 3;
				break;

			case ZEROPAGE_Y:
				cycles += 4;
				break;

			case ABSOLUTE:
				cycles += 4;
				break;

			default:
				break;
			}
			break;

		case STY:
			switch (processing.getAddressingMode()) {
			case ZEROPAGE:
				cycles += 3;
				break;

			case ZEROPAGE_X:
				cycles += 4;
				break;

			case ABSOLUTE:
				cycles += 4;
				break;

			default:
				break;
			}
			break;

		case TAX:
			cycles += 2;
			break;

		case TAY:
			cycles += 2;
			break;

		case TSX:
			cycles += 2;
			break;

		case TXA:
			cycles += 2;
			break;

		case TXS:
			cycles += 2;
			break;

		case TYA:
			cycles += 2;
			break;

		case NMI:
			cycles += 7;

		default:
			break;
		}

		return cycles;
	}

	public int processInstruction(Instruction toProcess, CPURegisters registres) throws AddressException {
		address = 0;
		byte P = registres.getP();
		C = (byte) (C_MASK & P);
		Z = (byte) ((Z_MASK & P) >> 1);
		I = (byte) ((I_MASK & P) >> 2);
		D = (byte) ((D_MASK & P) >> 3);
		B = (byte) ((B_MASK & P) >> 4);
		V = (byte) ((V_MASK & P) >> 6);
		N = (byte) (-(N_MASK & P) >> 7);

		getByteFromInstruction(toProcess, registres.getA(), registres.getX(), registres.getY(), registres.getPc());
		int cycles = getInstructionCycles(toProcess, registres);

		switch (toProcess.getInstruction()) {
		case ADC:
			adc(toProcess, registres);
			break;

		case AND:
			and(toProcess, registres);
			break;

		case ASL:
			asl(toProcess, registres);
			break;

		case BCC:
			bcc(toProcess, registres);
			break;

		case BCS:
			bcs(toProcess, registres);
			break;

		case BEQ:
			beq(toProcess, registres);
			break;

		case BIT:
			bit(toProcess, registres);
			break;

		case BMI:
			bmi(toProcess, registres);
			break;

		case BNE:
			bne(toProcess, registres);
			break;

		case BPL:
			bpl(toProcess, registres);
			break;

		case BRK:
			brk(toProcess, registres);
			break;

		case BVC:
			bvc(toProcess, registres);
			break;

		case BVS:
			bvs(toProcess, registres);
			break;

		case CLC:
			clc(toProcess, registres);
			break;

		case CLD:
			cld(toProcess, registres);
			break;

		case CLI:
			cli(toProcess, registres);
			break;

		case CLV:
			clv(toProcess, registres);
			break;

		case CMP:
			cmp(toProcess, registres);
			break;

		case CPX:
			cpx(toProcess, registres);
			break;

		case CPY:
			cpy(toProcess, registres);
			break;

		case DEC:
			dec(toProcess, registres);
			break;

		case DEX:
			dex(toProcess, registres);
			break;

		case DEY:
			dey(toProcess, registres);
			break;

		case EOR:
			eor(toProcess, registres);
			break;

		case INC:
			inc(toProcess, registres);
			break;

		case INX:
			inx(toProcess, registres);
			break;

		case INY:
			iny(toProcess, registres);
			break;

		case JMP:
			jmp(toProcess, registres);
			break;

		case JSR:
			jsr(toProcess, registres);
			break;

		case LDA:
			lda(toProcess, registres);
			break;

		case LDX:
			ldx(toProcess, registres);
			break;

		case LDY:
			ldy(toProcess, registres);
			break;

		case LSR:
			lsr(toProcess, registres);
			break;

		case NOP:
			nop(toProcess, registres);
			break;

		case ORA:
			ora(toProcess, registres);
			break;

		case PHA:
			pha(toProcess, registres);
			break;

		case PHP:
			php(toProcess, registres);
			break;

		case PLA:
			pla(toProcess, registres);
			break;

		case PLP:
			plp(toProcess, registres);
			break;

		case ROL:
			rol(toProcess, registres);
			break;

		case ROR:
			ror(toProcess, registres);
			break;

		case RTI:
			rti(toProcess, registres);
			break;

		case RTS:
			rts(toProcess, registres);
			break;

		case SBC:
			sbc(toProcess, registres);
			break;

		case SEC:
			sec(toProcess, registres);
			break;

		case SED:
			sed(toProcess, registres);
			break;

		case SEI:
			sei(toProcess, registres);
			break;

		case STA:
			sta(toProcess, registres);
			break;

		case STX:
			stx(toProcess, registres);
			break;

		case STY:
			sty(toProcess, registres);
			break;

		case TAX:
			tax(toProcess, registres);
			break;

		case TAY:
			tay(toProcess, registres);
			break;

		case TSX:
			tsx(toProcess, registres);
			break;

		case TXA:
			txa(toProcess, registres);
			break;

		case TXS:
			txs(toProcess, registres);
			break;

		case TYA:
			tya(toProcess, registres);
			break;

		case NMI:
			nmi(registres);

		default:
			break;
		}

		return cycles;
	}

	public int[] getOperandAndAddress(Instruction processing, byte A, byte X, byte Y, int pc) throws AddressException {
		getByteFromInstruction(processing, A, X, Y, pc);
		return new int[] { operand2, address };
	}

	private void getByteFromInstruction(Instruction processing, byte A, byte X, byte Y, int pc)
			throws AddressException {
		int lsb, msb;
		int tmpX = (X < 0 ? X + 256 : X);
		int tmpY = (Y < 0 ? Y + 256 : Y);
		switch (processing.getAddressingMode()) {
		case IMPLICIT:
			address = 0;
			operand2 = 0;
			break;

		case ACCUMULATOR:
			address = 0;
			operand2 = A;
			break;

		case IMMEDIATE:
			address = 0;
			operand2 = (byte) processing.getLsb();
			break;

		case ZEROPAGE:
			lsb = (processing.getLsb() < 0 ? processing.getLsb() + 256 : processing.getLsb());
			address = lsb;
			operand2 = bus.getByteFromMemory(address);
			break;

		case ZEROPAGE_X:
			lsb = (processing.getLsb() < 0 ? processing.getLsb() + 256 : processing.getLsb());
			address = (lsb + tmpX) % 0x100;
			operand2 = bus.getByteFromMemory(address);
			break;

		case ZEROPAGE_Y:
			lsb = (processing.getLsb() < 0 ? processing.getLsb() + 256 : processing.getLsb());
			address = (lsb + tmpY) % 0x100;
			operand2 = bus.getByteFromMemory(address);
			break;

		case RELATIVE:
			address = pc + processing.getLsb();
			operand2 = bus.getByteFromMemory(address);
			break;

		case ABSOLUTE:
			address = processing.getAdress();
			operand2 = bus.getByteFromMemory(address);
			break;

		case ABSOLUTE_X:
			address = processing.getAdress() + tmpX;
			operand2 = bus.getByteFromMemory(address);
			break;

		case ABSOLUTE_Y:
			address = processing.getAdress() + tmpY;
			operand2 = bus.getByteFromMemory(address);
			break;

		case INDIRECT:
			lsb = bus.getByteFromMemory(processing.getAdress());
			if ((processing.getAdress() & 0x00FF) == 0xFF) {
				msb = bus.getByteFromMemory(processing.getAdress() & 0xFF00);
			} else {
				msb = bus.getByteFromMemory(processing.getAdress() + 1);
			}
			lsb = (lsb < 0 ? lsb + 256 : lsb);
			msb = (msb < 0 ? msb + 256 : msb);
			address = (msb << 8) | lsb;
			operand2 = bus.getByteFromMemory(address);
			break;

		case INDIRECT_X:
			lsb = (processing.getLsb() < 0 ? processing.getLsb() + 256 : processing.getLsb());
			lsb = bus.getByteFromMemory((lsb + tmpX) % 0x100);
			msb = (processing.getLsb() < 0 ? processing.getLsb() + 256 : processing.getLsb());
			msb = bus.getByteFromMemory((msb + tmpX + 1) % 0x100);

			lsb = (lsb < 0 ? lsb + 256 : lsb);
			msb = (msb < 0 ? msb + 256 : msb);

			address = (msb << 8) | lsb;
			operand2 = bus.getByteFromMemory(address);
			break;

		case INDIRECT_Y:
			lsb = (processing.getLsb() < 0 ? processing.getLsb() + 256 : processing.getLsb());
			lsb = bus.getByteFromMemory(lsb);
			msb = (processing.getLsb() < 0 ? processing.getLsb() + 256 : processing.getLsb());
			msb = bus.getByteFromMemory(msb + 1);

			lsb = (lsb < 0 ? lsb + 256 : lsb);
			msb = (msb < 0 ? msb + 256 : msb);
			address = ((msb << 8) | lsb) + tmpY;
			operand2 = bus.getByteFromMemory(address);
			break;

		case NMI:
			break;

		default:
			break;
		}
	}

	private void adc(Instruction processing, CPURegisters registres) throws AddressException {
		int tmp = 0;
		byte A = registres.getA();
		byte P = registres.getP();
		tmp += (A < 0 ? A + 256 : A);
		tmp += (operand2 < 0 ? operand2 + 256 : operand2) + C;
		A = (byte) tmp;

		// Carry flag
		// Non-signé => carry flag
		// Signé => overflow flag
		if (tmp > 255) {
			// Pour l'instant que non signé
			P |= C_MASK;
		} else {
			P &= ~C_MASK;
		}

		// Zero flag
		if (A == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (A < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setA(A);
		registres.setP(P);

	}

	private void and(Instruction processing, CPURegisters registres) {
		byte A = registres.getA();
		byte P = registres.getP();

		A &= operand2;
		// Zero flag
		if (A == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (A < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setA(A);
		registres.setP(P);

	}

	private void asl(Instruction processing, CPURegisters registres) throws AddressException {
		byte C = (byte) -((N_MASK & operand2) >> 7);
		byte tmp = (byte) (2 * operand2);
		byte P = registres.getP();

		// Carry flag
		if (C == 1) {
			P |= C_MASK;
		} else {
			P &= ~C_MASK;
		}

		// Zero flag
		if (tmp == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (tmp < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setP(P);

		switch (processing.getAddressingMode()) {
		case ACCUMULATOR:
			registres.setA(tmp);
			break;

		case ZEROPAGE:
			bus.setByteToMemory(address, tmp);
			break;

		case ZEROPAGE_X:
			bus.setByteToMemory(address, tmp);
			break;

		case ABSOLUTE:
			bus.setByteToMemory(address, tmp);
			break;

		case ABSOLUTE_X:
			bus.setByteToMemory(address, tmp);
			break;

		default:
			break;
		}

	}

	private void bcc(Instruction processing, CPURegisters registres) {
		if (C == 0)
			registres.setPc(address);

	}

	private void bcs(Instruction processing, CPURegisters registres) {
		if (C == 1) {
			registres.setPc(address);
		}

	}

	private void beq(Instruction processing, CPURegisters registres) {
		if (Z == 1) {
			registres.setPc(address);
		}

	}

	private void bit(Instruction processing, CPURegisters registres) {
		byte A = registres.getA();
		byte P = registres.getP();
		A &= operand2;

		// Zero flag
		if (A == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Overflow flag
		P |= operand2 & 0b01000000;

		// Negative flag
		P |= operand2 & 0b10000000;

		registres.setA(A);
		registres.setP(P);

	}

	private void bmi(Instruction processing, CPURegisters registres) {
		if (N == 1) {
			registres.setPc(address);
		}

	}

	private void bne(Instruction processing, CPURegisters registres) {
		if (Z == 0) {
			registres.setPc(address);
		}

	}

	private void bpl(Instruction processing, CPURegisters registres) {
		if (N == 0) {
			registres.setPc(address);
		}

	}

	private void brk(Instruction processing, CPURegisters registres) throws AddressException {
		byte pcl = (byte) (registres.getPc() & 0x00FF);
		byte pch = (byte) ((registres.getPc() & 0xFF00) >> 8);
		// pch = (byte) (pch < 0 ? pch + 256 : pch);
		byte P = registres.getP();

		// On push pc et P dans le stack
		bus.setByteToMemory(registres.getSp(), pch);
		if (registres.getSp() == 0x100)
			registres.setSp(0x1FF);
		else
			registres.setSp(registres.getSp() - 1);

		bus.setByteToMemory(registres.getSp(), pcl);
		if (registres.getSp() == 0x100)
			registres.setSp(0x1FF);
		else
			registres.setSp(registres.getSp() - 1);

		bus.setByteToMemory(registres.getSp(), P);
		if (registres.getSp() == 0x100)
			registres.setSp(0x1FF);
		else
			registres.setSp(registres.getSp() - 1);

		// On met à jour les registres
		P |= 0b00010000;
		registres.setP(P);

		// On met à jour le pc
		pcl = bus.getByteFromMemory(0xFFFE);
		pch = bus.getByteFromMemory(0xFFFF);
		int lsb = (pcl < 0 ? pcl + 256 : pcl);
		int msb = (pch < 0 ? pch + 256 : pch);
		registres.setPc((msb << 8) | lsb);

	}

	private void bvc(Instruction processing, CPURegisters registres) {
		if (V == 0) {
			registres.setPc(address);
		}

	}

	private void bvs(Instruction processing, CPURegisters registres) {
		if (V == 1) {
			registres.setPc(address);
		}

	}

	private void clc(Instruction processing, CPURegisters registres) {
		registres.setP((byte) (registres.getP() & ~C_MASK));

	}

	private void cld(Instruction processing, CPURegisters registres) {
		registres.setP((byte) (registres.getP() & ~D_MASK));

	}

	private void cli(Instruction processing, CPURegisters registres) {
		registres.setP((byte) (registres.getP() & ~I_MASK));

	}

	private void clv(Instruction processing, CPURegisters registres) {
		registres.setP((byte) (registres.getP() & ~V_MASK));

	}

	private void cmp(Instruction processing, CPURegisters registres) {
		int tmp = (registres.getA() < 0 ? registres.getA() + 256 : registres.getA());
		int tmp2 = (operand2 < 0 ? operand2 + 256 : operand2);
		int res = tmp - tmp2;
		byte P = registres.getP();

		// Zero flag
		if (res == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Carry flag
		if (res >= 0) {
			P |= C_MASK;
		} else {
			P &= ~C_MASK;
		}

		// Negative flag
		if (res < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setP(P);

	}

	private void cpx(Instruction processing, CPURegisters registres) {
		int tmp = (registres.getX() < 0 ? registres.getX() + 256 : registres.getX());
		int tmp2 = (operand2 < 0 ? operand2 + 256 : operand2);
		int res = tmp - tmp2;
		byte P = registres.getP();

		// Zero flag
		if (res == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Carry flag
		if (res >= 0) {
			P |= C_MASK;
		} else {
			P &= ~C_MASK;
		}

		// Negative flag
		if (res < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setP(P);

	}

	private void cpy(Instruction processing, CPURegisters registres) {
		int tmp = (registres.getY() < 0 ? registres.getY() + 256 : registres.getY());
		int tmp2 = (operand2 < 0 ? operand2 + 256 : operand2);
		int res = tmp - tmp2;
		byte P = registres.getP();

		// Zero flag
		if (res == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Carry flag
		if (res >= 0) {
			P |= C_MASK;
		} else {
			P &= ~C_MASK;
		}

		// Negative flag
		if (res < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setP(P);

	}

	private void dec(Instruction processing, CPURegisters registres) throws AddressException {
		byte tmp = (byte) (operand2 - 1);
		byte P = registres.getP();

		// Zero flag
		if (tmp == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (tmp < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setP(P);
		bus.setByteToMemory(address, tmp);

	}

	private void dex(Instruction processing, CPURegisters registres) {
		byte tmp = (byte) (registres.getX() - 1);
		byte P = registres.getP();

		// Zero flag
		if (tmp == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (tmp < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setX(tmp);
		registres.setP(P);

	}

	private void dey(Instruction processing, CPURegisters registres) {
		byte tmp = (byte) (registres.getY() - 1);
		byte P = registres.getP();

		// Zero flag
		if (tmp == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (tmp < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setY(tmp);
		registres.setP(P);

	}

	private void eor(Instruction processing, CPURegisters registres) {
		byte A = registres.getA();
		byte P = registres.getP();

		A ^= operand2;
		// Zero flag
		if (A == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (A < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setA(A);
		registres.setP(P);

	}

	private void inc(Instruction processing, CPURegisters registres) throws AddressException {
		byte tmp = (byte) (operand2 + 1);
		byte P = registres.getP();

		// Zero flag
		if (tmp == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (tmp < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setP(P);
		bus.setByteToMemory(address, tmp);

	}

	private void inx(Instruction processing, CPURegisters registres) {
		byte tmp = (byte) (registres.getX() + 1);
		byte P = registres.getP();

		// Zero flag
		if (tmp == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (tmp < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setX(tmp);
		registres.setP(P);

	}

	private void iny(Instruction processing, CPURegisters registres) {
		byte tmp = (byte) (registres.getY() + 1);
		byte P = registres.getP();

		// Zero flag
		if (tmp == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (tmp < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setY(tmp);
		registres.setP(P);

	}

	private void jmp(Instruction processing, CPURegisters registres) {
		// Attention, jmp sur 3 bits
		registres.setPc(address - 3);
	}

	private void jsr(Instruction processing, CPURegisters registres) throws AddressException {
		// L'adresse de retour est adresse suivante - 1 donc pc + 2 !
		registres.setPc(registres.getPc() + 2);
		byte pcl = (byte) (registres.getPc() & 0x00FF);
		byte pch = (byte) ((registres.getPc() & 0xFF00) >> 8);

		// On push pc dans le stack
		bus.setByteToMemory(registres.getSp(), pch);
		if (registres.getSp() == 0x100)
			registres.setSp(0x1FF);
		else
			registres.setSp(registres.getSp() - 1);

		bus.setByteToMemory(registres.getSp(), pcl);
		if (registres.getSp() == 0x100)
			registres.setSp(0x1FF);
		else
			registres.setSp(registres.getSp() - 1);

//		System.out.println("On va à l'adresse : " + String.format("0x%04x", address));
//		System.out.println("Adresse de retour : " + String.format("0x%02x%02x", pch, pcl));
		// Ensuite, pc prend la valeur pointée -3 (pour compenser le +3 à la fin de
		// l'instruction)
		registres.setPc(address - 3);

	}

	private void lda(Instruction processing, CPURegisters registres) {
		byte A = operand2;
		byte P = registres.getP();

		// Zero flag
		if (A == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (A < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setP(P);
		registres.setA(A);

	}

	private void ldx(Instruction processing, CPURegisters registres) {
		byte X = operand2;
		byte P = registres.getP();

		// Zero flag
		if (X == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (X < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setP(P);
		registres.setX(X);

	}

	private void ldy(Instruction processing, CPURegisters registres) {
		byte Y = operand2;
		byte P = registres.getP();

		// Zero flag
		if (Y == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (Y < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setP(P);
		registres.setY(Y);

	}

	private void lsr(Instruction processing, CPURegisters registres) throws AddressException {
		int tmp = (operand2 < 0 ? operand2 + 256 : operand2) / 2;
		byte P = registres.getP();

		// Zero flag
		if (tmp == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		registres.setP((byte) (P | (operand2 & 0b00000001)));

		switch (processing.getAddressingMode()) {
		case ACCUMULATOR:
			registres.setA((byte) tmp);
			break;

		case ZEROPAGE:
			bus.setByteToMemory(address, (byte) tmp);
			break;

		case ZEROPAGE_X:
			bus.setByteToMemory(address, (byte) tmp);
			break;

		case ABSOLUTE:
			bus.setByteToMemory(address, (byte) tmp);
			break;

		case ABSOLUTE_X:
			bus.setByteToMemory(address, (byte) tmp);
			break;

		default:
			break;
		}

	}

	private void nop(Instruction processing, CPURegisters registres) {

	}

	private void ora(Instruction processing, CPURegisters registres) {
		byte A = registres.getA();
		byte P = registres.getP();

		A |= operand2;
		// Zero flag
		if (A == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (A < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setA(A);
		registres.setP(P);

	}

	private void pha(Instruction processing, CPURegisters registres) throws AddressException {
		bus.setByteToMemory(registres.getSp(), registres.getA());
		if (registres.getSp() == 0x100)
			registres.setSp(0x1FF);
		else
			registres.setSp(registres.getSp() - 1);

	}

	private void php(Instruction processing, CPURegisters registres) throws AddressException {
		bus.setByteToMemory(registres.getSp(), registres.getP());
		if (registres.getSp() == 0x100)
			registres.setSp(0x1FF);
		else
			registres.setSp(registres.getSp() - 1);

	}

	private void pla(Instruction processing, CPURegisters registres) throws AddressException {
		if (registres.getSp() == 0x1FF)
			registres.setSp(0x100);
		else
			registres.setSp(registres.getSp() + 1);
		registres.setA(bus.getByteFromMemory(registres.getSp()));

	}

	private void plp(Instruction processing, CPURegisters registres) throws AddressException {
		if (registres.getSp() == 0x1FF)
			registres.setSp(0x100);
		else
			registres.setSp(registres.getSp() + 1);
		registres.setP(bus.getByteFromMemory(registres.getSp()));

	}

	private void rol(Instruction processing, CPURegisters registres) throws AddressException {
		byte tmp = (byte) ((operand2 << 1) | C);
		byte P = registres.getP();

		// On reset le carry flag
		P &= ~C_MASK;

		// On met le bit 7 en carry
		P |= (operand2 & 0b10000000) >> 7;

		// Zero flag
		if (tmp == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (tmp < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setP(P);

		switch (processing.getAddressingMode()) {
		case ACCUMULATOR:
			registres.setA(tmp);
			break;

		case ZEROPAGE:
			bus.setByteToMemory(address, tmp);
			break;

		case ZEROPAGE_X:
			bus.setByteToMemory(address, tmp);
			break;

		case ABSOLUTE:
			bus.setByteToMemory(address, tmp);
			break;

		case ABSOLUTE_X:
			bus.setByteToMemory(address, tmp);
			break;

		default:
			break;
		}

	}

	private void ror(Instruction processing, CPURegisters registres) throws AddressException {
		byte tmp = (byte) ((operand2 >> 1) | (C << 7));
		byte P = registres.getP();

		// On reset le carry flag
		P &= ~C_MASK;

		// On met le bit 0 en carry
		P |= (operand2 & 0b00000001);

		// Zero flag
		if (tmp == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (tmp < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setP(P);

		switch (processing.getAddressingMode()) {
		case ACCUMULATOR:
			registres.setA(tmp);
			break;

		case ZEROPAGE:
			bus.setByteToMemory(address, tmp);
			break;

		case ZEROPAGE_X:
			bus.setByteToMemory(address, tmp);
			break;

		case ABSOLUTE:
			bus.setByteToMemory(address, tmp);
			break;

		case ABSOLUTE_X:
			bus.setByteToMemory(address, tmp);
			break;

		default:
			break;
		}

	}

	private void rti(Instruction processing, CPURegisters registres) throws AddressException {
		if (registres.getSp() == 0x1FF)
			registres.setSp(0x100);
		else
			registres.setSp(registres.getSp() + 1);
		byte P = bus.getByteFromMemory(registres.getSp());

		if (registres.getSp() == 0x1FF)
			registres.setSp(0x100);
		else
			registres.setSp(registres.getSp() + 1);
		byte pcl = bus.getByteFromMemory(registres.getSp());

		if (registres.getSp() == 0x1FF)
			registres.setSp(0x100);
		else
			registres.setSp(registres.getSp() + 1);
		byte pch = bus.getByteFromMemory(registres.getSp());

		registres.setP(P);

		int lsb = (pcl < 0 ? pcl + 256 : pcl);
		int msb = (pch < 0 ? pch + 256 : pch);
		registres.setPc((msb << 8) | lsb);

	}

	private void rts(Instruction processing, CPURegisters registres) throws AddressException {
		if (registres.getSp() == 0x1FF)
			registres.setSp(0x100);
		else
			registres.setSp(registres.getSp() + 1);
		byte pcl = bus.getByteFromMemory(registres.getSp());

		if (registres.getSp() == 0x1FF)
			registres.setSp(0x100);
		else
			registres.setSp(registres.getSp() + 1);
		byte pch = bus.getByteFromMemory(registres.getSp());

		int lsb = (pcl < 0 ? pcl + 256 : pcl);
		int msb = (pch < 0 ? pch + 256 : pch);
		registres.setPc((msb << 8) | lsb);

	}

	private void sbc(Instruction processing, CPURegisters registres) {
		int tmp = 0;
		byte A = registres.getA();
		byte P = registres.getP();

		tmp = A - operand2 - C;
		A = (byte) tmp;

		// Carry flag
		// Non-signé => carry flag
		// Signé => overflow flag
		if (tmp > 127 || tmp < -128) {
			// Pour l'instant que non signé
			P |= C_MASK;
		} else {
			P &= ~C_MASK;
		}

		// Zero flag
		if (A == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (A < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setA(A);
		registres.setP(P);

	}

	private void sec(Instruction processing, CPURegisters registres) {
		registres.setP((byte) (registres.getP() | C_MASK));

	}

	private void sed(Instruction processing, CPURegisters registres) {
		registres.setP((byte) (registres.getP() | D_MASK));

	}

	private void sei(Instruction processing, CPURegisters registres) {
		registres.setP((byte) (registres.getP() | I_MASK));

	}

	private void sta(Instruction processing, CPURegisters registres) throws AddressException {
		bus.setByteToMemory(address, registres.getA());

	}

	private void stx(Instruction processing, CPURegisters registres) throws AddressException {
		bus.setByteToMemory(address, registres.getX());

	}

	private void sty(Instruction processing, CPURegisters registres) throws AddressException {
		bus.setByteToMemory(address, registres.getY());

	}

	private void tax(Instruction processing, CPURegisters registres) {
		byte X = registres.getA();
		byte P = registres.getP();

		// Zero flag
		if (X == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (X < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setX(X);
		registres.setP(P);

	}

	private void tay(Instruction processing, CPURegisters registres) {
		byte Y = registres.getA();
		byte P = registres.getP();

		// Zero flag
		if (Y == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (Y < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setY(Y);
		registres.setP(P);

	}

	private void tsx(Instruction processing, CPURegisters registres) throws AddressException {
		byte X = (byte) (registres.getSp() & 0xFF);
		byte P = registres.getP();

		// Zero flag
		if (X == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (X < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setX(X);
		registres.setP(P);

	}

	private void txa(Instruction processing, CPURegisters registres) {
		byte A = registres.getX();
		byte P = registres.getP();

		// Zero flag
		if (A == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (A < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setA(A);
		registres.setP(P);

	}

	private void txs(Instruction processing, CPURegisters registres) {
		int x = (registres.getX() < 0 ? registres.getX() + 256 : registres.getX());
		int sp = 0x100 | x;
		byte P = registres.getP();

		registres.setSp(sp);
		registres.setP(P);

	}

	private void tya(Instruction processing, CPURegisters registres) {
		byte A = registres.getY();
		byte P = registres.getP();

		// Zero flag
		if (A == 0) {
			P |= Z_MASK;
		} else {
			P &= ~Z_MASK;
		}

		// Negative flag
		if (A < 0) {
			P |= N_MASK;
		} else {
			P &= ~N_MASK;
		}

		registres.setA(A);
		registres.setP(P);

	}

	// Utilisé par le PPU donc à la fin, mise à la NMI
	private void nmi(CPURegisters registres) throws AddressException {
		byte pcl = (byte) (registres.getPc() & 0x00FF);
		byte pch = (byte) ((registres.getPc() & 0xFF00) >> 8);
		byte P = registres.getP();
		int lsb = (pcl < 0 ? pcl + 256 : pcl);
		int msb = (pch < 0 ? pch + 256 : pch);
//		int oldAddress = (msb << 8) | lsb;
		// On push pc et P dans le stack
		bus.setByteToMemory(registres.getSp(), pch);
		if (registres.getSp() == 0x100)
			registres.setSp(0x1FF);
		else
			registres.setSp(registres.getSp() - 1);

		// On met le low - 1 pour contrebalancer le fait que NMI n'ait pas de byte
		// d'instruction et où on doit exécuter l'instruction qui aurait dû être
		// exécutée
		bus.setByteToMemory(registres.getSp(), (byte) (pcl - 1));
		if (registres.getSp() == 0x100)
			registres.setSp(0x1FF);
		else
			registres.setSp(registres.getSp() - 1);
	
		bus.setByteToMemory(registres.getSp(), P);
		if (registres.getSp() == 0x100)
			registres.setSp(0x1FF);
		else
			registres.setSp(registres.getSp() - 1);

		// On met à jour le pc
		pcl = bus.getByteFromMemory(0xFFFA);
		pch = bus.getByteFromMemory(0xFFFB);
		lsb = (pcl < 0 ? pcl + 256 : pcl);
		msb = (pch < 0 ? pch + 256 : pch);
		int address = (msb << 8) | lsb;
		registres.setPc(address);

//		System.out.println("On va à l'adresse : " + String.format("0x%04x", address));
//		System.out.println("Adresse de retour : " + String.format("0x%04x", oldAddress + 1));
	}

	private boolean isNewPage(int pc) {
		// Adresse sur 2 octets, si les octets de poids fort sont égaux alors sur la
		// même page
		return address >> 8 != pc >> 8;
	}
}
