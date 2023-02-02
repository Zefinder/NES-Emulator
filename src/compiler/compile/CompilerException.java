package compile;

import instructions.Instruction.AddressingMode;
import instructions.Instruction.InstructionSet;

public class CompilerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1799907175468621016L;

	public CompilerException(String message) {
		super(message);
	}

	public static void callAddressingException(InstructionSet instruction, AddressingMode addressingMode,
			int lineNumber) throws CompilerException {
		throw new CompilerException(
				String.format("[ERROR]: Adressing mode %s do not exist for instruction %s (line %d)\n",
						addressingMode.toString(), instruction.toString(), lineNumber));
	}
	
	public static void callNumberException(int lineNumber, String line, int offset) throws CompilerException {
		throw new CompilerException(String.format(
				"[ERROR]: Number argument must be a valid number! (line %d)\n%s\n%s\n",
				lineNumber, line, padLeftSpaces("^", 4)));
	}
	
	private static String padLeftSpaces(String inputString, int length) {
		StringBuilder sb = new StringBuilder();
		while (sb.length() < length) {
			sb.append(' ');
		}
		sb.append(inputString);

		return sb.toString();
	}

}
