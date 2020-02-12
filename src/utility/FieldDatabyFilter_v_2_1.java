package utility;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.opencsv.CSVReader;

//import curation.Base;

public class FieldDatabyFilter_v_2_1 {

	String handle = "", source_root = "", logFilePath = "",
			fieldName="",  runType = "", lftokenizer = "",rttokenizer = "",ic="";
	static String listPath = "", dictPath = "", expression = "", processFile = "";
	int count = 0, totalFieldinstances = 0;
	boolean useTokenizer = true;
	PrintStream pr = null;
	ArrayList<String[]> exceptionList = new ArrayList<String[]>();
	ArrayList<String[]> exceptionDict = new ArrayList<String[]>();

	public static Properties allExceptionHandle(String args[]) throws ArithmeticException, NullPointerException,FileNotFoundException, ArrayIndexOutOfBoundsException,Exception{
		if(args.length !=2)
			throw new ArithmeticException("Wrong Input Argument. Command Syntax :"
					+ " java -jar FieldDatabyFilter.jar -l[ist]/-e[xpression]/-d[dictionary]"
					+ " <ConfigFilePath>") ;
		if(args[0].isEmpty()|args[1].isEmpty()) {
			throw new NullPointerException("Please provide valid data");
		}
		String runType = args[0];

		if(!runType.matches("(?i)-l|-e|-d"))
			throw new Exception("Wrong Runtype. Runtype in -l[ist]/-e[xpression].");
		String propPath = args[1];
		if (propPath.isEmpty())
			throw new FileNotFoundException("Properties File not given. Please in put properties file path.");
		Properties prop = new Properties();
		File properties = new File(propPath);
		InputStream input = new FileInputStream(properties);
		prop.load(input);
		if (runType.equalsIgnoreCase("-e")) {
			if(prop.getProperty("expression") != null)
				expression=prop.getProperty("expression");
			else
				throw new Exception("Invalid Argument. -e[xpression] option to "
						+ "be provided with regular expression in config File expression "
						+ "parameter.");
		}
		else if (runType.equalsIgnoreCase("-l")) {
			if(prop.getProperty("listFile") != null)
				listPath=prop.getProperty("listFile");
			else
				throw new Exception("Invalid Argument. -l[ist] option to "
						+ "be provided with list file Path in config File listFile parameter.");
		}
		else if (runType.equalsIgnoreCase("-d")) {
			if(prop.getProperty("historyFile") != null)
				dictPath=prop.getProperty("historyFile");
			else
				throw new Exception("Invalid Argument. -d[ictionary] option to "
						+ "be provided with history file Path in config File historyFile "
						+ "parameter.");
		}
		return prop;
	}
	public FieldDatabyFilter_v_2_1(String runType) {
		// TODO Auto-generated constructor stub
		this.runType = runType;
		try {
			if(runType.equalsIgnoreCase("-l")) {
				CSVReader cr = new com.opencsv.CSVReader(new InputStreamReader(new FileInputStream(listPath), "UTF-8"),'|','"');
				for (String[] row : cr.readAll()) {
					exceptionList.add( new String[] { row[0].strip(),row[1].strip()} );
				}
				cr.close();
			} else if(runType.equalsIgnoreCase("-d")) {
				CSVReader crDict = new com.opencsv.CSVReader(new InputStreamReader(new FileInputStream(dictPath), "UTF-8"),'|','"');
				for (String[] row : crDict.readAll()) {
					exceptionDict.add( new String[] {row[0].strip(),row[1].strip(), row[2].strip(), row[3].strip(), row[4].strip()} );
				}
				crDict.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void traverse(File input) throws Exception {
		try {
			for (File source : input.listFiles()) {
				if (source.isDirectory()) {	
					traverse(source);
				} else if (source.getName().equals(processFile)) {
					System.out.println("Processing Item : " + count++ );
					//System.out.println(source.getAbsolutePath());
					BufferedReader br = new BufferedReader(new FileReader(source.getParentFile() + "/handle"));
					handle = br.readLine();
					br.close();
					process(source);
				}
			}
		} catch (Exception e ) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void process(File item) throws Exception {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = dbf.newDocumentBuilder();;
			dbf.setValidating(false);
			Document inputDoc = documentBuilder.parse(item);
			Element root = inputDoc.getDocumentElement();
			String schema = root.getAttribute("schema");
			NodeList docNodes = root.getChildNodes();

			String textContent = "";
			
			for (int i = 0; i < docNodes.getLength(); i++) {
				Node docNode = docNodes.item(i);
				if (docNode.getNodeType() != Node.ELEMENT_NODE)
					continue;

				String nodeNameNDL = formReadable(docNode, schema);
				if (nodeNameNDL.equals(fieldName)) {
					textContent = docNode.getTextContent();
					if(runType.equalsIgnoreCase("-l"))
						dumpByExceptionList(textContent);
					else if(runType.equalsIgnoreCase("-d"))
						dumpByExceptionDictionary(textContent);
					else if(runType.equalsIgnoreCase("-e"))
						dumpByExceptionExpression(textContent);
					continue;	
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void dumpByExceptionExpression(String textContent) {
		if(textContent.matches(expression)) {
			pr.println(handle+"\t"+textContent);
			totalFieldinstances++ ;
		}	
	}
	void dumpByExceptionList(String textContent) {
		for (String[] exList : exceptionList ) {
			String exChar = "" ;
			if(!exList[0].equalsIgnoreCase("regexp"))
				exChar = exList[1].replaceAll("([\\W&&\\S])", "\\\\$1");
			else
				exChar = exList[1];
			if (useTokenizer) {
				if(textContent.matches(ic+".*"+lftokenizer+exChar+rttokenizer+".*")) {
					pr.println(handle+"\t"+textContent);
					totalFieldinstances++ ;
					break;
				}
			} else {
				if(textContent.matches(ic+".*"+exChar+".*")) {
					pr.println(handle+"\t"+textContent);
					totalFieldinstances++ ;
					break;
				}
			}		
		}
	}
	
	void dumpByExceptionDictionary(String textContent) {
		for (String[] exList : exceptionDict ) {
			String exChar = "" ;
			if(!exList[0].equalsIgnoreCase("regexp"))
				exChar = exList[2].replaceAll("([\\W&&\\S])", "\\\\$1");
			else
				exChar = exList[2];
			ic = exList[4].equalsIgnoreCase("ignore")?"(?i)":"";
			lftokenizer = exList[1];
			rttokenizer = exList[3];
			if (textContent.matches(ic + lftokenizer + exChar + rttokenizer )) {
				pr.println(handle + "\t" + textContent+"\t"+exList[2]);
				totalFieldinstances++;
				break;
			}
		}
	}

	public String formReadable(Node thisNode, String schema) {
		// TODO Auto-generated method stub
		NamedNodeMap attrs = thisNode.getAttributes();
		String element = "", qualifier = "", read;
		for (int i = 0; i < attrs.getLength(); i++) {
			switch (attrs.item(i).getNodeName()) {
			case "element":
				element = attrs.getNamedItem("element").getNodeValue();
				break;
			case "qualifier":
				qualifier = attrs.getNamedItem("qualifier").getNodeValue();
				break;
			}
		}
		read = (schema + "." + element + "." + qualifier);
		read = read.replaceAll("[.]$", "");

		return read;
	}
	public static void main (String[] args) throws Exception{
		try {
			Properties prop=allExceptionHandle(args);	
			String runType = args[0];
			FieldDatabyFilter_v_2_1 dabf = new FieldDatabyFilter_v_2_1(runType);
			
			dabf.fieldName = prop.getProperty("fieldName").strip();
			String schema = dabf.fieldName.split("\\.")[0];
			
			switch(schema) {
			case "dc":
				processFile = "dublin_core.xml";
				break;
			case "lrmi":
				processFile = "metadata_lrmi.xml";
				break;
			case "ndl":
				processFile = "metadata_ndl.xml";
				break;
			default:
				break;		
			}
			
			if(runType.equalsIgnoreCase("-l")) {
				try {
					if(prop.getProperty("continuousMatch").isBlank()) {
						System.out.println("continuous match not given ");
						}
				}
				catch(Exception e) {
					System.out.println("please uncomment continuous match or provide it correctly");	
					System.exit(2);
				}
				if (prop.getProperty("continuousMatch").equalsIgnoreCase("true"))
					dabf.useTokenizer = false;
				else if (prop.getProperty("continuousMatch").equalsIgnoreCase("false"))
					dabf.useTokenizer = true;
				else
					throw new Exception("Use Tokenizer value wrong. Range is True/False. Program Terminating.");

				if(prop.getProperty("IgnoreCase").equalsIgnoreCase("true"))
					dabf.ic="(?i)";
				else if (prop.getProperty("IgnoreCase").equalsIgnoreCase("false"))
					dabf.ic="";
				else
					throw new Exception("Use IgnoreCase value wrong. Range is True/False. Program Terminating.");	

				if(dabf.useTokenizer) {
					dabf.lftokenizer = prop.getProperty("lftokenizer");
					dabf.rttokenizer = prop.getProperty("rttokenizer");
					try{
						if(dabf.lftokenizer.isBlank() || dabf.rttokenizer.isBlank()) {
							System.out.println("null value");
						}
					}catch(Exception e) {
						System.out.println("As you select discrete match i.e continuousmatch =false ");
						System.out.println("Please provide left or right tokenizer or both ");
						System.exit(1);
					}
				}
			}
			dabf.source_root = prop.getProperty("source_root");
			dabf.logFilePath = prop.getProperty("logPath");
			dabf.pr = new PrintStream (new File (dabf.logFilePath));
			dabf.traverse(new File (dabf.source_root));
			dabf.pr.close();
			System.out.println("Match Count : " + dabf.totalFieldinstances);
		}
		
		catch(ArithmeticException e) {
			System.out.println(e);
			System.exit(0);
		}
		catch(NullPointerException e) {
			System.out.println("you are using expression/list  match but expression/list is not properly given in property file");
			System.out.println("Please uncomment the expression/list from properties file or provide a valid expression/list ");
			System.out.println("OR");
			System.out.println("Please Provide IgnoreCase either true or false");
			System.out.println(e);
			System.exit(0);
		}
		catch(ArrayIndexOutOfBoundsException e) {
			System.out.println(e);
			System.exit(0);
		}
		catch(FileNotFoundException e) {
			System.out.println("Please provide right path for properties file");
			System.out.println(e);
			System.exit(0);
		}
		catch(Exception e) {
			System.out.println(e);
			System.out.println("you are using expression match but expression is not properly given in property file");
			System.out.println("Please uncomment the expression from properties file or provide a valid expression ");
//			if(runType.equalsIgnoreCase("-e") && prop.getProperty("expression").isEmpty()) {
//				System.out.println("RunType is -E however EXPRESSION is not specified. Program Terminating.");
//			}
		}		
	}
}
