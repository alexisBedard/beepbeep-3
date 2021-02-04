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
	
	protected ArrayList<Tuples<Processor, Processor>> m_processorList = new ArrayList<Tuples<Processor, Processor>>();
	protected Tuples<Processor, Processor> tuples;
	
	protected class Tuples<ProcessorIn, ProcessorOut>{
		protected  Processor m_PInput;
		protected  Processor m_POutput;
		
		protected Tuples(Processor input, Processor output) {
			m_PInput = input;
			m_POutput = output;
		}
	}
	
	public PipeConnection(Processor p) {
		this.crawl(p);
	}
	
	public ArrayList<Tuples<Processor, Processor>> getList(){
		return m_processorList;
	}
	
	@Override
	public void visit(Processor p) {
		int out_arity = p.getOutputArity();
		
		for (int i = 0; i < out_arity; i++) {
			Pushable push = p.getPushableOutput(i);
			
			if (push != null){
				Processor target = push.getProcessor();
		        tuples = new Tuples<Processor, Processor>(p, target);
		        m_processorList.add(tuples);
			}
		}
	}

}
