package run;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import curation.Base;
import curation.PostProcessing;

public class RunPostProcess {
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		try {
			
			String propPath = args[0];

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
			
			PostProcessing locpp = new PostProcessing();
			locpp.set_logs("pp_out", "pp_err");
			locpp.traverse(new File(Base.source_root));
			locpp.set_logs_close();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}
	
}
