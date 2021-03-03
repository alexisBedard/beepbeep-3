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
package ca.uqac.lif.cep.functions;

import ca.uqac.lif.cep.EventTracker;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.ProcessorException;
import ca.uqac.lif.cep.SMVInterface;
import ca.uqac.lif.cep.UniformProcessor;
import ca.uqac.lif.petitpoucet.NodeFunction;
import ca.uqac.lif.petitpoucet.ProvenanceNode;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;

/**
 * Applies a function to input events to produce output events. This class
 * provides a way to "lift" any <i>m</i>-to-<i>n</i> function into an
 * <i>m</i>-to-<i>n</i> processor, by simply calling the function on the inputs
 * to produce the outputs.
 * <p>
 * In earlier versions of the library, this class was called
 * <tt>FunctionProcessor</tt>.
 * 
 * @author Sylvain Hallé
 * @since 0.2.1
 */
@SuppressWarnings("squid:S2160")
public class ApplyFunction extends UniformProcessor implements SMVInterface
{
  /**
   * The object responsible for the computation
   */
  protected Function m_function;

  /**
   * A shift tracker
   * @since 0.10.3
   */
  protected ShiftTracker m_shiftTracker;

  /**
   * Instantiates a new function processor
   * 
   * @param comp
   *          The computable object responsible for the computation
   */
  public ApplyFunction(Function comp)
  {
    super(comp.getInputArity(), comp.getOutputArity());
    m_function = comp;
    m_shiftTracker = new ShiftTracker();
  }

  @Override
  public void reset()
  {
    super.reset();
    m_function.reset();
  }

  @Override
  protected boolean compute(Object[] inputs, Object[] outputs)
  {
    try
    {
      m_function.evaluate(inputs, outputs, m_context, m_shiftTracker);
      if (m_eventTracker != null)
      {
        for (int i = 0; i < inputs.length; i++)
        {
          for (int j = 0; j < outputs.length; j++)
          {
            associateToInput(i, m_inputCount, j, m_outputCount);
          }
        }
      }
      m_inputCount++;
      m_outputCount++;
    }
    catch (FunctionException e)
    {
      throw new ProcessorException(e);
    }
    return true;
  }

  @Override
  public synchronized ApplyFunction duplicate(boolean with_state)
  {
    ApplyFunction out = new ApplyFunction(m_function.duplicate(with_state));
    duplicateInto(out);
    return out;
  }

  @Override
  public final void getInputTypesFor(/*@ non_null @*/ Set<Class<?>> classes, int index)
  {
    // The type is determined by that of the underlying function
    m_function.getInputTypesFor(classes, index);
  }

  @Override
  public final synchronized Class<?> getOutputType(int index)
  {
    // The type is determined by that of the underlying function
    return m_function.getOutputTypeFor(index);
  }

  @Override
  public String toString()
  {
    return m_function.toString();
  }

  /**
   * Gets the function associated to that processor
   * 
   * @return The function
   */
  public Function getFunction()
  {
    return m_function;
  }

  public void cloneInto(ApplyFunction af, boolean with_state)
  {
    super.duplicateInto(af);
    af.m_function = m_function.duplicate(with_state);
  }

  /**
   * @since 0.10.2
   */
  @Override
  public Object printState()
  {
    return m_function;
  }

  /**
   * @since 0.10.2
   */
  @Override
  public ApplyFunction readState(Object o)
  {
    Function f = (Function) o;
    return new ApplyFunction(f);
  }

  /**
   * Simple tracker proxy that records associations from the underlying function,
   * and shifts its input/output by the current position in the input/output stream
   * @since 0.10.3
   */
  protected class ShiftTracker implements EventTracker
  {
    @Override
    public void associateTo(int id, NodeFunction f, int out_stream_index, int out_stream_pos)
    {
      if (m_eventTracker != null)
      {
        m_eventTracker.associateTo(getId(), f, out_stream_index, m_outputCount);
      }
    }

    @Override
    public void associateToInput(int id, int in_stream_index, int in_stream_pos,
        int out_stream_index, int out_stream_pos)
    {
      if (m_eventTracker != null)
      {
        m_eventTracker.associateToInput(getId(), in_stream_index, m_inputCount,
            out_stream_index, m_outputCount);
      }
    }

    @Override
    public void associateToOutput(int id, int in_stream_index, int in_stream_pos,
        int out_stream_index, int out_stream_pos)
    {
      if (m_eventTracker != null)
      {
        m_eventTracker.associateToOutput(getId(), in_stream_index, m_inputCount, 
            out_stream_index, m_outputCount);
      }

    }

    @Override
    public ProvenanceNode getProvenanceTree(int proc_id, int stream_index, int stream_pos)
    {
      return null;
    }

    @Override
    public void setConnection(int output_proc_id, int output_stream_index, int input_proc_id,
        int input_stream_index)
    {
      // Do nothing
    }

    @Override
    public void setTo(Processor ... processors)
    {
      // Do nothing
    }

    @Override
    public EventTracker getCopy()
    {
      return new ShiftTracker();
    }
  }

@Override
public void writingSMV(PrintStream printStream, int Id, int list, int[][] array, int arrayWidth, int maxInputArity, String pipeType) throws IOException {
	String s = m_function.toString();
	switch(s) {
	case "Not" :
		printStream.printf("MODULE Not(inc_1, inb_1,ouc_1, oub_1) \n");
		printStream.printf("	ASSIGN \n");
		printStream.printf("		init(ouc_1) := case \n");
		printStream.printf("			inb_1 : !inc_1; \n");
		printStream.printf("			TRUE : FALSE; \n");
		printStream.printf("		esac; \n");
		printStream.printf("\n");
		printStream.printf("		next(ouc_1) := case \n");
		printStream.printf("			next(inb_1) : next(!inc_1); \n");
		printStream.printf("			TRUE : FALSE; \n");
		printStream.printf("		esac; \n");
		printStream.printf("\n");
		printStream.printf("		init(oub_1) := inb_1; \n");
		printStream.printf("		next(oub_1) := next(inb_1); \n");
		printStream.printf("\n");
		printStream.printf("\n");
		break;
	case "Or" : 
		printStream.printf("MODULE Or(inc_1, inb_1, inc_2, inb_2, ouc_1, oub_1) \n");
		printStream.printf("	VAR \n");
		printStream.printf("		qc_1 : array 0.."+(list-1)+" of boolean; \n");
		printStream.printf("		qb_1 : array 0.."+(list-1)+" of boolean; \n");
		printStream.printf("		qc_2 : array 0.."+(list-1)+" of boolean; \n");
		printStream.printf("		qb_2 : array 0.."+(list-1)+" of boolean; \n");
		printStream.printf("\n");
		printStream.printf("	ASSIGN \n");
		
		printStream.printf("\n");
		for(int i = 1; i <= 2; i++) {
			  for(int j = 0; j < list; j++) {
				  if(j == 0) {
					  printStream.printf("		init(qc_"+i+"["+j+"]) := case \n");
					  	if(i == 1) {
					  		printStream.printf("			inb_1 & !inb_2 : inc_"+i+"; \n");
					  	}
					  	if(i == 2) {
					  		printStream.printf("			!inb_1 & inb_2 : inc_"+i+"; \n");
					  	}
					  printStream.printf("		TRUE : FALSE; \n");
					  printStream.printf("		esac; \n");
				  }
				  else {
					  printStream.printf("		init(qc_"+i+"["+j+"]) := FALSE; \n");
				  }
			  }
			  printStream.printf("\n");
		  }

		  for(int i = 1; i <= 2; i++) {
			  for(int j = 0; j < list; j++) {
				  if(j == 0) {
					  printStream.printf("		init(qb_"+i+"["+j+"]) := case \n");
					  	if(i == 1) {
					  		printStream.printf("			inb_1 & !inb_2 : TRUE; \n");
					  	}
					  	if(i == 2) {
					  		printStream.printf("			!inb_1 & inb_2 : TRUE; \n");
					  	}
					  printStream.printf("			TRUE : FALSE; \n");
					  printStream.printf("		esac; \n");
				  }
				  else {
					  printStream.printf("		init(qb_"+i+"["+j+"]) := FALSE; \n");
				  }
			  }
			  printStream.printf("\n");
		  }

		  printStream.printf("		init(ouc_1) := case \n");
		  printStream.printf("			inb_1 & inb_2 : inc_1 | inc_2; \n");
		  printStream.printf("		TRUE : FALSE; \n");
		  printStream.printf("		esac; \n");
		  printStream.printf("\n");
		  printStream.printf("		init(oub_1) := case \n");
		  printStream.printf("			inb_1 & inb_2 : TRUE; \n");
		  printStream.printf("			TRUE : FALSE; \n");
		  printStream.printf("		esac; \n");
		  printStream.printf("\n");
		 
		  //qb variables
		  for(int i = 1; i <= 2; i++) {
			  for(int j = 0; j < list; j++) {
				  printStream.printf("		next(qb_"+i+"["+j+"]) := case \n");
				  if(j == 0) {
					  printStream.printf("			-- inb_"+i+" is the only intput and both waiting lists are empty. \n");
					  if(i == 1) {
						  printStream.printf("			next(inb_1) & next(!inb_2) & !qb_1["+j+"] & !qb_2["+j+"]: TRUE; \n");
					  }
					  if(i == 2) {
						  printStream.printf("			next(!inb_1) & next(inb_2) & !qb_1["+j+"] & !qb_2["+j+"]: TRUE; \n");
					  }
					  if(j+1 == list) {
						  if(i == 1) {
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] : FALSE; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] : TRUE; \n");
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : FALSE; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] : TRUE; \n");
						  }
					  }
					  else {
						  if(i == 1) {
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: FALSE; \n");
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: TRUE;\n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE;\n");
							 
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: FALSE; \n");
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: TRUE;\n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE;\n");
						  }
					  }
				  }
				  if(j != 0) {
					  if(j+1 == list) {
						  if(i == 1) {
							  printStream.printf("			-- Only inb_1 and there is something in the waiting list at position "+ (j-1)+" \n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"] : TRUE; \n");
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"] \n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] : FALSE; \n");
							  printStream.printf("			-- Both inputs and there is something in qb_"+i+"["+(j-1)+"], something in qb_"+i+"["+j+"] \n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+(j-1)+"] & qb_"+i+"["+j+"]: TRUE; \n");
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_2 and there is something in the waiting list at position"+ (i-1)+" \n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & !qb_"+i+"["+i+"] & qb_"+i+"["+(i-1)+"] : TRUE; \n");
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"] \n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : FALSE; \n");
							  printStream.printf("			-- Both inputs and there is something in qb_"+i+"["+(j-1)+"], something in qb_"+i+"["+j+"] \n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+(j-1)+"] & qb_"+i+"["+j+"]: TRUE; \n");
						  }
					  }
					  else {
						  if(i == 1) {
							  printStream.printf("			-- Only inb_1 and there is something in the waiting list at position "+ (j-1)+" \n ");
							  printStream.printf("			next(inb_1) & next(!inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"]: TRUE; \n"); 
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: FALSE; \n");
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: TRUE;\n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE;\n");
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_2 and there is something in the waiting list at position "+ (j-1)+" \n ");
							  printStream.printf("			next(!inb_1) & next(inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"]: TRUE; \n"); 
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: FALSE; \n");
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: TRUE;\n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE;\n");
						  }
					  }
				  }
				  
				  if(j+1 == list) {
					  if(i == 1) {
						  printStream.printf("			--Waiting list is full.\n");
						  printStream.printf("			next(inb_1) & next(!inb_2) & qb_1["+j+"] & !qb_2[0]: TRUE;\n");
					  }
					  if(i == 2) {
						  printStream.printf("			--Waiting list is full.\n");
						  printStream.printf("			next(!inb_1) & next(inb_2) & qb_2["+j+"] & !qb_1[0]: TRUE;\n");
					  }
				  }
				  
				  printStream.printf("			TRUE : qb_"+i+"["+j+"]; \n");
				  printStream.printf("		esac; \n");
				  printStream.printf("\n");
			  }
		  }
		  
		//qc variables
		  for(int i = 1; i <= 2; i++) {
			  for(int j = 0; j < list; j++) {
				  printStream.printf("		next(qc_"+i+"["+j+"]) := case \n");
				  if(j == 0) {
					  printStream.printf("			-- inb_"+i+" is the only intput and both waiting lists are empty. \n");
					  if(i == 1) {
						  printStream.printf("			next(inb_1) & next(!inb_2) & !qb_1["+j+"] & !qb_2["+j+"]: next(inc_1); \n");
					  }
					  if(i == 2) {
						  printStream.printf("			next(!inb_1) & next(inb_2) & !qb_1["+j+"] & !qb_2["+j+"]: next(inc_2); \n");
					  }
					  if(j+1 == list) {
						  if(i == 1) {
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] : FALSE; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] : next(inc_1); \n");
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : FALSE; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] : next(inc_2); \n");
						  }
					  }
					  else {
						  if(i == 1) {
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: FALSE; \n");
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"]; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: next(inc_1);\n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"];\n");
							 
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: FALSE; \n");
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"]; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: next(inc_2);\n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"];\n");
						  }
					  }
				  }
				  if(j != 0) {
					  if(j+1 == list) {
						  if(i == 1) {
							  printStream.printf("			-- Only inb_1 and there is something in the waiting list at position "+ (j-1)+" \n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"] : next(inc_1); \n");
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"] \n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] : FALSE; \n");
							  printStream.printf("			-- Both inputs and there is something in qb_"+i+"["+(j-1)+"], something in qb_"+i+"["+j+"] \n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+(j-1)+"] & qb_"+i+"["+j+"]: next(inc_1); \n");
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_2 and there is something in the waiting list at position"+ (i-1)+" \n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"] : next(inc_2); \n");
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"] \n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : FALSE; \n");
							  printStream.printf("			-- Both inputs and there is something in qb_"+i+"["+(j-1)+"], something in qb_"+i+"["+j+"] \n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+(j-1)+"] & qb_"+i+"["+j+"]: next(inc_2); \n");
						  }
					  }
					  else {
						  if(i == 1) {
							  printStream.printf("			-- Only inb_1 and there is something in the waiting list at position "+ (j-1)+" \n ");
							  printStream.printf("			next(inb_1) & next(!inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"]: next(inc_1); \n"); 
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: FALSE; \n");
							  printStream.printf("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"]; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: next(inc_1);\n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"];\n");
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_2 and there is something in the waiting list at position "+ (j-1)+" \n ");
							  printStream.printf("			next(!inb_1) & next(inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"]: next(inc_2); \n"); 
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: FALSE; \n");
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"]; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: next(inc_2);\n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"];\n");
						  }
					  }
				  }
				  
				  if(j+1 == list) {
					  if(i == 1) {
						  printStream.printf("			--Waiting list is full.\n");
						  printStream.printf("			next(inb_1) & next(!inb_2) & qb_1["+j+"] & !qb_2[0]: next(inc_1);\n");
					  }
					  if(i == 2) {
						  printStream.printf("			--Waiting list is full.\n");
						  printStream.printf("			next(!inb_1) & next(inb_2) & qb_2["+j+"] & !qb_1[0]: next(inc_2);\n");
					  }
				  }
				  
				  printStream.printf("			TRUE : qc_"+i+"["+j+"]; \n");
				  printStream.printf("		esac; \n");
				  printStream.printf("\n");

			  }
		  }
		  printStream.printf("		next(oub_1) := case \n");
		  printStream.printf("			next(inb_1) & qb_2[0] : TRUE; \n");
		  printStream.printf("			next(inb_2) & qb_1[0] : TRUE; \n");
		  printStream.printf("			next(inb_1) & next(inb_2) : TRUE; \n");
		  printStream.printf("			TRUE : FALSE; \n");
		  printStream.printf("		esac; \n");
		  printStream.printf("\n");
		  printStream.printf("		next(ouc_1) := case \n");
		  printStream.printf("			next(inb_1) & qb_2[0] : next(inc_1) | qc_2[0]; \n");
		  printStream.printf("			next(inb_2) & qb_1[0] : next(inc_2) | qc_1[0]; \n");
		  printStream.printf("			next(inb_1) & next(inb_2) : next(inc_1) | next(inc_2); \n");
		  printStream.printf("			TRUE : FALSE; \n");
		  printStream.printf("		esac; \n");
		  printStream.printf("\n");
		  break;
	default:
		break;
	}
	}
}