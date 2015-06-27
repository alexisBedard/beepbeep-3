package ca.uqac.lif.cep.math;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.Queue;

import org.junit.Before;
import org.junit.Test;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Filter;
import ca.uqac.lif.cep.Fork;
import ca.uqac.lif.cep.Function;
import ca.uqac.lif.cep.Mutator;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.QueueSink;
import ca.uqac.lif.cep.QueueSource;
import ca.uqac.lif.cep.Window;

public class MathTest
{

	@Before
	public void setUp() throws Exception
	{
	}
	
	@Test
	public void testSumPush()
	{
		QueueSource cp = new QueueSource(1, 1);
		CumulativeSum cs = new CumulativeSum();
		QueueSink qs = new QueueSink(1);
		Connector.connect(cp, cs);
		Connector.connect(cs, qs);
		Queue<Object> q = qs.getQueue(0);
		Number n;
		cp.push();
		n = (Number) q.remove();
		if (n.intValue() != 1)
		{
			fail("Wrong number");
		}
		cp.push();
		n = (Number) q.remove();
		if (n.intValue() != 2)
		{
			fail("Wrong number");
		}
		cp.push();
		n = (Number) q.remove();
		if (n.intValue() != 3)
		{
			fail("Wrong number");
		}
	}
	
	@Test
	public void testSumPull()
	{
		QueueSource cp = new QueueSource(1, 1);
		CumulativeSum cs = new CumulativeSum();
		QueueSink qs = new QueueSink(1);
		Connector.connect(cp, cs);
		Connector.connect(cs, qs);
		Queue<Object> q = qs.getQueue(0);
		Number n;
		qs.pull();
		n = (Number) q.remove();
		if (n.intValue() != 1)
		{
			fail("Wrong number");
		}
		qs.pull();
		n = (Number) q.remove();
		if (n.intValue() != 2)
		{
			fail("Wrong number");
		}
		qs.pull();
		n = (Number) q.remove();
		if (n.intValue() != 3)
		{
			fail("Wrong number");
		}
	}

	@Test
	public void testPower()
	{
		LinkedList<Object> l_input1 = new LinkedList<Object>();
		l_input1.add(2);
		l_input1.add(3);
		l_input1.add(2);
		LinkedList<Object> l_input2 = new LinkedList<Object>();
		l_input2.add(4);
		l_input2.add(2);
		l_input2.add(0);
		QueueSource input1 = new QueueSource(l_input1, 1);
		QueueSource input2 = new QueueSource(l_input2, 1);
		Function pow = new Function(new Power());
		Connector.connect(input1, input2, pow);
		QueueSink sink = new QueueSink(1);
		Connector.connect(pow, sink);
		Number recv;
		input1.push();
		input2.push();
		recv = (Number) sink.remove().firstElement(); // 2^4
		if (recv == null || recv.intValue() != 16)
		{
			fail("Expected 16, got " + recv);
		}
		input1.push();
		input2.push();
		recv = (Number) sink.remove().firstElement(); // 3^2
		if (recv == null || recv.intValue() != 9)
		{
			fail("Expected 9, got " + recv);
		}
		input1.push();
		input2.push();
		recv = (Number) sink.remove().firstElement(); // 2^0
		if (recv == null || recv.intValue() != 1)
		{
			fail("Expected 1, got " + recv);
		}
	}
	
	@Test
	public void testStatisticalMomentPull1()
	{
		LinkedList<Object> l_input1 = new LinkedList<Object>();
		l_input1.add(2);
		l_input1.add(3);
		l_input1.add(2);
		QueueSource input1 = new QueueSource(l_input1, 1);
		QueueSink sink = new QueueSink(1);
		setupStatisticalMoment(input1, sink, 1);
		Number recv;
		sink.pull();
		recv = (Number) sink.remove().firstElement(); // 2
		if (recv == null || Math.abs(recv.floatValue() - 2) > 0.0001)
		{
			fail("Expected 2, got " + recv);
		}
		sink.pull();
		recv = (Number) sink.remove().firstElement(); // 2
		if (recv == null || Math.abs(recv.floatValue() - 2.5) > 0.0001)
		{
			fail("Expected 2.5, got " + recv);
		}
		sink.pull();
		recv = (Number) sink.remove().firstElement(); // 2
		if (recv == null || Math.abs(recv.floatValue() - 2.33333) > 0.0001)
		{
			fail("Expected 2.33, got " + recv);
		} 
	}
	
	@Test
	public void testStatisticalMomentPush1()
	{
		LinkedList<Object> l_input1 = new LinkedList<Object>();
		l_input1.add(2);
		l_input1.add(3);
		l_input1.add(2);
		QueueSource input1 = new QueueSource(l_input1, 1);
		QueueSink sink = new QueueSink(1);
		setupStatisticalMoment(input1, sink, 1);
		Number recv;
		input1.push();
		recv = (Number) sink.remove().firstElement(); // 2
		if (recv == null || Math.abs(recv.floatValue() - 2) > 0.0001)
		{
			fail("Expected 2, got " + recv);
		}
		input1.push();
		recv = (Number) sink.remove().firstElement(); // 2
		if (recv == null || Math.abs(recv.floatValue() - 2.5) > 0.0001)
		{
			fail("Expected 2.5, got " + recv);
		}
		input1.push();
		recv = (Number) sink.remove().firstElement(); // 2
		if (recv == null || Math.abs(recv.floatValue() - 2.33333) > 0.0001)
		{
			fail("Expected 2.33, got " + recv);
		} 
	}
	
	protected static void setupStatisticalMoment(Processor source, Processor sink, int n)
	{
		Fork fork = new Fork(2);
		Connector.connect(source, fork);
		CumulativeSum sum_left = new CumulativeSum();
		{
			// Left part: sum of x^n
			Fork fork2 = new Fork(2);
			Connector.connect(fork, fork2, 0, 0);
			Mutator exponent = new Mutator(1, n);
			Connector.connect(fork2, exponent, 0, 0);
			Function pow = new Function(new Power());
			Connector.connect(fork2, pow, 1, 0);
			Connector.connect(exponent, pow, 0, 1);
			Connector.connect(pow, sum_left);
		}
		CumulativeSum sum_right = new CumulativeSum();
		{
			// Right part: sum of 1
			Mutator one = new Mutator(1, 1);
			Connector.connect(fork, one, 1, 0);
			Connector.connect(one, sum_right);
		}
		Function div = new Function(new Division());
		Connector.connect(sum_left, sum_right, div);
		Connector.connect(div, sink);
	}
	
	protected static void setupSumIfGreater(Processor source, Processor sink)
	{
		Window win = new Window(new CumulativeSum(), 2);
		Connector.connect(source, win);
		Fork fork = new Fork(3);
		Connector.connect(win, fork);
		Function greater = new Function(new IsGreaterThan());
		Mutator five = new Mutator(1, 5);
		Connector.connect(fork, five, 0, 0);
		Connector.connect(fork, greater, 1, 0);
		Connector.connect(five, greater, 0, 1);
		Filter fil = new Filter();
		Connector.connect(fork, fil, 2, 0);
		Connector.connect(greater, fil, 0, 1);
		Connector.connect(fil, sink);
	}

	@Test
	public void testSumIfGreaterPush()
	{
		LinkedList<Object> l_input1 = new LinkedList<Object>();
		l_input1.add(2);
		l_input1.add(3);
		l_input1.add(4);
		l_input1.add(0);
		l_input1.add(6);
		QueueSource input1 = new QueueSource(l_input1, 1);
		QueueSink sink = new QueueSink(1);
		setupSumIfGreater(input1, sink);
		Number recv;
		input1.push();
		input1.push();
		recv = (Number) sink.remove().firstElement(); // 2+3=5, null
		if (recv != null)
		{
			fail("Expected null, got " + recv);
		}
		input1.push();
		recv = (Number) sink.remove().firstElement(); // 3+4=7
		if (recv == null || recv.intValue() != 7)
		{
			fail("Expected 7, got " + recv);
		}
		input1.push();
		recv = (Number) sink.remove().firstElement(); // 4+0=4, null
		if (recv != null)
		{
			fail("Expected null, got " + recv);
		}
		input1.push();
		recv = (Number) sink.remove().firstElement(); // 0+6=6
		if (recv == null || recv.intValue() != 6)
		{
			fail("Expected 6, got " + recv);
		}
	}
	
	@Test
	public void testSumIfGreaterPullHard()
	{
		LinkedList<Object> l_input1 = new LinkedList<Object>();
		l_input1.add(2);
		l_input1.add(3);
		l_input1.add(4);
		l_input1.add(0);
		l_input1.add(6);
		QueueSource input1 = new QueueSource(l_input1, 1);
		QueueSink sink = new QueueSink(1);
		setupSumIfGreater(input1, sink);
		Number recv;
		sink.pullHard();
		recv = (Number) sink.remove().firstElement(); // 3+4=7
		if (recv == null || recv.intValue() != 7)
		{
			fail("Expected 7, got " + recv);
		}
		sink.pullHard();
		recv = (Number) sink.remove().firstElement(); // 0+6=6
		if (recv == null || recv.intValue() != 6)
		{
			fail("Expected 6, got " + recv);
		}
	}

}