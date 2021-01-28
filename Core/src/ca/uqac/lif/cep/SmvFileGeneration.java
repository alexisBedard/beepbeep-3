package ca.uqac.lif.cep;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SmvFileGeneration {
	public FileWriter smvFileWriter;
	public File smvFile;
	
	protected static int minInput = 1;
	protected static int maxInput = 1;
	protected static int minOutput = 1;
	protected static int maxOutput = 1;
	protected static int numOfModules = 0;
	
	public void generateSMV() throws IOException{
		  smvFile = new File("Generation.smv");
			if(smvFile.exists()){
				System.out.println("Succesfully created file"); 
			}
			else {
		      System.out.println("File already exists.");
		  }
			try {
					smvFileWriter = new FileWriter(smvFile);
			    } 
			catch (IOException e) {
			      System.out.println("An error occurred.");
			      e.printStackTrace();
			}		
	 }
	
	public void generateMain(String ... args) throws IOException{
		smvFileWriter.write("MODULE main \n");
		smvFileWriter.write("	VAR \n");
		
		//Variable initialization
		for (int i = 1; i <= args.length; i++) {
			  String s = args[i-1];

			  switch (s) {
			  case "doubler" :
				  numOfModules += 1;
				  minOutput = minInput * 2;
				  maxOutput = maxInput * 2;
				  
					  if(numOfModules == 1) {
						  smvFileWriter.write("		pipe_"+ Integer.toString(i) +": 0" + /*Integer.toString(minInput)*/ ".." + Integer.toString(maxInput) + "; \n");
						  smvFileWriter.write("		b_pipe_"+ Integer.toString(i) +": boolean; \n");
						  smvFileWriter.write("		pipe_"+ Integer.toString(i+1) +": 0" + /*Integer.toString(minOutput)*/ ".." + Integer.toString(maxOutput) + "; \n");
						  smvFileWriter.write("		b_pipe_"+ Integer.toString(i+1) +": boolean; \n");
						  smvFileWriter.write("		double"+Integer.toString(numOfModules)+" : Doubler(pipe_"+Integer.toString(i)+", b_pipe_"+Integer.toString(i)+", pipe_"+Integer.toString(i+1)+", b_pipe_"+Integer.toString(i+1)+"); \n");  
					  }
					  else {
						  smvFileWriter.write("		pipe_"+ Integer.toString(i+1) +": 0" /*+ Integer.toString(minOutput)*/ + ".." + Integer.toString(maxOutput) + "; \n");
						  smvFileWriter.write("		b_pipe_"+ Integer.toString(i+1) +": boolean; \n");
						  smvFileWriter.write("		double"+Integer.toString(numOfModules)+" : Doubler(pipe_"+Integer.toString(i)+", b_pipe_"+Integer.toString(i)+", pipe_"+Integer.toString(i+1)+", b_pipe_"+Integer.toString(i+1)+"); \n");  
					  }
					minInput = minOutput;
					maxInput = maxOutput;
					smvFileWriter.write("\n");
			  }
		  }
		
		//Assign values to variables
		smvFileWriter.write("	ASSIGN \n");
		//for (int j = 1; j <= args.length; j++) {
			String s = args[0];

			  switch (s) {
			  case "doubler" :
				  smvFileWriter.write("		init(pipe_1) := 0; \n");
				  smvFileWriter.write("		init(b_pipe_1) := FALSE; \n");
				  smvFileWriter.write("		next(pipe_1) := (pipe_1 + 1) mod 7; \n");
				  smvFileWriter.write("		next(b_pipe_1) := TRUE; \n");
		}
		
		//close the file
		smvFileWriter.close();
	  }
	
	public void setIntMin(int value) {
		minInput = value;
	}
	
	public void setIntMax(int value) {
		maxInput = value;
	}
}