/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2017 Sylvain Hallé

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

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

/**
 * Performs a computation on input events to produce output events. A
 * uniform processor always outputs <em>exactly</em> one output front
 * for every input front to be processed. It can be viewed as a special
 * case of {@link SingleProcessor}; the additional hypotheses make it
 * possible to simplify the handling of events.
 * <p>
 * This is the direct descendant of {@link Processor}, and probably the one
 * you'll want to inherit from when creating your own processors. While
 * {@link Processor} takes care of input and output queues,
 * {@link UniformProcessor} also implements {@link Pullable}s and
 * {@link Pushable}s. These take care of collecting input events, waiting
 * until one new event is received from all input traces before triggering
 * the computation, pulling and buffering events from all outputs when
 * either of the {@link Pullable}s is being called, etc.
 * <p>
 * The only thing that is left undefined is what to do
 * when new input events have been received from all input traces. This
 * is the task of abstract method {@link #compute(Object[], Queue)}, which descendants
 * of this class must implement.
 * 
 * @author Sylvain Hallé
 *
 */
public abstract class UniformProcessor extends Processor
{	
	/**
	 * An array that will be used by the processor to compute
	 * its output
	 */
	protected Object[] m_outputArray;

	/**
	 * Initializes a processor
	 * @param in_arity The input arity
	 * @param out_arity The output arity
	 */
	public UniformProcessor(int in_arity, int out_arity)
	{
		super(in_arity, out_arity);
		m_outputArray = new Object[out_arity];
	}

	@Override
	synchronized public final Pushable getPushableInput(int index)
	{
		return new InputPushable(index);
	}

	@Override
	synchronized public Pullable getPullableOutput(int index)
	{
		if (index >= 0 && index < m_outputArity)
		{
			return new OutputPullable(index);
		}
		return null;
	}

	/**
	 * Computes one or more output events from its input events
	 * @param inputs An array of input events; its length corresponds to the
	 *   processor's input arity
	 * @param outputs An array where the outputs are produced
	 * @return A queue of vectors of output events, or null
	 *   if no event could be produced
	 */
	protected abstract boolean compute(Object[] inputs, Object[] outputs);

	/**
	 * Implementation of a {@link Pushable} for a single processor.
	 * 
	 * @author Sylvain Hallé
	 */
	protected class InputPushable implements Pushable
	{
		/**
		 * The index of the processor's input this pushable refers to
		 */
		private final int m_index;

		/**
		 * Creates a pushable associated to some of a processor's input
		 * traces.
		 * @param index The index of the trace. Should be between 0 and
		 *   the processor's input arity - 1. This is not checked by the
		 *   constructor, so beware.
		 */
		InputPushable(int index)
		{
			super();
			synchronized (this)
			{
				m_index = index;
			}
		}

		@Override
		synchronized public Pushable pushFast(Object o)
		{
			return push(o);
		}

		@Override
		synchronized public int getPosition()
		{
			return m_index;
		}

		@Override
		synchronized public Pushable push(Object o)
		{
			if (m_index < m_inputQueues.length)
			{
				Queue<Object> q = m_inputQueues[m_index];
				q.add(o);
			}
			// Check if each input queue has an event ready
			for (int i = 0; i < m_inputArity; i++)
			{
				Queue<Object> queue = m_inputQueues[i];
				if (queue.isEmpty())
				{
					// One of them doesn't: we can't produce an output yet
					return this;
				}
			}
			// Pick an event from each input queue
			Object[] inputs = new Object[m_inputArity];
			for (int i = 0; i < m_inputArity; i++)
			{
				Queue<Object> queue = m_inputQueues[i];
				Object ob = queue.remove();
				inputs[i] = ob;
			}
			// Compute output event
			boolean outs = compute(inputs, m_outputArray);
			if (outs != false)
			{
				for (int i = 0; i < m_outputPushables.length; i++)
				{
					Pushable p = m_outputPushables[i];
					if (p == null)
					{
						throw new PushableException("Output " + i + " of this processor is connected to nothing", getProcessor());
					}
					p.push(m_outputArray[i]);
					p.waitFor();
				}
			}
			return this;
		}

		@Override
		synchronized public Processor getProcessor()
		{
			return UniformProcessor.this;
		}

		@Override
		synchronized public void waitFor()
		{
			// Since this pushable is blocking
			return;
		}

		@Override
		synchronized public void dispose()
		{
			// Do nothing
		}
	}

	/**
	 * Implementation of a {@link Pullable} for a single processor.
	 * 
	 * @author Sylvain Hallé
	 */
	protected class OutputPullable implements Pullable
	{
		/**
		 * The index of the processor's output this pullable refers to
		 */
		private final int m_index;

		/**
		 * Creates a pullable associated to some of a processor's output
		 * traces.
		 * @param index The index of the trace. Should be between 0 and
		 *   the processor's output arity - 1. This is not checked by the
		 *   constructor, so beware.
		 */
		public OutputPullable(int index)
		{
			super();
			m_index = index;
		}

		@Override
		synchronized public void remove()
		{
			// Cannot remove an event on a pullable
			throw new UnsupportedOperationException();
		}

		@Override
		synchronized public Object pullSoft()
		{
			if (hasNextSoft() != NextStatus.YES)
			{
				return null;
			}
			synchronized (m_outputQueues)
			{
				Queue<Object> out_queue = m_outputQueues[m_index];
				// If an event is already waiting in the output queue,
				// return it and don't pull anything from the input
				if (!out_queue.isEmpty())
				{
					Object o = out_queue.remove();
					return o;
				}
			}
			return null;
		}

		@Override
		synchronized public Object pull()
		{
			if (hasNext() != true)
			{
				return null;
			}
			synchronized (m_outputQueues)
			{
				Queue<Object> out_queue = m_outputQueues[m_index];
				// If an event is already waiting in the output queue,
				// return it and don't pull anything from the input
				if (!out_queue.isEmpty())
				{
					Object o = out_queue.remove();
					return o;
				}
			}
			return null;
		}

		@Override
		synchronized public final Object next()
		{
			return pull();
		}

		@Override
		synchronized public boolean hasNext()
		{
			Queue<Object> out_queue = m_outputQueues[m_index];
			// If an event is already waiting in the output queue,
			// return it and don't pull anything from the input
			if (!out_queue.isEmpty())
			{
				return true;
			}
			// Check if each pullable has an event ready
			for (int i = 0; i < m_inputArity; i++)
			{
				Pullable p = m_inputPullables[i];
				if (p == null)
				{
					throw new PullableException("Input " + i + " of this processor is connected to nothing", getProcessor());
				}
				boolean status = p.hasNext();
				if (status == false)
				{
					return false;
				}
			}
			// We are here only if every input pullable has answered YES
			// Pull an event from each
			Object[] inputs = new Object[m_inputArity];
			for (int i = 0; i < m_inputArity; i++)
			{
				Pullable p = m_inputPullables[i];
				// Don't check for p == null, we did it above
				Object o = p.pull();
				inputs[i] = o;
			}
			// Compute output event(s)
			boolean computed = compute(inputs, m_outputArray);
			if (computed == false)
			{
				// No output will ever be returned: stop there
				return false;
			}
			for (int i = 0; i < m_outputArity; i++)
			{
				Queue<Object> queue = m_outputQueues[i];
				queue.add(m_outputArray[i]);
			}
			return true;
		}

		@Override
		synchronized public NextStatus hasNextSoft()
		{
			Queue<Object> out_queue = m_outputQueues[m_index];
			// If an event is already waiting in the output queue,
			// return yes and don't pull anything from the input
			if (!out_queue.isEmpty())
			{
				return NextStatus.YES;
			}
			// Check if each pullable has an event ready
			for (int i = 0; i < m_inputArity; i++)
			{
				Pullable p = m_inputPullables[i];
				NextStatus status = p.hasNextSoft();
				if (status == NextStatus.NO)
				{
					return NextStatus.NO;
				}
				if (status == NextStatus.MAYBE)
				{
					return NextStatus.MAYBE;
				}
			}
			// We are here only if every input pullable has answered YES
			// Pull an event from each
			Object[] inputs = new Object[m_inputArity];
			{
				int i = 0;
				for (Pullable p : m_inputPullables)
				{
					inputs[i] = p.pullSoft();
					i++;
				}
			}
			// Compute output event(s)
			boolean computed = compute(inputs, m_outputArray);
			if (computed == false)
			{
				return NextStatus.NO;
			}
			// We computed an output event; add it to the output queue
			// and answer YES
			int i = 0;
			for (Queue<Object> queue : m_outputQueues)
			{
				queue.add(m_outputArray[i]);
				i++;
			}
			return NextStatus.YES;
		}

		@Override
		synchronized public Processor getProcessor()
		{
			return UniformProcessor.this;
		}

		@Override
		synchronized public int getPosition()
		{
			return m_index;
		}

		@Override
		synchronized public Iterator<Object> iterator()
		{
			return this;
		}

		@Override
		synchronized public void start()
		{
			// Do nothing
		}

		@Override
		synchronized public void stop()
		{
			// Do nothing
		}

		@Override
		synchronized public void dispose()
		{
			// Do nothing
		}
	}

	/**
	 * Puts an array of objects (given as an argument) into an
	 * empty queue of arrays of objects. This is a convenience method
	 * that descendants of {@link UniformProcessor} (which implement
	 * {@link #compute(Object[], Queue)}) can use to avoid
	 * a few lines of code when they output a single array of events.
	 * @param v The array of objects
	 * @return The queue, or <code>null</code> if all elements of
	 *   <code>v</code> are null
	 */
	@SuppressWarnings("squid:S1168")
	protected static final Queue<Object[]> wrapVector(Object[] v)
	{
		if (v == null || allNull(v))
		{
			return null;
		}
		Queue<Object[]> out = newQueue();
		out.add(v);
		return out;
	}

	/**
	 * Puts a object (given as an argument) into an
	 *  array of objects. This is a convenience method
	 * that descendants of {@link UniformProcessor} (which implement
	 * {@link #compute(Object[], Queue)}) can use to avoid
	 * a few lines of code when they output a single event.
	 * @param o The object
	 * @return The queue
	 */
	protected static final Object[] wrapObject(Object o)
	{
		Object[] v = new Object[1];
		v[0] = o;
		return v;
	}

	/**
	 * Gets a new instance of an empty object queue
	 * @return The queue
	 */
	public static Queue<Object[]> newQueue()
	{
		return new ArrayDeque<Object[]>();
	}

}