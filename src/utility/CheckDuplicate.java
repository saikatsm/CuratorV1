package utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import com.opencsv.CSVReader;

import curation.Base;
import curation.Base_Iterator;
import curation.DuplicateRemoval;
import curation.DuplicateRemoval_InFile;

public class CheckDuplicate {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		String runType = args[0];
		String propPath = args[1];
		/**
		 * Informative error reporting required. FieldList Path throwing null pointer error 
		 * incase properties file provided without qualified name.
		 * e.g., prop_file throws error instead ./prop_file. 
		 */
		if (propPath.isEmpty())
			throw new Exception("Properties File not set.");

		Properties prop = new Properties();
		File properties = new File(propPath);
		InputStream input = new FileInputStream(properties);

		prop.load(input);
		
		String sourceInput = prop.getProperty("source_root");
		String targetLocation = prop.getProperty("target_root");
		String logInput = prop.getProperty("logPath");
		String configInput = prop.getProperty("configPath");
		String fieldListPath = prop.getProperty("fieldListPath");
		String fieldListFileName = prop.getProperty("fieldListFileName");
		String reportOnly = prop.getProperty("reportOnly");
		String keepOne = prop.getProperty("keepOne");
		
		
		if (runType.equalsIgnoreCase("-i")) {
			
			Base_Iterator.source_root = sourceInput;
			Base_Iterator.target_root = targetLocation;
			Base_Iterator.logPath = logInput;
			Base_Iterator.configPath = configInput;
			
			if(Base_Iterator.target_root.isEmpty())
				throw new Exception("With InFile RunType Target Location is Mandatory. \n Terminating Program.");

				if (Base_Iterator.source_root.endsWith("/") && Base_Iterator.target_root.endsWith("/")) {
					
					DuplicateRemoval_InFile dri = new DuplicateRemoval_InFile();

					Base_Iterator.set_logs("dup_out", "dup_err");
					
					try {					
						CSVReader cr = new CSVReader(new FileReader(fieldListPath + fieldListFileName));
						for (String row[] : cr.readAll()) {
							dri.checkFields.add(row[0].trim());
						}
						cr.close();

					} catch (Exception e) {
						e.printStackTrace();
					}
					
					dri.traverse(new File(Base_Iterator.source_root));
					
					Base_Iterator.set_logs_close();

				} else {

					System.out.println("Please terminate source and target root paths with /.");
				}
			
		} else if(runType.equalsIgnoreCase("-c")) {
		
		boolean seperateFiles = false;
		
		if( reportOnly == null)
			throw new Exception ("Mandatory parameter reportOnly is not set. Exiting Program.");
		else if ( reportOnly.equalsIgnoreCase("true") )
			seperateFiles = false ; 
		else if ( reportOnly.equalsIgnoreCase("false") )
			seperateFiles = true ;
		else
			throw new Exception("Report Only value should be true/false");
		
		DuplicateRemoval dr = new DuplicateRemoval(seperateFiles);
		
		dr.source_root = sourceInput;
		dr.logPath = logInput;
		dr.configPath = configInput;
		
		if(seperateFiles&&keepOne.equals("false"))
			dr.seperateParent = true;
		
		File source = new File(dr.source_root);
		String proceedFlag = "";
		
		if (fieldListPath == null)
			fieldListPath = properties.getParent();
		
		if (!fieldListPath.endsWith("/"))
			fieldListPath += "/";
		
		if (fieldListFileName != null) {
			try {
				
				CSVReader cr = new CSVReader(new FileReader(fieldListPath + fieldListFileName));
				for (String row[] : cr.readAll())
					dr.checkFields.add(row[0].trim());
				cr.close();

				proceedFlag = "yes";

			} catch (Exception e) {

				System.out.println("Duplicate field list not found at " + fieldListPath + fieldListFileName);
				proceedFlag = "no";
			}

		} else {
			
			System.out.println("Field list not provided. Duplicate Checking shall execute based on Title Only. Do you want to proceed? (Yes/No)");
			 Scanner sc = new Scanner(System.in); 
			proceedFlag = sc.nextLine();
			sc.close();
		}
		
		if (proceedFlag.equalsIgnoreCase("yes")) {
			
			String printString = "handle|parentHandle|duplicateCount|title|";
			
			for(String fields : dr.checkFields)
				printString += fields + "|";
			printString = printString.replaceAll("\\|$", "");
			
			dr.set_logs("out_dup","err_dup");
			dr.pr.println(printString);
			
			dr.traverse(source);		
			int parentCount = 0, duplicateCount = 0;
			
			for(Map.Entry<String, ArrayList<String>> entry : dr.itemMap.entrySet()) {
				ArrayList<String> duplicateList = entry.getValue(); 
				if(duplicateList.size() > 2) {
					++parentCount;
					for(int i = 2; i < duplicateList.size(); i++) {
						
						++duplicateCount;
						dr.pr.println(duplicateList.get(i) + "|" + duplicateList.get(1) + "|" + (duplicateList.size()-1) +"|" +entry.getKey());
					}
				}
			}	
			System.out.println(duplicateCount + " duplicates found in " + parentCount + " sets.");		
			dr.set_logs_close();
			
		} else {
			
			System.out.println("Exiting Program with proceed flag " + proceedFlag);
		}
	} else {
		throw new Exception("RynType Flag is not set.\n Terminating Program.");
	}
	}
	
}
