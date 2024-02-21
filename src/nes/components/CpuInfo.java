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

	/* DMA */
	public boolean dmaRequested;
	public int dmaHaltCycles;
	public int dmaState; // Put = 1

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

		this.dmaRequested = false;
		this.dmaHaltCycles = 0;
		this.dmaState = 0;
	}

	public int getP() {
		return N << 7 | V << 6 | B << 4 | D << 3 | I << 2 | Z << 1 | C;
	}

	public void setP(int P) {
		C = P & 0b1;
		Z = (P >> 1) & 0b1;
		I = (P >> 2) & 0b1;
		D = (P >> 3) & 0b1;
		B = (P >> 4) & 0b11;
		V = (P >> 6) & 0b1;
		N = (P >> 7) & 0b1;
	}

}
