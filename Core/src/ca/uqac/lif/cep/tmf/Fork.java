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

import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.SMVInterface;
import ca.uqac.lif.cep.UniformProcessor;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Duplicates an input trace into two or more output traces.
 * It is represented graphically as:
 * <p>
 * <img src="{@docRoot}/doc-files/tmf/Fork.png" alt="Fork">
 * 
 * @author Sylvain Hallé
 * @since 0.1
 */
@SuppressWarnings("squid:S2160")
public class Fork extends UniformProcessor implements SMVInterface
{

  private String pipeType = "";
	
  public Fork(int out_arity)
  {
    super(1, out_arity);
  }
  
  public Fork()
  {
    this(2);
  }

  @Override
  public Fork duplicate(boolean with_state)
  {
    return new Fork(getOutputArity());
  }

  @Override
  protected boolean compute(Object[] inputs, Object[] outputs)
  {
    int arity = getOutputArity();
    for (int i = 0; i < arity; i++)
    {
      outputs[i] = inputs[0];
    }
    return true;
  }

  /**
   * Creates a copy of the current fork with a greater arity
   * 
   * @param out_arity
   *          The desired arity for the output fork
   */
  @SuppressWarnings("unchecked")
  public void extendOutputArity(int out_arity)
  {
    m_outputArray = new Object[out_arity];
    m_outputQueues = new Queue[out_arity];
    Pullable[] new_out_pullables = new Pullable[out_arity];
    for (int i = 0; i < m_outputArity; i++)
    {
      new_out_pullables[i] = m_outputPullables[i];
    }
    m_outputPullables = new_out_pullables;
    m_outputArity = out_arity;
    for (int i = 0; i < m_outputArity; i++)
    {
      m_outputQueues[i] = new ArrayDeque<Object>();
    }
    Pushable[] out_pushables = new Pushable[out_arity];
    for (int i = 0; i < m_outputPushables.length; i++)
    {
      out_pushables[i] = m_outputPushables[i];
    }
    m_outputPushables = out_pushables;
  }
  
  /**
   * @since 0.10.2
   */
  public Object printState()
  {
    return getOutputArity();
  }
  
  /**
   * @since 0.10.2
   */
  @Override
  public Fork readState(Object o)
  {
    return new Fork(((Number) o).intValue());
  }
  
  public void setPipeType( String s) {
	pipeType = s;
  }
  
  public String getPipeType() {
	  return pipeType;
  }

  @Override
  public void writingSMV(PrintStream printStream, int Id, int list, int[][] array, int arrayWidth, int maxInputArity, String pipeType) throws IOException {
	  printStream.printf("MODULE Fork"+Id+" (inc_1, inb_1, ");
	  for(int i = 1; i <= getOutputArity(); i++) {
		  if(i != getOutputArity()) {
			  printStream.printf("ouc_"+i+", oub_"+i+" ,");
		  }
		  else {
			  printStream.printf("ouc_"+i+", oub_"+i+") \n");
		  }
	  }
	  printStream.printf("	ASSIGN \n");
	  for(int i = 1; i <= getOutputArity(); i++) {
		  printStream.printf("		init(ouc_"+i+") := inc_1; \n");
		  printStream.printf("		init(oub_"+i+") := inb_1; \n");
	  }
	  printStream.printf(" \n");
	  for(int i = 1; i <= getOutputArity(); i++) {
		  printStream.printf("		next(ouc_"+i+") := next(inc_1); \n");
		  printStream.printf("		next(oub_"+i+") := next(inb_1); \n");
	  }
	  printStream.printf(" \n");
	}
}