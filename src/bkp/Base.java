package bkp;

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

public class Base extends Logics {
	static int count_item = 0, err_count = 0;
	public int range = 0;
	public boolean norangeSet = true;
	public static String source_root = "", target_root = "", logPath = "", configPath = "", schemaActionFile = "", runType = "";
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
	NDLSchemaDetails ndlschema = null;
	public Base() {
		
		try {
			
			dbf.setValidating(false);
			documentBuilder = dbf.newDocumentBuilder();
			ndlschema = new NDLSchemaDetails();
			
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
					
					f_target = new File(target_root + target);
					
					
					/** TODO : Add Field pending features: ** Critical.
					 * 1. Creation of new metadata files when absent at source.
					 *    pending design and implementation. 
					 */
					if(!staticWrite) {
						
						staticWrite = true;
						/**TODO
						 * add block to be parameterised. Automated curation module
						 * development component. Needs Attention.
						 */
						
						writeLrmiOthers();
					}
					
					process(source);
					
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

		try {
			currentDoc = getDocument(f_target, false);

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

				//field do not exist.
/*
				if (nodeNameNDL.equals("dc.contributor.advisor")) {
					textContent = docNode.getTextContent();
					ArrayList<String[]> result = curateAdvisor(textContent);
					for (String[] eachresult : result) {
						write("contributor", "advisor", eachresult[1], currentDoc);
					}
					continue;
				}
*/
				if(!schemaAction.containsKey(nodeNameNDL)) {
					try {
						
						writeRestNodes(nodeNameNDL,textContent,currentDoc);
						
					} catch( Exception e ) {
						
						if(!tgterprint) {
							er.println(err_count++ + " : " +targetPath);
							tgterprint = true;
						}
						er.println(nodeNameNDL + " : " + textContent);
						e.printStackTrace(er);
						
						writeRestNodes(nodeNameNDL,textContent,currentDoc);
					}
					
					continue;
					
				} 
				
				else if (schemaAction.get(nodeNameNDL).get(0).actionName.equalsIgnoreCase("deleteField")) {
					
					continue;
				} 
				
				else {
					
				if (nodeNameNDL.equals("dc.contributor.author") ) {
					try {		
						ArrayList<String[]> result = curateAuthor(textContent,nodeNameNDL);
						int test_count = 0;
						for (String[] eachresult : result) {
							if (test_count++ > 1)
								er.println(textContent + item);
							if (!eachresult[1].isEmpty()) {
								switch (eachresult[0]) {
								case "dc.contributor.author":
									write("contributor", "author", eachresult[1], currentDoc, true);
									break;
								case "dc.contributor.illustrator":
									write("contributor", "illustrator", eachresult[1], currentDoc, true);
									break;
								case "dc.contributor.other@organization":
									eachresult[1] = eachresult[1].replaceAll("\"", "\\\\\"");
									write("contributor", "other", "{\"organization\":\"" + eachresult[1] + "\"}",
											currentDoc, true);
									break;
								case "dc.contributor.other@studyGroup":
									eachresult[1] = eachresult[1].replaceAll("\"", "\\\\\"");
									write("contributor", "other", "{\"studyGroup\":\"" + eachresult[1] + "\"}",
											currentDoc, true);
									break;
								case "dc.publisher":
									write("publisher", "", eachresult[1], currentDoc, true);
									break;
								case "dc.publisher.institution":
									write("publisher", "institution", eachresult[1], currentDoc, true);
									break;
								case "dc.publisher.department":
									write("publisher", "department", eachresult[1], currentDoc, true);
									break;
								case "dc.subject":
									write("subject", "", eachresult[1], currentDoc, true);
									break;
								case "dc.relation.ispartofseries":
									write("relation", "ispartofseries", eachresult[1], currentDoc, true);
									break;
								case "dc.identifier.other@journal":
									eachresult[1] = eachresult[1].replaceAll("\"", "\\\\\"");
									write("identifier", "other", "{\"journal\":\"" + eachresult[1] + "\"}",
											currentDoc, true);
									break;
								case "ndl.sourceMeta.additionalInfo@authorInfo":
									eachresult[1] = eachresult[1].replaceAll("\"", "\\\\\"");
									writeOther("sourceMeta", "additionalInfo", "{\"authorInfo\":\"" + eachresult[1] + "\"}",
											new File(targetPath + "/metadata_ndl.xml"), true);
									break;
								default:
									break;
								}
							}
						}
						
				} catch (Exception e) {
					if(!tgterprint) {
						er.println(err_count++ + " : " +targetPath);
						tgterprint = true;
					}
					er.println(nodeNameNDL + " : " + textContent);
					e.printStackTrace(er);
					writeRestNodes(nodeNameNDL,textContent,currentDoc);
				}				
					continue;
				}		

				
				if (nodeNameNDL.equals("dc.contributor.other")) {
					ArrayList<String[]> result = curateContributorOther(textContent,nodeNameNDL);
					for (String[] eachresult : result) {
						write("contributor", "other", eachresult[1], currentDoc, true);
					}
					
					continue;
				}
			
				if (nodeNameNDL.equals("dc.creator.researcher")) {
					ArrayList<String[]> result = curateResearcher(textContent,nodeNameNDL);
					for (String[] eachresult : result) {
						write("creator", "researcher", eachresult[1], currentDoc, true);
					}
					
					continue;
				}

				if (nodeNameNDL.equals("dc.language.iso")) {
					try {
						ArrayList<String[]> result = curateLanguage(textContent, nodeNameNDL);
						for (String[] eachresult : result) {
							if (!eachresult[1].equalsIgnoreCase("dc.language.iso"))
								write("language", "iso", eachresult[1], currentDoc, true);
						}
					} catch (Exception e) {
						if(!tgterprint) {
							er.println(err_count++ + " : " +targetPath);
							tgterprint = true;
						}
						er.println(nodeNameNDL + " : " + textContent);
						e.printStackTrace(er);
						writeRestNodes(nodeNameNDL,textContent,currentDoc);
					}
					
					continue;
				}

				if (nodeNameNDL.equals("ndl.sourceMeta.additionalInfo")) {
					try {
						ArrayList<String[]> result = curateAdditionalInfo(textContent,nodeNameNDL);
						for (String[] eachresult : result) {
							for (String eachdc : eachresult[0].split(";"))
								switch (eachdc) {
								case "reference":
									eachresult[1] = eachresult[1].replaceAll("\"", "\\\\\"");
									write("sourceMeta", "additionalInfo", "{\"reference\":\"" + eachresult[1] + "\"}", currentDoc, true);
									break;
								case "abstract":
									writeOther("description", "abstract", eachresult[1], new File(targetPath + "/dublin_core.xml"), true);
									break;
								default:
									break;
								}
						}
					} catch (Exception e) {
						if(!tgterprint) {
							er.println(err_count++ + " : " +targetPath);
							tgterprint = true;
						}
						er.println(nodeNameNDL + " : " + textContent);
						e.printStackTrace(er);
						writeRestNodes(nodeNameNDL,textContent,currentDoc);
					}
					continue;
				}
				
				if (nodeNameNDL.equals("dc.description.abstract")) {
					try {
					ArrayList<String[]> result = curateDescAbstract(textContent,nodeNameNDL);
					for (String[] eachresult : result) {
						switch (eachresult[0]) {
						/**TODO
						 * description abstract is moved to different field in lookup operation. Lookup operation 
						 * to be formally defined and implemented. MoveField is not part of lookup. Lookup
						 * to incorporate copy field and useMap together.
						 */
						case "dc.description.abstract":
							write("description", "abstract", eachresult[1], currentDoc,true);
							break;
						case "lrmi.learningResourceType":
							writeOther("learningResourceType", "", eachresult[1], new File(targetPath + "/metadata_lrmi.xml"), false);
							break;
						case "dc.description":
							write("description", "", eachresult[1], currentDoc,true);
							break;
						case "ndl.sourceMeta.additionalInfo@RightsStatement":
							eachresult[1] = eachresult[1].replaceAll("\"", "\\\\\"");
							writeOther("sourceMeta", "additionalInfo", "{\"RightsStatement\":\""+eachresult[1] +"\"}", new File(targetPath + "/metadata_ndl.xml"),true);
							break;
						default:
							break;
						}
					}
				} catch (Exception e) {
					if(!tgterprint) {
						er.println(err_count++ + " : " +targetPath);
						tgterprint = true;
					}
					er.println(nodeNameNDL + " : " + textContent);
					e.printStackTrace(er);
					writeRestNodes(nodeNameNDL,textContent,currentDoc);
				}
					continue;
				}

				if (nodeNameNDL.equals("dc.description.uri")) {
					try {
						ArrayList<String[]> result = curateDescriptionUri(textContent, nodeNameNDL);
						for (String[] eachresult : result) {
							switch (eachresult[0]) {
								case "dc.description.uri":
									write("description", "uri", eachresult[1], currentDoc, true);
									break;
								default:
									break;
							}
						}
					} catch (Exception e) {
						if (!tgterprint) {
							er.println(err_count++ + " : " + targetPath);
							tgterprint = true;
						}
						er.println(nodeNameNDL + " : " + textContent);
						e.printStackTrace(er);
						writeRestNodes(nodeNameNDL, textContent, currentDoc);
					}
					continue;
				}
				
				if (nodeNameNDL.equals("dc.description")) {
					ArrayList<String[]> result = curateDescription(textContent,nodeNameNDL);
					for (String[] eachresult : result) {
						switch (eachresult[0]) {
						case "volume":
							write("identifier", "other", "{\"volume\":\""+eachresult[1]+"\"}", currentDoc,true);
							break;
						case "issue":
							write("identifier", "other", "{\"issue\":\""+eachresult[1]+"\"}", currentDoc,true);
							break;
						case "pages":
							write("format", "extent", eachresult[1], currentDoc,true);
							break;
						case "publisher":
							write("publisher", "", eachresult[1], currentDoc,true);
							break;
						case "dc.description":
							write("description", "", eachresult[1], currentDoc,true);
							break;
						case "dc.description.uri":
							write("description", "uri", eachresult[1], currentDoc,true);
							break;
						case "lrmi.learningResourceType":
							writeOther("learningResourceType", "", eachresult[1], new File(targetPath + "/metadata_lrmi.xml"), false);
							break;
						case "dc.type.degree":
							write("type", "degree", eachresult[1], currentDoc,true);
							break;
						case "dc.identifier.citation":
							write("identifier", "citation", eachresult[1], currentDoc,true);
							break;
						}
					}
					continue;
				}
				
				if (nodeNameNDL.equals("dc.identifier.uri")) {
					//textContent = textContent.replace("hdl.handle.net", "");
					write("identifier", "uri", textContent, currentDoc,true);
					continue;
				}
				

				if (nodeNameNDL.equals("ndl.sourceMeta.uniqueInfo")) {
					try {
						textContent = textContent.replaceAll("[\\s+_]", "").trim();
						if (!textContent.matches("(?i)(Biblio.*)|(p\\.?.*)"))
							writeOther("identifier", "other", "{\"uniqueId\":\"" + textContent + "\"}",
									new File(targetPath + "/dublin_core.xml"), true);
					} catch (Exception e) {
					}
						continue;
				}
				
				if (nodeNameNDL.equals("dc.identifier.citation")) {
					//PlaceHolder.
					continue;
				}
				
				if (nodeNameNDL.equals("dc.identifier.other")) {
					try {
						ArrayList<String[]> result = curateIdentifierOther(textContent,nodeNameNDL);
						for (String[] eachresult : result) {
							switch (eachresult[0]) {
							case "dc.identifier.other":
								write("identifier", "other", eachresult[1], currentDoc, true);
								break;
							default:
								break;
							}
						}
					} catch (Exception e) {
						if(!tgterprint) {
							er.println(err_count++ + " : " +targetPath);
							tgterprint = true;
						}
						er.println(nodeNameNDL + " : " + textContent);
						e.printStackTrace(er);
						writeRestNodes(nodeNameNDL,textContent,currentDoc);
					}
					continue;
				}
				
				if (nodeNameNDL.equals("dc.subject")) {
					ArrayList<String[]> result = curateSubject(textContent,nodeNameNDL);
					for (String[] eachresult : result) {
						switch (eachresult[0]) {
						case "subject":
							write("subject", "", eachresult[1], currentDoc,true);
							break;
						default:
							break;
						}
					}
					continue;
				}
				
				if (nodeNameNDL.equals("dc.format.extent")) {
					try {
						ArrayList<String[]> result = curateFormatExtent(textContent,nodeNameNDL);
						for (String[] eachresult : result) {
							for (String eachdc : eachresult[0].split(";"))
								switch (eachdc) {
								case "dc.format.extent":
									write("format", "extent", eachresult[1], currentDoc,true);
									break;
								case "dc.publisher.date":
									write("publisher", "date", eachresult[1], currentDoc,true);
									break;
								default:
									break;
								}
						}
					} catch (Exception e) {
						if(!tgterprint) {
							er.println(targetPath);
							tgterprint = true;
						}
						er.println(nodeNameNDL + " : " + textContent);
						e.printStackTrace(er);
						writeRestNodes(nodeNameNDL,textContent,currentDoc);
					}
					continue;
				}

				if (nodeNameNDL.equals("dc.type.degree")) {				
					type_degree = true;
					switch (textContent) {
					case "M.Phil.":
						write("type", "degree", "mphil", currentDoc, true);
						break;
					case "Ph.D.":
						write("type", "degree", "phd", currentDoc, true);
						break;
					case "Phd":
						write("type", "degree", "phd", currentDoc, true);
						break;
					case "Ph.D":
						write("type", "degree", "phd", currentDoc, true);
						break;
					default:
						break;
					}
					continue;
				}

				if (nodeNameNDL.equals("dc.date.awarded")) {					
					try {
						ArrayList<String[]> result = curateDateAwarded(textContent,nodeNameNDL);
						for (String[] eachresult : result)
							if (!eachresult[1].isEmpty()) {
								write("date", "awarded", eachresult[1], currentDoc, true);
							} else {
								write("date", "awarded", "2012-01-01", currentDoc, true);
							}
					} catch (Exception e) {
						if(!tgterprint) {
							er.println(targetPath);
							tgterprint = true;
						}
						er.println(nodeNameNDL + " : " + textContent);
						e.printStackTrace(er);
						writeRestNodes(nodeNameNDL,textContent,currentDoc);
					}
					continue;
				}


				if (nodeNameNDL.equals("dc.date.other")) {
					try {
						ArrayList<String[]> result = curateDateOther(textContent,nodeNameNDL);
						for (String[] eachresult : result) {
							write("date", "submitted", eachresult[1], currentDoc, true);
						}
					} catch (Exception e) {
						if(!tgterprint) {
							er.println(targetPath);
							tgterprint = true;
						}
						er.println(nodeNameNDL + " : " + textContent);
						e.printStackTrace(er);
						writeRestNodes(nodeNameNDL,textContent,currentDoc);
					}
					continue;
				}


				if (nodeNameNDL.equals("dc.date.submitted")) {
					try {
						String NDLdate = curateDate(textContent);
						if (!NDLdate.isEmpty()) {
							if (NDLdate.matches("\\d{2}[\\/-]\\d{2}[\\/-]\\d{2}"))
								NDLdate = NDLdate.replaceAll("(.*[\\/-])(\\d+)", "$120$2");
							write("date", "submitted", NDLdate, currentDoc, true);
						}
					} catch (Exception e) {
						if(!tgterprint) {
							er.println(targetPath);
							tgterprint = true;
						}
						er.println(nodeNameNDL + " : " + textContent);
						e.printStackTrace(er);
						writeRestNodes(nodeNameNDL,textContent,currentDoc);
					}
					continue;
				}


				if (nodeNameNDL.equals("dc.date.issued")) {
					try {
						ArrayList<String[]> result = curateDateIssued(textContent,nodeNameNDL);
						for (String[] eachresult : result) {
							for (String eachdc : eachresult[0].split(";"))
								switch (eachdc) {
								case "dc.date.issued":
									write("date", "issued", eachresult[1], currentDoc,true);
									break;
								case "dc.publisher.date":
									write("publisher", "date", eachresult[1], currentDoc,true);
									break;
								default:
									break;
								}
						}
					} catch (Exception e) {
						if(!tgterprint) {
							er.println(targetPath);
							tgterprint = true;
						}
						er.println(nodeNameNDL + " : " + textContent);
						e.printStackTrace(er);
						writeRestNodes(nodeNameNDL,textContent,currentDoc);
					}
					continue;
				}

				if (nodeNameNDL.equals("dc.title")) {
					if (handle_title_map.containsKey(handle)) {
						String title = handle_title_map.get(handle);
						write("title", "", title, currentDoc,true);
					} else {
						ArrayList<String[]> result = curateTitle(textContent,nodeNameNDL);
						for (String[] eachresult : result) {
							for (String eachdc : eachresult[0].split(";"))
								switch (eachdc) {
								case "dc.title":
									write("title", "", eachresult[1], currentDoc,true);
									break;
								default:
									break;
								}
						}
					}
					continue;
				}

				if (nodeNameNDL.equals("dc.title.alternative")) {
					try {
						ArrayList<String[]> result = curateTitleAlternative(textContent,nodeNameNDL);
						for (String[] eachresult : result) {
							write("title", "alternative", eachresult[1], currentDoc, true);
						}
					} catch (Exception e) {
					}
					continue;
				}
				
				if (nodeNameNDL.equals("dc.publisher.date")) {
					try {
						ArrayList<String[]> result = curatepublisherDate(textContent,nodeNameNDL);
						for(String[] eachresult : result) {
						if (!eachresult[1].isEmpty()) {
							write("publisher", "date", eachresult[1], currentDoc,true);
						}
					}
					} catch (Exception e) {
						if(!tgterprint) {
							er.println(err_count++ + " : " +targetPath);
							tgterprint = true;
						}
						er.println(nodeNameNDL + " : " + textContent);
						e.printStackTrace(er);
						writeRestNodes(nodeNameNDL,textContent,currentDoc);
					}
					continue;
				}
				
				if (nodeNameNDL.equals("dc.publisher")) {
					try {
						ArrayList<String[]> result = curatePublisher(textContent,nodeNameNDL);
						for(String[] eachresult : result) {
						if (!eachresult[1].isEmpty()) {
							write("publisher", "", eachresult[1], currentDoc,true);
						}
					}
					} catch (Exception e) {
						if(!tgterprint) {
							er.println(err_count++ + " : " +targetPath);
							tgterprint = true;
						}
						er.println(nodeNameNDL + " : " + textContent);
						e.printStackTrace(er);
						writeRestNodes(nodeNameNDL,textContent,currentDoc);
					}
					continue;
				}
				
				if (nodeNameNDL.equals("dc.publisher.institution")) {
					try {
						ArrayList<String[]> result = curatePublisherInstitution(textContent, nodeNameNDL);
						if (!result.isEmpty())
							for (String[] eachresult : result) {
								for (String eachdc : eachresult[0].split(";"))
									switch (eachdc) {
									case "dc.publisher":
										write("publisher", "", eachresult[1], currentDoc, true);
										break;
									case "dc.publisher.institution":
										write("publisher", "institution", eachresult[1], currentDoc, true);
										break;
									case "dc.publisher.department":
										write("publisher", "department", eachresult[1], currentDoc, true);
										break;
									case "dc.identifier.isbn":
										eachresult[1] = eachresult[1].replaceAll("\"", "\\\\\"");
										write("identifier", "isbn", eachresult[1], currentDoc, true);
										break;
									case "dc.identifier.issn":
										eachresult[1] = eachresult[1].replaceAll("\"", "\\\\\"");
										write("identifier", "issn", eachresult[1], currentDoc, true);
										break;
									case "dc.identifier.other@volume":
										eachresult[1] = eachresult[1].replaceAll("\"", "\\\\\"");
										write("identifier", "other", "{\"volume\":\"" + eachresult[1] + "\"}",
												currentDoc, true);
										break;
									case "dc.identifier.other@journal":
										eachresult[1] = eachresult[1].replaceAll("\"", "\\\\\"");
										write("identifier", "other", "{\"journal\":\"" + eachresult[1] + "\"}",
												currentDoc, true);
										break;
									case "dc.description":
										write("description", "", eachresult[1], currentDoc, true);
										break;
									case "dc.subject":
										write("subject", "", eachresult[1], currentDoc, true);
										break;
									case "dc.type.degree":
										write("type", "degree", eachresult[1],currentDoc, true);
										break;
									case "lrmi.learningResourceType":
										writeOther("learningResourceType", "", eachresult[1],
												new File(targetPath + "/metadata_lrmi.xml"), false);
										break;
									default:
										break;
									}
							}
					} catch (Exception e) {
						if (!tgterprint) {
							er.println(err_count++ + " : " + targetPath);
							tgterprint = true;
						}
						er.println(nodeNameNDL + " : " + textContent);
						e.printStackTrace(er);
						writeRestNodes(nodeNameNDL, textContent, currentDoc);
					}
					continue;
				}
				
				if (nodeNameNDL.equals("dc.publisher.department")) {
					try {
						ArrayList<String[]> result = curatePublisherDepartment(textContent,nodeNameNDL);
						for(String[] eachresult : result) {
							/**TODO
							 * Implement getNodeNameDetails(eachresult[0]);
							 */
							for (String eachdc : eachresult[0].split(";"))
							switch (eachdc) {
							case "dc.description":
								write("description", "", eachresult[1], currentDoc, true);
								break;
							case "dc.publisher.department":
								write("publisher", "department", eachresult[1], currentDoc, true);
								break;
							default:
								break;
							}
					}
					} catch (Exception e) {
						if(!tgterprint) {
							er.println(err_count++ + " : " +targetPath);
							tgterprint = true;
						}
						er.println(nodeNameNDL + " : " + textContent);
						e.printStackTrace(er);
						writeRestNodes(nodeNameNDL,textContent,currentDoc);
					}
					continue;
				}
				
				if (nodeNameNDL.equals("lrmi.learningResourceType")) {
					try {
						ArrayList<String[]> result = curatelrt(textContent,nodeNameNDL);
						for(String[] eachresult : result) {
							/**TODO
							 * Implement getNodeNameDetails(eachresult[0]);
							 */
							for (String eachdc : eachresult[0].split(";"))
							switch (eachdc) {
							case "lrmi.learningResourceType":
								write("learningResourceType", "", eachresult[1], currentDoc, true);
								break;
							default:
								break;
							}
						}
					} catch (Exception e) {
						if(!tgterprint) {
							er.println(err_count++ + " : " +targetPath);
							tgterprint = true;
						}
						er.println(nodeNameNDL + " : " + textContent);
						e.printStackTrace(er);
						writeRestNodes(nodeNameNDL,textContent,currentDoc);
					}
					continue;
				}
				
			}
				
			}	
			for (Map.Entry<String, ArrayList<Action>> entry : schemaAction.entrySet()) {
				for(Action action : entry.getValue()) 
					if(action.actionName.equals("add")){
					String writeNode = entry.getKey();
					HashMap<String,String> components = getNodeNameDetails(writeNode);
					if(currentDoc.getDocumentElement().getAttribute("schema").equals(components.get("schema")))
						write(components.get("element"), components.get("qualifier"), action.targetValue, currentDoc, true);
				}
			//writeDublinOthers(item);
			
			}
			//writeNDLOthers(item);


			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StreamResult outputFile = new StreamResult(f_target);
			Source outputsrc = new DOMSource(currentDoc);
			transformer.transform(outputsrc, outputFile);

		} catch (Exception e) {

			e.printStackTrace();
			e.printStackTrace(er);

		}
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
			//write("language", "iso", "eng", currentDoc,true);
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
		//writeOther("interactivityType", "", "expositive", new File(targetPath + "/metadata_lrmi.xml"),true);
		//writeOther("learningResourceType", "", "proceeding", new File(targetPath + "/metadata_lrmi.xml"),true);
		writeOther("educationalRole", "", "student", new File(targetPath + "/metadata_lrmi.xml"),true);
		writeOther("educationalRole", "", "teacher", new File(targetPath + "/metadata_lrmi.xml"),true);
		writeOther("educationalAlignment", "educationalLevel", "ug_pg", new File(targetPath + "/metadata_lrmi.xml"),true);
		writeOther("educationalAlignment", "educationalLevel", "career_tech", new File(targetPath + "/metadata_lrmi.xml"),true);
		//writeOther("educationalAlignment", "difficultyLevel", "medium", new File(targetPath + "/metadata_lrmi.xml"),true);
		
		
		for (Map.Entry<String, ArrayList<Action>> entry : schemaAction.entrySet()) {
			for(Action action : entry.getValue()) 
				if(action.actionName.equals("add")){
				String writeNode = entry.getKey();
				HashMap<String,String> components = getNodeNameDetails(writeNode);
				if(currentDoc.getDocumentElement().getAttribute("schema").equals(components.get("schema")))
					write(components.get("element"), components.get("qualifier"), action.targetValue, currentDoc, true);
			}
		//writeDublinOthers(item);
		
		}
		//writeNDLOthers(item);


		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StreamResult outputFile = new StreamResult(f_target);
		Source outputsrc = new DOMSource(currentDoc);
		transformer.transform(outputsrc, outputFile);
		
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
			nodeDetails.put("element", nodeParts[1]);
			nodeDetails.put("qualifier", nodeParts[2]);
		} else {
			nodeDetails.put("schema", nodeParts[0]);
			nodeDetails.put("element", nodeParts[1]);
			nodeDetails.put("qualifier", "");
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
			 * NDLSchema Information implemtation started. To be followed by 
			 * enhancements and complete information.
			 */
			// Temp fix for NDLSchema implementation.
			String ndlNodeName =element+"."+qualifier;
			ndlNodeName = ndlNodeName.replaceAll("\\.$", "");
			boolean multi = true;
			HashMap<String, Object> nodeProperties = ndlschema.getAtrributeType(ndlNodeName);
			if(nodeProperties != null)
				multi = (Boolean) nodeProperties.get("multiValued");
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
				//System.out.println(document.getDocumentElement().getAttribute("schema")+element + qualifier + value);
				value=value.replaceAll("\\s+", " ").trim();
				elem.setTextContent(value);
				document.getDocumentElement().appendChild(newNode);
				//System.out.println(ndlNodeName + value);
			}
			else {
				 /**
				 * TODO Pending Implementation module. Temp fix with below code.
				 * Implementation pending for Enhanced NDLShema constraints infprmation.
				 */ 
				boolean write = true;
				for(int i = 0; i< result.getLength(); i++) {
					String currVal = result.item(i).getTextContent();
					if(priority && currVal.equalsIgnoreCase(value)) {
						if(!multi){
							result.item(i).setTextContent(value);
							write = false;
							break;
						}
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
