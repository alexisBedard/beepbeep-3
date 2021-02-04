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
		generateMain();
		
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
					q.writingSMV(smvFileWriter, processorsToGenerate.get(i).getId());
					break;
					
				case "Doubler":
					Doubler d = (Doubler)processorsToGenerate.get(i);
					d.writingSMV(smvFileWriter, processorsToGenerate.get(i).getId());
					break;
					
				default:
					System.out.println(processorsToGenerate.get(i).getShortName()+": This module is not supported at the moment");
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
	
	private void generateMain() {
		// TODO Auto-generated method stub
		
	}

}