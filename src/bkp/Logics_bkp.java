package bkp;

import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

import com.opencsv.CSVReader;


import config.NDL_DataService;

public class Logics_bkp {

	static NDL_DataService ndlDS = new NDL_DataService("http://10.4.8.239:65/services/", "normalizeDate");
	static NDL_DataService ndlDS_ddc = new NDL_DataService("http://10.4.8.239:65/services/", "getClassHierarchy");
	NDL_DataService ndlDS_lang = new NDL_DataService("http://10.4.8.239:65/services/", "normalizeLanguage");
	//Name_Normalization nn;
	public String dcFieldAuthor, dcFieldDate, dcFieldDeptValue, deptFullValue;
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
	HashMap<String, String> lang_other_map = new HashMap<String, String>();
	HashMap<String,ArrayList<String>> locauthmove = new HashMap<String,ArrayList<String>>();
	Set<String> locdesctonote = new HashSet<String>();
	Set<String> locdescdelete = new HashSet<String>();
	Set<String> locdescvalid = new HashSet<String>();
	Set<String> removeTokens_TitleAlternative = new HashSet<String>();
	Set<String> removeTokens_additionalInfo = new HashSet<String>();
	Set<String> removeTokens_descAbstract = new HashSet<String>();
	PrintStream log = null;
	public Boolean getThumb; 

	public Logics_bkp() {

		try {
			//nn = new Name_Normalization(Base.configPath + "/author/");			
			CSVReader crddc = new CSVReader(
					new FileReader(Base.configPath + "/ddc.csv"), '|','"');
			for (String[] row : crddc.readAll()) {		
				//System.out.println(row[0] + row[1].trim());
				locddcmap.put(row[0], row[1].trim());
			}
			crddc.close();
			
			CSVReader crlangoth = new CSVReader(
					new FileReader(Base.configPath + "/language_other.csv"), '|','"');
			for (String[] row : crlangoth.readAll()) {		
				//System.out.println(row[0] + row[1].trim());
				lang_other_map.put(row[0], row[1].trim());
			}
			crlangoth.close();
			
			CSVReader crTitleAlternative = new CSVReader(
					new FileReader(Base.configPath + "/titleAlternative.csv"), '|','"');
			for (String[] row : crTitleAlternative.readAll())
				removeTokens_TitleAlternative.add(row[0]);
			crTitleAlternative.close();
			
			CSVReader cradditionalInfo = new CSVReader(
					new FileReader(Base.configPath + "/additionalInfo.csv"), '|','"');
			for (String[] row : cradditionalInfo.readAll())
				removeTokens_additionalInfo.add(row[0]);
			cradditionalInfo.close();
			
			CSVReader crdescAbstract = new CSVReader(new FileReader(Base.configPath + "/abstract.csv"), '|','"');
			for (String[] row : crdescAbstract.readAll())
				removeTokens_descAbstract.add(row[0]);
			crdescAbstract.close();
			
		//System.out.println(locidothmap.toString());
			log = new PrintStream(new File("/home/arunavo/Desktop/data/LOC/authlogfinal.csv"));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

	}	
	
	public ArrayList<String[]> curateDateAwarded(String textContent) throws Exception {
		textContent = textContent.replaceAll("\\s+", " ");
		if(textContent.matches("\\d{2}[\\/-]\\d{2}[\\/-]\\d{2}"))
			textContent = textContent.replaceAll("(.*[\\/-])(\\d+)","$120$2");
		ArrayList<String[]> response = new ArrayList<>();
		String dateValue = "";
		String monthlist = "(jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|june?|july?|aug(?:ust)?|sep(?:tember)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?)";
		try {
			dateValue = curateDate(textContent);
			//System.out.println(dateValue);
			if(!dateValue.isEmpty())
				response.add(new String[] { "date.awarded", dateValue });
		} catch (Exception e) {
			Base.er.println(textContent);
			e.printStackTrace(Base.er);
		}
		return response;
	}
	
	public ArrayList<String[]> curateDateOther(String textContent) throws Exception {
		String inputValue = textContent.replaceAll("\\s+", " ").trim();
		ArrayList<String[]> response = new ArrayList<>();
		String  dateValue = "";
		try {
			JSONObject jo = new JSONObject(textContent);
			inputValue = jo.getString("sponsordate").trim();
			dateValue = curateDate(inputValue);
			if(!dateValue.isEmpty()) {
				if(dateValue.matches("\\d{2}[\\/-]\\d{2}[\\/-]\\d{2}"))
					dateValue = dateValue.replaceAll("(.*[\\/-])(\\d+)","$120$2");
				response.add(new String[] { "date.submitted", dateValue });
			}
		} catch (Exception e) {
			Base.er.println(inputValue);
			e.printStackTrace(Base.er);
		}
		return response;
	}
	
	public String curateDate(String dateInput) throws Exception {
		String returnValue = "";
		String inputValue = dateInput;
		try {
			//System.out.println(inputValue);
			returnValue = ndlDS.getResult(new String[][] { { "dates[]", inputValue } }, "dates").get(0);
			System.out.println(returnValue);
		} catch (Exception e) {
		}
		return returnValue;
	}
	

	public ArrayList<String[]> curateSubject(String textContent) {
		textContent = textContent.replaceAll("\\s+", " ");
		String inputValue = textContent.trim();
		ArrayList<String[]> response = new ArrayList<>();
		try {
			String[] values = inputValue.split(",|;");
			for(String value : values) {
				value = value.trim();
			if(!value.toUpperCase().matches("(^(?=[MDCLXVI])M*D?C{0,4}L?X{0,4}V?I{0,4}$)|(n.d.|-|A4|NA)")) {
			String[] removeterms = {"NA","Ke","ke", "Ka", "Ek", "YES", "University Name","Department of", "Dept. of", "School of", "College of"};
			for(String term : removeterms)
				value = value.replaceAll("(?i)\\b"+term+"\\b", "").trim();
			if(value.matches(".*\\d{1,3}.*")) {
				String endingPage = value.replaceAll("(\\d+).*","$1");
				response.add(new String[] { "startingPage", "1"});
				response.add(new String[] { "endingPage", endingPage});
				response.add(new String[] { "pageCount", endingPage});
			}
			else if(!value.matches("[A-Z\\d\\W]+")) {
				value = org.apache.commons.text.WordUtils.capitalizeFully(value);
				response.add(new String[] { "subject", value});
			} else {
				response.add(new String[] { "subject", value});
			}
		}
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curateAdvisor(String textContent) {
		textContent = textContent.replaceAll(",", ", ").replaceAll("\\s+", " ");
		String inputValue = textContent.trim();
		ArrayList<String[]> response = new ArrayList<>();
		try {
			String[] removeTokens = {"Dr\\.?","Prof\\.?(essor)?","\\(?Mrs\\.?\\)"};
			String[] names = inputValue.split(";|\\band\\b");
			for (String eachName : names) {
				for(String token : removeTokens)
					eachName = eachName.replaceAll("(?i)\\b"+token+"\\b", "");
				//System.out.println(eachName);
				eachName = eachName.replaceAll("([.]+)|(\\(.*?\\))?","").replaceAll("\\s+", " ");
				eachName = eachName.replaceAll("\\.(?!(\\s|$))", ". ");
				eachName = eachName.trim();
				eachName = org.apache.commons.text.WordUtils.capitalizeFully(eachName, ' ');
				eachName = eachName.replaceAll("([A-Z])(?!=\\.)(\\s|$)","$1. ");
				response.add(new String[] { "advisor", eachName.trim() });
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curateResearcher(String textContent) {
		textContent = textContent.replaceAll("\\s+", " ");
		String inputValue = textContent.trim();
		ArrayList<String[]> response = new ArrayList<>();
		try {
			inputValue = inputValue.replaceAll("\\(.*\\)", "");
			inputValue = inputValue.replaceAll("\\b(Md\\.?\\s|n\\.?d\\.?)\\b", "").replaceAll("\\s+"," ");
			inputValue = inputValue.replaceAll("\\b([A-Z](?!\\.))\\b", "$1.");
			response.add(new String[] { "researcher", inputValue });
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curateTitleAlternative(String textContent) {
		textContent = textContent.replaceAll("\\s+", " ");
		String inputValue = textContent.trim() ;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			for(String removeToken : removeTokens_TitleAlternative)
				inputValue = inputValue.replaceAll("\\b"+removeToken,"");
			if(!inputValue.isEmpty())
			response.add(new String[] { "title.alternative", inputValue});
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curatePublisherDepartment(String textContent) {
		textContent = textContent.replaceAll("\\s+", " ");
		String inputValue = textContent.trim() ;
		deptFullValue = inputValue;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			String[] checkToken = {"Centre for", "School of", "Dept. of", "Institute of", "College of", "Faculty of","College", "Department of" };
			for(String check : checkToken)
				if(inputValue.startsWith(check)) {
					dcFieldDeptValue = inputValue.replace(check,"").trim();
					response.add(new String[] { "publisher.dept", inputValue});
					return response;
			}
			dcFieldDeptValue = inputValue;
			inputValue = "Department of " + inputValue;
			response.add(new String[] { "publisher.dept", inputValue});
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curateLanguage(String textContent) {
		textContent = textContent.replaceAll("\\s+", " ");
		String inputValue = textContent.trim() ;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			if(inputValue.matches("(?i)Others?")) {
				response.add(new String[] { "language", "other" });
			}
			else {
				//System.out.println(inputValue);
				String lang_ndlformat = ndlDS_lang.getResult(new String[][] { { "codes[]", inputValue } }, "dc.language.iso").get(0);
				response.add(new String[] { "language", lang_ndlformat});
			}
		} catch (Exception e) {
			if(lang_other_map.containsKey(deptFullValue))
				response.add(new String[] { "language", lang_other_map.get(deptFullValue)});
			else
				response.add(new String[] { "language", "eng"});
			//Base_Curation.er.println(inputValue);
			//e.printStackTrace(Base_Curation.er);
		}
		return response;
	}
	
	public ArrayList<String[]> curateDDC(String textContent) {
		textContent = textContent.replaceAll("\\s+", " ");
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
		}
		return response;
	}
	
	public ArrayList<String[]> curateFormatExtent(String textContent) {
		textContent = textContent.replaceAll("\\s+", " ").trim();
		String startingPage = "", endingPage = "";
		Integer pageCount;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			String inputValue = "";
			try {
			JSONObject jo = new JSONObject(textContent);
			if(jo.has("pageCount"))
				inputValue = jo.getString("pageCount");
			else if(jo.has("dimensionHeight"))
				inputValue = jo.getString("dimensionHeight");
			} catch (Exception e ) {
				inputValue = textContent;
			}
			inputValue = inputValue.replaceAll(";.*", "");
			inputValue = inputValue.replaceAll("(?i)(^(?=[MDCLXVI])M*D?C{0,4}L?X{0,4}V?I{0,4}V?X?\\s*(,|$))", "").trim();
			inputValue = inputValue.replaceAll("(?i).*cm\\.?.*", "").trim();
			//System.out.println(inputValue);
			if(!(inputValue.toUpperCase().matches("[A-Z\\s-\\.,]+")||inputValue.isEmpty())) {
				//System.out.println(inputValue);
			if(inputValue.matches("(\\d+\\s*-)?\\s*\\d+\\s*((p\\.?.*)|((Total )?pages))?")) {
				if(inputValue.matches("(\\d+)\\s*-.*"))
					startingPage = inputValue.replaceAll("(\\d+)\\s*-.*", "$1").trim();
				else
					startingPage = "1";
				response.add(new String[] { "startingPage", startingPage});	
				endingPage = inputValue.replaceAll(".*(?:\\s|^|p\\.?|-)(\\d+).*", "$1").trim();
				response.add(new String[] { "endingPage", endingPage});
			} else if(inputValue.matches("(?i)[\\d+\\.\\s]+MB")) {
				Float sizeValue = Float.parseFloat(inputValue.replaceAll("(?i)MB", "").trim());
				Integer size = (int) (sizeValue * 1024 * 1024);
				response.add(new String[] { "size", size.toString()});
			} else if(inputValue.matches("(?i)v\\,\\s*\\d+")) {
				endingPage = inputValue.replaceAll(".*(\\d+).*", "$1");
				response.add(new String[] { "startingPage", "1"});
				response.add(new String[] { "endingPage", endingPage});
			}
				try {
					pageCount = (Integer.parseInt(endingPage) - Integer.parseInt(startingPage)) + 1;
					response.add(new String[] { "pageCount", pageCount.toString()});
				} catch (Exception e ) {
					Base.er.println("stPg : " + startingPage + " - endPg : " + endingPage);
					e.printStackTrace(Base.er);
				}
			}
		} catch (Exception e) {
			Base.er.println(textContent);
			e.printStackTrace();
			e.printStackTrace(Base.er);
		}
		return response;
	}
	
	public ArrayList<String[]> curateDescription(String textContent) {
		textContent = textContent.replaceAll("\\s+", " ");
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
	
	public ArrayList<String[]> curatedescAbstract(String textContent) {
		textContent = textContent.replaceAll("\\s+", " ");
		String inputValue = textContent.trim() ;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			for(String token : removeTokens_descAbstract)
				inputValue = inputValue.replaceAll("\\b"+token+"\\b","");
			response.add(new String[] { "abstarct", inputValue});
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curateTitle(String textContent) {
		textContent = textContent.replaceAll("\\s+", " ");
		String inputValue = textContent.trim() ;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			String alternativeTitle = "";
			if(inputValue.matches(".*\\(.*\\).*"))
				alternativeTitle = inputValue.replaceAll(".*\\((.*)\\).*", "$1");
			if(!alternativeTitle.isEmpty())
				response.add(new String[] { "title.alternative", alternativeTitle});
			String title = inputValue.replaceAll("\\(.*\\)", "");
			response.add(new String[] { "title", title});
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curateAdditionalInfo(String textContent) {
		textContent = textContent.replaceAll("\\s+", " ").trim();
		ArrayList<String[]> response = new ArrayList<>();
		try {		
			JSONObject jo = new JSONObject(textContent);
			String inputValue = "";
			if(jo.has("note")) {
			inputValue = jo.getString("note");
			for(String token : removeTokens_additionalInfo)
				inputValue = inputValue.replaceAll("(?i)\\b"+token+"\\b","");
			if(!inputValue.isEmpty())
				response.add(new String[] { "reference", inputValue });
			} else if (jo.has("RightsStatement")) {
				inputValue = jo.getString("RightsStatement").trim();
				for(String token : removeTokens_descAbstract) {
					//System.out.println(token + inputValue);
					inputValue = inputValue.replaceAll("(?i)\\b"+token+"\\b","");
				}
				if(!inputValue.isEmpty())
					response.add(new String[] { "abstract", inputValue});
			}
		} catch (Exception e) {
			e.printStackTrace();
			//response.add(new String[] { "", "" });
		}
		return response;
	}
	}
	/*
	 * public static void main(String[] args) throws Exception{ // TODO
	 * Auto-generated method stub
	 * 
	 * }
	 */

