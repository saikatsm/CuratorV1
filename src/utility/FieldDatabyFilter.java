package utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
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

public class FieldDatabyFilter {
	
	String handle = "", source_root = "", logFilePath = "",
			fieldName="", expression = "", runType = "", tokenizer = ""; 
	static String listPath = "";
	int count = 0, totalFieldinstances = 0;
	boolean useTokenizer = true;
	ArrayList<String[]> exceptionList = new ArrayList<String[]>();
	PrintStream pr = null;
	
	public FieldDatabyFilter(String runType) {
		// TODO Auto-generated constructor stub
		this.runType = runType;
		try {
		if(runType.equalsIgnoreCase("-l")) {
			CSVReader cr = new CSVReader(new InputStreamReader(new FileInputStream(listPath), "UTF-8"),'|','"');
			for (String[] row : cr.readAll()) {
				exceptionList.add( new String[] {row[0].trim(),row[1].trim()} );
			}
			cr.close();
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void traverse(File input) throws Exception {
		
		try {
		
			for (File source : input.listFiles()) {
				
				if (source.isDirectory()) {	
					traverse(source);
					
				} else if (source.getName().equals("dublin_core.xml")) {
					System.out.println("Processing Item : " + count++ );
					BufferedReader br = new BufferedReader(new FileReader(source.getParentFile() + "/handle"));
					handle = br.readLine();
					br.close();
					process(source);
					
				}
			}
		
		} catch (Exception e ) {
			
			e.printStackTrace();
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
		pr.println(textContent+"\t"+handle);
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
			if(textContent.matches(".*"+tokenizer+exChar+tokenizer+".*")) {
				pr.println(handle+"\t"+textContent);
				totalFieldinstances++ ;
				break;
			}
		} else {
			if(textContent.matches(".*"+exChar+".*")) {
				pr.println(handle+"\t"+textContent);
				totalFieldinstances++ ;
				break;
			}
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
	
	if(args.length<2)
		throw new Exception("Wrong Input Argument. Command Syntax : java -jar FieldDatabyFilter.jar -l[ist]/e[xpression] <ConfigFilePath>");
	
	String runType = args[0];
	if(!runType.matches("(?i)-l|-e"))
		throw new Exception("Wrong Runtype. Runtype in -l[ist]/-e[xpression].");
	String propPath = args[1];
	
	if (propPath.isEmpty())
		throw new Exception("Properties File not given. Please in put properties file path.");

	Properties prop = new Properties();
	File properties = new File(propPath);

	InputStream input = new FileInputStream(properties);

	prop.load(input);
	
	listPath = prop.getProperty("listPath");
	
	if(runType.equalsIgnoreCase("-l") && listPath.isEmpty())
		throw new Exception("RunType is -l however listPath is not specified. Program Terminating.");
	
	FieldDatabyFilter dabf = new FieldDatabyFilter(runType);
	
	dabf.fieldName = prop.getProperty("fieldName");
	dabf.expression = prop.getProperty("expression");
	if(dabf.runType.equalsIgnoreCase("-e") && dabf.expression.isEmpty())
		throw new Exception("RunType is -e however expression is not specified. Program Terminating.");
	
	dabf.source_root = prop.getProperty("source_root");
	dabf.logFilePath = prop.getProperty("logPath");
	if (prop.getProperty("continuousMatch").equalsIgnoreCase("true"))
		dabf.useTokenizer = false;
	else if (prop.getProperty("continuousMatch").equalsIgnoreCase("false"))
		dabf.useTokenizer = true;
	else
		throw new Exception("Use Tokenizer value wrong. Range is True/False. Program Terminating.");
	if(dabf.useTokenizer)
		dabf.tokenizer = prop.getProperty("tokenizer");
	dabf.pr = new PrintStream (new File (dabf.logFilePath));
	dabf.traverse(new File (dabf.source_root));
	dabf.pr.close();
	System.out.println("Match Count : " + dabf.totalFieldinstances);
}
}
