package instructions.jump;

import exceptions.InstructionNotSupportedException;
import instructions.AddressingMode;
import instructions.Instruction;

public class RTIInstruction extends RTSInstruction {

	public RTIInstruction(AddressingMode mode) {
		super(mode);
	}

	public RTIInstruction(AddressingMode mode, int constant) {
		super(mode, constant);
	}
	
	@Override
	public void execute() throws InstructionNotSupportedException {
		// We pop flags
		cpu.cpuInfo.setP(cpu.pop());
		
		// Then basic RTS
		super.execute();
	}
	
	@Override
	public Instruction newInstruction(int constant) {
		return new RTIInstruction(getMode(), constant);
	}

}	
