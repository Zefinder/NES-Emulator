package components;

public class CpuInfo {

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

	public int getP() {
		return N << 7 | V << 6 | B << 5 | D << 3 | I << 2 | Z << 1 | C;
	}

}
