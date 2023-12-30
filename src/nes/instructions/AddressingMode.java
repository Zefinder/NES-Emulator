package instructions;

/**
 * Enumeration of possible addressing modes of instructions. There are 13
 * different addressing modes and an instruction can have multiple addressing
 * modes.
 */
public enum AddressingMode {

	/**
	 * <p>
	 * Source and destination are implied and directly accessible from the CPU (eg.
	 * CLC, SEI). They do not need any additional information.
	 * </p>
	 */
	IMPLICIT,

	/**
	 * <p>
	 * When the instruction needs to use only one input, it can directly be the
	 * accumulator (A register). For example:
	 * </p>
	 * 
	 * <pre>
	 * LSR A ; Logical Shift Right, A <- 2A
	 * </pre>
	 * 
	 * <p>
	 * Only used by some ALU instructions.
	 * </p>
	 */
	ACCUMULATOR,

	/**
	 * <p>
	 * When the instruction needs to use two inputs, the first input is the
	 * accumulator (A register) and the second can be specified as a constant within
	 * the instruction. For example:
	 * </p>
	 * 
	 * <pre>
	 * ADC #$50 ; A <- A + 0x50
	 * LDX #40  ; X <- 40
	 * </pre>
	 * 
	 * <p>
	 * Only used by some ALU instructions and load instructions
	 * </p>
	 * 
	 * <p>
	 * This instruction differs from {@link #ZEROPAGE} thanks to the # at the start
	 * of the constant.
	 * </p>
	 */
	IMMEDIATE,

	/**
	 * <p>
	 * When the instruction needs to use two inputs, the first input is the
	 * accumulator (A register) and the second can be specified as some value in the
	 * first page of the memory. This value address is stored within the
	 * instruction. For example:
	 * </p>
	 * 
	 * <pre>
	 * ADC $50 ; A <- A + *(0x50)
	 * </pre>
	 * 
	 * <p>
	 * This instruction is used for optimization since it is one cycle faster and
	 * one byte shorter than the {@link #ABSOLUTE} addressing mode.
	 * </p>
	 * 
	 * <p>
	 * This instruction differs from {@link #IMMEDIATE} because it doesn't start
	 * with a $.
	 * </p>
	 */
	ZEROPAGE,

	/**
	 * <p>
	 * When the instruction needs to use two inputs, the first input is the
	 * accumulator (A register) and the second can be specified as some value in the
	 * first page of the memory with a certain offset. This value address is stored
	 * inside the instruction and the offset is the X register. For example:
	 * </p>
	 * 
	 * <pre>
	 * LDY $50,X ; Y <- Y + *(0x50 + X)
	 * </pre>
	 * 
	 * <p>
	 * Note that the address calculation wraps around if the sum exceeds 0xFF
	 * (modulo 0x100). The exact notation of the sum is (IMM + X) & 0xFF. Thus, 0x80
	 * + 0xFF will give 0x7F and not 0x17F.
	 * </p>
	 */
	ZEROPAGE_X,

	/**
	 * <p>
	 * This addressing mode is exactly the same as {@link #ZEROPAGE_X} but with the
	 * Y register. Only used by LDX and STX.
	 * </p>
	 */
	ZEROPAGE_Y,

	/**
	 * <p>
	 * Addressing mode used by branch instructions to jump in case of the condition
	 * is evaluated as true. As a branch instruction is always 2 bytes, the jump has
	 * a +2 offset. The constant value within the branch instruction is a signed
	 * value making a branch instruction branching with an offset in [-126;+129]
	 * </p>
	 */
	RELATIVE,

	/**
	 * <p>
	 * When an instruction needs an address or a value pointing to a certain
	 * address, this addressing mode is used. For example:
	 * </p>
	 * 
	 * <pre>
	 * JMP $0123 ; PC <- 0x0123
	 * ROL $DEAD ; *(0xDEAD) <- *(0xDEAD) <<< 1
	 * </pre>
	 */
	ABSOLUTE,

	/**
	 * <p>
	 * When an instruction needs an address or a value pointing to a certain address
	 * adding a certain offset, this addressing mode is used. The offset used is the
	 * value of the X register. For example:
	 * </p>
	 * 
	 * <pre>
	 * AND $1001,X ; A <- A & *(0x1001 + X)
	 * </pre>
	 */
	ABSOLUTE_X,

	/**
	 * <p>
	 * This addressing mode is exactly the same as {@link #ABSOLUTE_X} but with the
	 * Y register.
	 * </p>
	 */
	ABSOLUTE_Y,

	/**
	 * <p>
	 * This instruction takes the address pointed by the address stored within the
	 * instruction. For example:
	 * </p>
	 * 
	 * <pre>
	 * LDA #$1A    ; A <- 0x1A
	 * STA $2000   ; *(0x2000) <- A
	 * LDA #$8C    ; A <- 0x8C
	 * STA $2001   ; *(0x2001) <- A
	 * JMP ($2000) ; Jumps at the address at 0x2000, so 0x8C1A
	 * </pre>
	 * 
	 * <p>
	 * Remember that addresses are stored in the <em>LITTLE ENDIAN</em> mode! So the
	 * byte pointed is the least significant byte.
	 * </p>
	 * 
	 * <p>
	 * The only instruction that supports pure indirection is JMP (basic jump).
	 * </p>
	 */
	INDIRECT,

	/**
	 * <p>
	 * This addressing mode is similar to {@link #ZEROPAGE_X} but instead of taking
	 * the value pointed at the zero page address, it will take the value pointed by
	 * the address pointed by the value stored within the instruction plus some
	 * offset (the value of the X register). For example:
	 * </p>
	 * 
	 * <pre>
	 * LDA #$1A    ; A <- 0x1A
	 * STA $4F69   ; *(0x4F69) <- A
	 * 
	 * LDA #$69    ; A <- 0x69
	 * STA $2A     ; *(0x2A) <- A
	 * 
	 * LDA #$4F    ; A <- 0x4F
	 * STA $2B     ; *(0x2B) <- A
	 * 
	 * LDX #$0A    ; X <- 0x0A
	 * ORA ($20,X) ; A <- A | *(*(0x20 + X)) (equivalent to *(0x4F69), so 0x1A)
	 * </pre>
	 * 
	 * <p>
	 * This is better to use a table of addresses with this addressing mode. It is
	 * much easier to play with the X register than changing the indirection address
	 * each time.
	 * </p>
	 * 
	 * <p>
	 * As for {@link #ZEROPAGE_X}, the address calculation wraps around if the sum
	 * exceeds 0xFF (modulo 0x100).
	 * </p>
	 * 
	 * <p>
	 * This addressing mode is also called Indexed Indirect.
	 * </p>
	 */
	INDIRECT_X,

	/**
	 * <p>
	 * This addressing mode is one of the most popular indirect addressing mode of
	 * this CPU. It is almost similar to {@link #INDIRECT_X} but instead of applying
	 * the offset to the zero page address, the offset is applied at the final
	 * address. The offset is stored in the Y register. For example:
	 * </p>
	 * 
	 * <pre>
	 * LDA #$1A    ; A <- 0x1A
	 * STA $4F69   ; *(0x4F69) <- A
	 * 
	 * LDA #$00    ; A <- 0x00
	 * STA $2A     ; *(0x2A) <- A
	 * 
	 * LDA #$4F    ; A <- 0x4F
	 * STA $2B     ; *(0x2B) <- A
	 * 
	 * LDY #$69    ; X <- 0x0A
	 * ADC ($2A),Y ; A <- A + *(*(0x2A) + Y) (equivalent to *(0x4F00 + 0x69), so 0x1A)
	 * </pre>
	 * 
	 * <p>
	 * This addressing mode is also called Indirect Indexed.
	 * </p>
	 */
	INDIRECT_Y;
}