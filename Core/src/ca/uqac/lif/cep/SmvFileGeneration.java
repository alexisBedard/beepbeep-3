package ca.uqac.lif.cep;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import ca.uqac.lif.cep.tmf.QueueSource;

public class SmvFileGeneration {
	public FileWriter smvFileWriter;
	public File smvFile;
	
	//Used for the domain of our integer variables.
	protected static int minInput = 1;
	protected static int maxInput = 1;
	protected static int minOutput = 1;
	protected static int maxOutput = 1;
	
	//Counts the number of modules needed. Useful if, by example, two Doubler processor is needed therefore no variable will
	//be named similarly.
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
	
	public void generateQueueSource(QueueSource ... args) throws IOException {
		QueueSource mySource = args[0];
		int a = mySource.getSize();
		//System.out.println(mySource.getOutputType(0));
		//Comment savoir si le output est un Integer ou un boolean?
		//if(mySource.getOutputType(0) == (int)mySource.getOutputType(0));
		
		//for(int i = 0; i < mySource.getSize(); i++) {
			smvFileWriter.write("MODULE QueueSource(ouc_1, oub_1) \n");
			smvFileWriter.write("	VAR \n");
			smvFileWriter.write("		source : array 0.." + Integer.toString(mySource.getSize()) +" of " + mySource.getMinValue() + ".." + mySource.getMaxValue()+ "; \n");
			smvFileWriter.write("		cnt : 0.." + Integer.toString(mySource.getSize()) + "; \n");
			smvFileWriter.write("\n");
			smvFileWriter.write("	ASSIGN \n");
			smvFileWriter.write("		init(cnt) := 0; \n");
			for(int j = 0; j < mySource.getSize(); j++ ) {
				smvFileWriter.write("		init(source[" + j + "]) := " + mySource.getIntValue(j)+"; \n");
			}
			smvFileWriter.write("		init(ouc_1) := source[cnt]; \n");
			smvFileWriter.write("		init(oub_1) := TRUE; \n");
			smvFileWriter.write("\n");
			smvFileWriter.write("		next(cnt) := (cnt + 1) mod " + Integer.toString(mySource.getSize() + 1) +"; \n");
			
			for(int k = 0; k < mySource.getSize(); k++ ) {
				smvFileWriter.write("		next(source[" + k + "]) := " + mySource.getIntValue(k)+"; \n");
			}
			smvFileWriter.write("		next(ouc_1) := source[cnt]; \n");
			smvFileWriter.write("		next(oub_1) := TRUE; \n");
			smvFileWriter.write("\n");
			
		//}
	}
	
	public void generateMain(String ... args) throws IOException{
		smvFileWriter.write("MODULE main \n");
		smvFileWriter.write("	VAR \n");
		
		//VAR section
		for (int i = 1; i <= args.length; i++) {
			  String s = args[i-1];
			  s.toLowerCase();

			  switch (s) {
			  case "doubler" :
				  numOfModules += 1;
				  minOutput = minInput * 2;
				  maxOutput = maxInput * 2;
				  
					  if(numOfModules == 1) {
							if(minInput > 0) {
								smvFileWriter.write("		pipe_"+ Integer.toString(i) + ": 0.."+ Integer.toString(maxInput) + "; \n");
							}
							else {
								smvFileWriter.write("		pipe_"+ Integer.toString(i) + ": "+ Integer.toString(minInput) + ".." + Integer.toString(maxInput) + "; \n");
							}
								  
								  
							smvFileWriter.write("		b_pipe_"+ Integer.toString(i) +": boolean; \n");
							if(minInput > 0) {
								smvFileWriter.write("		pipe_"+ Integer.toString(i+1) + ": 0.."+ Integer.toString(maxOutput) + "; \n");
							}
							else {
								smvFileWriter.write("		pipe_"+ Integer.toString(i+1) +  ": "+ Integer.toString(minOutput) + ".." + Integer.toString(maxOutput) + "; \n");
							}
							
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
					break;
					
				default:
					System.out.println(s + " is not a module");
			  }
		  }
		
		//ASSIGN section
		smvFileWriter.write("	ASSIGN \n");
			String s = args[0];

			  switch (s) {
			  case "doubler" :
				  smvFileWriter.write("		init(pipe_1) := 0; \n");
				  smvFileWriter.write("		init(b_pipe_1) := FALSE; \n");
				  smvFileWriter.write("		next(pipe_1) := (pipe_1 + 1) mod 7; \n");
				  smvFileWriter.write("		next(b_pipe_1) := TRUE; \n");
		}
		
		//closing the file
		smvFileWriter.close();
	  }
	
	public void setIntMin(int value) {
		minInput = value;
	}
	
	public void setIntMax(int value) {
		maxInput = value;
	}
}