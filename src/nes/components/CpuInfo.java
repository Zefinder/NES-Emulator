package components;

public class CpuInfo {
	
	// TODO Remove because useless
	public static final int C_MASK = 0b00000001;
	public static final int Z_MASK = 0b00000010;
	public static final int I_MASK = 0b00000100;
	public static final int D_MASK = 0b00001000;
	public static final int B_MASK = 0b00110000;
	public static final int V_MASK = 0b01000000;
	public static final int N_MASK = 0b10000000;
	
	/* Registers */
	public int A;
	public int X;
	public int Y;
	public int SP;
	public int PC;
	
	/* Flags */
	public int C;
	public int Z;
	public int I;
	public int D;
	public int B;
	public int V;
	public int N;
	
	public CpuInfo() {
		this.A = 0;
		this.X = 0;
		this.Y = 0;
		this.SP = 0;
		this.PC = 0;
		
		this.C = 0;
		this.Z = 0;
		this.I = 0;
		this.D = 0;
		this.B = 0;
		this.V = 0;
		this.N = 0;
	}
	
}
