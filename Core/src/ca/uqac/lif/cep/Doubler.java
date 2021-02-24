/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2019 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.cep;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;

import ca.uqac.lif.cep.util.Strings;

/**
 * A processor that doubles every number it is given.
 * <p>
 * This processor was once
 * part of the <a href="https://liflab.github.io/beepbeep-3-examples">examples
 * repository</a>, and was used in a few code examples at the beginning of the
 * <a href="https://liflab.gitbook.io/event-stream-processing-with-beepbeep-3">book</a>
 * (before the {@link ca.uqac.lif.cep.functions.ApplyFunction ApplyFunction}
 * processor was introduced). However, people
 * who were simply copy-pasting the code snippets without cloning the whole
 * examples repository would not find the {@link Adder} processor and could not
 * compile the example. It was hence
 * <a href="https://github.com/liflab/beepbeep-3/issues/41">decided</a> to
 * repatriate this processor into the core library to make the whole thing
 * smoother for beginners.
 * <p>
 * Apart from the code examples, we do not recommend that you use this processor.
 * The "clean" way to multiply numbers is to use
 * {@link ca.uqac.lif.cep.functions.ApplyFunction ApplyFunction} with
 * {@link ca.uqac.lif.cep.util.Numbers#multiplication Numbers.multiplication}.
 * 
 * @author Sylvain Hallé
 * @since 0.10.1
 */
public class Doubler extends SynchronousProcessor implements SMVInterface
{
  /**
   * Creates a new adder processor. Since this processor is stateless, it would
   * make more sense to make it a singleton 
   * {@link ca.uqac.lif.cep.functions.Function Function}, and to
   * provide a static reference to a single instance of the class. However,
   * remember that the purpose of this processor is to be used in one of the
   * very first examples of the user manual, where it is too early to talk
   * about such complications. So, leave it like this!
   */
  public Doubler()
  {
    super(1, 1);
  }

  @Override
  protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
  {
    outputs.add(new Object[] {2 * ((Number) inputs[0]).intValue()});
    return true;
  }

  @Override
  public Processor duplicate(boolean with_state)
  {
    return new Doubler();
  }
  
  /**
   * @since 0.10.2
   */
  public Doubler readState(Object o)
  {
    return new Doubler();
  }
  

@Override
public void writingSMV(FileWriter file, int Id, int list) throws IOException {
	file.write("MODULE Doubler(inc_1, inb_1, ouc_1, oub_1) \n");
	file.write("	ASSIGN \n");
	file.write("		init(ouc_1) := case \n");
	file.write("		inb_1 : inc_1 * 2; \n");
	file.write("		TRUE : 0; \n");
	file.write("	esac; \n");
	file.write("\n");
	file.write("	init(oub_1) := inb_1; \n");
	file.write("\n");
	file.write("	next(oub_1) := case \n");
	file.write("		next(inb_1) : next(inb_1); \n");
	file.write("		TRUE : FALSE; \n");
	file.write("	esac; \n");
	file.write("\n");
	file.write("	next(ouc_1) := case \n");
	file.write("		next(inb_1) : next(inc_1) * 2; \n");
	file.write("		TRUE : 0; \n");
	file.write("	esac; \n");
	file.write("\n");
	//printStream = new printStream(fileWriter);	
	}
}