/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2019 Sylvain HallÃ©

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

/**
 * A simple processor that adds two integers.
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
 * The "clean" way to add numbers is to use 
 * {@link ca.uqac.lif.cep.functions.ApplyFunction ApplyFunction} with
 * {@link ca.uqac.lif.cep.util.Numbers#addition Numbers.addition}.
 * 
 * @author Sylvain HallÃ©
 * @since 0.10.1
 */
public class Adder extends SynchronousProcessor implements SMVInterface
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
  public Adder()
  {
    super(2, 1);
  }

  @Override
  public Adder duplicate(boolean with_state)
  {
    return new Adder();
  }

  @Override
  protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
  {
    outputs.add(new Object[]{((Integer) inputs[0]) + ((Integer) inputs[1])});
    return true;
  }
  
  /**
   * @since 0.10.2
   */
  @Override
  public Adder readState(Object o)
  {
    return new Adder();
  }
  
  
  //Interface smvWritable Implements
  @Override
  public void writingSMV(FileWriter file, int Id, int list) throws IOException{
	  file.write("MODULE Adder"+Id+"(inc_1, inb_1, inc_2, inb_2, ouc_1, oub_1) \n");
	  file.write("	VAR \n");
	  
	  for(int i = 1; i <= 2; i++) {
		  file.write("		qc_"+i+" : array 0.."+(list-1)+" of 0..3; \n");
		  file.write("		qb_"+i+" : array 0.."+(list-1)+" of boolean; \n");
	  }
	  file.write("\n");
	  file.write("	ASSIGN \n");

	  for(int i = 1; i <= 2; i++) {
		  for(int j = 0; j < list; j++) {
			  if(j == 0) {
				  file.write("		init(qc_"+i+"["+j+"]) := case \n");
				  	if(i == 1) {
				  		file.write("			inb_1 & !inb_2 : inc_"+i+"; \n");
				  	}
				  	if(i == 2) {
				  		file.write("			!inb_1 & inb_2 : inc_"+i+"; \n");
				  	}
				  file.write("			TRUE : 0; \n");
				  file.write("		esac; \n");
			  }
			  else {
				  file.write("		init(qc_"+i+"["+j+"]) := 0; \n");
			  }
		  }
		  file.write("\n");
	  }

	  for(int i = 1; i <= 2; i++) {
		  for(int j = 0; j < list; j++) {
			  if(j == 0) {
				  file.write("		init(qb_"+i+"["+j+"]) := case \n");
				  	if(i == 1) {
				  		file.write("			inb_1 & !inb_2 : TRUE; \n");
				  	}
				  	if(i == 2) {
				  		file.write("			!inb_1 & inb_2 : TRUE; \n");
				  	}
				  file.write("			TRUE : FALSE; \n");
				  file.write("		esac; \n");
			  }
			  else {
				  file.write("		init(qb_"+i+"["+j+"]) := FALSE; \n");
			  }
		  }
		  file.write("\n");
	  }

	  file.write("		init(ouc_1) := case \n");
	  file.write("			inb_1 & inb_2 : inc_1 + inc_2; \n");
	  file.write("			TRUE : 0; \n");
	  file.write("		esac; \n");
	  file.write("\n");
	  file.write("		init(oub_1) := case \n");
	  file.write("			inb_1 & inb_2 : TRUE; \n");
	  file.write("			TRUE : FALSE; \n");
	  file.write("		esac; \n");
	  file.write("\n");
	 
	  //qb variables
	  for(int i = 1; i <= 2; i++) {
		  for(int j = 0; j < list; j++) {
			  file.write("		next(qb_"+i+"["+j+"]) := case \n");
			  if(j == 0) {
				  file.write("			-- inb_"+i+" is the only intput and both waiting lists are empty. \n");
				  if(i == 1) {
					  file.write("			next(inb_1) & next(!inb_2) & !qb_1["+j+"] & !qb_2["+j+"]: TRUE; \n");
				  }
				  if(i == 2) {
					  file.write("			next(!inb_1) & next(inb_2) & !qb_1["+j+"] & !qb_2["+j+"]: TRUE; \n");
				  }
				  if(j+1 == list) {
					  if(i == 1) {
						  file.write("			-- Only inb_2 and there is something in qb_"+i+"["+j+"]\n");
						  file.write("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] : FALSE; \n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] : TRUE; \n");
					  }
					  if(i == 2) {
						  file.write("			-- Only inb_1 and there is something in qb_"+i+"["+j+"]\n");
						  file.write("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : FALSE; \n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] : TRUE; \n");
					  }
				  }
				  else {
					  if(i == 1) {
						  file.write("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
						  file.write("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: FALSE; \n");
						  file.write("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE; \n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: TRUE;\n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE;\n");
						 
					  }
					  if(i == 2) {
						  file.write("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
						  file.write("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: FALSE; \n");
						  file.write("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE; \n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: TRUE;\n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE;\n");
					  }
				  }
			  }
			  if(j != 0) {
				  if(j+1 == list) {
					  if(i == 1) {
						  file.write("			-- Only inb_1 and there is something in the waiting list at position "+ (j-1)+" \n");
						  file.write("			next(inb_1) & next(!inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"] : TRUE; \n");
						  file.write("			-- Only inb_2 and there is something in qb_"+i+"["+j+"] \n");
						  file.write("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] : FALSE; \n");
						  file.write("			-- Both inputs and there is something in qb_"+i+"["+(j-1)+"], something in qb_"+i+"["+j+"] \n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+(j-1)+"] & qb_"+i+"["+j+"]: TRUE; \n");
					  }
					  if(i == 2) {
						  file.write("			-- Only inb_2 and there is something in the waiting list at position"+ (i-1)+" \n");
						  file.write("			next(!inb_1) & next(inb_2) & !qb_"+i+"["+i+"] & qb_"+i+"["+(i-1)+"] : TRUE; \n");
						  file.write("			-- Only inb_1 and there is something in qb_"+i+"["+j+"] \n");
						  file.write("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : FALSE; \n");
						  file.write("			-- Both inputs and there is something in qb_"+i+"["+(j-1)+"], something in qb_"+i+"["+j+"] \n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+(j-1)+"] & qb_"+i+"["+j+"]: TRUE; \n");
					  }
				  }
				  else {
					  if(i == 1) {
						  file.write("			-- Only inb_1 and there is something in the waiting list at position "+ (j-1)+" \n ");
						  file.write("			next(inb_1) & next(!inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"]: TRUE; \n"); 
						  file.write("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
						  file.write("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: FALSE; \n");
						  file.write("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE; \n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: TRUE;\n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE;\n");
					  }
					  if(i == 2) {
						  file.write("			-- Only inb_2 and there is something in the waiting list at position "+ (j-1)+" \n ");
						  file.write("			next(!inb_1) & next(inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"]: TRUE; \n"); 
						  file.write("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
						  file.write("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: FALSE; \n");
						  file.write("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE; \n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: TRUE;\n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: TRUE;\n");
					  }
				  }
			  }
			  
			  if(j+1 == list) {
				  if(i == 1) {
					  file.write("			--Waiting list is full.\n");
					  file.write("			next(inb_1) & next(!inb_2) & qb_1["+j+"] & !qb_2[0]: TRUE;\n");
				  }
				  if(i == 2) {
					  file.write("			--Waiting list is full.\n");
					  file.write("			next(!inb_1) & next(inb_2) & qb_2["+j+"] & !qb_1[0]: TRUE;\n");
				  }
			  }
			  
			  file.write("			TRUE : qb_"+i+"["+j+"]; \n");
			  file.write("		esac; \n");
			  file.write("\n");
		  }
	  }
	  
	//qc variables
	  for(int i = 1; i <= 2; i++) {
		  for(int j = 0; j < list; j++) {
			  file.write("		next(qc_"+i+"["+j+"]) := case \n");
			  if(j == 0) {
				  file.write("			-- inb_"+i+" is the only intput and both waiting lists are empty. \n");
				  if(i == 1) {
					  file.write("			next(inb_1) & next(!inb_2) & !qb_1["+j+"] & !qb_2["+j+"]: next(inc_1); \n");
				  }
				  if(i == 2) {
					  file.write("			next(!inb_1) & next(inb_2) & !qb_1["+j+"] & !qb_2["+j+"]: next(inc_2); \n");
				  }
				  if(j+1 == list) {
					  if(i == 1) {
						  file.write("			-- Only inb_2 and there is something in qb_"+i+"["+j+"]\n");
						  file.write("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] : 0; \n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] : next(inc_1); \n");
					  }
					  if(i == 2) {
						  file.write("			-- Only inb_1 and there is something in qb_"+i+"["+j+"]\n");
						  file.write("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : 0; \n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] : next(inc_2); \n");
					  }
				  }
				  else {
					  if(i == 1) {
						  file.write("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
						  file.write("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: 0; \n");
						  file.write("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"]; \n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: next(inc_1);\n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"];\n");
						 
					  }
					  if(i == 2) {
						  file.write("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
						  file.write("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: 0; \n");
						  file.write("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"]; \n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: next(inc_2);\n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"];\n");
					  }
				  }
			  }
			  if(j != 0) {
				  if(j+1 == list) {
					  if(i == 1) {
						  file.write("			-- Only inb_1 and there is something in the waiting list at position "+ (j-1)+" \n");
						  file.write("			next(inb_1) & next(!inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"] : next(inc_1); \n");
						  file.write("			-- Only inb_2 and there is something in qb_"+i+"["+j+"] \n");
						  file.write("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] : 0; \n");
						  file.write("			-- Both inputs and there is something in qb_"+i+"["+(j-1)+"], something in qb_"+i+"["+j+"] \n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+(j-1)+"] & qb_"+i+"["+j+"]: next(inc_1); \n");
					  }
					  if(i == 2) {
						  file.write("			-- Only inb_2 and there is something in the waiting list at position"+ (i-1)+" \n");
						  file.write("			next(!inb_1) & next(inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"] : next(inc_2); \n");
						  file.write("			-- Only inb_1 and there is something in qb_"+i+"["+j+"] \n");
						  file.write("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : 0; \n");
						  file.write("			-- Both inputs and there is something in qb_"+i+"["+(j-1)+"], something in qb_"+i+"["+j+"] \n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+(j-1)+"] & qb_"+i+"["+j+"]: next(inc_2); \n");
					  }
				  }
				  else {
					  if(i == 1) {
						  file.write("			-- Only inb_1 and there is something in the waiting list at position "+ (j-1)+" \n ");
						  file.write("			next(inb_1) & next(!inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"]: next(inc_1); \n"); 
						  file.write("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
						  file.write("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: 0; \n");
						  file.write("			-- Only inb_2 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"]; \n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: next(inc_1);\n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"];\n");
					  }
					  if(i == 2) {
						  file.write("			-- Only inb_2 and there is something in the waiting list at position "+ (j-1)+" \n ");
						  file.write("			next(!inb_1) & next(inb_2) & !qb_"+i+"["+j+"] & qb_"+i+"["+(j-1)+"]: next(inc_2); \n"); 
						  file.write("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"] \n");
						  file.write("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: 0; \n");
						  file.write("			-- Only inb_1 and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"]; \n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"], nothing in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: next(inc_2);\n");
						  file.write("			--Both inputs and there is something in qb_"+i+"["+j+"], something in qb_"+i+"["+(j+1)+"]\n");
						  file.write("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] & qb_"+i+"["+(j+1)+"]: qc_"+i+"["+(j+1)+"];\n");
					  }
				  }
			  }
			  
			  if(j+1 == list) {
				  if(i == 1) {
					  file.write("			--Waiting list is full.\n");
					  file.write("			next(inb_1) & next(!inb_2) & qb_1["+j+"] & !qb_2[0]: next(inc_1);\n");
				  }
				  if(i == 2) {
					  file.write("			--Waiting list is full.\n");
					  file.write("			next(!inb_1) & next(inb_2) & qb_2["+j+"] & !qb_1[0]: next(inc_2);\n");
				  }
			  }
			  
			  file.write("			TRUE : qc_"+i+"["+j+"]; \n");
			  file.write("		esac; \n");
			  file.write("\n");
		  }
	  }
	  file.write("		next(oub_1) := case \n");
	  file.write("			next(inb_1) & qb_2[0] : TRUE; \n");
	  file.write("			next(inb_2) & qb_1[0] : TRUE; \n");
	  file.write("			next(inb_1) & next(inb_2) : TRUE; \n");
	  file.write("			TRUE : FALSE; \n");
	  file.write("		esac; \n");
	  file.write("\n");
	  file.write("		next(ouc_1) := case \n");
	  file.write("			next(inb_1) & qb_2[0] : next(inc_1) + qc_2[0]; \n");
	  file.write("			next(inb_2) & qb_1[0] : next(inc_2) + qc_1[0]; \n");
	  file.write("			next(inb_1) & next(inb_2) : next(inc_1) + next(inc_2); \n");
	  file.write("			TRUE : 0; \n");
	  file.write("		esac; \n");
	  file.write("\n");
  }
}