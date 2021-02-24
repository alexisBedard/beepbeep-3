package ca.uqac.lif.cep;

import java.io.FileWriter;
import java.io.IOException;

public interface SMVInterface {
	public void writingSMV(FileWriter file, int Id, int list) throws IOException;
}
