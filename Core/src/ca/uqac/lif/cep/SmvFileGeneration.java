package ca.uqac.lif.cep;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import ca.uqac.lif.cep.PipeConnection.Tuples;
import ca.uqac.lif.cep.tmf.CountDecimate;
import ca.uqac.lif.cep.tmf.QueueSource;

public class SmvFileGeneration {
	protected FileWriter smvFileWriter;
	protected File smvFile;
	
	private int m_WaitingList;
	
	protected static ArrayList<String> m_Modules = new ArrayList<String>();
	protected static ArrayList<Integer> m_SourceID = new ArrayList<Integer>();
	protected static ArrayList<Tuples<Processor, Integer, Processor, Integer>> m_ProcessorChain;
	protected static ArrayList<String> m_Functions = new ArrayList<String>();
	
	protected int arrayWidth = 0;
	protected int maxOutputArrity = 0;
	protected int maxInputArrity = 0;
	
	
	//Processor p will be the first processor of the chain.
	public SmvFileGeneration(int waitingList) throws IOException {
		m_WaitingList = waitingList;
	}
	
	private void distribute(Processor p) throws IOException {
		PipeConnection c = new PipeConnection(p);
		m_ProcessorChain = c.getList();
		fillModuleList();
		generateModules(m_ProcessorChain);
		generateMain();	
		
	}

	public void generateSMV(Processor p, String filename) throws IOException{
		  smvFile = new File(filename + ".smv");
			try {
					smvFileWriter = new FileWriter(smvFile);
					System.out.println("File created");
			    } 
			catch (IOException e) {
			      System.out.println("An error occurred.");
			      e.printStackTrace();
			}		
			
			distribute(p);
	 }
	
	protected void generateModules(ArrayList<Tuples<Processor, Integer, Processor, Integer>> list) throws IOException {
		boolean inputToBeGenerated = false;
		boolean outputToBeGenerated = false;
		
		/*
		 * First, we create a list of all the modules. Some don't need to be generated twice in the SMV file, 
		 * such as the doubler processor, but some others do, such as the QueueSources.
		 */
		ArrayList<Processor> processorsToGenerate = new ArrayList<Processor>();
		
			for(int i = 0; i < list.size(); i++) {
				Processor processorInputTemp = list.get(i).m_PInput;
				Processor processorOutputTemp = list.get(i).m_POutput;
				
				inputToBeGenerated = true;
				outputToBeGenerated = true;
				
				//We first add both processors
				if(processorsToGenerate.size() == 0) {
					processorsToGenerate.add(processorInputTemp);
					processorsToGenerate.add(processorOutputTemp);
				}
				//We then check if
				else {
					for(int j = 0; j < processorsToGenerate.size(); j++) {
						if(processorInputTemp.getId() == processorsToGenerate.get(j).getId()) {
							inputToBeGenerated = false;
						}
						if(processorOutputTemp.getId() == processorsToGenerate.get(j).getId()) {
							//outputToBeGenerated = checkDuplication(processorOutputTemp.getShortName());
							outputToBeGenerated = false;
						}
					}
					
					if(inputToBeGenerated) {
						for(int k = 0; k < processorsToGenerate.size(); k++) {
							if(processorInputTemp.getShortName().equals(processorsToGenerate.get(k).getShortName())) {
								inputToBeGenerated = checkDuplication(processorInputTemp.getShortName());
							}
						}
						if(inputToBeGenerated) {
							processorsToGenerate.add(processorInputTemp);
						}
					}
					if(outputToBeGenerated) {
						for(int k = 0; k < processorsToGenerate.size(); k++) {
							if(processorOutputTemp.getShortName().equals(processorsToGenerate.get(k).getShortName())) {
								outputToBeGenerated = checkDuplication(processorOutputTemp.getShortName());
							}
						}
						if(outputToBeGenerated) {
							processorsToGenerate.add(processorOutputTemp);
						}
					}
			}
		}
			
			//Now that we have all the modules to generates, let's generate them.
			for(int i = 0; i < processorsToGenerate.size(); i++) {
				String processorShortName = processorsToGenerate.get(i).getShortName();
				
				switch(processorShortName) {
				case "QueueSource":
					QueueSource queueSource = (QueueSource)processorsToGenerate.get(i);
					queueSource.writingSMV(smvFileWriter, processorsToGenerate.get(i).getId(), 0);
					m_SourceID.add(processorsToGenerate.get(i).getId());
					break;
					
				case "Doubler":
					Doubler doubler = (Doubler)processorsToGenerate.get(i);
					doubler.writingSMV(smvFileWriter, processorsToGenerate.get(i).getId(), 0);
					break;
					
				case "CountDecimate":
					CountDecimate countDecimate = (CountDecimate)processorsToGenerate.get(i);
					countDecimate.writingSMV(smvFileWriter, processorsToGenerate.get(i).getId(), 0);
					break;
					
				case "Adder":
					Adder add = (Adder)processorsToGenerate.get(i);
					add.writingSMV(smvFileWriter, processorsToGenerate.get(i).getId(), m_WaitingList);
					break;
				default:
					System.out.println(processorsToGenerate.get(i).getShortName()+": This module is not supported at the moment");
					break;
				}
			}
	}
	
	/**
	 * This is the list of all the modules that needs to be generated more than once if needed.
	 **/
	private void fillModuleList() {
		m_Modules.add("QueueSource");
		m_Modules.add("CountDecimate");
		m_Modules.add("Adder");
	}
	
	private boolean checkDuplication(String processorShortName) {
		
		for(int i = 0; i< m_Modules.size(); i++) {
			if(processorShortName.equals( m_Modules.get(i))) {
				return true;
			}
		}
		return false;
	}
	
	private void generateMain() throws IOException {
		ArrayList<Integer> modulesID = new ArrayList<Integer>();
		String s;
		
		int arrayHeight = 0;
		boolean inputIsPresent = false;
		boolean outputIsPresent = false;
		//Construction of an array that will help find the pipes values
			for(int i = 0; i < m_ProcessorChain.size(); i++) {
				int inputID = m_ProcessorChain.get(i).m_PInput.getId();
				int outputID = m_ProcessorChain.get(i).m_POutput.getId();
				for(int j = 0; j < modulesID.size(); j++) {
					if(inputID == modulesID.get(j)) {
						inputIsPresent = true;
					}
					if(outputID == modulesID.get(j)) {
						outputIsPresent = true;
					}
				}
				if(!inputIsPresent) {
					modulesID.add(inputID);
					arrayHeight += m_ProcessorChain.get(i).m_PInput.getOutputArity();
					if(m_ProcessorChain.get(i).m_PInput.getOutputArity() > maxOutputArrity) {
						maxOutputArrity = m_ProcessorChain.get(i).m_PInput.getOutputArity();
					}
					if(m_ProcessorChain.get(i).m_PInput.getInputArity() > maxInputArrity) {
						maxInputArrity = m_ProcessorChain.get(i).m_PInput.getInputArity();
					}
				}
				if(!outputIsPresent) {
					modulesID.add(outputID);
					arrayHeight += m_ProcessorChain.get(i).m_POutput.getOutputArity();
					if(m_ProcessorChain.get(i).m_POutput.getOutputArity() > maxOutputArrity) {
						maxOutputArrity = m_ProcessorChain.get(i).m_POutput.getOutputArity();
					}
					if(m_ProcessorChain.get(i).m_POutput.getInputArity() > maxInputArrity) {
						maxInputArrity = m_ProcessorChain.get(i).m_POutput.getInputArity();
					}
				}
				inputIsPresent = false;
				outputIsPresent = false;
			}
		/**
		 * 
		 * 
		 * 
		 * 
		 * 
		*/
			
		//The array will store pipe values and all the connections between the processors.
		int numArrity = (maxOutputArrity -1)+(maxInputArrity-1);
		 arrayWidth = 4 + numArrity;
		int[][] connectionArray = new int[arrayHeight][arrayWidth];
		for(int i = 0; i < arrayHeight; i++) {
			for(int j = 2; j < arrayWidth; j++) {
				connectionArray[i][j] = -1;
			}
		}
		smvFileWriter.write("MODULE main \n");
		smvFileWriter.write("	VAR \n");
		
		boolean canBeGenerated = false;
		int id = 0;
		for(int i = 0; i < m_SourceID.size(); i++){
			for(int j = 0 ; j < m_ProcessorChain.size(); j++) {
				canBeGenerated = false;
				//As we are looking for QueueSources, we only have to check for m_PInput
				if(m_ProcessorChain.get(j).m_PInput.getId() == m_SourceID.get(i)) {
					canBeGenerated = true;
					id = j;
					break;
				}
			}
			while(canBeGenerated) {
				//Next processors of m_PInput
				for(int k = 2; k < (2 + m_ProcessorChain.get(id).m_PInput.m_outputArity); k++) {
					connectionArray[m_ProcessorChain.get(id).m_PInput.getId()][k] = m_ProcessorChain.get(id).m_POutput.getId();
				}
				
				//m_PInput is one of possibly many m_POutput's input 
				int cell = (arrayWidth - maxInputArrity) + m_ProcessorChain.get(id).m_arityOut;
				connectionArray[m_ProcessorChain.get(id).m_POutput.getId()][cell] = m_ProcessorChain.get(id).m_PInput.getId();
				
				//Storing and writing the values of m_PInput
				s = m_ProcessorChain.get(id).m_PInput.getShortName();
				generateValues(connectionArray, s, id, m_ProcessorChain.get(id).m_PInput.getId());
				
				//If all inputs have been generated, output can be.
				for(int input = 0; input < m_ProcessorChain.get(id).m_POutput.m_inputArity; input++) {
					if(connectionArray[m_ProcessorChain.get(id).m_POutput.getId()][(arrayWidth - maxInputArrity) + input] == -1) {
						canBeGenerated = false;
					}
				}
				if(canBeGenerated){
					
					if(m_ProcessorChain.get(id).m_POutput.getPushableOutput(0) == null) {
						//Storing and writing the values of m_POutput
						s = m_ProcessorChain.get(id).m_POutput.getShortName();
						smvFileWriter.write("		--OUTPUT \n");
						generateValues(connectionArray, s, id, m_ProcessorChain.get(id).m_POutput.getId());
						//This is the last processor
						canBeGenerated = false;
					}
					else {
						for(int k = 0 ; k < m_ProcessorChain.size(); k++) {
							if(m_ProcessorChain.get(id).m_POutput.getId() == m_ProcessorChain.get(k).m_PInput.getId()) {
								id = k;
								break;
							}
						}
					}
				}
			}
		}
		smvFileWriter.write("\n");
		
		//Writing the functions
		for(int i = 0 ; i < m_Functions.size(); i++) {
			smvFileWriter.write("		"+ m_Functions.get(i));
		}
		
		//Closing the file
		smvFileWriter.close();	
	}

	private void generateValues(int[][] connectionArray, String s, int id, int ProcId) throws IOException {
		int prec1;
		int prec2;
		switch(s) {
		case "QueueSource" :
			smvFileWriter.write("		--QueueSource \n");
			QueueSource q = (QueueSource)m_ProcessorChain.get(id).m_PInput;
			smvFileWriter.write("		pipe_"+ProcId+" : ");
			if(q.getMinValue() >= 0) {
				connectionArray[ProcId][0] = 0;
			}
			else {
				connectionArray[ProcId][0] = q.getMinValue();
			}
			connectionArray[ProcId][1] = q.getMaxValue();
			smvFileWriter.write(Integer.toString(connectionArray[ProcId][0]) + ".." + Integer.toString(connectionArray[ProcId][1])+ "; \n");
			m_Functions.add("queueSource"+ProcId+" : QueueSource"+ProcId+"(pipe_"+ProcId+", b_pipe_"+ProcId+"); \n");
			break;
		case "Doubler":
			prec1 = connectionArray[ProcId][arrayWidth - maxInputArrity];
			smvFileWriter.write("		--Doubler\n");
			//new Minimum value
			connectionArray[ProcId][0] = (connectionArray[prec1][0])*2;
			//new Maximum value
			connectionArray[ProcId][1] = (connectionArray[prec1][1])*2;
			smvFileWriter.write("		pipe_"+ProcId+" : "+ Integer.toString(connectionArray[ProcId][0]) + ".." + Integer.toString(connectionArray[ProcId][1])+ ";\n");
			m_Functions.add("doubler"+ProcId+" : Doubler(pipe_"+prec1+", b_pipe_"+prec1+", pipe_"+ProcId+", b_pipe_"+ProcId+"); \n");
			break;
		case "CountDecimate":
			
			smvFileWriter.write("		--CountDecimate \n");
			//new Minimum value
			prec1 = connectionArray[ProcId][arrayWidth - maxInputArrity];
			connectionArray[ProcId][0] = (connectionArray[prec1][0]);
			//new Maximum value
			connectionArray[ProcId][1] = (connectionArray[prec1][1]);
			smvFileWriter.write("		pipe_"+ProcId+" : "+ Integer.toString(connectionArray[ProcId][0]) + ".." + Integer.toString(connectionArray[ProcId][1])+ ";\n");
			m_Functions.add("decimate"+ProcId+" : CountDecimate"+ProcId+"(pipe_"+prec1+", b_pipe_"+prec1+", pipe_"+ProcId+", b_pipe_"+ProcId+"); \n");
			break;
		case "Adder":
			smvFileWriter.write("		--Adder \n");
				//new Minimum value
				prec1 = connectionArray[ProcId][arrayWidth - maxInputArrity];
				prec2 = connectionArray[ProcId][arrayWidth - maxInputArrity + 1];
				connectionArray[ProcId][0] = (connectionArray[prec1][0] + connectionArray[prec2][0]);
				//new Maximum value
				connectionArray[ProcId][1] = (connectionArray[prec1][1] + connectionArray[prec2][1]);
				smvFileWriter.write("		pipe_"+ProcId+" : "+ Integer.toString(connectionArray[ProcId][0]) + ".." + Integer.toString(connectionArray[ProcId][1])+ ";\n");
				m_Functions.add("adder"+ProcId+" : Adder"+ProcId+" (pipe_"+prec1+", b_pipe_"+prec1+", pipe_"+prec2+", b_pipe_"+prec2 +", pipe_"+ProcId+", b_pipe_"+ProcId+"); \n");
				break;
			//}
		default:
			break;
		}
		smvFileWriter.write("		b_pipe_"+ProcId+ " : boolean; \n");
	}

};