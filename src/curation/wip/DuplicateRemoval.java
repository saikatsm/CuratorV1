package curation.wip;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DuplicateRemoval {

	ArrayList<String> duplicateList = new ArrayList<String>();
	public HashMap<String, ArrayList<String>> itemMap = new HashMap<String, ArrayList<String>>();
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder documentBuilder = null;
	public ArrayList<String> checkFields = new ArrayList<String>();
	public Set<String> parentList = new HashSet<String>();
	boolean createFile = false;
	public boolean seperateParent = false;
	static int count_item;
	public String handle, logPath, source_root, configPath;
	public PrintStream pr, er;
	
	public DuplicateRemoval(boolean executeSeperation) throws Exception{

			dbf.setValidating(false);
			documentBuilder = dbf.newDocumentBuilder();
			this.createFile = executeSeperation;
			
			//range=10;
			//norangeSet=false;
	}
	
	public void set_logs(String logFileName, String errFileName) throws Exception {
		
		if(!logPath.isEmpty()) {
			
			String datetime = LocalDateTime.now().toString();
			pr = new PrintStream(logPath + logFileName+"_"+datetime);
			er = new PrintStream(logPath + errFileName+"_"+datetime);
			
		} else {
			
			throw new Exception("logPath is empty.");
		}
}

public void set_logs_close() throws Exception{

	pr.close();
	er.close();
	
}
	
public void traverse(File input) throws Exception {
		
		try {
			
		boolean countItem = true;
		
			for (File source : input.listFiles()) {
				
				if (source.isDirectory()) {
					
					traverse(source);
					
				} else if (source.getName().contains("xml")) {

					if (countItem) {
						
						count_item++;
						countItem = false;
						System.out.println(count_item + " : " + source.getParent().replace(source_root, ""));
						
						BufferedReader br = new BufferedReader(new FileReader(source.getParentFile() + "/handle"));
						handle = br.readLine();
						br.close();
					}
					
					process(source);
					
				}
				if(createFile && seperateParent)
				for(String parent : parentList)
					new File(parent + "/dupl").createNewFile();
			}
		
		} catch (Exception e ) {
			
			e.printStackTrace();
		}
	}

	public void process(File item) {
		
		if(item.getName().contains("dublin")) {
			
		try {
			
			HashMap <String, ArrayList<String>> fieldValueMap = new HashMap<String, ArrayList<String>>();
			
			Document inputDoc = documentBuilder.parse(item);
			Element root = inputDoc.getDocumentElement();
			String schema = root.getAttribute("schema");
			NodeList docNodes = root.getChildNodes();
			
			for (int i = 0; i < docNodes.getLength(); i++) {
				Node docNode = docNodes.item(i);
				
				if (docNode.getNodeType() != Node.ELEMENT_NODE)
					continue;
				
				String nodeNameNDL = formReadable(docNode, schema);
				String textContent = docNode.getTextContent();
				
				if(checkFields.contains(nodeNameNDL) || nodeNameNDL.equals("dc.title")) {
					
					if(fieldValueMap.containsKey(nodeNameNDL)) {
					
						ArrayList<String> values = fieldValueMap.get(nodeNameNDL);
						values.add(textContent);
						
						fieldValueMap.put(nodeNameNDL, values);
					} else {
						
						ArrayList<String> values = new ArrayList<String>();
						values.add(textContent);
						
						fieldValueMap.put(nodeNameNDL, values);
					}
				}
			} 
			
			String printString = (fieldValueMap.get("dc.title").toString().replaceAll("\\[|\\]", "") + "|");
			
			for(String fieldName : checkFields) {
				
				String valueString = "";
				ArrayList<String> values = fieldValueMap.get(fieldName);
				
				if(values != null)
					for(String value : values)
						valueString += value + ";";
				
				valueString = valueString.replaceAll(";$", "");			
				printString += valueString+ "|";
			}
			
			printString = printString.replaceAll("\\|$", "");
			String checkString = printString.toLowerCase();
			
			if (itemMap.containsKey(checkString)) {
				
				ArrayList<String> titleinfo =  itemMap.get(checkString);
				titleinfo.add(handle);
				itemMap.put(checkString, titleinfo);
				parentList.add(titleinfo.get(0));
				if(createFile)
					new File(item.getParentFile().getPath() + "/dupl").createNewFile();
			
			} else {
				
				ArrayList<String> titleinfo = new ArrayList<String>();
				titleinfo.add(item.getParentFile().getPath());
				titleinfo.add(handle);
				itemMap.put(checkString, titleinfo);
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
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

}
