package utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.iitkgp.ndl.converter.NDLSIP2CSVConverter;

import curation.Base;

public class SipToCsv {

	public static void main(String[] args) throws Exception {
		
		int threshold = 50000;
		
		String propPath = args[0];
		
		if(propPath.isEmpty())
			throw new Exception("Properties File not set.");
		 
		Properties prop = new Properties();
		File properties = new File(propPath);
		InputStream input = new FileInputStream(properties);
		
		prop.load(input);
		
		String inputFile = prop.getProperty("inputFile");
		String logLocation = prop.getProperty("logLocation");
		String csvName = prop.getProperty("csvName");
		String thresholdLimit = prop.getProperty("rowLimit");
		
		input.close();
		
		if( thresholdLimit != null )
			threshold = Integer.parseInt(args[2]);
		
		NDLSIP2CSVConverter converter = new NDLSIP2CSVConverter(inputFile, logLocation, csvName);
		converter.setCsvThresholdLimit(threshold);
		// converter.addColumnSelector("dc.contributor.other", "dc.contributor.other");
		converter.convert();
	}
}