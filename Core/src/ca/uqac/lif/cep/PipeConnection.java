package ca.uqac.lif.cep;

import java.util.HashMap;
import java.util.Map;

/**
 * Using PipeCrawler, SMVPipeConnection will help generate a SMV file with the right pipe connections.
 **/
public class SMVPipeConnection extends PipeCrawler {

	private Map<Integer, Processor> m_connections = null;
	
	public SMVPipeConnection(Processor p) {
		m_connections = new HashMap<Integer, Processor>();
		this.crawl(p);
	}
	
	@Override
	public void visit(Processor p) {
		int out_arity = p.getOutputArity();
		//System.out.println("arrity " + out_arity );
		
		for (int i = 0; i < out_arity; i++) {
			Pushable push = p.getPushableOutput(i);
			
			if (push != null){
				//System.out.println("pushable " + push );
				Processor target = push.getProcessor();
				//System.out.println("target " + target );
				int j = push.getPosition();
				//System.out.println("position " + j );
				
				//Processor new_p;
		        int new_target;
		        //new_p = m_connections.get(p.getId());
	            new_target = target.getId();
	            m_connections.put(p.getId(), target);
	            
	            System.out.println("This: " + p.getShortName() +", ID: " + p.getId() +", Target: " + target.getShortName() +", ID: " + target.getId());
	            //System.out.println("Target" + target);
			}
		}
	}

}
