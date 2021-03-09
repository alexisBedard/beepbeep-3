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
package ca.uqac.lif.cep.util;

import java.io.IOException;
import java.io.PrintStream;

import ca.uqac.lif.cep.Context;
import ca.uqac.lif.cep.EventTracker;
import ca.uqac.lif.cep.SMVInterface;
import ca.uqac.lif.cep.functions.BinaryFunction;
import ca.uqac.lif.cep.functions.FunctionException;
import ca.uqac.lif.cep.functions.UnaryFunction;

/**
 * A container object for functions applying to numbers.
 * 
 * @author Sylvain Hallé
 * @since 0.7
 */
public class Numbers
{
  protected Numbers()
  {
    // Utility class
  }

  /**
   * Computes the absolute value of its argument
   */
  public static final AbsoluteValue absoluteValue = new AbsoluteValue();

  /**
   * Adds two numbers
   */
  public static final Addition addition = new Addition();
  
  /**
   * Rounds a number up to the nearest integer
   */
  public static final Ceiling ceiling = new Ceiling();

  /**
   * Computes the quotient of two numbers
   */
  public static final Division division = new Division();
  
  /**
   * Rounds a number down to the nearest integer
   */
  public static final Floor floor = new Floor();

  public static final IsEven isEven = new IsEven();

  public static final IsGreaterOrEqual isGreaterOrEqual = new IsGreaterOrEqual();

  public static final IsGreaterThan isGreaterThan = new IsGreaterThan();

  public static final IsLessOrEqual isLessOrEqual = new IsLessOrEqual();

  public static final IsLessThan isLessThan = new IsLessThan();

  public static final Maximum maximum = new Maximum();

  public static final Minimum minimum = new Minimum();

  public static final Multiplication multiplication = new Multiplication();

  public static final NumberCast numberCast = new NumberCast();

  public static final Power power = new Power();

  public static final Signum signum = new Signum();

  public static final SquareRoot squareRoot = new SquareRoot();

  public static final Subtraction subtraction = new Subtraction();

  /**
   * Computes the absolute value of its argument
   * 
   * @author Sylvain Hallé
   * @since 0.7
   */
  public static final class AbsoluteValue extends UnaryFunction<Number, Number>
  {
    protected AbsoluteValue()
    {
      super(Number.class, Number.class);
    }

    @Override
    public Number getValue(Number x)
    {
      return Math.abs(x.floatValue());
    }

    @Override
    public String toString()
    {
      return "ABS";
    }
  }

  /**
   * Computes the sum of its arguments
   * 
   * @author Sylvain Hallé
   * @since 0.7
   */
  public static final class Addition extends BinaryFunction<Number, Number, Number> implements SMVInterface
  {
    /**
     * Make constructor private, to force users to refer to the static instance of
     * addition
     */
    protected Addition()
    {
      super(Number.class, Number.class, Number.class);
    }

    @Override
    public Number getValue(Number x, Number y)
    {
      return x.floatValue() + y.floatValue();
    }

    @Override
    public Number getStartValue()
    {
      return 0f;
    }

    @Override
    public String toString()
    {
      return "+";
    }

	@Override
	public void writingSMV(PrintStream printStream, int Id, int list, int[][] array, int arrayWidth, int maxInputArity,
			String pipeType) throws IOException {
		  printStream.printf("MODULE Addition"+Id+"(inc_1, inb_1, inc_2, inb_2, ouc_1, oub_1) \n");
		  printStream.printf("	VAR \n");
		  
		  int prec1 = array[Id][arrayWidth - maxInputArity];
		  int prec2 = array[Id][arrayWidth - maxInputArity + 1];
		  printStream.printf("		qc_1 : array 0.."+(list-1)+" of "+array[prec1][0]+".."+array[prec1][1]+"; \n");
		  printStream.printf("		qb_1 : array 0.."+(list-1)+" of boolean; \n");
		  printStream.printf("		qc_2 : array 0.."+(list-1)+" of "+array[prec2][0]+".."+array[prec2][1]+"; \n");		  
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
					  		printStream.printf("			!inb_1 & inb_2 : inc_"+i+"; \n");
					  	}
					  	if(i == 1) {
					  		printStream.printf("		TRUE : "+array[prec1][0]+"; \n");
					  	}
					  	else {
					  		printStream.printf("		TRUE : "+array[prec2][0]+"; \n");
					  	}
					  printStream.printf("		esac; \n");
				  }
				  else {
					  if( i == 1) {
						  printStream.printf("		init(qc_"+i+"["+j+"]) := "+array[prec1][0]+"; \n");
					  }
					  else {
						  printStream.printf("		init(qc_"+i+"["+j+"]) := "+array[prec2][0]+"; \n");
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
		  printStream.printf("			inb_1 & inb_2 : inc_1 + inc_2; \n");
		  printStream.printf("		TRUE : "+array[Id][0]+"; \n");
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
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] : "+array[prec1][0]+"; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] : next(inc_1); \n");
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : "+array[prec2][0]+"; \n");
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
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: "+array[prec2][0]+"; \n");
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
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : "+array[prec2][0]+"; \n");
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
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: "+array[prec2][0]+"; \n");
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
		  printStream.printf("			next(inb_1) & qb_2[0] : next(inc_1) + qc_2[0]; \n");
		  printStream.printf("			next(inb_2) & qb_1[0] : next(inc_2) + qc_1[0]; \n");
		  printStream.printf("			next(inb_1) & next(inb_2) : next(inc_1) + next(inc_2); \n");
		  printStream.printf("		TRUE : "+array[Id][0]+"; \n");
		  printStream.printf("		esac; \n");
		  printStream.printf("\n");
		
	}

	@Override
	public void writePipes(PrintStream printStream, int ProcId, int[][] connectionArray) throws IOException {
		printStream.printf("		--Addition \n");
		printStream.printf("		pipe_"+ProcId+" : "+ Integer.toString(connectionArray[ProcId][0]) + ".." + Integer.toString(connectionArray[ProcId][1])+ ";\n");
		printStream.printf("		b_pipe_"+ProcId+ " : boolean; \n");
		
	}
  }

  /**
   * Computes the quotient of its arguments
   * 
   * @author Sylvain Hallé
   * @since 0.7
   */
  public static final class Division extends BinaryFunction<Number, Number, Number> implements SMVInterface
  {
    protected Division()
    {
      super(Number.class, Number.class, Number.class);
    }

    @Override
    public Number getValue(Number x, Number y)
    {
      return x.floatValue() / y.floatValue();
    }

    @Override
    public Number getStartValue()
    {
      return 1f;
    }

    @Override
    public String toString()
    {
      return "÷";
    }

	@Override
	public void writingSMV(PrintStream printStream, int Id, int list, int[][] array, int arrayWidth, int maxInputArity,
			String pipeType) throws IOException {
		printStream.printf("MODULE Division"+Id+"(inc_1, inb_1, inc_2, inb_2, ouc_1, oub_1) \n");
		  printStream.printf("	VAR \n");
		  
		  int prec1 = array[Id][arrayWidth - maxInputArity];
		  int prec2 = array[Id][arrayWidth - maxInputArity + 1];
		  printStream.printf("		qc_1 : array 0.."+(list-1)+" of "+array[prec1][0]+".."+array[prec1][1]+"; \n");
		  printStream.printf("		qb_1 : array 0.."+(list-1)+" of boolean; \n");
		  printStream.printf("		qc_2 : array 0.."+(list-1)+" of "+array[prec2][0]+".."+array[prec2][1]+"; \n");		  
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
					  		printStream.printf("			!inb_1 & inb_2 : inc_"+i+"; \n");
					  	}
					  	if(i == 1) {
					  		printStream.printf("		TRUE : "+array[prec1][0]+"; \n");
					  	}
					  	else {
					  		printStream.printf("		TRUE : "+array[prec2][0]+"; \n");
					  	}
					  printStream.printf("		esac; \n");
				  }
				  else {
					  if( i == 1) {
						  printStream.printf("		init(qc_"+i+"["+j+"]) := "+array[prec1][0]+"; \n");
					  }
					  else {
						  printStream.printf("		init(qc_"+i+"["+j+"]) := "+array[prec2][0]+"; \n");
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
		  printStream.printf("			inb_1 & inb_2 : inc_1 / inc_2; \n");
		  printStream.printf("		TRUE : "+array[Id][0]+"; \n");
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
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] : "+array[prec1][0]+"; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] : next(inc_1); \n");
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : "+array[prec2][0]+"; \n");
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
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: "+array[prec2][0]+"; \n");
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
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : "+array[prec2][0]+"; \n");
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
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: "+array[prec2][0]+"; \n");
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
		  printStream.printf("			next(inb_1) & qb_2[0] : next(inc_1) / qc_2[0]; \n");
		  printStream.printf("			next(inb_2) & qb_1[0] : next(inc_2) / qc_1[0]; \n");
		  printStream.printf("			next(inb_1) & next(inb_2) : next(inc_1) / next(inc_2); \n");
		  printStream.printf("		TRUE : "+array[Id][0]+"; \n");
		  printStream.printf("		esac; \n");
		  printStream.printf("\n");
		
	}

	@Override
	public void writePipes(PrintStream printStream, int ProcId, int[][] connectionArray) throws IOException {
		printStream.printf("		--Division \n");
		printStream.printf("		pipe_"+ProcId+" : "+ Integer.toString(connectionArray[ProcId][0]) + ".." + Integer.toString(connectionArray[ProcId][1])+ ";\n");
		printStream.printf("		b_pipe_"+ProcId+ " : boolean; \n");
		
	}
  }

  /**
   * Computes if a number is even
   * 
   * @author Sylvain Hallé
   * @since 0.7
   */
  public static final class IsEven extends UnaryFunction<Number, Boolean>
  {
    protected IsEven()
    {
      super(Number.class, Boolean.class);
    }

    @Override
    public Boolean getValue(Number x)
    {
      if (x.floatValue() != Math.round(x.floatValue()))
      {
        // Not an integer
        return false;
      }
      return x.intValue() % 2 == 0;
    }

    @Override
    public String toString()
    {
      return "IS EVEN";
    }
  }

  /**
   * Checks if a number is greater than or equal to an other number.
   * @author Sylvain Hallé
   * @since 0.7
   */
  public static final class IsGreaterOrEqual extends BinaryFunction<Number, Number, Boolean>
  {
    protected IsGreaterOrEqual()
    {
      super(Number.class, Number.class, Boolean.class);
    }

    @Override
    public Boolean getValue(Number x, Number y)
    {
      return x.floatValue() >= y.floatValue();
    }

    @Override
    public Boolean getStartValue()
    {
      return false;
    }

    @Override
    public String toString()
    {
      return "≥";
    }

  }

  /**
   * Checks if a number is greater than other number.
   * @author Sylvain Hallé
   * @since 0.7
   */
  public static final class IsGreaterThan extends BinaryFunction<Number, Number, Boolean>
  {
    protected IsGreaterThan()
    {
      super(Number.class, Number.class, Boolean.class);
    }

    @Override
    public Boolean getValue(Number x, Number y)
    {
      return x.floatValue() > y.floatValue();
    }

    @Override
    public Boolean getStartValue()
    {
      return false;
    }

    @Override
    public String toString()
    {
      return ">";
    }

  }

  /**
   * Checks if a number is less than or equal to an other number.
   * @author Sylvain Hallé
   * @since 0.7
   */
  public static final class IsLessOrEqual extends BinaryFunction<Number, Number, Boolean>
  {
    private IsLessOrEqual()
    {
      super(Number.class, Number.class, Boolean.class);
    }

    @Override
    public Boolean getValue(Number x, Number y)
    {
      return x.floatValue() <= y.floatValue();
    }

    @Override
    public Boolean getStartValue()
    {
      return false;
    }

    @Override
    public String toString()
    {
      return "≤";
    }

  }

  /**
   * Checks if a number is less than an other number.
   * @author Sylvain Hallé
   * @since 0.7
   */
  public static final class IsLessThan extends BinaryFunction<Number, Number, Boolean>
  {
    protected IsLessThan()
    {
      super(Number.class, Number.class, Boolean.class);
    }

    @Override
    public Boolean getValue(Number x, Number y)
    {
      return x.floatValue() < y.floatValue();
    }

    @Override
    public Boolean getStartValue()
    {
      return false;
    }

    @Override
    public String toString()
    {
      return "<";
    }

  }

  /**
   * Returns the maximum of two numbers.
   * 
   * @author Sylvain Hallé
   * @since 0.7
   */
  public static final class Maximum extends BinaryFunction<Number, Number, Number>
  {
    protected Maximum()
    {
      super(Number.class, Number.class, Number.class);
    }

    @Override
    public Number getValue(Number x, Number y)
    {
      return Math.max(x.floatValue(), y.floatValue());
    }

    @Override
    public Number getStartValue()
    {
      return Float.MIN_VALUE;
    }

  }

  /**
   * Returns the minimum of two numbers.
   * 
   * @author Sylvain Hallé
   * @since 0.7
   */
  public static final class Minimum extends BinaryFunction<Number, Number, Number>
  {
    protected Minimum()
    {
      super(Number.class, Number.class, Number.class);
    }

    @Override
    public Number getValue(Number x, Number y)
    {
      return Math.min(x.floatValue(), y.floatValue());
    }

    @Override
    public Number getStartValue()
    {
      return Float.MAX_VALUE;
    }

  }

  /**
   * Computes the product of its arguments
   * 
   * @author Sylvain Hallé
   * @since 0.7
   */
  public static final class Multiplication extends BinaryFunction<Number, Number, Number> implements SMVInterface
  {
    protected Multiplication()
    {
      super(Number.class, Number.class, Number.class);
    }

    @Override
    public Number getValue(Number x, Number y)
    {
      return x.floatValue() * y.floatValue();
    }
    
    @Override
    protected void trackAssociations(Number x, Number y, Number z, EventTracker tracker)
    {
      if (x.floatValue() == 0)
      {
        tracker.associateToOutput(-1, 0, 0, 0, 0);
      }
      else if (y.floatValue() == 0)
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
      if (inputs[0] != null && ((Number) inputs[0]).floatValue() == 0f)
      {
        outputs[0] = 0f;
        return true;
      }
      if (inputs[1] != null && ((Number) inputs[1]).floatValue() == 0f)
      {
        outputs[0] = 0f;
        return true;
      }
      if (inputs[0] != null && inputs[1] != null)
      {
        outputs[0] = ((Number) inputs[0]).floatValue() * ((Number) inputs[1]).floatValue();
        return true;
      }
      return false;
    }

    @Override
    public Number getStartValue()
    {
      return 1f;
    }

    @Override
    public String toString()
    {
      return "×";
    }

	@Override
	public void writingSMV(PrintStream printStream, int Id, int list, int[][] array, int arrayWidth, int maxInputArity,
			String pipeType) throws IOException {
		printStream.printf("MODULE Multiplication"+Id+"(inc_1, inb_1, inc_2, inb_2, ouc_1, oub_1) \n");
		  printStream.printf("	VAR \n");
		  
		  int prec1 = array[Id][arrayWidth - maxInputArity];
		  int prec2 = array[Id][arrayWidth - maxInputArity + 1];
		  printStream.printf("		qc_1 : array 0.."+(list-1)+" of "+array[prec1][0]+".."+array[prec1][1]+"; \n");
		  printStream.printf("		qb_1 : array 0.."+(list-1)+" of boolean; \n");
		  printStream.printf("		qc_2 : array 0.."+(list-1)+" of "+array[prec2][0]+".."+array[prec2][1]+"; \n");		  
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
					  		printStream.printf("			!inb_1 & inb_2 : inc_"+i+"; \n");
					  	}
					  	if(i == 1) {
					  		printStream.printf("		TRUE : "+array[prec1][0]+"; \n");
					  	}
					  	else {
					  		printStream.printf("		TRUE : "+array[prec2][0]+"; \n");
					  	}
					  printStream.printf("		esac; \n");
				  }
				  else {
					  if( i == 1) {
						  printStream.printf("		init(qc_"+i+"["+j+"]) := "+array[prec1][0]+"; \n");
					  }
					  else {
						  printStream.printf("		init(qc_"+i+"["+j+"]) := "+array[prec2][0]+"; \n");
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
		  printStream.printf("			inb_1 & inb_2 : inc_1 * inc_2; \n");
		  printStream.printf("		TRUE : "+array[Id][0]+"; \n");
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
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] : "+array[prec1][0]+"; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] : next(inc_1); \n");
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : "+array[prec2][0]+"; \n");
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
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: "+array[prec2][0]+"; \n");
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
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : "+array[prec2][0]+"; \n");
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
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: "+array[prec2][0]+"; \n");
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
		  printStream.printf("			next(inb_1) & qb_2[0] : next(inc_1) * qc_2[0]; \n");
		  printStream.printf("			next(inb_2) & qb_1[0] : next(inc_2) * qc_1[0]; \n");
		  printStream.printf("			next(inb_1) & next(inb_2) : next(inc_1) + next(inc_2); \n");
		  printStream.printf("		TRUE : "+array[Id][0]+"; \n");
		  printStream.printf("		esac; \n");
		  printStream.printf("\n");
		
	}

	@Override
	public void writePipes(PrintStream printStream, int ProcId, int[][] connectionArray) throws IOException {
		printStream.printf("		--Multiplication \n");
		printStream.printf("		pipe_"+ProcId+" : "+ Integer.toString(connectionArray[ProcId][0]) + ".." + Integer.toString(connectionArray[ProcId][1])+ ";\n");
		printStream.printf("		b_pipe_"+ProcId+ " : boolean; \n");
		
	}

  }

  /**
   * Converts an object into a number
   * 
   * @author Sylvain Hallé
   * @since 0.7
   */
  public static final class NumberCast extends UnaryFunction<Object, Number>
  {
    protected NumberCast()
    {
      super(Object.class, Number.class);
    }

    @Override
    public Number getValue(Object x)
    {
      return getNumber(x);
    }

    @Override
    public NumberCast duplicate(boolean with_state)
    {
      return this;
    }

    /**
     * Converts an object into a number
     * 
     * @param x
     *          The object
     * @return A number
     */
    public static final Number getNumber(Object x)
    {
      if (x instanceof Number)
      {
        return (Number) x;
      }
      if (!(x instanceof String))
      {
        // Anything but a string: work on the value of toString
        x = x.toString();
      }
      if (x instanceof String)
      {
        try
        {
          return Integer.parseInt((String) x);
        }
        catch (NumberFormatException e)
        {
          try
          {
            return Float.parseFloat((String) x);
          }
          catch (NumberFormatException e2)
          {
            throw new FunctionException(e2);
          }
        }
      }
      throw new FunctionException("Object incompatible with Number");
    }
  }

  /**
   * Computes the power of its arguments
   * 
   * @author Sylvain Hallé
   * @since 0.7
   */
  public static final class Power extends BinaryFunction<Number, Number, Number>
  {
    protected Power()
    {
      super(Number.class, Number.class, Number.class);
    }

    @Override
    public Number getValue(Number x, Number y)
    {
      return Math.pow(x.floatValue(), y.floatValue());
    }

    @Override
    public Number getStartValue()
    {
      return 1f;
    }
  }

  /**
   * Computes the signum of its argument
   * 
   * @author Sylvain Hallé
   * @since 0.7
   */
  public static final class Signum extends UnaryFunction<Number, Number>
  {
    protected Signum()
    {
      super(Number.class, Number.class);
    }

    @Override
    public Number getValue(Number x)
    {
      if (x.floatValue() < 0)
      {
        return -1;
      }
      if (x.floatValue() > 0)
      {
        return 1;
      }
      return 0;
    }

    @Override
    public String toString()
    {
      return "SIG";
    }
  }

  /**
   * Computes the square root of its argument
   * 
   * @author Sylvain Hallé
   * @since 0.7
   */
  public static final class SquareRoot extends UnaryFunction<Number, Number>
  {
    protected SquareRoot()
    {
      super(Number.class, Number.class);
    }

    @Override
    public Number getValue(Number x)
    {
      return Math.sqrt(x.floatValue());
    }

    @Override
    public String toString()
    {
      return "√";
    }

  }

  /**
   * Computes the difference of its arguments
   * 
   * @author Sylvain Hallé
   * @since 0.7
   */
  public static final class Subtraction extends BinaryFunction<Number, Number, Number> implements SMVInterface
  {
    private Subtraction()
    {
      super(Number.class, Number.class, Number.class);
    }

    @Override
    public Number getValue(Number x, Number y)
    {
      return x.floatValue() - y.floatValue();
    }

    @Override
    public Number getStartValue()
    {
      return 0f;
    }

    @Override
    public String toString()
    {
      return "-";
    }

	@Override
	public void writingSMV(PrintStream printStream, int Id, int list, int[][] array, int arrayWidth, int maxInputArity,
			String pipeType) throws IOException {
		printStream.printf("MODULE Subtraction"+Id+"(inc_1, inb_1, inc_2, inb_2, ouc_1, oub_1) \n");
		  printStream.printf("	VAR \n");
		  
		  int prec1 = array[Id][arrayWidth - maxInputArity];
		  int prec2 = array[Id][arrayWidth - maxInputArity + 1];
		  printStream.printf("		qc_1 : array 0.."+(list-1)+" of "+array[prec1][0]+".."+array[prec1][1]+"; \n");
		  printStream.printf("		qb_1 : array 0.."+(list-1)+" of boolean; \n");
		  printStream.printf("		qc_2 : array 0.."+(list-1)+" of "+array[prec2][0]+".."+array[prec2][1]+"; \n");		  
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
					  		printStream.printf("			!inb_1 & inb_2 : inc_"+i+"; \n");
					  	}
					  	if(i == 1) {
					  		printStream.printf("		TRUE : "+array[prec1][0]+"; \n");
					  	}
					  	else {
					  		printStream.printf("		TRUE : "+array[prec2][0]+"; \n");
					  	}
					  printStream.printf("		esac; \n");
				  }
				  else {
					  if( i == 1) {
						  printStream.printf("		init(qc_"+i+"["+j+"]) := "+array[prec1][0]+"; \n");
					  }
					  else {
						  printStream.printf("		init(qc_"+i+"["+j+"]) := "+array[prec2][0]+"; \n");
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
		  printStream.printf("			inb_1 & inb_2 : inc_1 - inc_2; \n");
		  printStream.printf("		TRUE : "+array[Id][0]+"; \n");
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
							  printStream.printf("			next(!inb_1) & next(inb_2) & qb_"+i+"["+j+"] : "+array[prec1][0]+"; \n");
							  printStream.printf("			--Both inputs and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(inb_2) & qb_"+i+"["+j+"] : next(inc_1); \n");
						  }
						  if(i == 2) {
							  printStream.printf("			-- Only inb_1 and there is something in qb_"+i+"["+j+"]\n");
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : "+array[prec2][0]+"; \n");
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
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: "+array[prec2][0]+"; \n");
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
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] : "+array[prec2][0]+"; \n");
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
							  printStream.printf("			next(inb_1) & next(!inb_2) & qb_"+i+"["+j+"] & !qb_"+i+"["+(j+1)+"]: "+array[prec2][0]+"; \n");
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
		  printStream.printf("			next(inb_1) & qb_2[0] : next(inc_1) - qc_2[0]; \n");
		  printStream.printf("			next(inb_2) & qb_1[0] : next(inc_2) - qc_1[0]; \n");
		  printStream.printf("			next(inb_1) & next(inb_2) : next(inc_1) - next(inc_2); \n");
		  printStream.printf("		TRUE : "+array[Id][0]+"; \n");
		  printStream.printf("		esac; \n");
		  printStream.printf("\n");
		
	}

	@Override
	public void writePipes(PrintStream printStream, int ProcId, int[][] connectionArray) throws IOException {
		printStream.printf("		--Subtraction \n");
		printStream.printf("		pipe_"+ProcId+" : "+ Integer.toString(connectionArray[ProcId][0]) + ".." + Integer.toString(connectionArray[ProcId][1])+ ";\n");
		printStream.printf("		b_pipe_"+ProcId+ " : boolean; \n");
		
	}
  }
  
  /**
   * Rounds a number up to the nearest integer
   * @author Sylvain Hallé
   * @since 0.10.2
   */
  public static final class Ceiling extends UnaryFunction<Number,Number>
  {
    private Ceiling()
    {
      super(Number.class, Number.class);
    }

    @Override
    public Number getValue(Number x)
    {
      return Math.ceil(x.floatValue());
    }
    
    @Override
    public String toString()
    {
      return "CEIL";
    }
  }
  
  /**
   * Rounds a number down to the nearest integer
   * @author Sylvain Hallé
   * @since 0.10.2
   */
  public static final class Floor extends UnaryFunction<Number,Number>
  {
    private Floor()
    {
      super(Number.class, Number.class);
    }

    @Override
    public Number getValue(Number x)
    {
      return Math.floor(x.floatValue());
    }
    
    @Override
    public String toString()
    {
      return "FLOOR";
    }
  }
}
