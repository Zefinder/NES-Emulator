package frame;

import instructions.Instruction;
import instructions.branch.BranchInstruction;
import instructions.jump.JMPInstruction;
import instructions.jump.JSRInstruction;

public class InstructionDialog extends CpuInfoDialog {

	private static final String TITLE = "Instructions";
	private static final int ELEMENT_NUMBER = 3;

	private final Instruction[] romInstructions;

	private String instructionPrevious = "";
	private String instructionReady = "";
	private String instructionNext = "";

	private int oldPC = 0;
	private boolean hasBranched = false;
	private boolean hasJumped = false;

	/**
	 * 
	 */
	private static final long serialVersionUID = -4913622929738924125L;

	public InstructionDialog(Instruction[] romInstructions) {
		super(TITLE, ELEMENT_NUMBER);
		this.romInstructions = romInstructions;
		this.setLocation(328, 512);
	}

	@Override
	protected void registerElements() {
		addElement("Previous");
		addElement("Ready");
		addElement("Next");
	}

	@Override
	protected void update() {
		// If PC didn't change, then why update?
		if (oldPC == cpuInfo.PC) {
			return;
		}

		// Updating the old PC
		oldPC = cpuInfo.PC;

		// We step forward!
		if (hasBranched) {
			instructionPrevious = "Branched?";
			hasBranched = false;
		} else if (hasJumped) {
			instructionPrevious = "Jumped!";
			hasJumped = false;
		} else {
			instructionPrevious = instructionReady;
		}

		// Fetching the instruction to execute (if not in ROM, just ignore)
		if (cpuInfo.PC < 0x8000) {
			instructionReady = "Not in ROM";
			instructionNext = "";
		} else {
			Instruction instruction = romInstructions[cpuInfo.PC - 0x8000];
			// If the instruction is null, give up
			if (instruction == null) {
				instructionReady = "Unreadable";
				instructionNext = "";
			} else {
				instructionReady = instruction.toString();

				// If this is a branching instruction, do not tell the next one
				if (instruction instanceof BranchInstruction) {
					instructionNext = "Branching?";
					hasBranched = true;
				}
				// If this is a jump instruction, same thing
				else if (instruction instanceof JMPInstruction || instruction instanceof JSRInstruction) {
					instructionNext = "Jumping!";
					hasJumped = true;
				}
				// Else just tell the next one
				else {
					int byteNumber = instruction.getByteNumber();
					int nextPC = (cpuInfo.PC + byteNumber) & 0xFFFF;

					// If not in ROM then give up
					if (nextPC < 0x8000) {
						instructionNext = "Not in ROM";
					} else {
						Instruction nextInstruction = romInstructions[nextPC - 0x8000];
						// Next instruction should always be readable so it's ok
						instructionNext = nextInstruction.toString();
					}
				}
			}
		}

		// Instruction have a max size of 11
		setElementValue(0, "%s".formatted(instructionPrevious));
		setElementValue(1, "%s".formatted(instructionReady));
		setElementValue(2, "%s".formatted(instructionNext));
	}

}
