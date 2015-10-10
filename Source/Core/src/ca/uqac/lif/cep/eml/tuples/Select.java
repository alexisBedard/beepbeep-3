/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2015 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.cep.eml.tuples;

import java.util.HashMap;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.SingleProcessor;

public class Select extends SingleProcessor
{
	/**
	 * The list of processors appearing in the "FROM" part
	 * of the statement
	 */
	protected ProcessorDefinitionList m_processors;
	
	/**
	 * The list of attribute expressions appearing in the "SELECT"
	 * part of the statement
	 */
	protected AttributeList m_attributeList;
	
	protected FixedTupleBuilder m_builder;
	
	public Select(int in_arity)
	{
		super(in_arity, 1);
		m_processors = null;
		m_attributeList = null;
		m_builder = null;
	}

	public Select(int in_arity, String ... attributes)
	{
		this(in_arity);
		setAttributeList(attributes);
	}

	/**
	 * Convenience method to set the attributes of the selection
	 * @param attributes
	 */
	protected void setAttributeList(String[] attributes)
	{
		AttributeList al = new AttributeList();
		for (String att : attributes)
		{
			AttributeExpression aexp = null;
			if (att.contains("."))
			{
				String[] parts = att.split("\\.");
				aexp = new AttributeNameQualified(parts[0], parts[1]);
			}
			else
			{
				aexp = new AttributeNamePlain(att);
			}
			AttributeDefinition adef = new AttributeDefinitionPlain(aexp);
			al.add(adef);
		}
		m_attributeList = al;
	}
	
	public static void build(Stack<Object> stack)
	{
		stack.pop(); // (
		ProcessorDefinitionList pdl = (ProcessorDefinitionList) stack.pop();
		stack.pop(); // )
		stack.pop(); // FROM
		AttributeList al = (AttributeList) stack.pop();
		stack.pop(); // SELECT
		Select sel = new Select(pdl.size());
		sel.m_processors = pdl;
		// Connect each processor to the input
		int j = 0;
		for (ProcessorDefinition pd : pdl)
		{
			Connector.connect(pd.m_processor, sel, 0, j);
			j++;
		}
		sel.m_attributeList = al;
		stack.push(sel);
	}
	
	public void setProcessor(String name, Processor p)
	{
		if (m_processors == null)
		{
			m_processors = new ProcessorDefinitionList();
		}
		m_processors.add(new ProcessorDefinitionAs(name, p));
	}

	@Override
	protected Queue<Object[]> compute(Object[] inputs)
	{
		Map<String,Tuple> in = new HashMap<String,Tuple>();
		int i = 0;
		for (ProcessorDefinition pd : m_processors)
		{
			String alias = pd.getAlias();
			Object o = inputs[i];
			if (!(o instanceof Tuple))
			{
				// A SELECT should receive only tuples for input!
				return null; 
			}
			in.put(alias, (Tuple) o);
			i++;
		}
		Queue<Object[]> out = new ArrayDeque<Object[]>();
		Object[] tuples = new Object[1];
		Tuple t = computeCast(in);
		tuples[0] = t;
		out.add(tuples);
		return out;
	}
	
	/**
	 * Performs the computation of the SELECT on a typecast vector of
	 * inputs
	 * @param inputs A map from trace aliases to the tuple coming from 
	 *   that trace
	 * @return The output tuple
	 */
	protected Tuple computeCast(Map<String,Tuple> inputs)
	{
		if (m_attributeList.size() == 1)
		{
			AttributeDefinition a_def = m_attributeList.getFirst();
			if (a_def instanceof AttributeDefinitionPlain)
			{
				// The select clause has a single attribute with no name:
				// the output is an unnamed tuple of size 1, i.e. a constant
				AttributeExpression a_exp = a_def.getExpression();
				return a_exp.evaluate(inputs);
			}
		}
		// In all other cases, we return a named tuple
		if (m_builder == null)
		{
			// First tuple we build: first tell the builder what are the
			// attribute names for the output tuples
			String[] att_names = new String[m_attributeList.size()];
			int i = 0;
			for (AttributeDefinition a_def : m_attributeList)
			{
				String alias = a_def.getAlias();
				if (alias.isEmpty())
				{
					alias = a_def.getExpression().toString();
				}
				att_names[i] = alias;
				i++;
			}
			m_builder = new FixedTupleBuilder(att_names);
		}
		// Now build a tuple with the values we compute
		EmlConstant[] t_values = new EmlConstant[m_attributeList.size()];
		int i = 0;
		for (AttributeDefinition a_def : m_attributeList)
		{
			// For each attribute definition, evaluate and put its result
			// in the tuple with the given alias
			AttributeExpression a_exp = a_def.getExpression();
			EmlConstant a_result = a_exp.evaluate(inputs);
			t_values[i] = a_result;
			i++;
		}
		return m_builder.createTuple(t_values);
	}
	
	@Override
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		out.append("SELECT ").append(m_attributeList)
			.append(" FROM ").append(m_processors);
		return out.toString();
	}
}
