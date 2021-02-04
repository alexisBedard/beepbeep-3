package ca.uqac.lif.cep;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import ca.uqac.lif.cep.PipeConnection.Tuples;
import ca.uqac.lif.cep.tmf.QueueSource;

public class SmvFileGeneration {
	public FileWriter smvFileWriter;
	public File smvFile;
	
	private static ArrayList<String> m_Modules = new ArrayList<String>();
	protected static ArrayList<Tuples<Processor, Processor>> m_ProcessorChain;
	
	//Processor p will be the first processor of the chain.
	public SmvFileGeneration(Processor p) throws IOException {
		generateSMV("Generation");
		distribute(p);
	}

	public SmvFileGeneration(Processor p, String filename) throws IOException {
		generateSMV(filename);
		distribute(p);
	}
	
	private void distribute(Processor p) throws IOException {
		PipeConnection c = new PipeConnection(p);
		m_ProcessorChain = c.getList();
		fillModuleList();
		generateModules(m_ProcessorChain);
		
	}

	protected void generateSMV(String filename) throws IOException{
		  smvFile = new File(filename + ".smv");
			try {
					smvFileWriter = new FileWriter(smvFile);
					System.out.println("File created");
			    } 
			catch (IOException e) {
			      System.out.println("An error occurred.");
			      e.printStackTrace();
			}		
	 }
	
	protected void generateModules(ArrayList<Tuples<Processor, Processor>> list) throws IOException {
		boolean inputToBeGenerate = false;
		boolean outputToBeGenerated = false;
		/*
		 * First, we create a list of all the modules. Some don't need to be generated twice in the SMV file, 
		 * such as the doubler processor, but some others do, such as the QueueSources.
		 */
		ArrayList<Processor> processorsToGenerate = new ArrayList<Processor>();
		
			for(int i = 0; i < list.size(); i++) {
				Processor processorInputTemp = list.get(i).m_PInput;
				Processor processorOutputTemp = list.get(i).m_POutput;
				inputToBeGenerate = false;
				outputToBeGenerated = false;
				
				//Comparing the IDs
				if(processorsToGenerate.size() == 0) {
					processorsToGenerate.add(processorInputTemp);
					processorsToGenerate.add(processorOutputTemp);
				}
				else {
					for(int j = 0; j < processorsToGenerate.size(); j++) {
						if(processorInputTemp.getId() == processorsToGenerate.get(j).getId()) {
							inputToBeGenerate = checkDuplication(processorInputTemp.getShortName());
						}
						else {
							inputToBeGenerate = true;
						}
						if(processorOutputTemp.getId() == processorsToGenerate.get(j).getId()) {
							outputToBeGenerated = checkDuplication(processorInputTemp.getShortName());;
						}
						else {
							outputToBeGenerated = true;
						}
					}
					if(inputToBeGenerate) {
						processorsToGenerate.add(processorInputTemp);
					}
					if(outputToBeGenerated) {
						processorsToGenerate.add(processorOutputTemp);
					}
				}
		}
			
			//Now that we have all the modules to generates, let's generate them.
			for(int i = 0; i < processorsToGenerate.size(); i++) {
				String processorShortName = processorsToGenerate.get(i).getShortName();
				
				switch(processorShortName) {
				case "QueueSource":
					QueueSource q = (QueueSource)processorsToGenerate.get(i);
					smvFileWriter.write("MODULE QueueSource"+processorsToGenerate.get(i).getId()+"(ouc_1, oub_1) \n");
					smvFileWriter.write("	VAR \n");
					smvFileWriter.write("		source : array 0.." + Integer.toString(q.getSize()) +" of " + Integer.toString(q.getMinValue()) + ".." + Integer.toString(q.getMaxValue())+ "; \n");
					smvFileWriter.write("		cnt : 0.." + Integer.toString(q.getSize()) + "; \n");
					smvFileWriter.write("\n");
					smvFileWriter.write("	ASSIGN \n");
					smvFileWriter.write("		init(cnt) := 0; \n");
					for(int j = 0; j < q.getSize(); j++ ) {
						smvFileWriter.write("		init(source["+ j +"]) := " + q.getIntValue(j)+"; \n");
					}
					smvFileWriter.write("		init(ouc_1) := source[cnt]; \n");
					smvFileWriter.write("		init(oub_1) := TRUE; \n");
					smvFileWriter.write("\n");
					smvFileWriter.write("		next(cnt) := (cnt + 1) mod " + Integer.toString(q.getSize() + 1) +"; \n");
					
					for(int k = 0; k < q.getSize(); k++ ) {
						smvFileWriter.write("		next(source["+ k +"]) := " + q.getIntValue(k)+"; \n");
					}
					smvFileWriter.write("		next(ouc_1) := source[cnt]; \n");
					smvFileWriter.write("		next(oub_1) := TRUE; \n");
					smvFileWriter.write("\n");
					break;
				case "Doubler":
					smvFileWriter.write("MODULE Doubler"+processorsToGenerate.get(i).getId()+"(inc_1, inb_1, ouc_1, oub_1) \n");
					smvFileWriter.write("	ASSIGN \n");
					smvFileWriter.write("		init(ouc_1) := case \n");
					smvFileWriter.write("		inb_1 : inc_1 * 2; \n");
					smvFileWriter.write("		TRUE : 0; \n");
					smvFileWriter.write("	esac; \n");
					smvFileWriter.write("\n");
					smvFileWriter.write("	init(oub_1) := inb_1; \n");
					smvFileWriter.write("\n");
					smvFileWriter.write("	next(oub_1) := case \n");
					smvFileWriter.write("		next(inb_1) : next(inb_1); \n");
					smvFileWriter.write("		TRUE : FALSE; \n");
					smvFileWriter.write("	esac; \n");
					smvFileWriter.write("\n");
					smvFileWriter.write("	next(ouc_1) := case \n");
					smvFileWriter.write("		next(inb_1) : next(inc_1) * 2; \n");
					smvFileWriter.write("		TRUE : 0; \n");
					smvFileWriter.write("	esac; \n");
					smvFileWriter.write("\n");
					break;
					
				default:
					
					break;
				}
			}
			smvFileWriter.close();
	}
	
	/**
	 * This is the list of all the modules that needs to be generated more than once if needed.
	 **/
	private void fillModuleList() {
		m_Modules.add("QueueSource");
		m_Modules.add("Decimate");
	}
	
	private boolean checkDuplication(String processorShortName) {
		
		for(int i = 0; i< m_Modules.size(); i++) {
			if(processorShortName == m_Modules.get(i)) {
				return true;
			}
		}
		return false;
	}
	

	
	/*public void setIntMin(int value) {
		minInput = value;
	}
	
	public void setIntMax(int value) {
		maxInput = value;
	}*/
}