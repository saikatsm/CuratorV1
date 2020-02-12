package bkp;

import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.InputVerifier;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.opencsv.CSVReader;

import AuthorNormalization.Name_Normalization;
import config.NDL_DataService;
import curation.Base;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class LOC_Logics {

	static NDL_DataService ndlDS = new NDL_DataService("http://10.4.8.239:65/services/", "normalizeDate");
	static NDL_DataService ndlDS_ddc = new NDL_DataService("http://10.4.8.239:65/services/", "getClassHierarchy");
	Name_Normalization nn;
	String dcFieldAuthor, dcFieldDate, dcFieldIdOth;
	HashMap<String, String[]> locauthmap = new HashMap<String, String[]>();
	HashMap<String, String[]> locdatemap = new HashMap<String, String[]>();
	HashMap<String, String[]> locidothmap = new HashMap<String, String[]>();
	HashMap<String, String[]> locpubmap = new HashMap<String, String[]>();
	HashMap<String, String> locsubmap = new HashMap<String, String>();
	HashMap<String, String> locpubplacemap = new HashMap<String, String>();
	HashMap<String, String> loclangmap = new HashMap<String, String>();
	HashMap<String, String> loctemporalmap = new HashMap<String, String>();
	HashMap<String, String> loclrtmap = new HashMap<String, String>();
	HashMap<String, String> locddcmap = new HashMap<String, String>();
	HashMap<String,ArrayList<String>> locauthmove = new HashMap<String,ArrayList<String>>();
	Set<String> locdesctonote = new HashSet<String>();
	Set<String> locdescdelete = new HashSet<String>();
	Set<String> locdescvalid = new HashSet<String>();
	PrintStream log = null;
	public Boolean getThumb; 

	public LOC_Logics() {

		try {
			nn = new Name_Normalization("/home/arunavo/Desktop/data/LOC/config/author/");
			CSVReader crauth = new CSVReader(
					new FileReader("/home/arunavo/Desktop/data/LOC/config/author/LOCauth_onetoone.csv"), '|', '"');
			for (String[] row : crauth.readAll())
				locauthmap.put(row[0], new String[] { row[1], row[2] });
			crauth.close();

			CSVReader crdate = new CSVReader(
					new FileReader("/home/arunavo/Desktop/data/LOC/config/author/LOCdate_onetoone.csv"), '|', '"');
			for (String[] row : crdate.readAll())
				locdatemap.put(row[0], new String[] { row[1], row[2] });
			crdate.close();
			
			CSVReader cridoth = new CSVReader(
					new FileReader("/home/arunavo/Desktop/data/LOC/config/author/LOCidoth_onetoone.csv"), '|','"');
			for (String[] row : cridoth.readAll())			
				locidothmap.put(row[0], new String[] { row[1], row[2] });
			cridoth.close();
			
			CSVReader crpublisher = new CSVReader(
					new FileReader("/home/arunavo/Desktop/data/LOC/config/author/LOCpub_onetoone.csv"), '|','"');
			for (String[] row : crpublisher.readAll())			
				locpubmap.put(row[0], new String[] { row[1].trim(), row[2].trim() });
			crpublisher.close();
			
			CSVReader crsubject = new CSVReader(
					new FileReader("/home/arunavo/Desktop/data/LOC/config/author/LOCsub_onetoone.csv"), '|','"');
			for (String[] row : crsubject.readAll())			
				locsubmap.put(row[0], row[1]);
			crsubject.close();
			
			CSVReader crpubplace = new CSVReader(
					new FileReader("/home/arunavo/Desktop/data/LOC/config/author/LOCpubplace_onetoone.csv"), '|','"');
			for (String[] row : crpubplace.readAll())			
				locpubplacemap.put(row[0], row[1]);
			crpubplace.close();
			
			CSVReader crlanguage = new CSVReader(
					new FileReader("/home/arunavo/Desktop/data/LOC/config/author/LOClang_onetoone.csv"), '|','"');
			for (String[] row : crlanguage.readAll())			
				loclangmap.put(row[0], row[1].trim());
			crlanguage.close();
			
			CSVReader crtemporal = new CSVReader(
					new FileReader("/home/arunavo/Desktop/data/LOC/config/author/LOCtemporal_onetoone.csv"), '|','"');
			for (String[] row : crtemporal.readAll())			
				loctemporalmap.put(row[0], row[1].trim());
			crtemporal.close();
			
			CSVReader crlrt = new CSVReader(
					new FileReader("/home/arunavo/Desktop/data/LOC/config/author/LOClrt_onetoone.csv"), '|','"');
			for (String[] row : crlrt.readAll())			
				loclrtmap.put(row[0], row[1].trim());
			crlrt.close();
			
			CSVReader crddc = new CSVReader(
					new FileReader("/home/arunavo/Desktop/data/LOC/config/author/LOCddc_onetoone.csv"), '|','"');
			for (String[] row : crddc.readAll())			
				locddcmap.put(row[0], row[1].trim());
			crddc.close();
			
			{
				ArrayList<String> rowValue_org = new ArrayList<String>();
				CSVReader crauthtoorg = new CSVReader(
						new FileReader("/home/arunavo/Desktop/data/LOC/config/author/LOCauthor_toorg.csv"), ',', '"');
				for (String[] row : crauthtoorg.readAll())
					rowValue_org.add(row[0].trim());
				locauthmove.put("organization", rowValue_org);
				crauthtoorg.close();

				ArrayList<String> rowValue_pub = new ArrayList<String>();
				CSVReader crauthtopub = new CSVReader(
						new FileReader("/home/arunavo/Desktop/data/LOC/config/author/LOCauthor_topub.csv"), ',', '"');
				for (String[] row : crauthtopub.readAll())
					rowValue_pub.add(row[0].trim());
				locauthmove.put("publisher", rowValue_pub);
				crauthtopub.close();

				ArrayList<String> rowValue_inst = new ArrayList<String>();
				CSVReader crauthtoinst = new CSVReader(
						new FileReader("/home/arunavo/Desktop/data/LOC/config/author/LOCauthor_toinst.csv"), ',', '"');
				for (String[] row : crauthtoinst.readAll())
					rowValue_inst.add(row[0].trim());
				locauthmove.put("institution", rowValue_inst);
				crauthtoinst.close();
			}
			
		//System.out.println(locidothmap.toString());
			log = new PrintStream(new File("/home/arunavo/Desktop/data/LOC/authlogfinal.csv"));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

	}

	public ArrayList<String[]> curatePublisherDate(String textContent) throws Exception {
		dcFieldDate = "";
		ArrayList<String[]> response = new ArrayList<>();
		JSONObject dateOth = new JSONObject(textContent);
		String dateValue,
				monthlist = "(jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|june?|july?|aug(?:ust)?|sep(?:tember)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?)";
		try {
			if (dateOth.has("publisher")) {
				dateValue = dateOth.getString("publisher");
				if (locdatemap.containsKey(dateValue)) {
					// System.out.println(dateValue);
					String[] dates = locdatemap.get(dateValue)[0].split(";");
					String[] fields = locdatemap.get(dateValue)[1].split(";");
					for (int i = 0; i < dates.length; i++) {
						String result = dates[i];
						dcFieldDate = fields[i];
						response.add(new String[] { dcFieldDate, result });
					}
				} else if (dateValue.matches(".*\\d+.*")) {
					if (dateValue.matches("(?i)" + monthlist + "\\s?(?:-\\s?" + monthlist + "\\s)?\\d+")) {
						// System.out.println("Hit1");
						dateValue = dateValue
								.replaceAll("(?i)" + monthlist + "\\s?(?:-\\s?" + monthlist + "\\s)?(\\d+)", "$1-$3");
						// System.out.println(dateValue);
						String result = ndlDS.getResult(new String[][] { { "dates[]", dateValue } }, "dates").get(0);
						// System.out.println(result);
						dcFieldDate = "publisher";
						response.add(new String[] { dcFieldDate, result });
					} else if (dateValue.matches("(?i)" + monthlist + "\\s(\\d+)(?:-\\s?" + monthlist + "\\s)?\\d+")) {
						// System.out.println("Hit2");
						dateValue = dateValue
								.replaceAll("(?i)" + monthlist + "\\s(\\d+)(-\\s?" + monthlist + "\\s\\d+)?", "$1-$2");
						String result = ndlDS.getResult(new String[][] { { "dates[]", dateValue } }, "dates").get(0);
						// System.out.println(result);
						dcFieldDate = "publisher";
						response.add(new String[] { dcFieldDate, result });
					} else if (dateValue.contains("c")) {
						String valueList[] = dateValue.split("c");
						Boolean replicateCopyright = false;
						for (int j = 0; j < valueList.length; j++) {
							//System.out.println(valueList[j]);
							dateValue = valueList[j].replace("[", "").replaceAll(".*?(\\d+).*", "$1");
							//System.out.println(dateValue);
							if (!dateValue.isEmpty()) {
								if (j == 0) {
									String result = ndlDS
											.getResult(new String[][] { { "dates[]", dateValue } }, "dates").get(0);
									dcFieldDate = "publisher";
									response.add(new String[] { dcFieldDate, result });
								} else if (replicateCopyright){
									dcFieldDate = "copyright";
									response.add(new String[] { dcFieldDate, dateValue });
									 String result = ndlDS.getResult(new String[][] { { "dates[]", dateValue } }, "dates").get(0);
									dcFieldDate = "publisher";
									response.add(new String[] { dcFieldDate, result });
								} else {
									dcFieldDate = "copyright";
									response.add(new String[] { dcFieldDate, dateValue });									
								}
							} else
								replicateCopyright = true;
						}
					} else {
						//System.out.println(dateValue);
						if(dateValue.equals("189-?]"))
							dateValue = "1890";
						if(dateValue.equals("18930014108379A"))
								dateValue = "1893";
						if(dateValue.equals("[185-?]"))
							dateValue = "1850";
						dateValue = dateValue.replaceAll(".*?(\\d+).*", "$1");
						String result = ndlDS.getResult(new String[][] { { "dates[]", dateValue } }, "dates").get(0);
						dcFieldDate = "publisher";
						response.add(new String[] { dcFieldDate, result });
					}
				}
			} else if (dateOth.has("sponsordate")) {
				dateValue = dateOth.getString("sponsordate");
				if (!dateValue.equals("NULL")) {
					String result = dateValue.substring(0, 4) + "-" + dateValue.substring(4, 6) + "-"
							+ dateValue.substring(6, 8);
					dcFieldDate = "sponsor";
					response.add(new String[] { dcFieldDate, result });
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", textContent });
		}
		return response;
	}

	public ArrayList<String[]> curateIdentifierOther(String textContent) {
		dcFieldIdOth = "volume";
		String blockID = "";
		ArrayList<String[]> response = new ArrayList<>();
		try {
			JSONObject input_jo = new JSONObject(textContent);
			if (locidothmap.containsKey(input_jo.toString())) {
				blockID = "0";
				//System.out.println("blockID : "+blockID);
				String[] values = locidothmap.get(input_jo.toString());
				response.add(new String[] { values[0] , values[1] });
			} else if (input_jo.has("volume")) {
				String ipValue = input_jo.getString("volume");
				if (ipValue.contains("Vol"))
					dcFieldIdOth = "volume";
				else if (ipValue.matches("(?i)(?:part|pt).*"))
					dcFieldIdOth = "part";
				else if (ipValue.matches("[\\d]+\\s(part|pt).*"))
					dcFieldIdOth = "volume;part";
				else if (ipValue.matches("(?i).*ed(ition)?\\.?.*"))
					dcFieldIdOth = "edition";
				//System.out.println(dcFieldIdOth);

				if (ipValue.matches("(?:(?:\\s?\\d\\.?\\s?)+(?:-|,|$))+")) {
					blockID = "1";
					//System.out.println("blockID : "+blockID);
					if (ipValue.contains(",")) {
						String values[] = ipValue.split(",");
						response.add(new String[] { dcFieldIdOth , values[0].trim() + "-" + values[values.length - 1].trim() });
					} else
						response.add(new String[] { dcFieldIdOth , ipValue.replaceAll("\\s","") });
				} else if (ipValue.matches(".*(&|and).*")) {
					blockID = "2";
					//System.out.println("blockID : "+blockID);
					response.add(new String[] {dcFieldIdOth , ipValue.replaceAll("(&|and)", "-").replaceAll("\\s", "")});
				} else if (ipValue.matches("(?i)(?:Vols?\\.?\\s\\d+(?:\\s?-\\s?\\d?)?)+(\\snos?.*)?")) {
					blockID = "3";
					//System.out.println("blockID : "+blockID);
					String value = ipValue.replaceAll("(?i)(Vols?\\.?)|\\s", "").trim();
					value = value.replaceAll(";", "-");
					value = value.replaceAll("(?i)nos\\.(\\d+-\\d+)?", "($1)");
					response.add(new String[] { dcFieldIdOth , value });
				}  else if (ipValue.matches("(?i)[\\d|I|L|V|X]+,?\\s(part|pt)\\.?.*")) {
					blockID = "4";
					//System.out.println("blockID : "+blockID);
					String values[] = ipValue.split("(?i)(pt|part)\\.?");
					for (int i = 0; i < values.length; i++) {
						if (i == 0) {
							dcFieldIdOth = "volume";
							response.add(new String[] { dcFieldIdOth , values[i].replaceAll(",","").trim() });
						} else {
							dcFieldIdOth = "part";
							response.add(new String[] { dcFieldIdOth , values[i].trim() });
						}
					}
				} else if (ipValue.matches("(?i).*((Vols?)|(part)|(pt)|(edition)|(ed))\\.?.*")) {
					blockID = "5";
					//System.out.println("blockID : "+blockID);
					response.add(new String[] {dcFieldIdOth ,ipValue.replaceAll("(?i)((Vols?)|(part)|(pt)|(edition)|(ed)|\\s)\\.?", "")});
				} else if (ipValue.matches("(?i)(ser)\\.?.*")) {
					blockID = "6";
					//System.out.println("blockID : "+blockID);
					String series = ipValue.replaceAll("(?i)ser\\.?\\s?(\\d+).*", "ser. $1");
					dcFieldIdOth = "part";
					response.add(new String[] { dcFieldIdOth , series });
					String volume = ipValue.replaceAll("(?i).*v\\.?\\s?(\\d+).*", "$1");
					dcFieldIdOth = "volume";
					response.add(new String[] { dcFieldIdOth , volume });
				} else if (ipValue.matches("(?i)\\d+(?:\\s|,)no?\\.?\\s?\\d+(?:\\s-\\s\\d+\\s?no?\\.?\\s?\\d+)+")) {
					blockID = "7";
					//System.out.println("blockID : "+blockID);
					dcFieldIdOth = "volume";
					response.add(new String[] { dcFieldIdOth , ipValue.replaceAll("(?i)\\s?(\\d+),?\\sno?\\.?\\s?(\\d+)\\s?", "$1($2)") });
				} else if (ipValue.matches("(?i).*no?\\.?.*")) {
					blockID = "8";
					//System.out.println("blockID : "+blockID);
					String values[] = ipValue.split("(?i)no?\\.?");
					if (values.length == 1 || values[0].isEmpty()) {
						dcFieldIdOth = "issue";
						response.add(new String[] { dcFieldIdOth , values[1].trim().replaceAll(",", "") });
					} else if (values.length >= 2) {
						dcFieldIdOth = "volume";
						response.add(new String[] { dcFieldIdOth , values[0].replaceAll(",|\\(", "").trim() });
						dcFieldIdOth = "issue";
						response.add(new String[] { dcFieldIdOth , 
								(values.length > 2 ? (values[1].replaceAll("\\)|\\s|-", "").trim() + "-" + values[values.length - 1].replaceAll("\\)|\\s|-", "")) : values[1].replaceAll("\\)|\\s", "").trim()) });
					}
				} else if (ipValue.matches("(?i).*no\\.?-no\\.?.*")) {
					blockID = "9";
					//System.out.println("blockID : "+blockID);
					dcFieldIdOth = "volume";
					response.add(new String[] { dcFieldIdOth , ipValue.replaceAll("no\\.?\\s?(\\d+)", "($1)") });
				}
			} else if (input_jo.has("alternativeLink")) {
				String ipValue = input_jo.getString("alternativeLink");
				if (ipValue.equalsIgnoreCase("http://medhistoryproject.org")
						|| ipValue.equalsIgnoreCase("mediahistoryproject.org"))
					ipValue = "http://mediahistoryproject.org";
				dcFieldIdOth = "alternativeLink";
				response.add(new String[] { dcFieldIdOth , ipValue.trim() });
			} else if (input_jo.has("alternativeLink")) {
				String ipValue = input_jo.getString("alternativeLink");
				if (ipValue.equalsIgnoreCase("http://medhistoryproject.org")
						|| ipValue.equalsIgnoreCase("mediahistoryproject.org"))
					ipValue = "http://mediahistoryproject.org";
				dcFieldIdOth = "alternativeLink";
				response.add(new String[] { dcFieldIdOth , ipValue.trim() });
			} else if (input_jo.has("alternativeLink")) {
				String ipValue = input_jo.getString("alternativeLink");
				if (ipValue.equalsIgnoreCase("http://medhistoryproject.org")
						|| ipValue.equalsIgnoreCase("mediahistoryproject.org"))
					ipValue = "http://mediahistoryproject.org";
				dcFieldIdOth = "alternativeLink";
				response.add(new String[] { dcFieldIdOth , ipValue.trim() });
			} else if (input_jo.has("alternativeLink")) {
				String ipValue = input_jo.getString("alternativeLink");
				if (ipValue.equalsIgnoreCase("http://medhistoryproject.org")
						|| ipValue.equalsIgnoreCase("mediahistoryproject.org"))
					ipValue = "http://mediahistoryproject.org";
				dcFieldIdOth = "alternativeLink";
				response.add(new String[] { dcFieldIdOth , ipValue.trim() });
			} else if (input_jo.has("alternativeLink")) {
				String ipValue = input_jo.getString("alternativeLink");
				if (ipValue.equalsIgnoreCase("http://medhistoryproject.org")
						|| ipValue.equalsIgnoreCase("mediahistoryproject.org"))
					ipValue = "http://mediahistoryproject.org";
				dcFieldIdOth = "alternativeLink";
				response.add(new String[] { dcFieldIdOth , ipValue.trim() });
			} else if (input_jo.has("alternativeLink")) {
				String ipValue = input_jo.getString("alternativeLink");
				if (ipValue.equalsIgnoreCase("http://medhistoryproject.org")
						|| ipValue.equalsIgnoreCase("mediahistoryproject.org"))
					ipValue = "http://mediahistoryproject.org";
				dcFieldIdOth = "alternativeLink";
				response.add(new String[] { dcFieldIdOth , ipValue.trim() });
			} else if (input_jo.has("alternativeLink")) {
				String ipValue = input_jo.getString("alternativeLink");
				if (ipValue.equalsIgnoreCase("http://medhistoryproject.org")
						|| ipValue.equalsIgnoreCase("mediahistoryproject.org"))
					ipValue = "http://mediahistoryproject.org";
				dcFieldIdOth = "alternativeLink";
				response.add(new String[] { dcFieldIdOth , ipValue.trim() });
			} else if (input_jo.has("alternativeLink")) {
				String ipValue = input_jo.getString("alternativeLink");
				if (ipValue.equalsIgnoreCase("http://medhistoryproject.org")
						|| ipValue.equalsIgnoreCase("mediahistoryproject.org"))
					ipValue = "http://mediahistoryproject.org";
				dcFieldIdOth = "alternativeLink";
				response.add(new String[] { dcFieldIdOth , ipValue.trim() });
			} else if (input_jo.has("alternativeLink")) {
				String ipValue = input_jo.getString("alternativeLink");
				if (ipValue.equalsIgnoreCase("http://medhistoryproject.org")
						|| ipValue.equalsIgnoreCase("mediahistoryproject.org"))
					ipValue = "http://mediahistoryproject.org";
				dcFieldIdOth = "alternativeLink";
				response.add(new String[] { dcFieldIdOth , ipValue.trim() });
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;

	}

	public ArrayList<String[]> curateAuthor(String textContent) {
		dcFieldAuthor = "";
		String inputName = textContent;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			if (locauthmap.containsKey(inputName)) {
				dcFieldAuthor = locauthmap.get(inputName)[0];
				String[] names = locauthmap.get(inputName)[1].split(";");
				for (String name : names)
					if(!name.equalsIgnoreCase("Remove")) {
						response.add(new String[] { dcFieldAuthor, name });
					}
					else
						response.add(new String[] { "", "Remove" });
			} else {
				if (inputName.matches("(?i).*\\bcollection\\b.*")) {
					dcFieldAuthor += "collection" + ";";
					 inputName = inputName.replaceAll("\\[from old catalog\\]", "").trim();
					 response.add(new String[] { dcFieldAuthor, inputName });
					 return response;
				}
				else if (inputName.matches("(?i).*\\b((d[eé]p(arte?men)?t\\.?)|(office)|(bureau)|(board)|(service)|(administration)|(library)|(division))\\b.*")) {
					dcFieldAuthor += "department" + ";" ;
					inputName = inputName.replaceAll("\\[from old catalog\\]", "").trim();
					 response.add(new String[] { dcFieldAuthor, inputName });
					 return response;
				} else {
					for(Map.Entry<String, ArrayList<String>> entry : locauthmove.entrySet()) {
						//System.out.println(entry.getValue());
						for(String token : entry.getValue())
							if(inputName.matches("(?i).*\\b"+token+"\\b.*")) {
								dcFieldAuthor += entry.getKey() + ";";
								inputName = inputName.replaceAll("\\.?\\s*\\[from old catalog\\]", "").replaceAll("[\\[\\]]+", "").trim();
								response.add(new String[] { dcFieldAuthor, inputName });
								return response;
						}
					}
				}
				if(inputName.contains("&")) {
						for(String parts : inputName.split("&")) {
							String[] tokens = parts.split("\\s");
							if(tokens.length == 1) {
								dcFieldAuthor += "publisher";
								inputName = inputName.replaceAll("\\.?\\s*\\[from old catalog\\]", "").trim();
								 response.add(new String[] { dcFieldAuthor, inputName });
								 return response;
						}
					}
				}
				if (inputName.matches(".*\\bill(us)?\\b.*"))
					dcFieldAuthor += "illustrator" + ";";
				if (inputName.matches(".*\\b(joint )?ed\\.?\\b.*"))
					dcFieldAuthor += "editor" + ";";
				if (inputName.matches(".*\\btr\\b.*"))
					dcFieldAuthor += "translator" + ";";
				dcFieldAuthor = dcFieldAuthor.replaceAll(";$", "");
				//System.out.println("LOC logics author block \n" + inputName);
				//System.out.println(dcFieldAuthor);
				ArrayList<String[]> result = nn.process(inputName);
				for (String[] eachName : result)
					if(eachName[1] != null || eachName[1].isEmpty()) {
						if (dcFieldAuthor.isEmpty())
							dcFieldAuthor = "author";
						response.add(new String[] { dcFieldAuthor, eachName[1] });
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", inputName });
		}
		return response;
	}
	
	public ArrayList<String[]> curatePublisher(String textContent) {
		String inputValue = textContent ;
		ArrayList<String[]> response = new ArrayList<>();
		
		if(locpubmap.containsKey(inputValue)) {
			response.add(locpubmap.get(inputValue));
			return response;
		}
		inputValue = inputValue.replaceAll("(\\?\\s*$)|(\\[n\\.p\\.\\])", "");
		inputValue = inputValue.replaceAll("^(?:\\[|\\))(.*)(?:\\]|\\))$", "$1");
		if(inputValue.matches("^\\[.*[^\\]]"))
			inputValue = inputValue.replaceFirst("\\[", "");
		inputValue = inputValue.replaceAll("(.*)\\?\\s*(.*)", "$1 [$2]");
		inputValue = inputValue.replaceAll("\\[\\s*\\]", "");
		
		if(locpubmap.containsKey(inputValue)) {
			response.add(locpubmap.get(inputValue));
			return response;
		}
		
		if(inputValue.matches("(?i).*((dept\\.?)|(department)).*")) {
			response.add(new String[] { "publisher.department", inputValue.trim() });
			return response;
		}
		
		if(inputValue.matches("(?i)(.*((inc\\.?)|(company))$)|(.*((co\\.?)|(printers?)|(ltd.)|(publishers?)|(printing)|(printed)|(association)|(press)).*)")) {
			response.add(new String[] { "publisher", inputValue.replaceAll("\\[etc\\.(, etc\\.)?\\]", "").trim() });
			return response;
		}
		try {
			if (inputValue.contains(",")) {
				String[] values = inputValue.split(",");
				//System.out.println(inputValue);
				if (inputValue.contains("[etc.]") || inputValue.contains("[etc., etc.]")) {
					values = inputValue.split("\\[etc\\.(, etc\\.)?\\]");
					//System.out.println(values.length);
					if(values.length!=1) {
					String[] places = values[0].split(",");
					for (String place : places) {
						place = place.replaceAll("\\(|\\)", "");
						response.add(new String[] { "publisher.place", place.trim() });
					}
					response.add(new String[] { "publisher", values[1].trim().replaceAll(";$","") });
					} else {
						//System.out.println("response block");
						response.add(new String[] { "publisher", values[0].trim().replaceAll(";$","") });
					}
				} else if (values.length == 2) {
					String[] places = values[0].split("and");
					for (String place : places) {
						place = place.replaceAll("\\(|\\)", "");
						response.add(new String[] { "publisher.place", place.trim().replaceAll(",", "") });
					}
					response.add(new String[] { "publisher", values[1].trim() });
				} else {
					response.add(new String[] { "publisher.place", values[0].replaceAll("\\(|\\)", "").trim() });
					if(values.length > 1)
					response.add(new String[] { "publisher.place", values[1].replaceAll("\\(|\\)", "").trim() });
					String pub = "";
					for (int i = 2; i < values.length; i++)
						pub += values[i].trim() + ", ";
					response.add(new String[] { "publisher", pub.trim().replaceAll(",$", "") });
				}
			} else if (inputValue.contains(":")) {
				//System.out.println("Publisher" + inputValue);
				String[] values = inputValue.split(":");
				response.add(new String[] { "publisher.place", values[0].replaceAll("\\(|\\)", "").trim() });
				if(values.length > 1)
				response.add(new String[] { "publisher", values[1].trim() });
			} else if (inputValue.contains("[etc.]") || inputValue.contains("[etc., etc.]")) {
				String[] values = inputValue.split("\\[etc\\.(, etc\\.)?\\]");
				if(values.length!=1) {
				String[] places = values[0].split(",");
				for (String place : places)
					response.add(new String[] { "publisher.place", place.replaceAll("\\(|\\)", "").trim() });
					response.add(new String[] { "publisher", values[1].trim().replaceAll(";$","") });
				} else
					response.add(new String[] { "publisher", values[0].trim().replaceAll(";$","") });
			} else if (inputValue.contains(";")) {
				String[] values = inputValue.split(";");
				String[] places = values[0].split(",");
				response.add(new String[] { "publisher.place", places[0].replaceAll("\\(|\\)", "").trim() });
				if(places.length > 1)
				response.add(new String[] { "publisher.place", places[1].replaceAll("\\(|\\)", "").trim() });
				if(values.length > 1)
				response.add(new String[] { "publisher", values[1].trim() });
			} else
				response.add(new String[] { "publisher", inputValue });
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}

	public ArrayList<String[]> curateSubject(String textContent) {
		String inputValue = textContent.trim() ;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			if(locsubmap.containsKey(inputValue)) {
			String loc = locsubmap.get(inputValue); 
				if(loc.contains(";"))
					for(String value : loc.split(";"))
						response.add(new String[] { "subject", value});
				else
					response.add(new String[] { "subject", loc});
			}
			else {
			inputValue = inputValue.replaceAll("--", "-").replaceAll("(\\.?\\s*\\[from old catalog\\])|\\?", "").replaceAll("[\\.\\-]+\\s*$", "").trim();
			inputValue = inputValue.replaceAll("(?<=\\d)-(?!\\d)|(?<!\\d)-(?=\\d)", "");
			response.add(new String[] { "subject", inputValue});
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curatePublisherPlace(String textContent) {
		String inputValue = textContent.trim() ;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			if(locpubplacemap.containsKey(inputValue)) {
			String mapReturnValue = locpubplacemap.get(inputValue); 
				if(mapReturnValue.contains(";"))
					for(String value : mapReturnValue.split(";"))
						response.add(new String[] { "publisher.place", value});
				else
					response.add(new String[] { "publisher.place", mapReturnValue});
			}
			else {
				response.add(new String[] { "publisher.place", inputValue});
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	public ArrayList<String[]> curateLanguage(String textContent) {
		String inputValue = textContent.trim() ;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			if(loclangmap.containsKey(inputValue)) {
			String mapReturnValue = loclangmap.get(inputValue); 
				if(mapReturnValue.contains(";"))
					for(String value : mapReturnValue.split(";"))
						response.add(new String[] { "language", value});
				else
					response.add(new String[] { "language", mapReturnValue});
			}
			else {
				response.add(new String[] { "language", inputValue});
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	public ArrayList<String[]> curateCoverageTemporal(String textContent) {
		String inputValue = textContent.trim() ;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			if(loctemporalmap.containsKey(inputValue)) {
			String mapReturnValue = loctemporalmap.get(inputValue); 
				if(mapReturnValue.contains(";"))
					for(String value : mapReturnValue.split(";"))
						response.add(new String[] { "relation.ispartofseries", value});
				else
					response.add(new String[] { "relation.ispartofseries", mapReturnValue});
			}
			else {
				response.add(new String[] { "relation.ispartofseries", inputValue});
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curateLRT(String textContent) {
		String inputValue = textContent.trim() ;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			Boolean defaultLRT = true;
			for(Map.Entry<String, String> entry : loclrtmap.entrySet()) {
				//System.out.println(inputValue + entry.getKey());
			if(inputValue.toLowerCase().contains(entry.getKey().toLowerCase())) {
				defaultLRT = false;
			String mapReturnValue = entry.getValue();
			if(mapReturnValue.equals("report") && inputValue.toLowerCase().contains("annual report"))
				mapReturnValue = "annualReport";
				if(mapReturnValue.contains(";"))
					for(String value : mapReturnValue.split(";"))
						response.add(new String[] { "lrt", value});
				else
					response.add(new String[] { "lrt", mapReturnValue});
				break;
			}
		}
			if(defaultLRT){
				response.add(new String[] { "lrt", "book"});
			}
		} catch (Exception e) {
			e.printStackTrace(Base.er);
			response.add(new String[] { "", inputValue });
		}
		return response;
	}
	
	public ArrayList<String[]> curateDDC(String textContent) {
		String inputValue = textContent.trim() ;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			if(locddcmap.containsKey(inputValue)) {
			String mapReturnValue = locddcmap.get(inputValue); 
				if(mapReturnValue.contains(";")) {
					String[] mapReturnValues = mapReturnValue.split(";"); 
					String[][] ndlDS_ddc_param = new String[mapReturnValues.length+1][2];
					for(int i = 0; i< mapReturnValues.length; i++) {
						ndlDS_ddc_param[i][0] = "codes[]";
						ndlDS_ddc_param[i][1] = mapReturnValues[i];
					}
					ndlDS_ddc_param[mapReturnValues.length][0] = "type";
					ndlDS_ddc_param[mapReturnValues.length][1] = "ddc";
						ArrayList<String> ddcList = ndlDS_ddc.getResult(ndlDS_ddc_param, "dc.subject.ddc"); 
					for(String ddc : ddcList)
						response.add(new String[] { "ddc", ddc});
				}
				else {
					ArrayList<String> ddcList = ndlDS_ddc.getResult(new String[][]{{"codes[]",mapReturnValue},{"type","ddc"}}, "dc.subject.ddc");
					for(String ddc : ddcList)
						response.add(new String[] { "ddc", ddc});
					//response.add(new String[] { "ddc", mapReturnValue});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curateFormatExtent(String textContent) {
		String inputValue = textContent.trim() ;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			JSONObject jo = new JSONObject(inputValue);
			int pageCount = Integer.parseInt(jo.getString("pageCount"));
				response.add(new String[] { "pageCount", jo.put("pageCount", pageCount).toString()});
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curateDescription(String textContent) {
		String inputValue = textContent.trim() ;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			if(locdesctonote.contains(inputValue))
				response.add(new String[] { "note", inputValue});
			else if(locdescvalid.contains(inputValue))
				response.add(new String[] { "description", inputValue});
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curateRightsLicense(String textContent) {
		String inputValue = textContent.trim() ;
		ArrayList<String[]> response = new ArrayList<>();
		try {
					response.add(new String[] { "note", inputValue});
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curateEdualignEduframe(String textContent) {
		String inputValue = textContent.trim() ;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			response.add(new String[] { "note", inputValue});
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curateTitle(String textContent) {
		String inputValue = textContent.trim() ;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			inputValue = inputValue.replace("\"", "");
			inputValue = inputValue.replaceAll("--", "");
			inputValue = inputValue.replaceAll("((?<=^)\\.\\.\\.?)|(\\.\\.\\.?(?=$))|([:;_-]+$)|([\\[\\]\\!]+)", "").trim();
			response.add(new String[] { "title", inputValue});
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curateAdditionalInfo(String textContent) {
		String inputValue = textContent.trim();
		ArrayList<String[]> response = new ArrayList<>();
		try {
			JSONObject jo = new JSONObject(inputValue);
			if(!getThumb && jo.has("asset")) {
			/*if(jo.getJSONObject("asset").getString("format").equalsIgnoreCase("Animated GIF")) {
				getThumb = true;
				inputValue = jo.getJSONObject("asset").getString("path") + "/" + jo.getJSONObject("asset").getString("name");
				response.add(new String[] { "thumbnail", inputValue});
			} else if (jo.getJSONObject("asset").getString("format").equalsIgnoreCase("JPEG Thumb")) {
				getThumb = true;
				inputValue = jo.getJSONObject("asset").getString("path") + "/" + jo.getJSONObject("asset").getString("name");
				response.add(new String[] { "thumbnail", inputValue});
			}*/
			
			if(jo.getJSONObject("asset").getString("format").equalsIgnoreCase("DjVuTXT")) {
				getThumb = true;
				inputValue = jo.getJSONObject("asset").getString("path") + "/" + jo.getJSONObject("asset").getString("name");
				response.add(new String[] { "fulltext", inputValue});
			} else if (jo.getJSONObject("asset").getString("format").equalsIgnoreCase("DjVu")) {
				getThumb = true;
				inputValue = jo.getJSONObject("asset").getString("path") + "/" + jo.getJSONObject("asset").getString("name").replaceAll(".djvu","_djvu.txt");
				response.add(new String[] { "fulltext", inputValue});
			}
			return response;
		}
		} catch (Exception e) {
			e.printStackTrace();
			//response.add(new String[] { "", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curateAsset(String textContent) {
		String inputValue = textContent.trim() ;
		String marker = "", uri = "";
		ArrayList<String[]> response = new ArrayList<>();
		try {
			JSONObject jo = new JSONObject(inputValue);
			if(jo.has("asset")) {
				marker = jo.getJSONObject("asset").getString("format");
				uri = jo.getJSONObject("asset").getString("path")+jo.getJSONObject("asset").getString("name");
			//if(checkUrl(uri) != 404)
				response.add(new String[] { marker+"|uri", uri });		
			response.add(new String[] { marker+"|size", jo.getJSONObject("asset").getString("size") });
		}
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	public int checkUrl(String url_str) {
		try {
		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("172.16.2.30", 8080));
		URL url = new URL(url_str);		
		HttpURLConnection huc =  (HttpURLConnection)  url.openConnection(proxy);
		huc.setRequestMethod("POST");
		huc.setUseCaches(false);
		huc.setDoInput(true);
		huc.setDoOutput(true);
	    huc.connect();
	    return huc.getResponseCode();
		} catch (Exception e) {
			return 0;
		}
 
	}
	}
	/*
	 * public static void main(String[] args) throws Exception{ // TODO
	 * Auto-generated method stub
	 * 
	 * }
	 */

