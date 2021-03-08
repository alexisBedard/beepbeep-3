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
 * Discards events from an input trace based on a selection criterion. The
 * processor takes as input two events simultaneously; it outputs the first if
 * the second is true.
 * <p>
 * Graphically, this processor is represented as:
 * <p>
 * <img src="{@docRoot}/doc-files/tmf/Filter.png" alt="Filter">
 * 
 * @author Sylvain Hallé
 * @since 0.2.1
 * @see FilterOn
 */
@SuppressWarnings("squid:S2160")
public class Filter extends SynchronousProcessor implements SMVInterface
{
  public Filter()
  {
    super(2, 1);
  }

  @Override
  @SuppressWarnings("squid:S3516")
  protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
  {
    Object o = inputs[0];
    Object[] out = new Object[1];
    boolean b = (Boolean) inputs[inputs.length - 1];
    if (b)
    {
      out[0] = o;
    }
    else
    {
      return true;
    }
    outputs.add(out);
    return true;
  }

  @Override
  public Filter duplicate(boolean with_state)
  {
    return new Filter();
  }

@Override
public void writingSMV(PrintStream printStream, int Id, int list, int[][] array, int arrayWidth, int maxInputArity, String pipeType) throws IOException {
	int prec1 = array[Id][arrayWidth - maxInputArity];
	
	printStream.printf("MODULE Filter"+Id+"(inc_1, inb_1, inc_2, inb_2, ouc_1, oub_1)\n");
	printStream.printf("	VAR \n");
	printStream.printf("		qc_1 : array 0.."+(list-1)+" of "+array[prec1][0]+".."+array[prec1][1]+"; \n");
	printStream.printf("		qb_1 : array 0.."+(list-1)+" of boolean; \n");
	printStream.printf("		qc_2 : array 0.."+(list-1)+" of boolean; \n");
	printStream.printf("		qb_2 : array 0.."+(list-1)+" of boolean; \n");
	printStream.printf("\n");
	printStream.printf("	ASSIGN \n");
	for(int i = 1; i <= 2; i++) {
		  for(int j = 0; j < list; j++) {
			  if(j == 0) {
				  printStream.printf("		init(qc_"+i+"["+j+"]) := case \n");
				  	if(i == 1) {
				  		printStream.printf("			inb_1 & !inb_2 : inc_"+i+"; \n");
				  	}
				  	if(i == 2) {
				  		printStream.printf("			!inb_1 & inb_2 : TRUE; \n");
				  	}
				  	if(i == 1) {
				  		printStream.printf("		TRUE : "+array[prec1][0]+"; \n");
				  	}
				  	else {
				  		printStream.printf("		TRUE : FALSE; \n");
				  	}
				  printStream.printf("		esac; \n");
			  }
			  else {
				  if( i == 1) {
					  printStream.printf("		init(qc_"+i+"["+j+"]) := "+array[prec1][0]+"; \n");
				  }
				  else {
					  printStream.printf("		init(qc_"+i+"["+j+"]) := FALSE; \n");
				  }
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
	  printStream.printf("			inb_1 & inb_2 & inc_2 : inc_1; \n");
	  printStream.printf("		TRUE : "+array[Id][0]+"; \n");
	  printStream.printf("		esac; \n");
	  printStream.printf("\n");
	  printStream.printf("		init(oub_1) := case \n");
	  printStream.printf("			inb_1 & inb_2 & inc_2 : TRUE; \n");
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
						  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] : "+array[prec1][0]+"; \n");
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
						  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: "+array[prec1][0]+"; \n");
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
						  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] : "+array[prec1][0]+"; \n");
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
						  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: "+array[prec1][0]+"; \n");
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
	  printStream.printf("			next(inb_1) & qb_2[0] & qc_2[0] : TRUE; \n");
	  printStream.printf("			next(inb_1) & qb_2[0] & !qc_2[0] : FALSE; \n");
	  printStream.printf("			next(inb_2) & next(inc_2) & qb_1[0] : TRUE; \n");
	  printStream.printf("			next(inb_2) & next(!inc_2) & qb_1[0] : FALSE; \n");
	  printStream.printf("			next(inb_1) & next(inb_2) & next(inc_2) : TRUE; \n");
	  printStream.printf("			next(inb_1) & next(inb_2) & next(!inc_2) : FALSE; \n");
	  printStream.printf("			TRUE : FALSE; \n");
	  printStream.printf("		esac; \n");
	  printStream.printf("\n");
	  printStream.printf("		next(ouc_1) := case \n");
	  printStream.printf("			next(inb_1) & qb_2[0] & qc_2[0] : next(inc_1); \n");
	  printStream.printf("			next(inb_1) & qb_2[0] & !qc_2[0] : "+array[prec1][0]+"; \n");
	  printStream.printf("			next(inb_2) & next(inc_2) & qb_1[0] : qc_1[0]; \n");
	  printStream.printf("			next(inb_2) & next(inc_2) & qb_1[0] : "+array[prec1][0]+"; \n");
	  printStream.printf("			next(inb_1) & next(inb_2) & next(inc_2) : next(inc_1); \n");
	  printStream.printf("			next(inb_1) & next(inb_2) & next(!inc_2) : "+array[prec1][0]+"; \n");
	  printStream.printf("		TRUE : "+array[Id][0]+"; \n");
	  printStream.printf("		esac; \n");
	  printStream.printf("\n");
	}

@Override
 public void writePipes(PrintStream printStream, int ProcId, int[][] connectionArray) throws IOException {
	printStream.printf("		--Filter \n");
	printStream.printf("		pipe_"+ProcId+" : "+ Integer.toString(connectionArray[ProcId][0]) + ".." + Integer.toString(connectionArray[ProcId][1])+ ";\n");
	printStream.printf("		b_pipe_"+ProcId+ " : boolean; \n");
 }
}
