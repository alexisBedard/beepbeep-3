package ca.uqac.lif.cep;

import java.io.IOException;
import java.io.PrintStream;

public interface SMVInterface {
	public void writingSMV(PrintStream printStream, int Id, int list, int[][] array, int arrayWidth, int maxInputArity) throws IOException;
}
