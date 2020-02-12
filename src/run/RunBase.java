/**
 * runBase :
 * This is the wrapper method to run Base curation procedure(curation.Base class). The Base curation class variables are 
 * set through a config.properties file. This method is not safe i.e., Base variables can be modified using
 * the wrapper. The wrapper only implements main and within main it calls 
 * 1. Base.set_logs(String logFileName, String errFileName): to set log files at the path
 * 	  defined in Base.logPath. 
 * 2. Base.traverse(File input): traverse through the source defined by the variable Base.source_root.
 * 3. Base.set_logs_close(): Close log files create using set_logs method.
 * 
 * ******IMP*************
 * Pending work : If target path exists delete any existing file/take backup interactively and then run program.
 */
package run;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import curation.Base;

public class RunBase {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {

			Base.runType = args[0];
			String propPath = args[1];

			if (propPath.isEmpty())
				throw new Exception("Properties File not set.");

			Properties prop = new Properties();
			File properties = new File(propPath);
			InputStream input = new FileInputStream(properties);

			prop.load(input);
 
			Base.source_root = prop.getProperty("source_root");
			Base.target_root = prop.getProperty("target_root");
			Base.logPath = prop.getProperty("logPath");
			Base.configPath = prop.getProperty("configPath");
			Base.schemaActionFile = prop.getProperty("logicFile");
			String isSampleRun = prop.getProperty("isSampleRun");
			String sampleRange = prop.getProperty("sampleRange");

			input.close();

			if (Base.source_root.endsWith("/") && Base.target_root.endsWith("/")) {

				Base.set_logs("out", "err");
				Base base = new Base();

				if (isSampleRun != null) {
					if (isSampleRun.equalsIgnoreCase("true")) {
						if (sampleRange != null) {
							try {
								base.range = Integer.parseInt(sampleRange);
								base.norangeSet = false;

							} catch (Exception e) {

								throw new Exception("Sample run detected. Please set numeric values for Sample Range.");
							}

						} else {

							throw new Exception("Sample run detected. Please set sampleRange parameter.");

						}

					} else

						throw new Exception("Sample run detected. Please check paremeter values.");
				}
				/*
				 * BufferedReader br = new BufferedReader(new FileReader(
				 * "/home/arunavo/Desktop/data/Shodhganga/Inflibnet-Shodhganga-mapped-SIP/col_lists2"
				 * )); String coll_name = ""; while ((coll_name = br.readLine()) != null ) {
				 * bc.traverse(new File(source_root+coll_name)); } br.close();
				 */
				
				base.traverse(new File(Base.source_root),false);
				Base.set_logs_close();

			} else {

				System.out.println("Please terminate source and target root paths with /.");
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

}
