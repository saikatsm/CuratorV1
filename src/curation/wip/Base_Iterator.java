package curation.wip;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.opencsv.CSVReader;

public class Base_Iterator {
	static int count_item = 0, err_count = 0;
	public int range = 0;
	public boolean norangeSet = true;
	public static String source_root = "", target_root = "", logPath = "", configPath = "", schemaActionFile = "";
	public static PrintStream pr = null;
	public static PrintStream er = null;
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	public DocumentBuilder documentBuilder = null;
	public File f_target;
	Document currentDoc;
	public String targetPath, handle;
	Boolean lrt_oth = false, fileCreated = false, lang_oth = false, type_degree = false, lang_blank = true;
	Set<String> f_create = new HashSet<String>();
	BufferedImage bufferedImage;
	HashMap<String, String> handle_title_map = new HashMap<String, String>();
	
	public Base_Iterator() {
		
		try {
			
			dbf.setValidating(false);
			documentBuilder = dbf.newDocumentBuilder();
			
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
					
					f_create.clear();
					f_target = new File(target_root + source.getAbsolutePath().replace(source_root, ""));
					
					if(!f_target.exists())
						f_target.mkdir();
					
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
					
					String target = source.getAbsolutePath().replace(source_root, "");
					
					if(target.contains("/")) {
						
						targetPath = target_root + target.substring(0,target.lastIndexOf('/'));
						
					} else {
						
						targetPath = target_root + target;
					}
					
					f_target = new File(target_root + target);
					
					process(source);
					
					if(!staticWrite) {
						
						staticWrite = true;
						/**TODO
						 * add block to be parameterised. Automated curation module
						 * development component. Needs Attention.
						 */
						
						//writeLrmiOthers();
					}
					
				} /*else if (source.getName().contains(".gif")) {
					System.out.println(count_item++);
					updateThumbnail(source);
				}*/	else {					
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
	
	protected Document getDocument(File target, boolean getBlankDocument) {
		Document doc = null;
		try {
			if (!f_create.contains(target.getAbsolutePath())) {

				target.createNewFile();
				f_create.add(target.getAbsolutePath());

				doc = getBlankDocument(target);

				return doc;

			} else {

				if (!getBlankDocument) {

					doc = documentBuilder.parse(target);

				} else {

					doc = getBlankDocument(target);

				}

				return doc;
			}

		} catch (Exception e) {
			
			System.out.println("Please check the parameters.");
			e.printStackTrace();

			return doc;

		}
	}
	
	/** Base.getBlankDocument(File target) :
	 * This method returns a blank document as per NDL requirement.
	 * Used in getDocument method.
	 */
	
	private Document getBlankDocument(File target) {
		
		try {

			String documentType = "";
			
			if (target.getName().contains("dublin")) {
				
				documentType = "dc";
				
			} else if (target.getName().contains("lrmi")) {
				
				documentType = "lrmi";
				
			} else if (target.getName().contains("ndl")) {
				
				documentType = "ndl";
				
			}
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
			Document blankDoc = documentBuilder.newDocument();
			Element dcRoot = blankDoc.createElement("dublin_core");

			if (documentType.equalsIgnoreCase("dc")) {

				dcRoot.setAttribute("schema", "dc");

			} else if (documentType.equalsIgnoreCase("lrmi")) {

				dcRoot.setAttribute("schema", "lrmi");

			} else if (documentType.equalsIgnoreCase("ndl")) {

				dcRoot.setAttribute("schema", "ndl");

			} else {

				System.err.println("Wrong DocumentType Input Filter.");

				throw new Exception();

			}

			blankDoc.appendChild(dcRoot);

			return blankDoc;

		} catch (Exception e) {

			e.printStackTrace();

			return null;

		}
	}

	/*
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

		
	}

	/*
	 * The following three methods 
	 * 1. writeDublinOthers, 
	 * 2. writeLrmiOthers,
	 * 3. writeNDLOthers
	 * writes static values and other additional values
	 * for the source items based on information captured from source file iterations. 
	 */
	
	private void writeDublinOthers(File source) {
		
		if (source.getName().equals("dublin_core.xml")) {
			
			write("description", "searchVisibility", "true", currentDoc,true);
			write("language", "iso", "eng", currentDoc,true);
			write("format", "mimetype", "application/pdf", currentDoc,true);
			write("type", "", "text", currentDoc,true);
			write("rights", "accessRights", "open", currentDoc,true);
			write("subject", "ddc", "000::Computer science, information & general works", currentDoc,true);
			write("subject", "ddc", "004::Data processing & computer science", currentDoc,true);

			/*if (lang_oth == true || lang_blank == true) {
				try {
					//System.out.println(dcFieldDeptValue);
					ArrayList<String[]> result = curateLanguage(dcFieldDeptValue);
					for (String[] eachresult : result) {
						write("language", "iso", eachresult[1], currentDoc);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}*/
		}
	}

	private void writeLrmiOthers() {
		
		writeOther("educationalUse", "", "research", new File(targetPath + "/metadata_lrmi.xml"),true);
		writeOther("typicalAgeRange", "", "22+", new File(targetPath + "/metadata_lrmi.xml"),true);
		writeOther("interactivityType", "", "expositive", new File(targetPath + "/metadata_lrmi.xml"),true);
		writeOther("learningResourceType", "", "proceeding", new File(targetPath + "/metadata_lrmi.xml"),true);
		writeOther("educationalRole", "", "student", new File(targetPath + "/metadata_lrmi.xml"),true);
		writeOther("educationalRole", "", "teacher", new File(targetPath + "/metadata_lrmi.xml"),true);
		writeOther("educationalAlignment", "educationalLevel", "ug_pg", new File(targetPath + "/metadata_lrmi.xml"),true);
		writeOther("educationalAlignment", "educationalLevel", "career_tech", new File(targetPath + "/metadata_lrmi.xml"),true);
		writeOther("educationalAlignment", "difficultyLevel", "medium", new File(targetPath + "/metadata_lrmi.xml"),true);
		
	}
	
	private void writeNDLOthers(File source) {
		
		if (source.getName().equals("metadata_ndl.xml")) {

			//Nothing to write as of now.	
		}		
	}
	
	protected void writeRestNodes(String nodeNameText, String inputValue, Document currentDoc) {
		String[] nodeParts = nodeNameText.split("\\.");
		inputValue = inputValue.replaceAll("\\s+", " ").trim();
		if(!inputValue.isEmpty()) {
			if (nodeParts.length == 3)
				write(nodeParts[1], nodeParts[2], inputValue, currentDoc, true);
			else
				write(nodeParts[1], "", inputValue, currentDoc, true);
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
			nodeDetails.put("schema", nodeParts[0]);
			nodeDetails.put("jsonKey", nodeParts[1]);
			nodeDetails.put("jsonKey", nodeParts[2]);
		}
		return nodeDetails;
	}
	
	public String formReadable(Node thisNode, String schema) {
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

	public boolean write(String element, String qualifier, String value, Document document,boolean priority) {
		
		try {
			/**
			 * Need to write wrapper for automatic write.			
			 * String[] fieldNameComponents = fieldName.split("\\.");
			 */
			XPath xPath = XPathFactory.newInstance().newXPath();
			String expression = "/dublin_core/dcvalue[@element='" + element + "' and " ;			
			if(!qualifier.isEmpty()) {
				expression += "@qualifier='" + qualifier + "']"; 
			} else
				expression += "not(@qualifier)]";
			
			NodeList result = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			
			if(result.getLength() == 0) {
				Node newNode = document.createElement("dcvalue");
				Element elem = (org.w3c.dom.Element) newNode;
				elem.setAttribute("element", element);
				if (!qualifier.isEmpty())
					elem.setAttribute("qualifier", qualifier);
				value=value.replaceAll("\\s+", " ").trim();
				elem.setTextContent(value);
				document.getDocumentElement().appendChild(newNode);
			} /**
			 * TODO Pending Implementation module. Temp fix with below code.
			 */ 
			else if (element.equalsIgnoreCase("learningResourceType")) {
			}
			else {
				boolean write = true;
				for(int i = 0; i< result.getLength(); i++) {
					String currVal = result.item(i).getTextContent();
					if(priority && currVal.equalsIgnoreCase(value)) {
						write = false;
						break;
					}
				}
					if(priority && write) {
						Node newNode = document.createElement("dcvalue");
						Element elem = (org.w3c.dom.Element) newNode;
						elem.setAttribute("element", element);
						if (!qualifier.isEmpty())
							elem.setAttribute("qualifier", qualifier);
						value=value.replaceAll("\\s+", " ").trim();
						elem.setTextContent(value);
						document.getDocumentElement().appendChild(newNode);
					}
				}
			return true;
		} catch (Exception e) {
			
			e.printStackTrace();
			
			return false;
		}
	}
	public boolean writeOther(String element, String qualifier, String value, File target, boolean priority) {
		
		try {

			Document outDoc = getDocument(target, false);
			
			XPath xPath = XPathFactory.newInstance().newXPath();
			String expression = "/dublin_core/dcvalue[@element='" + element + "' and " ;
			
			if(!qualifier.isEmpty()) {
				expression += "@qualifier='" + qualifier + "']"; 
			} else
				expression += "not(@qualifier)]";
			NodeList result = (NodeList) xPath.compile(expression).evaluate(outDoc, XPathConstants.NODESET);
			
			if(result.getLength() == 0) {
				Node newNode = outDoc.createElement("dcvalue");
				Element elem = (org.w3c.dom.Element) newNode;
				elem.setAttribute("element", element);				
				if (!qualifier.isEmpty())
					elem.setAttribute("qualifier", qualifier);
				value=value.replaceAll("\\s+", " ").trim();
				elem.setTextContent(value);
			 outDoc.getDocumentElement().appendChild(newNode);
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				StreamResult outputFile = new StreamResult(target);
				Source outputsrc = new DOMSource(outDoc);
				transformer.transform(outputsrc, outputFile);
			}
			else {
				boolean write = true, replace = false;
				for(int i = 0; i< result.getLength(); i++) {
					String currVal = result.item(i).getTextContent();
					if(priority && currVal.equalsIgnoreCase(value)) {
						write = false;
						break;
					}
/**
 * TODO Implement Schema based approach of writing.
 */
					if(element.equalsIgnoreCase("learningResourceType")) {
						result.item(i).setTextContent(value);
						Transformer transformer = TransformerFactory.newInstance().newTransformer();
						StreamResult outputFile = new StreamResult(target);
						Source outputsrc = new DOMSource(outDoc);
						transformer.transform(outputsrc, outputFile);
					}
				}
				
					if(priority && write) {
						Node newNode = outDoc.createElement("dcvalue");
						Element elem = (org.w3c.dom.Element) newNode;
						elem.setAttribute("element", element);						
						if (!qualifier.isEmpty())
							elem.setAttribute("qualifier", qualifier);
						value=value.replaceAll("\\s+", " ").trim();
						elem.setTextContent(value);
						outDoc.getDocumentElement().appendChild(newNode);
						Transformer transformer = TransformerFactory.newInstance().newTransformer();
						StreamResult outputFile = new StreamResult(target);
						Source outputsrc = new DOMSource(outDoc);
						transformer.transform(outputsrc, outputFile);
					}
			}
			return true;
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			return false;
			
		}
	}
	
}
