package curation.wip;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DuplicateRemoval_InFile extends Base_Iterator {

	ArrayList<String> duplicateList = new ArrayList<String>();

	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder documentBuilder = null;
	public ArrayList<String> checkFields = new ArrayList<String>();
	public Set<String> valueList = new HashSet<String>();
	boolean createFile = false;
	public boolean seperateParent = false;
	static int count_item;
	public String handle, logPath, source_root, configPath;
	public PrintStream pr, er;
	
@Override
public void process(File item) {
		try {
			
			currentDoc = getDocument(f_target, false);			
			dbf.setValidating(false);
			documentBuilder = dbf.newDocumentBuilder();
			Document inputDoc = documentBuilder.parse(item);
			Element root = inputDoc.getDocumentElement();
			String schema = root.getAttribute("schema");
			NodeList docNodes = root.getChildNodes();

			String checkVal = "", checkNodeName = "";
			HashMap<String, ArrayList<String>> checkValMap = new HashMap<String, ArrayList<String>>();
			
			for (int i = 0; i < docNodes.getLength(); i++) {
				Node docNode = docNodes.item(i);

				if (docNode.getNodeType() != Node.ELEMENT_NODE)
					continue;

				String nodeNameNDL = formReadable(docNode, schema);
				String textContent = docNode.getTextContent();
				if(checkFields.contains(nodeNameNDL)) {
					if(!checkValMap.containsKey(textContent)) {
						checkVal = textContent;
						checkNodeName = nodeNameNDL;
						ArrayList<String> checkNodeDetails = new ArrayList<String>();
						checkNodeDetails.add(checkNodeName);
						for(int j = 0; j < checkFields.size(); j++) {
							if (checkFields.get(j).equalsIgnoreCase(nodeNameNDL)) {
								checkNodeDetails.add(String.valueOf(j));
								break;
							}
						}
						checkValMap.put(checkVal, checkNodeDetails);
					}
					else {
						int currIdx = checkFields.size()+1;
						for(int j = 0; j < checkFields.size(); j++) {
							if (checkFields.get(j).equalsIgnoreCase(nodeNameNDL)) {
								currIdx = j;
								break;
							}
						}
						ArrayList<String> checkNodeDetails = checkValMap.get(textContent);
						int checkIdx = Integer.parseInt(checkNodeDetails.get(1));
						if(currIdx<checkIdx) {
							checkNodeDetails.set(0, nodeNameNDL);
							checkNodeDetails.set(1, String.valueOf(currIdx));
						}
					}
				} else {
					writeRestNodes(nodeNameNDL,textContent,currentDoc);
				}
			}		
			for(Map.Entry<String, ArrayList<String>> entry : checkValMap.entrySet()) {
				String nodeNameNDL = entry.getValue().get(0);
				String textContent = entry.getKey();
				writeRestNodes(nodeNameNDL,textContent,currentDoc);
			}
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StreamResult outputFile = new StreamResult(f_target);
			Source outputsrc = new DOMSource(currentDoc);
			transformer.transform(outputsrc, outputFile);
			
			
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

}
