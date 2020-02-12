package curation.release;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.WriterOutputStream;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.opencsv.CSVReader;

public class Base extends Logics {
	static int count_item = 0, err_count = 0;
	public int range = 0;
	public boolean norangeSet = true;
	public static String source_root = "", target_root = "", logPath = "", configPath = "", schemaActionFile = "", runType = "";
	public static PrintStream pr = null, er = null;
	Set<String> validsourceset = new HashSet<String>();
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder documentBuilder = null;
	public File f_target;
	public String targetPath, handle;
	BufferedImage bufferedImage;
	NDLSchemaDetails ndlschema = null;
	HashMap<Document,File > domTargetMap = new HashMap<Document, File>();
	HashMap<String, Document> schemaDomMap = new HashMap<String, Document>();
	
	public Base() {	
		try {		
			dbf.setValidating(false);
			documentBuilder = dbf.newDocumentBuilder();
			ndlschema = new NDLSchemaDetails();
			validsourceset.addAll(Arrays.asList("dublin_core.xml","metadata_ndl.xml","metadata_lrmi.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Base.set_logs(String logFileName, String errFileName) :
	 * Method for ouput log setting for Base Curation. The methods sets the output log 
	 * and the error log for Base Curation process. 
	 */
	
	public static void set_logs(String logFileName, String errFileName) throws Exception {
		if (!logPath.isEmpty()) {
			String datetime = LocalDateTime.now().toString();
			pr = new PrintStream(logPath + logFileName+"_"+datetime);
			er = new PrintStream(logPath + errFileName+"_"+datetime);
		} else {

			throw new Exception("logPath is empty.");
		}
	}

	public static void set_logs_close() throws Exception{
		pr.close();
		er.close();
	}
	
/** Base.traverse(File input) :
 * The method traverses through the source path and calls the curation process for XML files
 * and subsequent processes for other files as required. The current process for other files is
 * to simply copy to the target location. The location need not to exist in prior.
 */
	public void traverse(File input) throws Exception {		
		try {
		boolean countItem = true, staticWrite = false;
		if (count_item < range || norangeSet)
			for (File source : input.listFiles()) {
				if (source.isDirectory()) {
					f_target = new File(target_root + source.getAbsolutePath().replace(source_root, ""));
					if(!f_target.exists())
						f_target.mkdir();
					traverse(source);
					writetargetdoms();
				} else if (validsourceset.contains(source.getName())) {
					if (countItem) {
						count_item++;
						countItem = false;
						System.out.println(count_item + " : " + source.getParent().replace(source_root, ""));
						BufferedReader br = new BufferedReader(new FileReader(source.getParentFile() + "/handle"));
						handle = br.readLine();
						br.close();
						if(runType.equalsIgnoreCase("-h")) {
							if(schemaActionByHandle.containsKey(handle))
								schemaAction = schemaActionByHandle.get(handle);
							else
								schemaAction.clear();
						}
					}
					String target = source.getAbsolutePath().replace(source_root, "");
					if(target.contains("/"))
						targetPath = target_root + target.substring(0,target.lastIndexOf('/'));
					else
						targetPath = target_root + target;					
					if (!staticWrite) {
						staticWrite = true;
						writeSaticFields();
					}
					f_target = new File(target_root + target);
					process(source);
					
				} else {
						String target = source.getAbsolutePath().replace(source_root, "");
						f_target = new File(target_root + target);
						FileUtils.copyFile(source, f_target);
				}
			}
		
		} catch (Exception e ) {
			
			e.printStackTrace();
		}
	}
	
	/** Base.getDocument(File target, boolean getBlankDocument) :
	 * This method returns the relevant output document. If no target file exists the method creates 
	 * the specific one mentioned in the actual parameter returns the specific Document object.
	 * The method is NOT AGNOSTIC across items i.e., data cannot be written accross items. The 
	 * boolean flag getBlankDocument return a blank Document if set true else it parses the 
	 * XML file which exists at the location and returns the Document. This method may throw exception
	 * if the boolean parameter is not set correctly, i.e., if getBlankDocument is set false before the DOM
	 * object is written to the file. 
	 */
	
	protected Document getDocument(String ipschema) {
		Document doc = null;
		try {
			if (schemaDomMap.containsKey(ipschema))
				doc = schemaDomMap.get(ipschema);
			else {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				dbf.setValidating(false);
				DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
				doc = documentBuilder.newDocument();
				Element dcRoot = doc.createElement("dublin_core");
				File outFile = null;
				switch (ipschema) {
				case "dc":
					outFile = new File(targetPath + "/dublin_core.xml");
					outFile.createNewFile();
					dcRoot.setAttribute("schema", "dc");
					doc.appendChild(dcRoot);
					schemaDomMap.put(ipschema, doc);
					domTargetMap.put(doc, outFile);
					break;
				case "lrmi":
					outFile = new File(targetPath + "/metadata_lrmi.xml");
					outFile.createNewFile();
					dcRoot.setAttribute("schema", "lrmi");
					doc.appendChild(dcRoot);
					schemaDomMap.put(ipschema, doc);
					domTargetMap.put(doc, outFile);
					break;
				case "ndl":
					outFile = new File(targetPath + "/metadata_ndl.xml");
					outFile.createNewFile();
					dcRoot.setAttribute("schema", "lrmi");
					doc.appendChild(dcRoot);
					schemaDomMap.put(ipschema, doc);
					domTargetMap.put(doc, outFile);
					break;
				default:
					System.err.println("Wrong DocumentType Input Filter.");
					break;
				}
			}
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
			return doc;
		}
	}

	/**
	 * Main curation process method. This is source file agonistic, i.e.,
	 * all the curation logics are written in the single method irrespective
	 * of the source file wheather dublin_core, metadata_lrmi or metadata_ndl.
	 * Field logics are captured based on field names and addtional fields are 
	 * written otherwise using 
	 * 1. writeDublinOthers()
	 * 2. writeLrmiOthers()
	 * 1. writeNDLOthers() methods. These methods are source specific.
	 */
	
	public void process(File item) throws Exception {
		try {
			Document inputDoc = documentBuilder.parse(item);
			Element root = inputDoc.getDocumentElement();
			String schema = root.getAttribute("schema");
			NodeList docNodes = root.getChildNodes();
			boolean tgterprint = false;
			for (int i = 0; i < docNodes.getLength(); i++) {
				Node docNode = docNodes.item(i);
				if (docNode.getNodeType() != Node.ELEMENT_NODE)
					continue;
				String nodeNameNDL = formReadable(docNode, schema);
				String textContent = docNode.getTextContent().trim();
				try {
					if (!schemaAction.containsKey(nodeNameNDL)) {
						write(nodeNameNDL, textContent);
					} else if (schemaAction.get(nodeNameNDL).get(0).actionName.equalsIgnoreCase("deleteField")) {
						;
					} else {
						ArrayList<String[]> result = nodeAction(textContent, nodeNameNDL);
						for (String[] eachresult : result)
							write(eachresult[0], eachresult[1]);
					}
					continue;
				} catch (Exception e) {
					if (!tgterprint) {
						er.println(err_count++ + " : " + targetPath);
						tgterprint = true;
					}
					er.println(nodeNameNDL + " : " + textContent);
					e.printStackTrace(er);
					write(nodeNameNDL, textContent);
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace(er);
		}
	}
	
	private boolean write(String nodeName, String value) {
		try {
			HashMap<String, String> nodeComponents = getNodeNameDetails(nodeName);
			String nodeSchema = nodeComponents.get("schema");
			Document outDoc = getDocument(nodeSchema);
				if(nodeComponents.containsKey("jsonKey")) {
					if(!value.matches(".*\\\\\".*")) {
						value = value.replaceAll("\"", "\\\\\"");
					}
					value = "{\""+nodeComponents.get("jsonKey")+"\":\"" + value.replaceAll("\"", "\\\\\"")+ "\"}";
				}
				writeNDLDom(nodeComponents.get("element"), nodeComponents.get("qualifier"), value, outDoc, true);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void writeSaticFields() {
		try {
			for (Map.Entry<String, ArrayList<Action>> entry : schemaAction.entrySet()) {
				for (Action action : entry.getValue())
					if (action.actionName.equals("add")) {
						String writeNode = entry.getKey();
						if (action.targetvalueList.isEmpty())
							write(writeNode, action.targetValue.replaceAll("\"", "\\\\\""));
						else {
							for (String targetValue : action.targetvalueList)
								write(writeNode, targetValue.replaceAll("\"", "\\\\\""));
						}
					}
			}

		} catch (Exception e) {
			e.printStackTrace(er);
		}
	}

	private HashMap<String,String> getNodeNameDetails(String nodeNameText) {
		HashMap<String, String> nodeDetails = new HashMap<String, String>();
		if(nodeNameText.contains("@")) {
			String jsonKey = nodeNameText.replaceAll(".*@", "");
			nodeNameText = nodeNameText.replaceAll("@.*", "");
			nodeDetails.put("jsonKey", jsonKey);
		}
		String[] nodeParts = nodeNameText.split("\\.");
		if(nodeParts.length == 3) {
			nodeDetails.put("schema", nodeParts[0].toLowerCase());
			nodeDetails.put("element", nodeParts[1]);
			nodeDetails.put("qualifier", nodeParts[2]);	
		} else {
			nodeDetails.put("schema", nodeParts[0].toLowerCase());
			nodeDetails.put("element", nodeParts[1]);
			nodeDetails.put("qualifier", "");
		}
		return nodeDetails;
	}
	
	public String formReadable(Node thisNode, String schema ) {
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
	
	private void writeNDLDom(String element, String qualifier, String value, Document document, boolean priority) throws Exception {
		String ndlNodeName = element + "." + qualifier;
		ndlNodeName = ndlNodeName.replaceAll("\\.$", "");
		boolean multi = true;
		HashMap<String, Object> nodeProperties = ndlschema.getAtrributeType(ndlNodeName);
		if (nodeProperties != null)
			multi = (Boolean) nodeProperties.get("multiValued");
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression = "/dublin_core/dcvalue[@element='" + element + "' and ";
		if (!qualifier.isEmpty()) {
			expression += "@qualifier='" + qualifier + "']";
		} else
			expression += "not(@qualifier)]";
		NodeList result = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
		if (result.getLength() == 0) {
			Node newNode = document.createElement("dcvalue");
			Element elem = (org.w3c.dom.Element) newNode;
			elem.setAttribute("element", element);
			if (!qualifier.isEmpty())
				elem.setAttribute("qualifier", qualifier);
			value = value.replaceAll("\\s+", " ").trim();
			elem.setTextContent(value);
			document.getDocumentElement().appendChild(newNode);
		} else {
			boolean write = true;
			for (int i = 0; i < result.getLength(); i++) {
				String currVal = result.item(i).getTextContent();
				if (priority && currVal.equalsIgnoreCase(value)) {
					if (!multi) {
						result.item(i).setTextContent(value);
						write = false;
						break;
					}
					write = false;
					break;
				}
			}
			if (priority && write) {
				Node newNode = document.createElement("dcvalue");
				Element elem = (org.w3c.dom.Element) newNode;
				elem.setAttribute("element", element);
				if (!qualifier.isEmpty())
					elem.setAttribute("qualifier", qualifier);
				value = value.replaceAll("\\s+", " ").trim();
				elem.setTextContent(value);
				document.getDocumentElement().appendChild(newNode);
			}
		}
	}
	private boolean writetargetdoms() {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			for (Map.Entry<Document, File> entry : domTargetMap.entrySet()) {
				StreamResult outputFile = new StreamResult(entry.getValue());
				Source outputsrc = new DOMSource(entry.getKey());
				transformer.transform(outputsrc, outputFile);
			}
			schemaDomMap.clear();
			domTargetMap.clear();
			return true;
			
		} catch (Exception e) {		
			e.printStackTrace();
			e.printStackTrace(er);
			return false;
		}
		
	}
	
}
