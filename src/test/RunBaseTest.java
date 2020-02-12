/**
 * runBase :
 * This is the wrapper method to run Base curation procedure. The Base curation class variables are 
 * set through this wrapper. This method is not safe i.e., Base variables can be modified using
 * the wrapper. The wrapper only implements main and within main it calls 
 * 1. Base.set_logs(String logFileName, String errFileName): to set log files at the path
 * 	  defined in Base.logPath. 
 * 2. Base.traverse(File input): traverse through the source defined by the variable Base.source_root.
 * 3. Base.set_logs_close(): Close log files create using set_logs method.
 */
package test;

import java.io.File;

import curation.Base;
import utility.ValidateMappingFile;

public class RunBaseTest {

	public static void main(String[] args){
		// TODO Auto-generated method stub

		try {

			Base.source_root = "/home/arunavo/Desktop/data/Shodhganga/sample/";
			Base.target_root = "/home/arunavo/Desktop/data/Shodhganga/sampleTrans/";
			Base.logPath = "/home/arunavo/Desktop/data/Shodhganga/log/";
			Base.configPath = "/home/arunavo/Desktop/data/Shodhganga/configFiles/";

			if (Base.source_root.endsWith("/") && Base.target_root.endsWith("/")) {
				
				Base base = new Base();
				base.set_logs("out", "err");

				// bc.range = 100; bc.norangeSet = false;
				/*
				 * BufferedReader br = new BufferedReader(new FileReader("/home/arunavo/Desktop/data/Shodhganga/Inflibnet-Shodhganga-mapped-SIP/col_lists2"));
				 * String coll_name = ""; 
				 * while ((coll_name = br.readLine()) != null ) {
				 * bc.traverse(new File(source_root+coll_name)); 
				 * } 
				 * br.close();
				 */
				
				base.traverse(new File(Base.source_root));
				base.set_logs_close();

			} else {
				
				System.out.println("Please terminate source and target root paths with /.");
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
	
}
