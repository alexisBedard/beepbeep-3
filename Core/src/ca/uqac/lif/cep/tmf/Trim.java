/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2016 Sylvain Hallé

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
package ca.uqac.lif.cep.tmf;

import ca.uqac.lif.cep.SMVInterface;
import ca.uqac.lif.cep.SynchronousProcessor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Queue;

/**
 * Discards the first <i>n</i> events of the input, and outputs the remaining ones.
 * 
 * @author Sylvain Hallé
 * @since 0.2.1
 */
@SuppressWarnings("squid:S2160")
public class Trim extends SynchronousProcessor implements SMVInterface
{
  /**
   * How many events to ignore at the beginning of the trace
   */
  protected final int m_delay;
  
  /**
   * No-args constructor. Useful only for deserialization.
   */
  private Trim()
  {
    super(1, 1);
    m_delay = 0;
  }

  /**
   * Creates a new delay processor.
   * 
   * @param delay
   *          The number of events from the input trace to discard
   */
  public Trim(int delay)
  {
    super(1, 1);
    m_delay = delay;
  }

  @Override
  protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
  {
    if (m_inputCount >= getDelay())
    {
      outputs.add(inputs);
      if (m_eventTracker != null)
      {
        for (int i = 0; i < inputs.length; i++)
        {
          m_eventTracker.associateToInput(getId(), i, m_inputCount, i, m_outputCount);
        }
      }
      m_outputCount++;
    }
    m_inputCount++;
    return true;
  }

  @Override
  public Trim duplicate(boolean with_state)
  {
    Trim t = new Trim(getDelay());
    if (with_state)
    {
      t.m_inputCount = m_inputCount;
      t.m_outputCount = m_outputCount;
    }
    return t;
  }

  /**
   * Gets the delay associated to the trim processor
   * @return The delay
   */
  public int getDelay()
  {
    return m_delay;
  }
  
  /**
   * @since 0.10.2
   */
  @Override
  protected Object printState()
  {
    return m_delay;
  }
  
  /**
   * @since 0.10.2
   */
  @Override
  protected Trim readState(Object o)
  {
    int delay = ((Number) o).intValue();
    return new Trim(delay);
  }

@Override
public void writingSMV(PrintStream printStream, int Id, int list, int[][] array, int arrayWidth, int maxInputArity,
		String pipeType) throws IOException {
	printStream.printf("MODULE Trim"+Id+"(inc_1, inb_1, ouc_1, oub_1) \n");
	printStream.printf("	VAR \n");
	printStream.printf("		cnt : 0.."+(m_delay - 1)+"; \n");
	printStream.printf("		done : boolean; \n");
	printStream.printf("\n");
	printStream.printf("	ASSIGN \n");
	printStream.printf("		init(cnt) := 0; \n");
	printStream.printf("		init(done) := FALSE; \n");
	printStream.printf("		init(ouc_1) := "+array[Id][0]+"; \n");
	printStream.printf("		init(oub_1) := FALSE;\n");
	printStream.printf("\n");
	printStream.printf("		next(cnt) := (cnt + 1) mod "+(m_delay - 1)+"; \n");
	printStream.printf("		next(done) := case \n");
	printStream.printf("			next(cnt) = 0 : TRUE;\n");
	printStream.printf("			done & next(inb_1) : TRUE; \n");
	printStream.printf("			TRUE : FALSE; \n");
	printStream.printf("		esac; \n");
	printStream.printf("\n");
	printStream.printf("		next(ouc_1) := case \n");
	printStream.printf("			done & next(inb_1) : next(inc_1); \n");
	printStream.printf("		TRUE : "+array[Id][0]+"; \n");
	printStream.printf("		esac; \n");
	printStream.printf("\n");
	printStream.printf("		next(oub_1) := case \n");
	printStream.printf("			done & next(inb_1) : next(inb_1); \n");
	printStream.printf("		TRUE : FALSE; \n");
	printStream.printf("		esac; \n");
	printStream.printf("\n");
	
}

@Override
public void writePipes(PrintStream printStream, int ProcId, int[][] connectionArray) throws IOException {
	printStream.printf("		--Trim \n");
	printStream.printf("		pipe_"+ProcId+" : "+ Integer.toString(connectionArray[ProcId][0]) + ".." + Integer.toString(connectionArray[ProcId][1])+ ";\n");
	printStream.printf("		b_pipe_"+ProcId+ " : boolean; \n");
	
}
}
