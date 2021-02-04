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

import ca.uqac.lif.cep.Connector.Variant;
import ca.uqac.lif.petitpoucet.DirectValue;
import ca.uqac.lif.petitpoucet.NodeFunction;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Source whose input is a queue of objects. One gives the
 * <code>QueueSource</code> a list of events, and that source sends these events
 * as its input one by one. When reaching the end of the list, the source
 * returns to the beginning and keeps feeding events from the list endlessly.
 * This behaviour can be changed with {@link #loop(boolean)}.
 * 
 * @author Sylvain Hallé
 * @since 0.1
 */
@SuppressWarnings("squid:S2160")
public class QueueSource extends Source
{
  /**
   * The events to repeat endlessly
   */
  protected List<Object> m_events;

  /**
   * Whether to loop over the events endlessly
   */
  protected boolean m_loop = true;

  /**
   * The index of the next event to produce
   */
  protected int m_index;
  
  protected int m_minValue = Integer.MAX_VALUE;
  protected int m_maxValue = Integer.MIN_VALUE;

  /**
   * Creates a new queue source of given output arity. The events of the queue
   * source will be duplicated on each of the outputs.
   * 
   * @param arity
   *          The output arity
   */
  public QueueSource(int arity)
  {
    super(arity);
    m_events = new ArrayList<Object>();
    m_index = 0;
  }

  /**
   * Creates a new queue source of output arity 1
   */
  public QueueSource()
  {
    super(1);
    m_events = new ArrayList<Object>();
    m_index = 0;
  }

  /**
   * Sets the events that the queue will output
   * 
   * @param queue
   *          A collection of events that the queue source will output. If the
   *          collection is ordered, the events will be output in the order they
   *          appear in the collection.
   */
  public void setEvents(Collection<Object> queue)
  {
    for (Object o : queue)
    {
      m_events.add(o);
    }
  }

  /**
   * Sets the events that the queue will output
   * 
   * @param queue
   *          An array of events that the queue source will output. The events
   *          will be output in the order they appear in the collection.
   * @return This queue source
   */
  public QueueSource setEvents(Object ... queue)
  {
    for (Object o : queue)
    {
      m_events.add(o);
    }
    return this;
  }

  /**
   * Adds an event to the queue
   * 
   * @param e
   *          The event to add
   * @return This queue source
   */
  public QueueSource addEvent(Object e)
  {
    m_events.add(e);
    return this;
  }

  /**
   * Sets whether to loop over the events endlessly
   * 
   * @param b
   *          Set to <code>true</code> to loop over the events endlessly
   *          (default), or <code>false</code> to play them only once.
   * @return This queue source
   */
  public QueueSource loop(boolean b)
  {
    m_loop = b;
    return this;
  }

  @Override
  @SuppressWarnings("squid:S1168")
  protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
  {
    int size = m_events.size();
    if (m_index >= size)
    {
      return false;
    }
    Object[] output = new Object[getOutputArity()];
    Object event = m_events.get(m_index);
    if (m_loop)
    {
      m_index = (m_index + 1) % size;
    }
    else
    {
      // If we don't loop, play the events only once
      m_index++;
    }
    if (m_index > size && !m_loop)
    {
      // No more events from this queue
      return false;
    }
    for (int i = 0; i < getOutputArity(); i++)
    {
      if (event == null)
      {
        // If one of the elements is null, don't output anything
        return true;
      }
      output[i] = event;
    }
    outputs.add(output);
    if (m_eventTracker != null)
    {
      int index = m_index - 1;
      if (index < 0)
      {
        index += size;
      }
      for (int i = 0; i < getOutputArity(); i++)
      {
        associateTo(new QueueFunction(getId(), index), i, m_outputCount);
      }
    }
    m_outputCount++;
    return true;
  }

  @Override
  public void reset()
  {
    super.reset();
    m_index = 0;
  }

  @Override
  public QueueSource duplicate(boolean with_state)
  {
    QueueSource out = new QueueSource(getOutputArity());
    out.m_loop = m_loop;
    out.setEvents(m_events);
    if (with_state)
    {
      out.m_index = m_index;
    }
    return out;
  }

  @Override
  public Class<?> getOutputType(int index)
  {
    // We return the type of the first non-null object in the queue
    for (Object o : m_events)
    {
      if (o != null)
      {
        return o.getClass();
      }
    }
    return Variant.class;
  }

  /**
   * Provenance function that links an output event to an element of the
   * processor's queue.
   */
  public static class QueueFunction implements NodeFunction
  {
    protected int m_queueIndex = 0;

    protected int m_processorId = 0;

    /**
     * Creates a new QueueFunction
     * @param proc_id The id of the processor
     * @param index The position of the event in the queue
     */
    public QueueFunction(int proc_id, int index)
    {
      super();
      m_processorId = proc_id;
      m_queueIndex = index;
    }

    @Override
    public String getDataPointId()
    {
      return "BP" + m_processorId + ".Q." + m_queueIndex;
    }

    @Override
    public String toString()
    {
      return getDataPointId();
    }

    @Override
    public NodeFunction dependsOn()
    {
      return DirectValue.instance;
    }

    public int getIndex()
    {
      return m_queueIndex;
    }
  }
  
  /**
   * @since 0.10.2
   */
  @Override
  public Object printState()
  {
    Map<String,Object> map = new HashMap<String,Object>();
    map.put("index", m_index);
    map.put("loop", m_loop);
    map.put("events", m_events);
    return map;
  }
  
  /**
   * @since 0.10.2
   */
  @SuppressWarnings("unchecked")
  @Override
  public QueueSource readState(Object o)
  {
    Map<String,Object> map = (Map<String,Object>) o;
    QueueSource qs = new QueueSource();
    qs.m_index = ((Number) map.get("index")).intValue();
    qs.m_events = (List<Object>) map.get("events");
    qs.m_loop = (Boolean) map.get("loop");
    return qs;
  }
  
  public int getSize() {
	  return m_events.size();
  }
  
  //Get the minimum value ONLY IF QUEUESOURCE CONTAINS INTEGERS
  public int getMinValue() {
	  for(int i = 0; i < m_events.size(); i++) {
		  if((Integer)m_events.get(i) < m_minValue) {
			  m_minValue = (Integer)m_events.get(i);
		  }
	  }
	  return m_minValue;
  }
  
//Get the maximum value ONLY IF QUEUESOURCE CONTAINS INTEGERS
  public int getMaxValue() {
	  for(int i = 0; i < m_events.size(); i++) {
		  if((Integer)m_events.get(i) > m_maxValue) {
			  m_maxValue = (Integer)m_events.get(i);
		  }
	  }
	  return m_maxValue;
  }
  
  //Get a specific value INTEGER ONLY
  public int getIntValue(int index) {
	  return (Integer)m_events.get(index);
  }
  
  public void writingSMV(FileWriter file, int Id) throws IOException{
	  file.write("MODULE QueueSource"+Id+"(ouc_1, oub_1) \n");
		file.write("	VAR \n");
		file.write("		source : array 0.." + Integer.toString(this.getSize() -1) +" of ");
		if(this.getMinValue() > 0){
			file.write("0.."+Integer.toString(this.getMaxValue())+ "; \n");
		}
		else {
			file.write(Integer.toString(this.getMinValue()) + ".." + Integer.toString(this.getMaxValue())+ "; \n");
		}
		file.write("		cnt : 0.." + Integer.toString(this.getSize()-1) + "; \n");
		file.write("\n");
		file.write("	ASSIGN \n");
		file.write("		init(cnt) := 0; \n");
		for(int j = 0; j < this.getSize(); j++ ) {
			file.write("		init(source["+ j +"]) := " + this.getIntValue(j)+"; \n");
		}
		file.write("		init(ouc_1) := source[cnt]; \n");
		file.write("		init(oub_1) := TRUE; \n");
		file.write("\n");
		file.write("		next(cnt) := (cnt + 1) mod " + Integer.toString(this.getSize()) +"; \n");
		
		for(int k = 0; k < this.getSize(); k++ ) {
			file.write("		next(source["+ k +"]) := " + this.getIntValue(k)+"; \n");
		}
		file.write("		next(ouc_1) := next(source[cnt]); \n");
		file.write("		next(oub_1) := TRUE; \n");
		file.write("\n");
  }
}
