package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.opencsv.CSVReader;

import utility.FieldDatabyFilter;

public class test1 {
	
	String handle = "", source_root = "", logFilePath = "",
			fieldName="", expression = "", runType = "", tokenizer = ""; 
	static String listPath = "";
	int count = 0, totalFieldinstances = 0;
	boolean useTokenizer = true;
	ArrayList<String[]> exceptionList = new ArrayList<String[]>();
	PrintStream pr = null;

	public test1(String runType) {
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
				if (nodeNameNDL.equals("dc.title")) {
					textContent = docNode.getTextContent();
						dumpByExceptionList(textContent);
					continue;	
				}
			}
			} catch (Exception e) {
				e.printStackTrace();
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
	
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		listPath = "/home/arunavo/Desktop/Phase 7_Pattern1.csv";
		test1 dabf = new test1("-l");
		dabf.pr = new PrintStream (new File ("/home/arunavo/Desktop/output.log"));
		dabf.traverse(new File ("/home/arunavo/Desktop/CiteSeerX_Phase7_MISC_V2.0"));
		
	}

}
