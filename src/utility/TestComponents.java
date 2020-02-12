package utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import curation.Base;
import curation.FieldMethodMap;

public class TestComponents extends Base {
	int count_item = 0;
	ArrayList<String> fieldList = new ArrayList<String>();
	ArrayList<CSVWriter> writerList = new ArrayList<CSVWriter>();
	HashMap<String, ArrayList<String[]>> fieldMap = new HashMap<String, ArrayList<String[]>>();
	HashMap<String, Set<String>> fieldvalues = new HashMap<String, Set<String>>();
	boolean dumpType = true;
	FieldMethodMap fm = null;
	static Scanner sc = new Scanner(System.in);

	public TestComponents(boolean generateRawDump, String configurationPath) throws Exception {
		
		fm = new FieldMethodMap(configurationPath);
		
		this.dumpType = generateRawDump;
	}

	public TestComponents() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void traverse(File input) throws Exception {

		try {

			boolean countItem = true;
			if (count_item < range || norangeSet)
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
				}

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	@Override
	public void process(File item) throws Exception {
		try {
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

				if (fieldList.contains(nodeNameNDL)) {

					textContent = docNode.getTextContent();
					/*
					 * dumpType determines the type of CSV generation, whether it only contains raw
					 * data or transformed data as well. Default value TRUE to generate raw dump
					 * information.
					 */

					if (fieldvalues.containsKey(nodeNameNDL) && fieldvalues.get(nodeNameNDL).contains(textContent))
						continue;

					else {

						Set<String> values = new HashSet<String>();

						if (fieldvalues.containsKey(nodeNameNDL)) {

							values = fieldvalues.get(nodeNameNDL);
							values.add(textContent);
							fieldvalues.put(nodeNameNDL, values);

						} else {

							values.add(textContent);
							fieldvalues.put(nodeNameNDL, values);
						}

						if (dumpType) {

							if (fieldMap.containsKey(nodeNameNDL)) {

								String row[] = new String[] { handle, textContent };
								ArrayList<String[]> rows = fieldMap.get(nodeNameNDL);
								rows.add(row);
								fieldMap.put(nodeNameNDL, rows);

							} else {

								String row[] = new String[] { handle, textContent };
								ArrayList<String[]> rows = new ArrayList<String[]>();
								rows.add(row);
								fieldMap.put(nodeNameNDL, rows);

							}

						} else {

							ArrayList<String[]> response = fm.curateField(nodeNameNDL, textContent);
							ArrayList<String[]> rows = new ArrayList<String[]>();
							String[] row = new String[4];
							
							for (String[] eachresult : response) {

								for (String eachdc : eachresult[0].split(";")) {


									row[0] = handle;
									row[1] = textContent;
									row[2] = eachdc;
									row[3] = eachresult[1];
								}
							}
							if(fieldMap.containsKey(nodeNameNDL)) {
								
								rows = fieldMap.get(nodeNameNDL);
								rows.add(row);
								
							}
							
							fieldMap.put(nodeNameNDL, rows);
						}
					}
				}
			}
		} catch (Exception e) {
			
			System.out.println(e.getMessage());
		}
	}

	void processListData(String nodeNameNDL, String textContent) throws Exception {

		ArrayList<String[]> response = fm.curateField(nodeNameNDL, textContent);
		ArrayList<String[]> rows = new ArrayList<String[]>();

		for (String[] eachresult : response) {

			for (String eachdc : eachresult[0].split(";")) {

				String[] row = new String[3];
				row[0] = textContent;
				row[1] = eachdc;
				row[2] = eachresult[1];
				rows.add(row);
			}
		}

		fieldMap.put(nodeNameNDL, rows);

	}

	ArrayList<String[]> getInteractiveOutput(String nodeNameNDL, String textContent) throws Exception {

		ArrayList<String[]> response = fm.curateField(nodeNameNDL, textContent);

		return response;

	}

	public void createLog() throws Exception {

		for (int i = 0; i < fieldList.size(); i++) {
			File log = new File(logPath + fieldList.get(i) + ".csv");
			log.createNewFile();
			writerList.add(new CSVWriter(new FileWriter(log)));

			if (!dumpType) {
				
				ArrayList<String[]> rows = new ArrayList<String[]>();
				rows.add(new String[] { "handle", "input", "marker", "output" });
				fieldMap.put(fieldList.get(i), rows);
				
			} else {
				
				ArrayList<String[]> rows = new ArrayList<String[]>();
				rows.add(new String[] { "handle", "input" });
				fieldMap.put(fieldList.get(i), rows);
			}
		}
		
	}
	
	static boolean continueExecution(String message) {
		
		boolean retVal = true;
		
		while (true) {

			System.out.println(message);
			
			String scannerInput = sc.nextLine();

			if (scannerInput.equalsIgnoreCase("Y")) {
				
				retVal = true;
				break;

			} else if (scannerInput.equalsIgnoreCase("N")) {
				
				retVal =  false;
				break;

			} else {
				
				System.out.println("Your input didn't match with the expected input.");
			}
		}
		
		return retVal;
	}
	
	public void writeLog() throws Exception {

		for (Map.Entry<String, ArrayList<String[]>> entry : fieldMap.entrySet()) {
			int writerID = fieldList.indexOf(entry.getKey());
			writerList.get(writerID).writeAll(entry.getValue());
			writerList.get(writerID).close();
		}
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		String runTypeFlag = args[0];
		String propPath = args[1];

		if (propPath.isEmpty())
			throw new Exception("Properties File not set.");

		Properties prop = new Properties();
		File properties = new File(propPath);
		InputStream input = new FileInputStream(properties);

		prop.load(input);

		Base.source_root = prop.getProperty("source_root");
		Base.logPath = prop.getProperty("logPath");
		Base.configPath = prop.getProperty("configPath");
		String fieldListPath = prop.getProperty("fieldListPath");
		String dataFilePath = prop.getProperty("dataFilePath");
		String fieldListName = prop.getProperty("fieldListName");
		String dumpTypeInput = prop.getProperty("dumpType");
		String isSampleRun = prop.getProperty("isSampleRun");
		String sampleRange = prop.getProperty("sampleRange");

		input.close();
		
		try {

			if (runTypeFlag == null || Base.source_root == null || Base.logPath == null || Base.configPath == null
					|| dumpTypeInput == null || fieldListName == null) {

				throw new Exception("One or Many mandatory parameters not set. Please check" + " ReadMe manual. \nExiting Program.");
			}

			else {
			
				if (!runTypeFlag.equalsIgnoreCase("-i")) {

					File log = new File(logPath);

					if (!log.exists())
						throw new Exception("Log Path doesn't exist. Please check config file " + propPath + "\nExiting Program.");

					else if (log.listFiles().length > 0) {

						boolean proceed = continueExecution("Old Files exist in logPath. The program will delete and create new Files."
										+ "\nDo you wish to proceed? (Y/N)");

						if (proceed) {
							
							for (File files : log.listFiles())
								files.delete();

						} else {
							
							throw new Exception("Exiting Program.");
							
						}

					}
				}
				TestComponents tc = null;

				dumpTypeInput = dumpTypeInput.toLowerCase();

				if (!dumpTypeInput.matches("raw|curated")) {

					throw new Exception("Dump Type value is set as " + dumpTypeInput + ". Please set a value between Raw/Curated."
							+ "\n Exiting Program.");

				} else if (dumpTypeInput.matches("raw")) {

					tc = new TestComponents(true, properties.getParent());

				} else {

					tc = new TestComponents(false, properties.getParent());

				}

				if (fieldListPath == null)
					fieldListPath = properties.getParent();
				
				if (!fieldListPath.endsWith("/"))
					fieldListPath += "/";
				
				File f_FieldList = new File(fieldListPath + fieldListName);

				if (!f_FieldList.exists())
					throw new Exception("Field List file not found at " + f_FieldList.getAbsolutePath() 
						+ "\n Exiting Program.");

				CSVReader cr = new CSVReader(new FileReader(f_FieldList));
				List<String[]> allRows = cr.readAll();

				for (String row[] : allRows) {

					try {

						tc.fm.checkFieldValidity(row[0]);
						tc.fieldList.add(row[0].trim());

					} catch (Exception e) {

						System.out.println(e.getMessage());
						continue;
					}
				}
				cr.close();

				boolean continueExecution = true;

				if (tc.fieldList.size() != allRows.size()) {
					
					continueExecution = continueExecution("Some field did not match NDL schema. No outputfile will"
							+ " be generated for them. Do you wish to continue? (Y/N)");
					
				}

				runTypeFlag = runTypeFlag.toLowerCase();

				if (runTypeFlag.equals("-s") && continueExecution) {

					if (isSampleRun != null) {

						if (isSampleRun.equalsIgnoreCase("true")) {

							if (sampleRange != null) {

								try {

									tc.range = Integer.parseInt(sampleRange);
									tc.norangeSet = false;

								} catch (Exception e) {
									throw new Exception(
											"Sample run detected. Please set numeric values for Sample Range "
													+ "in the properties file. Exiting program.");
								}
							} else {
								throw new Exception("Sample run detected. Please set sample range value "
										+ "in the properties file. Exiting program");
							}
						} else
							throw new Exception("Sample run detected. Please check paremeter values"
									+ "in the properties file. Exiting program.");
					}

					File source_root = new File(Base.source_root);

					tc.createLog();
					tc.traverse(source_root);
					tc.writeLog();
					
					sc.close();

				} else if (runTypeFlag.toLowerCase().equals("-f") && continueExecution) {

					tc.createLog();
					
					int fieldCounter = 0;
					
					if (dataFilePath == null)
						dataFilePath = fieldListPath;
					else if (!dataFilePath.endsWith("/"))
						dataFilePath += "/";
					
					fieldLoop: for (String field : tc.fieldList) {

						fieldCounter ++;
						File listData = new File(dataFilePath + field + ".csv");

						if (listData.exists()) {

							System.out.println("Reading " + listData.getAbsolutePath());

							int dataCount = 0;
							CSVReader inputData = new CSVReader(new FileReader(listData), '\n');

							for (String[] row : inputData.readAll()) {

								dataCount++;

								try {

									tc.processListData(field, row[0]);

								} catch (Exception e) {

									System.out.println(e.getMessage());
									
									if(fieldCounter != tc.fieldList.size())
										continueExecution = continueExecution("Continue Execution for other fields? (Y/N)");
									else
										continueExecution = false;
									
									if(continueExecution) {
										
										continue fieldLoop;
										
									}
									else {
										
										System.out.println("Exiting Progam.");
										break fieldLoop;
									}			
								}
							}

							inputData.close();

							System.out.println("Read Count : " + dataCount);

						} else {

							System.out.println("List data doesn't exist for " + field
									+ "\nPlease create list data at " + listData.getAbsolutePath());
							
							if(fieldCounter != tc.fieldList.size())
								continueExecution = continueExecution("Continue Execution for other fields? (Y/N)");
							else
								continueExecution = false;
							
							if(!continueExecution) {
								
								System.out.println("Exiting Progam.");
								break fieldLoop;
							}
						}
					}

					tc.writeLog();
					
					sc.close();
				} else if (runTypeFlag.toLowerCase().equals("-i")) {

					System.out.println("You have selected interactive mode. Dump type is always Curated.");

					dumpTypeInput = "curated";
					
					tc = new TestComponents(true, properties.getParent());

					boolean continueRun = true, setField = true;
					
					String nodeNameNDL = "";
					ArrayList<String[]> response = null;

					while (continueRun) {

						if (setField) {

							System.out.println("Please provide the field name : ");
							nodeNameNDL = sc.nextLine();
							try {

								tc.fm.checkFieldValidity(nodeNameNDL);

							} catch (Exception e) {

								System.out.println(e.getMessage());
								
							boolean proceed = continueExecution("Do you wish to continue with any other field? (Y/N)");
								
								if (proceed) {
									
									continue;
									
								} else {
									
									throw new Exception("Exiting Program.");
									
								}
								
							}
							setField = false;
						}

						System.out.println("Please provide the field value : ");
						String textContent = sc.nextLine();

						try {
							
							if(!textContent.isEmpty())
								response = tc.getInteractiveOutput(nodeNameNDL, textContent);

						} catch (Exception e) {

							System.out.println(e.getMessage());
							
							boolean proceed = continueExecution("Do you wish to continue with any other field? (Y/N)");
							
							if (proceed) {
								
								setField = true;
								
								continue;
								
							} else {
								
								throw new Exception("Exiting Program.");
								
							}

						}
						
						System.out.println("Input : " + textContent);
					
						if(!(response == null || textContent.isEmpty())) {
							
							for (String[] result : response) {
								for (int i = 0; i < result.length; i++) {
								
									String key = i == 0 ? "Marker" : "Value" ;
								
									System.out.println( key + " : " + result[i]);
								
								}
							} 
						}
						

						while (true) {

							System.out.println("Continue[C], Change Field[F], Exit[E] ? ");
							String runMode = sc.nextLine();

							if (runMode.trim().equalsIgnoreCase("F")) {

								setField = true;
								break;

							} else if (runMode.trim().equalsIgnoreCase("C")) {

								continueRun = true;
								break;

							} else if (runMode.trim().equalsIgnoreCase("E")) {
								
								System.out.println("Exiting Program.");
								continueRun = false;
								break;

							} else {
								System.out.println("Invalid input " + runMode);
							}
						}
					}
					
					sc.close();
					
				} else {
					
					if(!continueExecution)
						throw new Exception("Exiting Program.");
					else
						throw new Exception("Invalid run type. Run type is within Source[-s]/File[-f]/Interactive[-i]."
								+ "\n Exiting Program.");
				}
			}
		} catch (Exception e) {

			System.out.println(e.getMessage());
			
			sc.close();
		}
	}
}
