package curation.wip;

import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.opencsv.CSVReader;

public class PostProcessing extends Base {

	HashMap<String, ArrayList<String>> titleMap = new HashMap<String, ArrayList<String>>();
	PrintStream printFieldDuplicates, printTitleDuplicates, printFieldCuration;
	HashMap<String[], String[]> fieldTranslation = new HashMap<String[], String[]>();
	HashMap<String, String> handle_map = new HashMap<String, String>();
	String itemPath = "";
	
	public PostProcessing() throws Exception {		
	
		File f_outFD = new File(logPath+"/out_field_duplicates");
		f_outFD.createNewFile();
		printFieldDuplicates = new PrintStream(f_outFD);
		
		File f_outCur = new File(logPath+ "/out_field_curation");
		f_outCur.createNewFile();
		printFieldCuration = new PrintStream(f_outCur);
		
		File f_outTD = new File(logPath + "/out_title_duplicates");
		f_outTD.createNewFile();
		printTitleDuplicates = new PrintStream(f_outTD);
		
		CSVReader crFT = new CSVReader(new FileReader(configPath + "/fieldtranslation.csv"), '|');
		for (String[] row : crFT.readAll()) {
			fieldTranslation.put(new String[] { row[0].trim(), row[1].trim() }, new String[] { row[2].trim(), row[3].trim() });	
		}
		crFT.close();
		
		CSVReader crLang = new CSVReader(new FileReader(configPath + "/lang-iso.csv"));
		for (String[] row : crLang.readAll()) {
			handle_map.put(row[0].trim(), row[1].trim());
		}
		crLang.close();
	}

	/**
	 * @see wrapper.Base_Curation_Shodhganga#process(java.io.File)
	 * This is an override of the method which calls the post processings on 
	 * the initially translated data. The postprocessing methods include
	 * 1. OnetoOne mapping translations.
	 * 2. Global Curation process if any.
	 * 3. Duplicate field value removals.
	 * 4. Duplicate Title updations.
	 * The methods are independent to each other and always generated new output XML Document in each run.
	 */
	
	@Override
	public void process(File item) throws Exception {
	
		try {
			
			Document inputDoc = documentBuilder.parse(item);
	        Document targetDoc = null;
			itemPath = item.getAbsolutePath();
			
			//One to One mapping translations on data. The csv file is loaded initially.
			//Translations can happen intra fields and inter fields.
			targetDoc = mapOnetoOne(inputDoc);
			
			//Global Curation procedure if any. The method run on all the values for the field
			//for which the procedure is defined.
			targetDoc = curate(targetDoc);			
			
			//Removal of fields nodes and values which contains duplicate data.
			//This is intra XML.
			targetDoc = removeDuplicateFieldValue(targetDoc);
			
			//Method to update duplicate title.
			if(item.getName().contains("dublin_core"))
				updateTitle(targetDoc);
			
			/*
			 * Write output XML to output file mentioned in the global static variable f_target.
			 */
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StreamResult outputFile = new StreamResult(f_target); 
			Source outputsrc = new DOMSource(targetDoc);
			transformer.transform(outputsrc, outputFile);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			System.out.println(item.getAbsoluteFile());
			e.printStackTrace(er);
			er.println(item.getAbsoluteFile());
			
		}
	}

	Document curate(Document inputDoc) throws Exception {
        
		// Generate blank XML Document with applicable root node appended.
		Document outputDoc = getDocument(f_target, true);
		
		try {
			
			Element root = inputDoc.getDocumentElement();
			String schema = root.getAttribute("schema");
			NodeList docNodes = root.getChildNodes(); 
	        
			for (int i = 0; i < docNodes.getLength(); i++) {
				Node docNode = docNodes.item(i);
				if (docNode.getNodeType() != Node.ELEMENT_NODE)
					continue;

				String nodeNameNDL = formReadable(docNode, schema);
				String textContent = docNode.getTextContent().replaceAll("\\s+", " ").trim();
/*				
				if (nodeNameNDL.equals("dc.creator.researcher")) {
					textContent = docNode.getTextContent();
					ArrayList<String[]> result = curateResearcher(textContent);
					for (String[] eachresult : result) {
						write("creator", "researcher", eachresult[1], outputDoc);
					}
					continue;
				}
				*/
/*
				if (nodeNameNDL.equals("dc.contributor.advisor")) {
					textContent = docNode.getTextContent();
					ArrayList<String[]> result = curateAdvisor(textContent);
					for (String[] eachresult : result) {
						write("contributor", "advisor", eachresult[1], outputDoc);
					}
					continue;
				}
			*/
				if (nodeNameNDL.equals("dc.description.uri"))
					continue;
				
				String[] writeNode = nodeNameNDL.split("\\.");
				if (writeNode.length == 2)
					write(writeNode[1], "", textContent, outputDoc,true);
				else
					write(writeNode[1], writeNode[2], textContent, outputDoc,true);
				
			}
				/*XPath xPath = XPathFactory.newInstance().newXPath();
				String expression = "/dublin_core/dcvalue[@element='publisher' and @qualifier='department']";
				Node dept = (Node) xPath.compile(expression).evaluate(outputDoc, XPathConstants.NODE);
				if (dept != null) {
					String nodeNameNDL = formReadable(dept, schema);
					String textContent = dept.getTextContent().trim();
					if (nodeNameNDL.equals("dc.publisher.department") && textContent.equals("Department of Hindi")) {
						expression = "/dublin_core/dcvalue[@element='language' and @qualifier='iso']";
						Node lang = (Node) xPath.compile(expression).evaluate(outputDoc, XPathConstants.NODE);
						String langValue = lang.getTextContent().trim();
						if (langValue.equals("eng"))
							lang.setTextContent("hin");
					}
				}
				if (handle_map.containsKey(handle) && f_target.getName().matches("dublin_core\\.xml")) {
					String expression_lang = "/dublin_core/dcvalue[@element='language' and @qualifier='iso']";
					Node lang = (Node) xPath.compile(expression_lang).evaluate(outputDoc, XPathConstants.NODE);
					lang.setTextContent(handle_map.get(handle));
				}*/
			
				return outputDoc;

		} catch (Exception e) {
			
			return outputDoc;
		}

	}
	
Document mapOnetoOne(Document inputDoc) {
	
	Document outputDoc = getDocument(f_target, true);
	
	try {
		
		Element root = inputDoc.getDocumentElement();
		String schema = root.getAttribute("schema");
		NodeList docNodes = root.getChildNodes();
		
		for (int i = 0; i < docNodes.getLength(); i++) {
			Node docNode = docNodes.item(i);
			if (docNode.getNodeType() != Node.ELEMENT_NODE)
				continue;

			String nodeNameNDL = formReadable(docNode, schema);
			String textContent = docNode.getTextContent().replaceAll("\\s+", " ").trim();
			
		Boolean write = true;
		if(textContent.isEmpty())
			write = false;
		else {
		for (Map.Entry<String[], String[]> entry : fieldTranslation.entrySet()) {
			String fieldName = entry.getKey()[0].trim();
			String srcValue = entry.getKey()[1].trim();
			String marker = entry.getValue()[0].trim();
			if (nodeNameNDL.equals(fieldName) && (textContent.equals(srcValue) || srcValue.isEmpty())) {
				if(!marker.isEmpty()) {
					String[] writeValues = entry.getValue()[1].split(";");
					for(String writeValue : writeValues) {
						//printFieldCuration.println(target.getAbsolutePath() + ":" + nodeNameNDL + ":" + textContent + ":" + writeValue);
						String[] writeNode = marker.split("\\.");
						if (writeNode.length == 2)
							write(writeNode[1], "", writeValue, outputDoc,true);
						else
							write(writeNode[1], writeNode[2], writeValue, outputDoc,true);
					}
					write = false;
					break;
				} else {
					write = false;
					//printFieldCuration.println(target.getAbsolutePath() + ":" + nodeNameNDL + ":" + srcValue + ":Remove");
				}
				}
		}
	}
		if(write) {
			String[] writeNode = nodeNameNDL.split("\\.");
			if (writeNode.length == 2)
				write(writeNode[1], "", textContent, outputDoc,true);
			else
				write(writeNode[1], writeNode[2], textContent, outputDoc,true);
		}		
		}
		return outputDoc;
	} catch(Exception e) {
		return outputDoc;
	}
}
/*
 * Remove Duplicate Nodes from XML Document. A new document is produced after this procedure and returned back 
 * as the target document to the calling method process.
 */
	Document removeDuplicateFieldValue(Document inputDoc) throws Exception {

		HashMap<String, ArrayList<String>> fieldMap = new HashMap<String, ArrayList<String>>();
		Document outputDoc = getDocument(f_target, true);
		
		Element root = inputDoc.getDocumentElement();
		String schema = root.getAttribute("schema");
		NodeList docNodes = root.getChildNodes();
		
		for (int i = 0; i < docNodes.getLength(); i++) {
			Node docNode = docNodes.item(i);
			
			if (docNode.getNodeType() != Node.ELEMENT_NODE)
				continue;

			ArrayList<String> values;

			String nodeNameNDL = formReadable(docNode, schema);
			String textContent = docNode.getTextContent();
			
			if (fieldMap.containsKey(nodeNameNDL)) {
				values = fieldMap.get(nodeNameNDL);
				if (!values.contains(textContent)) {
					values.add(textContent);
					fieldMap.put(nodeNameNDL, values);
					String[] write = nodeNameNDL.split("\\.");
					if (write.length == 2)
						write(write[1], "", textContent, outputDoc,true);
					else
						write(write[1], write[2], textContent, outputDoc,true);
				} else {
					//printFieldDuplicates.println(target.getAbsolutePath() + " : " + nodeNameNDL + " : " + textContent);
				}
			} else {
				values = new ArrayList<String>();
				values.add(docNode.getTextContent());
				fieldMap.put(nodeNameNDL, values);
				
				String[] write = nodeNameNDL.split("\\.");
				if (write.length == 2) {	
					write(write[1], "", textContent, outputDoc,true);
				}
				else {
					write(write[1], write[2], textContent, outputDoc,true);
				}
			}
		}
		
		return outputDoc;
		
	}
	
/*
 * Checks for an item title across collection. Titles are stored in a class variable HashMap data structure.
 * If a match of title is found the first occurrence is marked with a duplicate title flag Y.
 * Items having duplicate titles at later occurrences are curated at once. The initial occurence of 
 * duplicate title item is curated calling another method updateExisting() after initial iteration is completed.
 */

	void updateTitle(Document inputDoc) {
		
		try {
		
			XPath xPath = XPathFactory.newInstance().newXPath();
			String expression = "/dublin_core/dcvalue[@element='title']";
			
			Node title = (Node) xPath.compile(expression).evaluate(inputDoc, XPathConstants.NODE);
			
			String textContent = title.getTextContent();
			
			ArrayList<String> titleValue;
			
			if (titleMap.containsKey(textContent)) {
				
				titleValue = titleMap.get(textContent);
				titleValue.set(1, "Y");
				titleMap.put(textContent, titleValue);
			
				String newTitle = updateTitleValue(textContent, inputDoc);
				title.setTextContent(newTitle);
				
				printTitleDuplicates.println(itemPath);
				printTitleDuplicates.println(textContent + "\n" + title.getTextContent());
				
			} else {
				
				titleValue = new ArrayList<String>();
				titleValue.add(f_target.getAbsolutePath());
				titleValue.add("N");
				titleMap.put(textContent, titleValue);
				
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}
	
	
	String updateTitleValue(String oldTitle, Document in) {
		
		oldTitle = oldTitle.replaceAll("\\(.*", "").trim();
		String newTitle = "";
		
		try {
			
			XPath xPath = XPathFactory.newInstance().newXPath();
			String expression = "/dublin_core/dcvalue[@element='creator' and @qualifier='researcher']";
			
			NodeList targetNodes = (NodeList) xPath.compile(expression).evaluate(in, XPathConstants.NODESET);
				
			String textContent = "(";
			
			for (int i = 0; i < targetNodes.getLength(); i++) {
			
				textContent = textContent + targetNodes.item(i).getTextContent()+", ";
					
					/*JSONObject jo = new JSONObject(textContent);
					if (jo.has("volume")) {
						newTitle = oldTitle + " (volume - " + jo.getString("volume") + ")";
						break;
					} else if (jo.has("edition")) {
						newTitle = oldTitle + " (edition - " + jo.getString("edition") + ")";
						break;
					}*/
				}
				
				textContent = textContent.trim().replaceAll(",$", ")");
				newTitle = oldTitle + " " + textContent;
				
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		if (newTitle.isEmpty())
			newTitle = oldTitle;
		
		return newTitle.trim();
	}

	void updateExisting() {
		
		for (Map.Entry<String, ArrayList<String>> entry : titleMap.entrySet()) {
			
			if (entry.getValue().get(1).equals("Y")) {
				
				try {
					
					File item = new File(entry.getValue().get(0));
					Document inputDoc = documentBuilder.parse(item);
					
					XPath xPath = XPathFactory.newInstance().newXPath();
					String expression = "/dublin_core/dcvalue[@element='title']";
					
					Node titleNode = (Node) xPath.compile(expression).evaluate(inputDoc, XPathConstants.NODE);
					titleNode.setTextContent(updateTitleValue(entry.getKey(), inputDoc));
					
					printTitleDuplicates.println("From Map " +item.getAbsolutePath());
					printTitleDuplicates.println(entry.getKey() + "\n" + titleNode.getTextContent());

					Transformer transformer = TransformerFactory.newInstance().newTransformer();
					StreamResult outputFile = new StreamResult(item); 
					Source outputsrc = new	DOMSource(inputDoc); 
					transformer.transform(outputsrc, outputFile);
					
				} catch (Exception e) {
					
					e.printStackTrace();
					
				}
			}
		}
	}

}
