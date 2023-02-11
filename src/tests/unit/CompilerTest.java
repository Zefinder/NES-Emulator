package unit;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import compile.Compiler;
import compile.CompilerException;

class CompilerTest {

	private static final File TMP_DIR = new File("./tmp");
	private static final File TMP_FILE = new File("./tmp/tmp.nesasm");
	private static final File TMP_BINARY = new File("./tmp/tmp.nes");
	private static final File ERROR_CONSTANT_FILE = new File("./src/tests/unit/res/errorConstant.nesasm");
	private static final File ERROR_LABEL_FILE = new File("./src/tests/unit/res/errorLabel.nesasm");
	private static final File ERROR_DIRECTIVES_FILE = new File("./src/tests/unit/res/errorDirective.nesasm");
	private static final File ERROR_IMPLICIT_FILE = new File("./src/tests/unit/res/errorImplicit.nesasm");
	private static final File ERROR_ACC_FILE = new File("./src/tests/unit/res/errorAccumulator.nesasm");
	private static final File ERROR_IMM_FILE = new File("./src/tests/unit/res/errorImmediate.nesasm");
	private static final File ERROR_ZP_FILE = new File("./src/tests/unit/res/errorZeroPage.nesasm");
	private static final File ERROR_ZPX_FILE = new File("./src/tests/unit/res/errorZeroPageX.nesasm");
	private static final File ERROR_ZPY_FILE = new File("./src/tests/unit/res/errorZeroPageY.nesasm");
	private static final File ERROR_ABS_FILE = new File("./src/tests/unit/res/errorAbsolute.nesasm");
	private static final File ERROR_ABSX_FILE = new File("./src/tests/unit/res/errorAbsoluteX.nesasm");
	private static final File ERROR_ABSY_FILE = new File("./src/tests/unit/res/errorAbsoluteY.nesasm");
	private static final File ERROR_REL_FILE = new File("./src/tests/unit/res/errorRelative.nesasm");
	private static final File ERROR_INDIRECT_FILE = new File("./src/tests/unit/res/errorIndirect.nesasm");
	private static final File ERROR_INDIRECTX_FILE = new File("./src/tests/unit/res/errorIndirectX.nesasm");
	private static final File ERROR_INDIRECTY_FILE = new File("./src/tests/unit/res/errorIndirectY.nesasm");

	@BeforeAll
	public static void init() throws IOException {
		if (!TMP_DIR.exists())
			TMP_DIR.mkdir();

		if (TMP_FILE.exists())
			TMP_FILE.delete();

		TMP_FILE.createNewFile();
	}

	@ParameterizedTest
	@MethodSource("errorConstantInstructionsProvider")
	public void errorConstantTest(String constant) throws IOException, CompilerException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(TMP_FILE));
		writer.write(constant);
		writer.close();

		Compiler c = new Compiler(TMP_FILE);
		System.out.println(constant);
		assertThrows(CompilerException.class, () -> c.compile(TMP_BINARY));
	}

	@ParameterizedTest
	@MethodSource("errorLabelInstructionsProvider")
	public void errorLabelTest(String constant) throws IOException, CompilerException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(TMP_FILE));
		writer.write(constant);
		writer.close();

		Compiler c = new Compiler(TMP_FILE);
		System.out.println(constant);
		assertThrows(CompilerException.class, () -> c.compile(TMP_BINARY));
	}

	@ParameterizedTest
	@MethodSource("errorDirectivesInstructionsProvider")
	public void errorDirectivesTest(String constant) throws IOException, CompilerException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(TMP_FILE));
		writer.write(constant);
		writer.close();

		Compiler c = new Compiler(TMP_FILE);
		System.out.println(constant);
		assertThrows(CompilerException.class, () -> c.compile(TMP_BINARY));
	}

	@ParameterizedTest
	@MethodSource("errorImplicitInstructionsProvider")
	public void errorImplicitTest(String constant) throws IOException, CompilerException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(TMP_FILE));
		writer.write(constant);
		writer.close();

		Compiler c = new Compiler(TMP_FILE);
		System.out.println(constant);
		assertThrows(CompilerException.class, () -> c.compile(TMP_BINARY));
	}

	@ParameterizedTest
	@MethodSource("errorAccumulatorInstructionsProvider")
	public void errorAccumulatorTest(String constant) throws IOException, CompilerException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(TMP_FILE));
		writer.write(constant);
		writer.close();

		Compiler c = new Compiler(TMP_FILE);
		System.out.println(constant);
		assertThrows(CompilerException.class, () -> c.compile(TMP_BINARY));
	}

	@ParameterizedTest
	@MethodSource("errorImmediateInstructionsProvider")
	public void errorImmediateTest(String constant) throws IOException, CompilerException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(TMP_FILE));
		writer.write(constant);
		writer.close();

		Compiler c = new Compiler(TMP_FILE);
		System.out.println(constant);
		assertThrows(CompilerException.class, () -> c.compile(TMP_BINARY));
	}

	@ParameterizedTest
	@MethodSource("errorZeroPageInstructionsProvider")
	public void errorZeroPageTest(String constant) throws IOException, CompilerException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(TMP_FILE));
		writer.write(constant);
		writer.close();

		Compiler c = new Compiler(TMP_FILE);
		System.out.println(constant);
		assertThrows(CompilerException.class, () -> c.compile(TMP_BINARY));
	}

	@ParameterizedTest
	@MethodSource("errorZeroPageXInstructionsProvider")
	public void errorZeroPageXTest(String constant) throws IOException, CompilerException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(TMP_FILE));
		writer.write(constant);
		writer.close();

		Compiler c = new Compiler(TMP_FILE);
		System.out.println(constant);
		assertThrows(CompilerException.class, () -> c.compile(TMP_BINARY));
	}

	@ParameterizedTest
	@MethodSource("errorZeroPageYInstructionsProvider")
	public void errorZeroPageYTest(String constant) throws IOException, CompilerException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(TMP_FILE));
		writer.write(constant);
		writer.close();

		Compiler c = new Compiler(TMP_FILE);
		System.out.println(constant);
		assertThrows(CompilerException.class, () -> c.compile(TMP_BINARY));
	}

	@ParameterizedTest
	@MethodSource("errorAbsoluteInstructionsProvider")
	public void errorAbsoluteTest(String constant) throws IOException, CompilerException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(TMP_FILE));
		writer.write(constant);
		writer.close();

		Compiler c = new Compiler(TMP_FILE);
		System.out.println(constant);
		assertThrows(CompilerException.class, () -> c.compile(TMP_BINARY));
	}

	@ParameterizedTest
	@MethodSource("errorAbsoluteXInstructionsProvider")
	public void errorAbsoluteXTest(String constant) throws IOException, CompilerException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(TMP_FILE));
		writer.write(constant);
		writer.close();

		Compiler c = new Compiler(TMP_FILE);
		System.out.println(constant);
		assertThrows(CompilerException.class, () -> c.compile(TMP_BINARY));
	}

	@ParameterizedTest
	@MethodSource("errorAbsoluteYInstructionsProvider")
	public void errorAbsoluteYTest(String constant) throws IOException, CompilerException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(TMP_FILE));
		writer.write(constant);
		writer.close();

		Compiler c = new Compiler(TMP_FILE);
		System.out.println(constant);
		assertThrows(CompilerException.class, () -> c.compile(TMP_BINARY));
	}

	@ParameterizedTest
	@MethodSource("errorRelativeInstructionsProvider")
	public void errorRelativeTest(String constant) throws IOException, CompilerException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(TMP_FILE));
		writer.write(constant);
		writer.close();

		Compiler c = new Compiler(TMP_FILE);
		System.out.println(constant);
		assertThrows(CompilerException.class, () -> c.compile(TMP_BINARY));
	}

	@ParameterizedTest
	@MethodSource("errorIndirectInstructionsProvider")
	public void errorIndirectTest(String constant) throws IOException, CompilerException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(TMP_FILE));
		writer.write(constant);
		writer.close();

		Compiler c = new Compiler(TMP_FILE);
		System.out.println(constant);
		assertThrows(CompilerException.class, () -> c.compile(TMP_BINARY));
	}

	@ParameterizedTest
	@MethodSource("errorIndirectXInstructionsProvider")
	public void errorIndirectXTest(String constant) throws IOException, CompilerException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(TMP_FILE));
		writer.write(constant);
		writer.close();

		Compiler c = new Compiler(TMP_FILE);
		System.out.println(constant);
		assertThrows(CompilerException.class, () -> c.compile(TMP_BINARY));
	}

	@ParameterizedTest
	@MethodSource("errorIndirectYInstructionsProvider")
	public void errorIndirectYTest(String constant) throws IOException, CompilerException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(TMP_FILE));
		writer.write(constant);
		writer.close();

		Compiler c = new Compiler(TMP_FILE);
		System.out.println(constant);
		assertThrows(CompilerException.class, () -> c.compile(TMP_BINARY));
	}
	
	@ParameterizedTest
	@MethodSource("errorDuplicatedNamesProvider")
	public void errorDuplicatedNamesTest(String constant) throws IOException, CompilerException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(TMP_FILE));
		writer.write(constant);
		writer.close();

		Compiler c = new Compiler(TMP_FILE);
		System.out.println(constant);
		assertThrows(CompilerException.class, () -> c.compile(TMP_BINARY));
	}

	@SuppressWarnings("resource")
	private static Stream<String> errorConstantInstructionsProvider() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(ERROR_CONSTANT_FILE));
		Stream<String> stream = reader.lines();

		return stream;
	}

	@SuppressWarnings("resource")
	private static Stream<String> errorLabelInstructionsProvider() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(ERROR_LABEL_FILE));
		Stream<String> stream = reader.lines();

		return stream;
	}

	@SuppressWarnings("resource")
	private static Stream<String> errorDirectivesInstructionsProvider() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(ERROR_DIRECTIVES_FILE));
		Stream<String> stream = reader.lines();

		return stream;
	}

	@SuppressWarnings("resource")
	private static Stream<String> errorImplicitInstructionsProvider() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(ERROR_IMPLICIT_FILE));
		Stream<String> stream = reader.lines();

		return stream;
	}

	@SuppressWarnings("resource")
	private static Stream<String> errorAccumulatorInstructionsProvider() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(ERROR_ACC_FILE));
		Stream<String> stream = reader.lines();

		return stream;
	}

	@SuppressWarnings("resource")
	private static Stream<String> errorImmediateInstructionsProvider() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(ERROR_IMM_FILE));
		Stream<String> stream = reader.lines();

		return stream;
	}

	@SuppressWarnings("resource")
	private static Stream<String> errorZeroPageInstructionsProvider() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(ERROR_ZP_FILE));
		Stream<String> stream = reader.lines();

		return stream;
	}

	@SuppressWarnings("resource")
	private static Stream<String> errorZeroPageXInstructionsProvider() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(ERROR_ZPX_FILE));
		Stream<String> stream = reader.lines();

		return stream;
	}

	@SuppressWarnings("resource")
	private static Stream<String> errorZeroPageYInstructionsProvider() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(ERROR_ZPY_FILE));
		Stream<String> stream = reader.lines();

		return stream;
	}

	@SuppressWarnings("resource")
	private static Stream<String> errorAbsoluteInstructionsProvider() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(ERROR_ABS_FILE));
		Stream<String> stream = reader.lines();

		return stream;
	}

	@SuppressWarnings("resource")
	private static Stream<String> errorAbsoluteXInstructionsProvider() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(ERROR_ABSX_FILE));
		Stream<String> stream = reader.lines();

		return stream;
	}

	@SuppressWarnings("resource")
	private static Stream<String> errorAbsoluteYInstructionsProvider() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(ERROR_ABSY_FILE));
		Stream<String> stream = reader.lines();

		return stream;
	}

	@SuppressWarnings("resource")
	private static Stream<String> errorRelativeInstructionsProvider() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(ERROR_REL_FILE));
		Stream<String> stream = reader.lines();

		return stream;
	}

	@SuppressWarnings("resource")
	private static Stream<String> errorIndirectInstructionsProvider() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(ERROR_INDIRECT_FILE));
		Stream<String> stream = reader.lines();

		return stream;
	}

	@SuppressWarnings("resource")
	private static Stream<String> errorIndirectXInstructionsProvider() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(ERROR_INDIRECTX_FILE));
		Stream<String> stream = reader.lines();

		return stream;
	}

	@SuppressWarnings("resource")
	private static Stream<String> errorIndirectYInstructionsProvider() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(ERROR_INDIRECTY_FILE));
		Stream<String> stream = reader.lines();

		return stream;
	}

	private static Stream<String> errorDuplicatedNamesProvider() {
		return Stream.of("Test1 = 2\nTest1 = 3", "Test1 = 2\n.eq Test1 3", ".eq Test1 2\nTest1 = 3",
				".eq Test1 2\n.eq Test1 3");
	}
	
	@AfterAll
	public static void clean() throws IOException {
		TMP_FILE.delete();
		TMP_BINARY.delete();
		TMP_DIR.delete();
	}

}
