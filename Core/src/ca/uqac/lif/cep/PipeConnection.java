package ca.uqac.lif.cep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sun.tools.javac.util.List;

/**
 * Using PipeCrawler, PipeConnection generates the piping between the processors. It contains an internal structure
 * of Tuples<Processor, Processor>, to know the input processor and the output processor. For now, this List is only used 
 * in the SmvFileGeneration class.
 **/
public class PipeConnection extends PipeCrawler {
	
	protected ArrayList<Tuples<Processor, Integer, Processor, Integer>> m_processorList = new ArrayList<Tuples<Processor, Integer, Processor, Integer>>();
	protected Tuples<Processor, Integer, Processor, Integer> tuples;
	
	protected class Tuples<ProcessorIn, ArityIn, ProcessorOut, ArityOut>{
		protected Processor m_PInput;
		protected Processor m_POutput;
		protected int m_arityIn;
		protected int m_arityOut;
		
		protected Tuples(Processor input, int arityIn, Processor output, int arityOut) {
			m_PInput = input;
			m_POutput = output;
			m_arityIn = arityIn;
			m_arityOut = arityOut;
		}
	}
	
	public PipeConnection(Processor p) {
		this.crawl(p);
	}
	
	public ArrayList<Tuples<Processor, Integer, Processor, Integer>> getList(){
		return m_processorList;
	}
	
	@Override
	public void visit(Processor p) {
		int out_arity = p.getOutputArity();
		
		for (int i = 0; i < out_arity; i++) {
			Pushable push = p.getPushableOutput(i);
	        // int j = push.getPosition();
			
			if (push != null){
				Processor target = push.getProcessor();
				int j = push.getPosition();
		        tuples = new Tuples<Processor,Integer, Processor, Integer>(p, i, target, j);
		        m_processorList.add(tuples);
			}
		}
	}

}
