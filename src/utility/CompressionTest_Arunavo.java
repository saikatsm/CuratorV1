package utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.FileWriter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.opencsv.CSVReader;

import org.iitkgp.ndl.data.compress.CompressedDataItem;
import org.iitkgp.ndl.data.compress.TarGZCompressedFileReader;

public class CompressionTest_Arunavo {
	static int count;
	static int counter = 1;
	private static Map<String, Integer> nodeindexmap = new HashMap<String, Integer>();
	private static HashMap<String, ArrayList<String>> dataMap = new HashMap<String, ArrayList<String>>();
	private static Set<String[]> rowset = new HashSet<String[]>();
	private static String outputPath = "";
	private static int thresold = 100;
	private static int low = 0;
	private static int high = thresold;
	public static String[] row;
	public static String p = "";
	public static PrintWriter pw = null;

	public static void main(String[] args) throws Exception {

		System.out.println("Program started ...");
		long start = System.currentTimeMillis();
		if (args.length != 2)
			throw new ArithmeticException(
					"Wrong Input Argument. Command line should be provide source path and destination path");
		if (args[0].isEmpty() | args[1].isEmpty()) {
			throw new NullPointerException("Please provide valid data");
		}
		File source = new File(args[0]);
		outputPath = args[1];
		if (source.exists()) {
			traverse(source);
		}
		multiplexmltocsv(outputPath, count);
		long end = System.currentTimeMillis();
		System.out.println("Finished in " + (end - start) / (1000 * 60) + "m");

	}

	public static void traverse(File parent) throws IOException {
//iterating  file from tar.gz file
		TarGZCompressedFileReader reader = new TarGZCompressedFileReader(parent);
		reader.init();
		CompressedDataItem item;
		try {
			//
			while ((item = reader.next()) != null) {
				String s1 = new String(item.getEntryName());
				String parentString = s1.substring(0, s1.lastIndexOf("/"));
				if (!p.equals(parentString)) {
					p = parentString;
					System.out.println("Accessing File : " + count + " : " + parentString);
					if (!dataMap.isEmpty() )
						if(dataMap.containsKey("lrmi.learningResourceType"))
							if(dataMap.get("lrmi.learningResourceType").get(0).equals("thesis"))
								setrowSet(dataMap);
					dataMap.clear();
					count++;
				}

				if (item.getEntryName().endsWith(".xml")) {
					String fileContents = new String(item.getContents());
					// parsing filecontent of xml file
					parseDublin(fileContents);
				}
				if (item.getEntryName().endsWith("handle")) {
					String HandleID = new String(item.getContents());
					// adding Handle ID in the ArrayList
					dataMap.put("handleId", new ArrayList() {{	add(HandleID);	}});
				}
			}
			
			pw = new PrintWriter(new File(outputPath + "/kavitaResult.csv"));
			System.out.println(pw.toString());
			int authorIdx = nodeindexmap.get("dc.contributor.author");
			for (String[] eachrow :  rowset) {
				System.out.println(eachrow[authorIdx]);
				if(eachrow[authorIdx] != null) {
					String[] authors = eachrow[authorIdx].split("\\|");
					for(String auth : authors)
						pw.println(auth);
				}
			}
			pw.close();
			
			for (int count = 0; count < rowset.size(); count++) {
				if (count == high) {
					multiplexmltocsv(outputPath, count);
					low = high;
					high = low + thresold; // each csv file will contains thresold number of items
				}
			}


		} catch (Exception e) {
			System.out.println(e);
		}

	}

	private static void multiplexmltocsv(String path, int count) {
		String csvName = path + count + ".csv";
		//System.out.println("CSV name: "+csvName);
		File csvFile = new File(csvName);
		if (!csvFile.getParentFile().exists())
			csvFile.getParentFile().mkdirs();
		String[] header;

		header = new String[nodeindexmap.size()];
		for (Map.Entry<String, Integer> entry : nodeindexmap.entrySet()) {
			header[entry.getValue()] = entry.getKey();

		}
		try {
			FileWriter fW = new FileWriter(csvFile);
			CSVPrinter csvPrinter = new CSVPrinter(fW, CSVFormat.DEFAULT.withHeader(header));

			for (String[] onerow : rowset) {
				csvPrinter.printRecord(Arrays.asList(onerow));
			}
			csvPrinter.close();
			rowset.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void setrowSet(HashMap<String, ArrayList<String>> valueMap) {
		// TODO Auto-generated method stub
		String[] row = new String[nodeindexmap.size()];
		for (Map.Entry<String, ArrayList<String>> entry : valueMap.entrySet()) {
			String columnname = entry.getKey();
			int columnindex = 0;
			if (nodeindexmap.containsKey(columnname))
				columnindex = nodeindexmap.get(columnname);
			String data = "";
			for (String eachValue : entry.getValue())
				data += eachValue + "|";
			data = data.replaceAll("\\|$", ""); //replace the last "|" 
			row[columnindex] = data;
		}
		row[0] = valueMap.get("handleId").get(0);
		rowset.add(row);

	}

	private static void parseDublin(String fileContents) {
		// TODO Auto-generated method stub
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
			Document dcf = documentBuilder.parse(new InputSource(new java.io.StringReader(fileContents)));
			dcf.getDocumentElement().normalize();

			// Parse attributes of rootnode
			Element rootElement = dcf.getDocumentElement();
			ArrayList rootElementlist = listAllAttributes(rootElement);
			String attrVal = rootElementlist.get(0).toString();

			NodeList nList = dcf.getDocumentElement().getChildNodes();
			nodeindexmap.put("WEL_V2/ID", 0);
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					ArrayList<String> attrlist = listAllAttributes(eElement);
					int listSize = attrlist.size();
					String columnname = "";

					for (int k = 0; k < listSize; k++) {
						if (k == (listSize - 1)) {
							columnname = columnname + attrlist.get(k);
						} else {
							columnname = columnname + attrlist.get(k) + ".";
						}
					}
					columnname = attrVal + "." + columnname;

					if (!nodeindexmap.containsKey(columnname)) {
						nodeindexmap.put(columnname, counter++);
					}
//					System.out.println("node index map  :"+nodeindexmap);

					String textContent = eElement.getTextContent();
					if (dataMap.containsKey(columnname))
						dataMap.get(columnname).add(textContent);
					else
						dataMap.put(columnname, new ArrayList() {
							{
								add(textContent);
							}
						});
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private static ArrayList<String> listAllAttributes(Element element) {

		// get a map containing the attributes of this node
		NamedNodeMap attributes = element.getAttributes();

		// get the number of nodes in this map
		int numAttrs = attributes.getLength();
		ArrayList<String> attributeslist = new ArrayList<String>();

		for (int i = 0; i < numAttrs; i++) {
			Attr attr = (Attr) attributes.item(i);
			String attrName = attr.getNodeName();
			String attrValue = attr.getNodeValue();
			attributeslist.add(attrValue);
		}
		return attributeslist;
	}

}