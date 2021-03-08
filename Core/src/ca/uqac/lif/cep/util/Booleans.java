/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2018 Sylvain Hallé

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
package ca.uqac.lif.cep.util;

import ca.uqac.lif.cep.Context;
import ca.uqac.lif.cep.EventTracker;
import ca.uqac.lif.cep.SMVInterface;
import ca.uqac.lif.cep.functions.BinaryFunction;
import ca.uqac.lif.cep.functions.UnaryFunction;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

/**
 * A container object for Boolean functions.
 * 
 * @author Sylvain Hallé
 * @since 0.7
 */
public class Booleans
{
  private Booleans()
  {
    // Utility class
  }

  public static final transient And and = And.instance;

  public static final transient Or or = Or.instance;

  public static final transient Implies implies = Implies.instance;

  public static final transient Not not = Not.instance;
  
  public static final transient BagAnd bagAnd = BagAnd.instance;
  
  public static final transient BagOr bagOr = BagOr.instance;

  /**
   * Implementation of the logical conjunction
   * @since 0.7
   * @author Sylvain Hallé
   */
  public static class And extends BinaryFunction<Boolean, Boolean, Boolean> implements SMVInterface
  {
    public static final transient And instance = new And();

    private And()
    {
      super(Boolean.class, Boolean.class, Boolean.class);
    }

    @Override
    public Boolean getValue(Boolean x, Boolean y)
    {
      return x.booleanValue() && y.booleanValue();
    }
    
    @Override
    protected void trackAssociations(Boolean x, Boolean y, Boolean z, EventTracker tracker)
    {
      if (!x)
      {
        tracker.associateToOutput(-1, 0, 0, 0, 0);
      }
      else if (!y)
      {
        tracker.associateToOutput(-1, 1, 0, 0, 0);
      }
      else
      {
        tracker.associateToOutput(-1, 0, 0, 0, 0);
        tracker.associateToOutput(-1, 1, 0, 0, 0);
      }
    }

    @Override
    public boolean evaluatePartial(Object[] inputs, Object[] outputs, Context context)
    {
      if (inputs[0] != null && ((Boolean) inputs[0]) == false)
      {
        outputs[0] = false;
        return true;
      }
      if (inputs[1] != null && ((Boolean) inputs[1]) == false)
      {
        outputs[0] = false;
        return true;
      }
      if (inputs[0] != null && inputs[1] != null)
      {
        outputs[0] = ((Boolean) inputs[0]) && ((Boolean) inputs[1]);
        return true;
      }
      return false;
    }

    @Override
    public String toString()
    {
      return "∧";
    }

	@Override
	public void writingSMV(PrintStream printStream, int Id, int list, int[][] array, int arrayWidth, int maxInputArity, String pipeType) throws IOException {
		printStream.printf("MODULE And(inc_1, inb_1, inc_2, inb_2, ouc_1, oub_1) \n");
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
		  printStream.printf("			inb_1 & inb_2 : inc_1 & inc_2; \n");
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
		  printStream.printf("			next(inb_1) & qb_2[0] : next(inc_1) & qc_2[0]; \n");
		  printStream.printf("			next(inb_2) & qb_1[0] : next(inc_2) & qc_1[0]; \n");
		  printStream.printf("			next(inb_1) & next(inb_2) : next(inc_1) & next(inc_2); \n");
		  printStream.printf("			TRUE : FALSE; \n");
		  printStream.printf("		esac; \n");
		  printStream.printf("\n");
		
	}

	@Override
	public void writePipes(PrintStream printStream, int ProcId, int[][] connectionArray) throws IOException {
		printStream.printf("		--And \n");
		printStream.printf("		pipe_"+ProcId+" : boolean;\n");
		printStream.printf("		b_pipe_"+ProcId+" : boolean; \n");
		
	}
  }

  /**
   * Implementation of the logical implication
   * @since 0.7
   * @author Sylvain Hallé
   */
  public static class Implies extends BinaryFunction<Boolean, Boolean, Boolean>
  {
    public static final transient Implies instance = new Implies();

    private Implies()
    {
      super(Boolean.class, Boolean.class, Boolean.class);
    }

    @Override
    public Boolean getValue(Boolean x, Boolean y)
    {
      return !x.booleanValue() || y.booleanValue();
    }

    @Override
    public boolean evaluatePartial(Object[] inputs, Object[] outputs, Context context)
    {
      if (inputs[0] != null && ((Boolean) inputs[0]) == false)
      {
        outputs[0] = true;
        return true;
      }
      if (inputs[1] != null && ((Boolean) inputs[1]) == true)
      {
        outputs[0] = true;
        return true;
      }
      if (inputs[0] != null && inputs[1] != null)
      {
        outputs[0] = !((Boolean) inputs[0]) || ((Boolean) inputs[1]);
        return true;
      }
      return false;
    }
    
    @Override
    protected void trackAssociations(Boolean x, Boolean y, Boolean z, EventTracker tracker)
    {
      if (!x)
      {
        tracker.associateToOutput(-1, 0, 0, 0, 0);
      }
      else if (y)
      {
        tracker.associateToOutput(-1, 1, 0, 0, 0);
      }
      else
      {
        tracker.associateToOutput(-1, 0, 0, 0, 0);
        tracker.associateToOutput(-1, 1, 0, 0, 0);
      }
    }

    @Override
    public String toString()
    {
      return "→";
    }
  }

  /**
   * Implementation of the logical disjunction
   * @since 0.7
   * @author Sylvain Hallé
   */
  public static class Or extends BinaryFunction<Boolean, Boolean, Boolean> implements SMVInterface
  {
    public static final transient Or instance = new Or();

    private Or()
    {
      super(Boolean.class, Boolean.class, Boolean.class);
    }

    @Override
    public Boolean getValue(Boolean x, Boolean y)
    {
      return x.booleanValue() || y.booleanValue();
    }
    
    @Override
    protected void trackAssociations(Boolean x, Boolean y, Boolean z, EventTracker tracker)
    {
      if (x)
      {
        tracker.associateToOutput(-1, 0, 0, 0, 0);
      }
      else if (y)
      {
        tracker.associateToOutput(-1, 1, 0, 0, 0);
      }
      else
      {
        tracker.associateToOutput(-1, 0, 0, 0, 0);
        tracker.associateToOutput(-1, 1, 0, 0, 0);
      }
    }

    @Override
    public boolean evaluatePartial(Object[] inputs, Object[] outputs, Context context)
    {
      if (inputs[0] != null && ((Boolean) inputs[0]) == true)
      {
        outputs[0] = true;
        return true;
      }
      if (inputs[1] != null && ((Boolean) inputs[1]) == true)
      {
        outputs[0] = true;
        return true;
      }
      if (inputs[0] != null && inputs[1] != null)
      {
        outputs[0] = ((Boolean) inputs[0]) || ((Boolean) inputs[1]);
        return true;
      }
      return false;
    }

    @Override
    public String toString()
    {
      return "∨";
    }

	@Override
	public void writingSMV(PrintStream printStream, int Id, int list, int[][] array, int arrayWidth, int maxInputArity, String pipeType) throws IOException {
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
		
	}

	@Override
	public void writePipes(PrintStream printStream, int ProcId, int[][] connectionArray) throws IOException {		
		printStream.printf("		--Or \n");
		printStream.printf("		pipe_"+ProcId+" : boolean;\n");
		printStream.printf("		b_pipe_"+ProcId+" : boolean; \n");
		
	}
  }

  /**
   * Implementation of the logical negation
   * @since 0.7
   * @author Sylvain Hallé
   */
  public static class Not extends UnaryFunction<Boolean, Boolean> implements SMVInterface
  {
    public static final transient Not instance = new Not();

    public Not()
    {
    	
      super(Boolean.class, Boolean.class);
    }

    @Override
    public Boolean getValue(Boolean x)
    {
      return !x.booleanValue();
    }

    @Override
    public String toString()
    {
      return "¬";
    }

	@Override
	public void writingSMV(PrintStream printStream, int Id, int list, int[][] array, int arrayWidth, int maxInputArity, String pipeType) throws IOException {
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
		
	}

	@Override
	public void writePipes(PrintStream printStream, int ProcId, int[][] connectionArray) throws IOException {
		printStream.printf("		--Not \n");
		printStream.printf("		pipe_"+ProcId+" : boolean;\n");
		printStream.printf("		b_pipe_"+ProcId+" : boolean; \n");
		
	}
  }
  
  /**
   * Implementation of the logical conjunction over a collection
   * @since 0.10.3
   * @author Sylvain Hallé
   */
  public static class BagAnd extends UnaryFunction<Object,Boolean>
  {
    public static final transient BagAnd instance = new BagAnd();
    
    protected BagAnd()
    {
      super(Object.class, Boolean.class);
    }
    
    @Override
    public Boolean getValue(Object o)
    {
      if (o.getClass().isArray())
      {
        Object[] a = (Object[]) o;
        for (Object e : a)
        {
          if (!parseBoolValue(e))
          {
            return false;
          }
        }
        return true;
      }
      if (o instanceof Collection)
      {
        Collection<?> a = (Collection<?>) o;
        for (Object e : a)
        {
          if (!parseBoolValue(e))
          {
            return false;
          }
        }
        return true;
      }
      return false;
    }
  }
  
  /**
   * Implementation of the logical disjunction over a collection
   * @since 0.10.3
   * @author Sylvain Hallé
   */
  public static class BagOr extends UnaryFunction<Object,Boolean>
  {
    public static final transient BagOr instance = new BagOr();
    
    protected BagOr()
    {
      super(Object.class, Boolean.class);
    }
    
    @Override
    public Boolean getValue(Object o)
    {
      if (o.getClass().isArray())
      {
        Object[] a = (Object[]) o;
        for (Object e : a)
        {
          if (parseBoolValue(e))
          {
            return true;
          }
        }
        return false;
      }
      if (o instanceof Collection)
      {
        Collection<?> a = (Collection<?>) o;
        for (Object e : a)
        {
          if (parseBoolValue(e))
          {
            return true;
          }
        }
        return false;
      }
      return true;
    }
  }

  /**
   * Attempts to convert an object into a Boolean
   * 
   * @param o
   *          The object
   * @return The Boolean value
   * @since 0.7
   */
  public static boolean parseBoolValue(Object o)
  {
    if (o instanceof Boolean)
    {
      return (Boolean) o;
    }
    else if (o instanceof String)
    {
      String s = (String) o;
      return s.compareToIgnoreCase("true") == 0 || s.compareToIgnoreCase("T") == 0
          || s.compareToIgnoreCase("1") == 0;
    }
    if (o instanceof Number)
    {
      Number n = (Number) o;
      return Math.abs(n.doubleValue()) >= 0.00001;
    }
    // When in doubt, return false
    return false;
  }
}
