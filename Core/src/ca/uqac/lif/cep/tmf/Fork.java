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

  @Override
  public void writingSMV(FileWriter file, int Id, int list) throws IOException {
	  file.write("MODULE Fork (");
	  		//+ "inc_1, inb_1, ouc_1, oub_1, ouc_2, oub_2) \n");
	  System.out.println(getOutputArity());
	  for(int i = 1; i <= getOutputArity(); i++) {
		  if(i != getOutputArity()) {
			  file.write("inc_"+i+", inb_"+i+" ,");
		  }
		  else {
			  file.write("inc_"+i+", inb_"+i+"); \n");
		  }
	  }
	  file.write("	ASSIGN \n");
	  for(int i = 1; i <= getOutputArity(); i++) {
		  file.write("		init(ouc_"+i+") := inc_"+i+"; \n");
		  file.write("		init(oub_"+i+") := inb_"+i+"; \n");
	  }
	  file.write(" \n");
	  for(int i = 1; i <= getOutputArity(); i++) {
		  file.write("		next(ouc_"+i+") := next(inc_"+i+"); \n");
		  file.write("		next(oub_"+i+") := next(inb_"+i+"); \n");
	  }
	  file.write(" \n");
	}
}