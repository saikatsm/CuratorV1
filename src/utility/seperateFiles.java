package utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import com.opencsv.CSVReader;

public class seperateFiles {

	private static String handle = "";
	private static File target_noise = null, target_clean = null;
	private static File splitRoot = null, splitFiles = null;
	private static int count = -1, count_per_set = 0, handleIndex = 1;
	private static ArrayList<String> handleList = new ArrayList<String>();

	public static void traversebyReport(File input) throws Exception {
		try {
			for (File source : input.listFiles()) {
				if (source.isDirectory()) {
					traversebyReport(source);
				} else if (source.getName().equals("handle")) {
					System.out.println("Processing Item : " + ++count);
					BufferedReader br = new BufferedReader(new FileReader(source));
					handle = br.readLine();
					br.close();
					if (handleList.contains(handle)) {
						File target = new File(target_noise+"/"+input.getName()+"/");
						target.mkdirs();
						FileUtils.copyDirectory(input, target);
					}
					else {
						File target = new File(target_clean+"/"+input.getName()+"/");
						target.mkdirs();
						FileUtils.copyDirectory(input, target);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void traversebyCount(File input) throws Exception {
		try {
			for (File source : input.listFiles()) {
				if (source.isDirectory()) {
					traversebyCount(source);
				} else if (source.getName().equals("handle")) {
					System.out.println("Processing Item : " + ++count);
					if(count%count_per_set != 0) {
						File target = new File(splitFiles+"/"+input.getName()+"/");
						target.mkdirs();
						FileUtils.copyDirectory(input, target);
					}
					else {
						int index = count/count_per_set;
						splitFiles = new File(splitRoot.getAbsolutePath() + "_" + index+"/");
						splitFiles.mkdirs();
						File target = new File(splitFiles+"/"+input.getName()+"/");
						target.mkdirs();
						FileUtils.copyDirectory(input, target);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void loadReport(File reportFile) throws Exception {

		CSVReader report = new CSVReader(new FileReader(reportFile), '\t');
		for (String[] row : report.readAll()) {
			String handle = row[handleIndex].trim();
			if (!handleList.contains(handle)) {
				handleList.add(handle);
			}
		}
		report.close();
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length < 2)
			throw new Exception("Wrong Input Argument. \n"
					+ "Command Syntax : java -jar seperateFiles.jar -r[eport]/-c[ount]"
					+ "<ConfigFilePath>");
		String runType = args[0];
		String propPath = args[1];
		Properties prop = new Properties();
		File properties = new File(propPath);
		InputStream input = new FileInputStream(properties);
		prop.load(input);
		File source = new File(prop.getProperty("source_root"));
		File target_root = new File(prop.getProperty("target_root"));
		if(runType.equalsIgnoreCase("-r")) {
		File reportFile = new File(prop.getProperty("reportFile"));
		handleIndex = Integer.parseInt(prop.getProperty("handleIndex"));
		target_clean = new File(target_root+"/"+source.getName()+"/clean_data/");
		target_clean.mkdirs();
		target_noise = new File(target_root+"/"+source.getName()+"/noise_data/");
		target_noise.mkdirs();
		loadReport(reportFile);
		traversebyReport(source);
		} else if(runType.equalsIgnoreCase("-c")) {
			count_per_set = Integer.parseInt(prop.getProperty("count_per_set"));
			splitRoot = new File(target_root+"/"+source.getName());
			traversebyCount(source);
		}

	}

}
