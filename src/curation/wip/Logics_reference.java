package curation.wip;

import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

import com.opencsv.CSVReader;


import config.NDL_DataService;

public class Logics_reference extends LoadandParseSchemaActionJSON {

	static NDL_DataService ndlDS = new NDL_DataService("http://10.4.8.239:65/services/", "normalizeDate");
	static NDL_DataService ndlDS_ddc = new NDL_DataService("http://10.4.8.239:65/services/", "getClassHierarchy");
	NDL_DataService ndlDS_lang = new NDL_DataService("http://10.4.8.239:65/services/", "normalizeLanguage");
	NDL_DataService ndlDS_text = new NDL_DataService("http://10.4.8.239:65/services/", "normalizeText");
	//Name_Normalization nn;
	public String dcFieldAuthor, dcFieldDate, dcFieldDeptValue, deptFullValue;
	HashMap<String,HashMap<String, ArrayList<String[]>>> fieldtranslationList = new HashMap<String, HashMap<String, ArrayList<String[]>>>();
	HashMap<String[], String[]> lookup = new HashMap<String[], String[]>();
	HashMap<String, HashSet<String>> lookupSet = new HashMap<String, HashSet<String>>();
	HashMap<String, String[]> datemap = new HashMap<String, String[]>();
	HashMap<String, String[]> idothmap = new HashMap<String, String[]>();
	HashMap<String, String[]> pubmap = new HashMap<String, String[]>();
	HashMap<String, String> submap = new HashMap<String, String>();
	HashMap<String, String> pubplacemap = new HashMap<String, String>();
	HashMap<String, String> pubinstmap = new HashMap<String, String>();
	HashMap<String, String> langmap = new HashMap<String, String>();
	HashMap<String, String> temporalmap = new HashMap<String, String>();
	HashMap<String, String> lrtmap = new HashMap<String, String>();
	HashMap<String, String> ddcmap = new HashMap<String, String>();
	HashMap<String, String> lang_other_map = new HashMap<String, String>();
	HashMap<String, ArrayList<String>> startsWithTokensMap = new HashMap<String, ArrayList<String>>();
	HashMap<String, ArrayList<String>> endsWithTokensMap = new HashMap<String, ArrayList<String>>();
	HashMap<String, ArrayList<String>> containsTokensMap = new HashMap<String, ArrayList<String>>();
	HashMap<String, HashMap<String,String>> moveField = new HashMap<String, HashMap<String,String>>();
	Set<String> desctonote = new HashSet<String>();
	Set<String> descdelete = new HashSet<String>();
	Set<String> descvalid = new HashSet<String>();
	Set<String> removeTokens_TitleAlternative = new HashSet<String>();
	Set<String> removeTokens_additionalInfo = new HashSet<String>();
	Set<String> removeTokens_descAbstract = new HashSet<String>();
	Set<String> removeTokens_author = new HashSet<String>();
	
	Set<String> authRemove = new HashSet<String>();
	//PrintStream log = null;
	public Boolean getThumb; 

	public Logics_reference() {

		try {
			//nn = new Name_Normalization(Base.configPath + "/author/"); 
			CSVReader crFT = new CSVReader(new FileReader(Base.configPath + "/fieldtranslation.csv"), '|');
			for (String[] row : crFT.readAll()) {
				String keyField = row[0].trim();
				String keyvalue = row[1].toLowerCase().trim();
				//System.out.println(keyvalue);
				if(fieldtranslationList.containsKey(keyField)) {
					if(fieldtranslationList.get(keyField).containsKey(keyvalue)) {
						fieldtranslationList.get(keyField).get(keyvalue).add(new String[] { row[2].trim(), row[3].trim() });
					} else {
						fieldtranslationList.get(keyField).put(keyvalue, 
								new ArrayList<String[]>() {{ add(new String[] { row[2].trim(), row[3].trim()}); }});
					}
					} else {
						HashMap<String, ArrayList<String[]>> valueMap = new HashMap<String,ArrayList<String[]>>();
						valueMap.put(keyvalue, new ArrayList<String[]>() {{ add(new String[] { row[2].trim(), row[3].trim()}); }} );
						fieldtranslationList.put(keyField, valueMap);
					}
				}
			crFT.close();
			CSVReader crFTSA = new CSVReader(new FileReader(Base.configPath + "/lookup.csv"), '|');
			for (String[] row : crFTSA.readAll()) {
				String key = row[0].trim();
				lookup.put(new String[] { key, row[1].trim(),row[2].trim() }, new String[] { row[3].trim(), row[4].trim() });
				if(lookupSet.containsKey(key)) {
					lookupSet.get(key).add(row[2].trim());
				}
				else {
					HashSet<String> set = new HashSet<String>();
					set.add(row[2].trim());
					lookupSet.put(key, set);
				}
			}
			crFTSA.close();
			
			/*CSVReader crddc = new CSVReader(
					new FileReader(Base.configPath + "/ddc.csv"), '|','"');
			for (String[] row : crddc.readAll()) {		
				//System.out.println(row[0] + row[1].trim());
				ddcmap.put(row[0], row[1].trim());
			}
			crddc.close();*/
			
			/*CSVReader crlangoth = new CSVReader(
					new FileReader(Base.configPath + "/language_other.csv"), '|','"');
			for (String[] row : crlangoth.readAll()) {		
				//System.out.println(row[0] + row[1].trim());
				lang_other_map.put(row[0], row[1].trim());
			}
			crlangoth.close();*/
			
			/*CSVReader crpubinst = new CSVReader(
					new FileReader(Base.configPath + "/dc.publisher.institution.csv"), '|','"');
			for (String[] row : crpubinst.readAll()) {		
				//System.out.println(row[0] + row[1].trim());
				pubinstmap.put(row[0], row[1].trim());
			}
			crpubinst.close();*/
			
			/*CSVReader crTitleAlternative = new CSVReader(
					new FileReader(Base.configPath + "/titleAlternative.csv"), '|','"');
			for (String[] row : crTitleAlternative.readAll())
				removeTokens_TitleAlternative.add(row[0]);
			crTitleAlternative.close();*/
			
			/*CSVReader cradditionalInfo = new CSVReader(
					new FileReader(Base.configPath + "/additionalInfo.csv"), '|','"');
			for (String[] row : cradditionalInfo.readAll())
				removeTokens_additionalInfo.add(row[0]);
			cradditionalInfo.close();*/
			
			CSVReader crauthMove = new CSVReader(new FileReader(Base.configPath + "/moveField.csv"), '|','"');
			for (String[] row : crauthMove.readAll()) {		
				String keyField = row[0].trim();
				if(moveField.containsKey(keyField)) {
					moveField.get(keyField).put(row[2].trim(), row[3].trim());
				} else {
					moveField.put(keyField, new HashMap<String,String>() {{put(row[2].trim(),row[3].trim());}});
				}
			}
			crauthMove.close();
			
			CSVReader crdescAbstract = new CSVReader(new FileReader(Base.configPath + "/abstract.csv"), '|','"');
			for (String[] row : crdescAbstract.readAll())
				removeTokens_descAbstract.add(row[0]);
			crdescAbstract.close();
			
			CSVReader crRemTokenAuth = new CSVReader(new FileReader(Base.configPath + "/authorRemoveTokens.csv"), '\n','"');
			for (String[] row : crRemTokenAuth.readAll())
				removeTokens_author.add(row[0].trim());
			crRemTokenAuth.close();
			
			CSVReader crauthswTokens = new CSVReader(
					new FileReader(Base.configPath + "/author/Inpr-v2/startsWithTokens.csv"), '|','"');
			for (String[] row : crauthswTokens.readAll()) {		
				//System.out.println(row[0].toLowerCase() + row[1].trim());
				startsWithTokensMap.put(row[0].trim(),new ArrayList<String>(Arrays.asList(new String[] {row[1],row[2]})));
			}
			crauthswTokens.close();
			
			CSVReader crauthewTokens = new CSVReader(
					new FileReader(Base.configPath + "/author/Inpr-v2/endsWithTokens.csv"), '|','"');
			for (String[] row : crauthewTokens.readAll()) {		
				//System.out.println(row[0].toLowerCase() + row[1].trim());
				endsWithTokensMap.put(row[0].trim(),new ArrayList<String>(Arrays.asList(new String[] {row[1],row[2]})));
			}
			crauthewTokens.close();
			
			CSVReader crauthcnTokens = new CSVReader(
					new FileReader(Base.configPath + "/author/Inpr-v2/containsTokens.csv"), '|','"');
			for (String[] row : crauthcnTokens.readAll()) {		
				//System.out.println(row[0].toLowerCase() + row[1].trim());
				containsTokensMap.put(row[0].trim(),new ArrayList<String>(Arrays.asList(new String[] {row[1],row[2]})));
			}
			crauthcnTokens.close();
			/*
			CSVReader crauthRem = new CSVReader(
					new FileReader(Base.configPath + "/authorRemove.csv"), '|','"');
			for (String[] row : crauthRem.readAll()) {		
				//System.out.println(row[0].toLowerCase() + row[1].trim());
				authRemove.add(row[0].toLowerCase().trim());
			}
			crauthRem.close();
			*/
		//System.out.println(locidothmap.toString());
			//log = new PrintStream(new File(Base.logPath + "/authlogfinal.csv"));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

	}	
	
	public ArrayList<String[]> curateAuthor(String textContent, String nodeNameNDL) throws Exception {
		/**TODO
		 * Dynamic logic generation requirement implemenatation block.
		 * Part of PhaseII generation of automated curation.
		 */
		String inputValue = textContent.trim();
		String authMapKey = inputValue.toLowerCase();
		inputValue = inputValue.replaceAll("\\s+", " ");
		ArrayList<String[]> response = new ArrayList<>();
		
		for(Action action : schemaAction.get(nodeNameNDL)) {

		if(action.actionName.equalsIgnoreCase("useMap")) {
		ArrayList<String[]> mapReturnValueList = null;
		if ((mapReturnValueList = staticFieldTranslate(nodeNameNDL, authMapKey)) != null) {
			for(String[] mapReturnValue : mapReturnValueList)
			if (!mapReturnValue[0].equals("remove")) {
				for(String mapSplitValue : mapReturnValue[1].split(";")) {
					response.add(new String[] { mapReturnValue[0], mapSplitValue.trim() });
				}
			}
			return response;
		}
		}
		/**TODO **Important module for ACP development.
		 * Dynamic curation logic writing and preservation of existing logic 
		 * implementation module. Tobe implemented and designed as part of automated curation
		 * program. However preservation of old logic is important and solutioned.
		 */

		if (action.actionName.equalsIgnoreCase("moveField")) {
			for (Map.Entry<String, String> entry : moveField.get(nodeNameNDL).entrySet()) {
				String token = entry.getKey().replaceAll("([\\W&&\\S])", "\\\\$1");
				if (inputValue.matches("(?i).*(^|,|\\s)" + token + "(\\s|,|$).*")) {
					String field = entry.getValue();
					if (!field.equalsIgnoreCase("remove"))
						response.add(new String[] { field, inputValue });
					return response;
				}
			}
		}
		if (action.actionName.equalsIgnoreCase("curate")) {
			//TechReport/Article Implementation.
			/*if (inputValue.matches("(?i)(?:^|\\w+\\W+)\\bdepartment\\b(?!\\Wof).*")) {
			response.add(new String[] { "dc.publisher.department", inputValue });
			return response;
		} else if (inputValue.matches("(?i).+\\bdepartment of\\b.+")) {
			inputValue = inputValue.replaceAll("(?i)(.*)\\bdepartment of\\b.*", "$1").trim();
			String department = inputValue.replaceAll("(?i).*\\b(department of\\b.*)", "$1").trim();
			response.add(new String[] { "dc.publisher.department", department });
		} else if (inputValue.matches("(?i)(?:^|\\w+\\W+)\\b(Instituut|Instituto|Institutes?|Institut|Institutions?|Institu)\\b.*")) {
			response.add(new String[] { "dc.publisher.institution", inputValue });
			return response;
		} else if (inputValue.matches("(?i).*\\b(centre|college|Foundation|Organization)\\b.*")) {
			response.add(new String[] { "dc.publisher.institution", inputValue });
			return response;
		} else if (inputValue.matches("(?i).*\\bsoftware\\b.*")) {
			response.add(new String[] { "dc.subject", inputValue });
			return response;
		} else if (inputValue.matches("(?i).*\\btelecom\\b.*")) {
			response.add(new String[] { "dc.contributor.other@organization", inputValue });
			return response;
		}*/
			//**********IMPORTANT******** IMPLEMENTATION TO BE CHANGED.*******************
			//Inproceedings implementation. Need to check for differences. Unnormalized implementation.
			//Implementation of startswith block
			String updAuth = inputValue;
			String mapField = "";
			boolean startMatch = false;
			for(Map.Entry<String,ArrayList<String>> tokenAction : startsWithTokensMap.entrySet()) {
				String token = tokenAction.getKey();
				token = token.replaceAll("([\\W&&\\S])", "\\\\$1");
					if (updAuth.matches("(?i)^" + token + "\\s.*")) {
							if(tokenAction.getValue().get(0).equals("move")) {
								mapField = tokenAction.getValue().get(1);
								response.add(new String[] { mapField, inputValue });
								return response;
							} else if (tokenAction.getValue().get(0).equals("removeToken")){
								//startMatch = true;
								mapField = tokenAction.getValue().get(1);
								updAuth = updAuth.replaceAll("(?:^|\\s)"+token+"\\s", " ").trim();
							} else if (tokenAction.getValue().get(0).equals("remove"))
								return response;
					}
			}
			//String value to pass through full curation cycle instead direct return.
			/*if(startMatch) {
				response.add(new String[] { mapField, updAuth });
				return response;
			}*/

			boolean endMatch = false;
			for(Map.Entry<String,ArrayList<String>> tokenAction : endsWithTokensMap.entrySet()) {
				String token = tokenAction.getKey();
				token = token.replaceAll("([\\W&&\\S])", "\\\\$1");
					if (updAuth.matches("(?i).*\\s" + token + "$")) {
							if(tokenAction.getValue().get(0).equals("move")) {
								mapField = tokenAction.getValue().get(1);
								response.add(new String[] { mapField, inputValue });
								return response;
							} else if (tokenAction.getValue().get(0).equals("removeToken")){
								//endMatch = true;
								mapField = tokenAction.getValue().get(1);
								updAuth = updAuth.replaceAll("\\s"+token+"(?:\\s|$)", " ").trim();
							} else if (tokenAction.getValue().get(0).equals("remove"))
								return response;
			}
			}
			//String value to pass through full curation cycle instead direct return.
			/*if(endMatch) {
				response.add(new String[] { mapField, updAuth });
				return response;
			}*/
			
			boolean containsMatch = false;
			for(Map.Entry<String,ArrayList<String>> tokenAction : containsTokensMap.entrySet()) {
				String token = tokenAction.getKey();
				token = token.replaceAll("([\\W&&\\S])", "\\\\$1");
				//System.out.println(inputValue + " : " + token);
					if (updAuth.matches("(?i).*\\s" + token + "\\s.*")) {
							if(tokenAction.getValue().get(0).equals("move")) {
								mapField = tokenAction.getValue().get(1);
								response.add(new String[] { mapField, inputValue });
								return response;
							} else if (tokenAction.getValue().get(0).equals("removeToken")){
								//containsMatch = true;
								mapField = tokenAction.getValue().get(1);
								updAuth = updAuth.replaceAll("(?:^|\\s)"+token+"(?:\\s|$)", " ");
							} else if (tokenAction.getValue().get(0).equals("remove"))
								return response;
			}
			}
			//String value to pass through full curation cycle instead direct return.
			/*
			if(containsMatch) {
				response.add(new String[] { mapField, updAuth });
				return response;
			}*/

//			if(updAuth.matches("[\\w\\.]+\\sDepartment|Faculty$")) {
//				response.add(new String[] { "dc.publisher.department", inputValue });
//				return response;
//			}
			
//			if(updAuth.matches("(?:[\\w\\.]+\\s){2,}Department|Faculty$")) {
//				updAuth = updAuth.replaceAll("Department|Faculty", "").trim();
				//**********Implementation with alteration in logic. To be tested. ********//
				//response.add(new String[] { "dc.contributor.author", updAuth });
				//return response;
			//}
			//Code part of Normalization Excercise. Code commented for temporary execution. 
			/*
			if (inputValue.matches("(?i).+\\bdepartment of\\b.+")) {
				inputValue = inputValue.replaceAll("(?i)(.*)\\bdepartment of\\b.*", "$1").trim();
				String department = inputValue.replaceAll("(?i).*\\b(department of\\b.*)", "$1").trim();
				response.add(new String[] { "dc.publisher.department", department });
			} else if (inputValue.matches("(?i)(?:^|\\w+\\W+)\\b(Instituut|Instituto|Institutes?|Institut|Institutions?|Institu)\\b.*")) {
				response.add(new String[] { "dc.publisher.institution", inputValue });
				return response;
			}
			for (String tokens : removeTokens_author)
				inputValue = inputValue.replace(tokens, "").trim();
				*/
			inputValue = updAuth;
			if(inputValue.matches(".*\\([^\\)]*"))
				inputValue = inputValue.replaceAll("(.*)\\(([^\\)]*)", "$1 $2");
			
			if(!inputValue.isEmpty()) {
				inputValue = ndlDS_text.getResult(new String[][] { { "text[]", inputValue } }, "text").get(0);
				inputValue = inputValue.replaceAll("(\\(.*\\))|�|^-|-$|\\{|\\[", "");
				inputValue = inputValue.replaceAll("\\b(Md\\.?\\s|n\\.?d\\.?)\\b", "").replaceAll("\\s+", " ");
				inputValue = inputValue.replaceAll("(?i)(^'.*)\\s+([a-z])\\.?\\s?$", "$2$1").replaceAll(",\\s*\\.?$","");
				inputValue = inputValue.replaceAll("\\.(?!\\s|$)", ". ");
				inputValue = inputValue.replaceAll("(?i)\\b([A-Z](?!\\.))\\b", "$1.");
				inputValue = inputValue.replaceAll("\\.\\s*,",".").replaceAll(",\\s*\\.?$", "");
				inputValue = inputValue.replaceAll("(?i)(^'.*)\\s+([a-z])\\.?\\s?$", "$2$1").replaceAll(",$","");
				inputValue = org.apache.commons.text.WordUtils.capitalizeFully(inputValue, new char[] { ' ', '-','\'' });
				if (!inputValue.contains(",")) {
					if (!inputValue.matches("(?i).*\\b([a-z]\\.?|\\W+)$"))
						inputValue = inputValue.replaceAll("(.*)\\s(.*)", "$2, $1");
					else if (inputValue.matches("(?i)(?<!^[a-z]\\.).*\\s+[a-z]\\.?"))
						inputValue = inputValue.replaceAll("(.*)\\s(.*)", "$1, $2");
				}
			}
			if (inputValue.length() > 2) {
				 for(String remAuthtokens : authRemove)
				 if(inputValue.matches("(?i).*\\b"+remAuthtokens+"\\b.*")) { 
				 	response.add(new String[] { "dc.contributor.author", "" }); 
				 	return response; 
				 }
				response.add(new String[] { nodeNameNDL, inputValue });
			}
			return response;
		}
		if (action.actionName.equalsIgnoreCase("copyData")) {
			if(!action.targetField.isEmpty())
				response.add(new String[] { action.targetField, inputValue });
			else
				response.add(new String[] { nodeNameNDL, inputValue });
			return response;
		}
	}
		return response;
	}
	
	public ArrayList<String[]> curateContributorOther(String textContent, String nodeNameNDL) throws Exception{
		/**TODO
		 * Dynamic logic generation requirement implemenatation block.
		 * Part of PhaseII generation of automated curation.
		 */
		String inputValue = textContent.trim();
		inputValue = inputValue.replaceAll("\\s+", " ");
		ArrayList<String[]> response = new ArrayList<>();
		
		for(Action action : schemaAction.get(nodeNameNDL)) {

			if (action.actionName.equalsIgnoreCase("useMap")) {
				ArrayList<String[]> mapReturnValueList = null;
				if ((mapReturnValueList = staticFieldTranslate(nodeNameNDL, inputValue)) != null) {
					for(String[] mapReturnValue : mapReturnValueList)
					if (!mapReturnValue[0].equals("remove")) {
						for (String mapSplitValue : mapReturnValue[1].split(";"))
							response.add(new String[] { mapReturnValue[0], mapSplitValue.trim() });
					}
					return response;
				}
			}

		if (action.actionName.equalsIgnoreCase("copyData")) {
			if(!action.targetField.isEmpty())
				response.add(new String[] { action.targetField, inputValue });
			else
				response.add(new String[] { nodeNameNDL, inputValue });
			return response;
		}
	}
		return response;
	}
	
	
	public ArrayList<String[]> curateDateAwarded(String textContent, String nodeNameNDL) throws Exception {
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
	
	public ArrayList<String[]> curateDateOther(String textContent, String nodeNameNDL) throws Exception {
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
			//System.out.println(returnValue);
		} catch (Exception e) {
		}
		return returnValue;
	}
	
	public ArrayList<String[]> curateDateIssued(String textContent, String nodeNameNDL) throws Exception {
		String inputValue = textContent.replaceAll("\\s+", " ").trim();
		ArrayList<String[]> response = new ArrayList<>();
		String dateValue = "";
		try {
			// if(inputValue.matches("\\d{2}[\\/-]\\d{2}[\\/-]\\d{2}"))
			// inputValue = inputValue.replaceAll("(.*[\\/-])(\\d+)","$120$2");
			for (Action action : schemaAction.get(nodeNameNDL)) {			
				if (action.actionName.equalsIgnoreCase("useMap")) {
					ArrayList<String[]> mapReturnValueList = null;
					if ((mapReturnValueList = staticFieldTranslate(nodeNameNDL, inputValue)) != null) {
						for(String[] mapReturnValue : mapReturnValueList)
						if (!mapReturnValue[0].equals("remove")) {
							response.add(new String[] { mapReturnValue[0], mapReturnValue[1] });
						}
						return response;
					}
				}
				if (action.actionName.equalsIgnoreCase("curate")) {
					dateValue = curateDate(inputValue);
					if (!dateValue.isEmpty()) {
						if(!action.targetField.isEmpty())
							response.add(new String[] { action.targetField, dateValue });
						else
							response.add(new String[] { nodeNameNDL, dateValue });
					}
				}
			}
		} catch (Exception e) {
			Base.er.println(inputValue);
			e.printStackTrace(Base.er);
		}
		return response;
	}
	
	public ArrayList<String[]> curateSubject(String textContent, String nodeNameNDL) {
		
		String inputValue = textContent.trim();
		ArrayList<String[]> response = new ArrayList<>();
		try {
			for (Action action : schemaAction.get(nodeNameNDL)) {
				
				if (action.actionName.equalsIgnoreCase("curate")) {
					String newValue = "";
					ArrayList<String> stopwords = new ArrayList<>(
							Arrays.asList("of", "and", "an", "the", "with", "other", "in", "a", "above"));
					for (String token : inputValue.split("\\s+")) {
						if (!stopwords.contains(token.toLowerCase()))
							newValue += org.apache.commons.text.WordUtils.capitalizeFully(token) + " ";
						else
							newValue += token + " ";
					}
					newValue = newValue.trim();
					response.add(new String[] { "subject", newValue });
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "subject", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curateAdvisor(String textContent, String nodeNameNDL) {
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
	
	public ArrayList<String[]> curateResearcher(String textContent, String nodeNameNDL) {
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
	
	public ArrayList<String[]> curateTitleAlternative(String textContent, String nodeNameNDL) {
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
	
	public ArrayList<String[]> curatePublisher(String textContent, String nodeNameNDL) throws Exception {
		String inputValue = textContent.trim() ;
		ArrayList<String[]> response = new ArrayList<>();
			inputValue = ndlDS_text.getResult(new String[][] { { "text[]", inputValue } }, "text").get(0);
			for (Action action : schemaAction.get(nodeNameNDL)) {
				if (action.actionName.equalsIgnoreCase("useMap")) {
					ArrayList<String[]> mapReturnValueList = null;
					if ((mapReturnValueList = staticFieldTranslate(nodeNameNDL, inputValue)) != null) {
						for(String[] mapReturnValue : mapReturnValueList)
						if (!mapReturnValue[0].equals("remove")) {
							response.add(new String[] { mapReturnValue[0], mapReturnValue[1] });
						}
						return response;
					}
				}
				if (action.actionName.equalsIgnoreCase("copyData")) {
					if(!action.targetField.isEmpty())
						response.add(new String[] { action.targetField, inputValue });
					else
						response.add(new String[] { nodeNameNDL, inputValue });
					return response;
				}
			}
		return response;
	}
	
	public ArrayList<String[]> curatePublisherDepartment(String textContent, String nodeNameNDL) {
		String inputValue = textContent.replaceAll("\\s+", " ").trim();
		ArrayList<String[]> response = new ArrayList<>();
		
		for (Action action : schemaAction.get(nodeNameNDL)) {
			
			if (action.actionName.equalsIgnoreCase("moveField")) {
				for (Map.Entry<String, String> entry : moveField.get(nodeNameNDL).entrySet()) {
					String token = entry.getKey().replaceAll("([\\W&&\\S])", "\\\\$1");
					if (inputValue.matches("(?i).*(^|,|\\s)" + token + "(\\s|,|$).*")) {
						String field = entry.getValue();
						if (!field.equalsIgnoreCase("remove"))
							response.add(new String[] { field, inputValue });
						return response;
					}
				}
			}
			if (action.actionName.equalsIgnoreCase("useMap")) {
				ArrayList<String[]> mapReturnValueList = null;
				if ((mapReturnValueList = staticFieldTranslate(nodeNameNDL, inputValue)) != null) {
					for(String[] mapReturnValue : mapReturnValueList)
					if (!mapReturnValue[0].equals("remove")) {
						response.add(new String[] { mapReturnValue[0], mapReturnValue[1] });
					}
					return response;
				}
			}
			if (action.actionName.equalsIgnoreCase("copyData")) {
				if(!action.targetField.isEmpty())
					response.add(new String[] { action.targetField, inputValue });
				else
					response.add(new String[] { nodeNameNDL, inputValue });
				return response;
			}
			/* {
				deptFullValue = inputValue;
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
			}*/
		}
		return response;
	}
	
	public ArrayList<String[]> curatePublisherInstitution(String textContent, String nodeNameNDL) throws Exception {
		String inputValue = textContent.trim().replaceAll("\\s+", " ").trim();
		ArrayList<String[]> response = new ArrayList<>();
		inputValue = ndlDS_text.getResult(new String[][] { { "text[]", inputValue } }, "text").get(0);
		
		for (Action action : schemaAction.get(nodeNameNDL)) {
			if (action.actionName.equalsIgnoreCase("useMap")) {
				ArrayList<String[]> mapReturnValueList = null;
				if ((mapReturnValueList = staticFieldTranslate(nodeNameNDL, inputValue)) != null) {
					for(String[] mapReturnValue : mapReturnValueList)
					if (!mapReturnValue[0].equals("remove")) {
						response.add(new String[] { mapReturnValue[0], mapReturnValue[1] });
					}
					return response;
				}
			}
			if (action.actionName.equalsIgnoreCase("lookup")) {
				ArrayList<String[]> lookupResponse = new ArrayList<String[]>();
				if (!(lookupResponse = FieldTranslatewithSourceAction(nodeNameNDL, inputValue)).isEmpty()) {
					for (String[] returnValue : lookupResponse)
					if (!returnValue[0].equals("remove")) {
						for (String mapSplitValue : returnValue[1].split(";"))
							response.add(new String[] { returnValue[0], mapSplitValue.trim() });
					}
				}
			}
			if (action.actionName.equalsIgnoreCase("copyData")) {
				if(!action.targetField.isEmpty())
					response.add(new String[] { action.targetField, inputValue });
				else
					response.add(new String[] { nodeNameNDL, inputValue });
				return response;
			}
		}
		return response;
	}
	
	public ArrayList<String[]> curatepublisherDate(String textContent, String nodeNameNDL) throws Exception {
		
		String inputValue = textContent.replaceAll("\\s+", " ").trim();
		ArrayList<String[]> response = new ArrayList<>();
		
		for (Action action : schemaAction.get(nodeNameNDL)) {
			
			if (action.actionName.equalsIgnoreCase("useMap")) {
				ArrayList<String[]> mapReturnValueList = null;
				if ((mapReturnValueList = staticFieldTranslate(nodeNameNDL, inputValue)) != null) {
					for(String[] mapReturnValue : mapReturnValueList)
					if (!mapReturnValue[0].equals("remove")) {
						response.add(new String[] { mapReturnValue[0], mapReturnValue[1] });
					}
					return response;
				}
			}
			if (action.actionName.equalsIgnoreCase("copyData")) {
				if(!action.targetField.isEmpty())
					response.add(new String[] { action.targetField, inputValue });
				else
					response.add(new String[] { nodeNameNDL, inputValue });
				return response;
			}
		}
		return response;
	}
	
	public ArrayList<String[]> curateLanguage(String textContent, String nodeNameNDL) {
		textContent = textContent.replaceAll("\\s+", " ");
		String inputValue = textContent.trim();
		ArrayList<String[]> response = new ArrayList<>();
		
		for (Action action : schemaAction.get(nodeNameNDL)) {
			
			if (action.actionName.equalsIgnoreCase("useMap")) {
				ArrayList<String[]> mapReturnValueList = null;
				if ((mapReturnValueList = staticFieldTranslate(nodeNameNDL, inputValue)) != null) {
					for(String[] mapReturnValue : mapReturnValueList)
					if (!mapReturnValue[0].equals("remove")) {
						response.add(new String[] { mapReturnValue[0], mapReturnValue[1] });
					}
					return response;
				}
			}
			if (action.actionName.equalsIgnoreCase("copyData")) {
				if(!action.targetField.isEmpty())
					response.add(new String[] { action.targetField, inputValue });
				else
					response.add(new String[] { nodeNameNDL, inputValue });
				return response;
			}
			
			if (action.actionName.equalsIgnoreCase("setValue")) {
				if(!action.targetValue.isEmpty())
					response.add(new String[] { nodeNameNDL, action.targetValue });
				else
					response.add(new String[] { nodeNameNDL, inputValue });
				return response;
			}
		}
		
	/* Old Code.
	 * 	try {
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
		}*/
		return response;
	}
	
	public ArrayList<String[]> curateDDC(String textContent, String nodeNameNDL) {
		textContent = textContent.replaceAll("\\s+", " ");
		String inputValue = textContent.trim() ;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			if(ddcmap.containsKey(inputValue)) {
			String mapReturnValue = ddcmap.get(inputValue); 
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
	
	public ArrayList<String[]> curateFormatExtent(String textContent, String nodeNameNDL) {
		String inputValue = textContent.replaceAll("\\s+", " ").trim();
		ArrayList<String[]> response = new ArrayList<>();		
		for (Action action : schemaAction.get(nodeNameNDL)) {
			
			if (action.actionName.equalsIgnoreCase("useMap")) {
				ArrayList<String[]> mapReturnValueList = null;
				if ((mapReturnValueList = staticFieldTranslate(nodeNameNDL, inputValue)) != null) {
					for(String[] mapReturnValue : mapReturnValueList)
					if (!mapReturnValue[0].equals("remove")) {
						response.add(new String[] { mapReturnValue[0], mapReturnValue[1] });
					}
					return response;
				}
			}
			
			if (action.actionName.equalsIgnoreCase("copyData")) {
				if(!action.targetField.isEmpty())
					response.add(new String[] { action.targetField, inputValue });
				else
					response.add(new String[] { nodeNameNDL, inputValue });
				return response;
			}
			
			if (action.actionName.equalsIgnoreCase("delete")) {		
				return response;
			}
		}
		return response;
	}
	
	public ArrayList<String[]> curateDescription(String textContent, String nodeNameNDL) {
		String inputValue = textContent.trim() ;
		ArrayList<String[]> response = new ArrayList<>();
		try {
			/*if(inputValue.matches(".*volume\\s*=\\s*\\{.*?\\}.*")) {
				String volume = inputValue.replaceAll(".*?volume\\s*=\\s*\\{(.*?)\\}.*", "$1");
				response.add(new String[] { "volume", volume});
			}
			if(inputValue.matches(".*issue\\s*=\\s*\\{.*?\\}.*")) {
				String issue = inputValue.replaceAll(".*?issue\\s*=\\s*\\{(.*?)\\}.*", "$1");
				response.add(new String[] { "issue", issue});
			}
			if(inputValue.matches(".*pages\\s*=\\s*\\{.*?\\}.*")) {
				String pages = inputValue.replaceAll(".*?pages\\s*=\\s*\\{(.*?)\\}.*", "$1");
				response.add(new String[] { "pages", pages});
			}*/
			for (Action action : schemaAction.get(nodeNameNDL)) {
				
				if (action.actionName.equalsIgnoreCase("useMap")) {
					ArrayList<String[]> mapReturnValueList = null;
					if ((mapReturnValueList = staticFieldTranslate(nodeNameNDL, inputValue)) != null) {
						for(String[] mapReturnValue : mapReturnValueList)
						if (!mapReturnValue[0].equals("remove")) {
							response.add(new String[] { mapReturnValue[0], mapReturnValue[1] });
						}
						return response;
					}
				}
				
				if (action.actionName.equalsIgnoreCase("curate")) {
					/*if(inputValue.matches(".*publisher\\s*=\\s*\\{.*?\\}.*")) {
						String pages = inputValue.replaceAll(".*?publisher\\s*=\\s*\\{(.*?)\\}.*", "$1");
						response.add(new String[] { "publisher", pages});
					}*/	
					String normtext = ndlDS_text.getResult(new String[][] { { "text[]", inputValue } }, "text").get(0);
					normtext = normtext.replaceAll("�", "");
					response.add(new String[] { "dc.identifier.citation", normtext});
				}
				if (action.actionName.equalsIgnoreCase("lookup")) {
					ArrayList<String[]> lookupResponse = new ArrayList<String[]>();
					if (!(lookupResponse = FieldTranslatewithSourceAction(nodeNameNDL, inputValue)).isEmpty()) {
						for (String[] returnValue : lookupResponse)
						if (!returnValue[0].equals("remove")) {
							for (String mapSplitValue : returnValue[1].split(";"))
								response.add(new String[] { returnValue[0], mapSplitValue.trim() });
						}
					}
				}
				if (action.actionName.equalsIgnoreCase("moveField")) {
					for (Map.Entry<String, String> entry : moveField.get(nodeNameNDL).entrySet()) {
						String token = entry.getKey().replaceAll("([\\W&&\\S])", "\\\\$1");
						/**TODO
						 * Implementation update required. Movefield and lookup to be merged.
						 * Implemented for emergency delivery temp fix.
						 */
						if (inputValue.matches("(?i).*(^|,|\\s)" + token + "(\\s|,|$).*")) {
							String field = entry.getValue();
							if (!field.equalsIgnoreCase("remove"))
								response.add(new String[] { field, inputValue });
							return response;
						}
					}
				}
				if (action.actionName.equalsIgnoreCase("copyData")) {
					if(!action.targetField.isEmpty())
						response.add(new String[] { action.targetField, inputValue });
					else
						response.add(new String[] { nodeNameNDL, inputValue });
					return response;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curateDescAbstract(String textContent, String nodeNameNDL) {
		String inputValue = textContent.replaceAll("\\s+", " ").trim();
		ArrayList<String[]> response = new ArrayList<>();
		for (Action action : schemaAction.get(nodeNameNDL)) {
			/**TODO
			 * description abstract is moved to different field in lookup operation. Lookup operation 
			 * to be formally defined and implemented. MoveField is not part of lookup. Lookup
			 * to incorporate copy field and useMap togather.
			 */
			if (action.actionName.equalsIgnoreCase("lookup")) {
				ArrayList<String[]> lookupResponse = new ArrayList<String[]>();
				if (!(lookupResponse = FieldTranslatewithSourceAction(nodeNameNDL, inputValue)).isEmpty()) {
					for (String[] returnValue : lookupResponse)
					if (!returnValue[0].equals("remove")) {
						for (String mapSplitValue : returnValue[1].split(";"))
							response.add(new String[] { returnValue[0], mapSplitValue.trim() });
					}
				}
			}
			if (action.actionName.equalsIgnoreCase("copyData")) {
				if(!action.targetField.isEmpty())
					response.add(new String[] { action.targetField, inputValue });
				else
					response.add(new String[] { nodeNameNDL, inputValue });
				return response;
			}
			
			if (action.actionName.equalsIgnoreCase("moveField")) {
				for (Map.Entry<String, String> entry : moveField.get(nodeNameNDL).entrySet()) {
					String token = entry.getKey().replaceAll("([\\W&&\\S])", "\\\\$1");
					if (inputValue.matches("(?i).*(^|,|\\s)" + token + "(\\s|,|$).*")) {
						String field = entry.getValue();
						if (!field.equalsIgnoreCase("remove"))
							response.add(new String[] { field, inputValue });
						return response;
					}
				}
			}
/*				
				//for(String token : removeTokens_descAbstract)
				//inputValue = inputValue.replaceAll("\\b"+token+"\\b","");
			if(inputValue.matches(".*\\b(dissertation|thesis)\\b.*")) {
				//response.add(new String[] { "lrmi.learningResourceType", "thesis"});
				if(inputValue.length() > 50) 
					response.add(new String[] { "abstract", inputValue});
			}
			else if ( !inputValue.matches("\\w+") && inputValue.length() > 50)
				response.add(new String[] { "abstract", inputValue});
*/
		}
		return response;
	}
	
	public ArrayList<String[]> curateDescriptionUri(String textContent, String nodeNameNDL) throws Exception{
		String inputValue = textContent.trim();
		inputValue = inputValue.replaceAll("\\s+", " ").trim();
		ArrayList<String[]> response = new ArrayList<>();
			for (Action action : schemaAction.get(nodeNameNDL)) {
				if (action.actionName.equalsIgnoreCase("curate")) {
					if(inputValue.startsWith("www"))
					inputValue += "http://";
					response.add(new String[] { "dc.description.uri", inputValue});
				}
			}
		return response;
	}
	
	public ArrayList<String[]> curateIdentifierOther(String textContent, String nodeNameNDL) throws Exception {
		
		textContent = textContent.replaceAll("\\s+", " ");
		String inputValue = textContent.trim();

		ArrayList<String[]> response = new ArrayList<>();

		for (Action action : schemaAction.get(nodeNameNDL)) {

			if (action.actionName.equalsIgnoreCase("useMap")) {
				ArrayList<String[]> mapReturnValueList = null;
				if ((mapReturnValueList = staticFieldTranslate(nodeNameNDL, inputValue)) != null) {
					for(String[] mapReturnValue : mapReturnValueList)
					if (!mapReturnValue[0].equals("remove")) {
						response.add(new String[] { mapReturnValue[0], mapReturnValue[1] });
					}
					return response;
				}
			}

			if (action.actionName.equalsIgnoreCase("delete")) {
				String repValue = inputValue.replaceAll("(?<![\\{:])\"(?![:\\}])", "").replaceAll("\\s+", " ");
				JSONObject jo = new JSONObject(repValue);
				if (jo.has("doi") || jo.has("alternateContentUri"))
					;
				
				return response;
			}
			
			if (action.actionName.equalsIgnoreCase("curate")) {
				String repValue = inputValue.replaceAll("(?<![\\{:])\"(?![:\\}])", "").replaceAll("\\s+", " ");
				JSONObject jo = new JSONObject(repValue);
				if (jo.has("doi") || jo.has("alternateContentUri"))
					;
				else if (jo.has("journal")) {
					String value = jo.getString("journal").trim();
					value = StringUtils.capitalize(value.toLowerCase());
					value = value.replaceAll("\"", "\\\\\"");
					repValue = jo.put("journal", value).toString();
					response.add(new String[] { nodeNameNDL, repValue });
				}
				else
					response.add(new String[] { nodeNameNDL, repValue });
				return response;
			}
			
			if (action.actionName.equalsIgnoreCase("copyData")) {
				if(!action.targetField.isEmpty())
					response.add(new String[] { action.targetField, inputValue });
				else
					response.add(new String[] { nodeNameNDL, inputValue });
				return response;
			}
		}
		return response;
	}
	
	public ArrayList<String[]> curateFormatExtent_bkp(String textContent, String nodeNameNDL) {
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
	
	public ArrayList<String[]> curateTitle(String textContent, String nodeNameNDL) {
		String inputValue = textContent.trim();
		inputValue = inputValue.replaceAll("\\s+", " ").trim();
		ArrayList<String[]> response = new ArrayList<>();
		try {
			for (Action action : schemaAction.get(nodeNameNDL)) {
				if (action.actionName.equalsIgnoreCase("curate")) {
					String title = ndlDS_text.getResult(new String[][] { { "text[]", inputValue } }, "text").get(0);
					title = title.replaceAll("�", "");
					String updTitle = "";
					for (String titleParts : title.split("\\.(\\s|$)")) {
						titleParts = titleParts.trim();
						if(!titleParts.isEmpty())
							updTitle += StringUtils.capitalize(titleParts.toLowerCase())+". ";
/** TODO Advanced Implementation: Need to do usability check. Not implemented in initial version.
 * 		for (String titleParts : title.split("\\.(\\s|$)")) {
			titleParts = titleParts.trim();
			if(titleParts.length()!=0) {
				int pst = 0, pend = 0;
				Pattern pattern = Pattern.compile("[^\\s]*\\.[^\\s]*");
				Matcher matcher = pattern.matcher(titleParts);
				while(matcher.find()) {
					pst = matcher.start();
					pend = matcher.end();
			}
			titleParts = titleParts.substring(0, pst).toLowerCase()+titleParts.substring(pst, pend)+titleParts.substring(pend).toLowerCase();
			updTitle += StringUtils.capitalize(titleParts) + ". ";
* 
*/
					}
					updTitle = updTitle.trim();
					response.add(new String[] { "dc.title", updTitle});
				}
				
				if (action.actionName.equalsIgnoreCase("setValue")) {
					if(!action.targetValue.isEmpty())
						response.add(new String[] { nodeNameNDL, action.targetValue });
					else
						response.add(new String[] { nodeNameNDL, inputValue });
					return response;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.add(new String[] { "", "" });
		}
		return response;
	}
	
	public ArrayList<String[]> curatelrt(String textContent, String nodeNameNDL) {
		String inputValue = textContent.replaceAll("\\s+", " ").trim();
		ArrayList<String[]> response = new ArrayList<>();		
		for (Action action : schemaAction.get(nodeNameNDL)) {
			if (action.actionName.equalsIgnoreCase("useMap")) {
				ArrayList<String[]> mapReturnValueList = null;
				if ((mapReturnValueList = staticFieldTranslate(nodeNameNDL, inputValue)) != null) {
					for(String[] mapReturnValue : mapReturnValueList)
					if (!mapReturnValue[0].equals("remove")) {
						response.add(new String[] { mapReturnValue[0], mapReturnValue[1] });
					}
					return response;
				}
			}
			
			if (action.actionName.equalsIgnoreCase("copyData")) {
				if(!action.targetField.isEmpty())
					response.add(new String[] { action.targetField, inputValue });
				else
					response.add(new String[] { nodeNameNDL, inputValue });
				return response;
			}
			
			if (action.actionName.equalsIgnoreCase("delete")) {
				return response;
			}
			
			if (action.actionName.equalsIgnoreCase("setValue")) {
				if(!action.targetValue.isEmpty())
					response.add(new String[] { nodeNameNDL, action.targetValue });
				else
					response.add(new String[] { nodeNameNDL, inputValue });
				
				//schemaAction.get(nodeNameNDL).remove(action.executionOrder+1);
				
				return response;
			}
		}
		return response;
	}
	
	public ArrayList<String[]> curateAdditionalInfo(String textContent, String nodeNameNDL) {
		String inputValue = textContent.replaceAll("\\s+", " ").trim();
		ArrayList<String[]> response = new ArrayList<>();
		/*		
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
			}*/
			for (Action action : schemaAction.get(nodeNameNDL)) {

				if (action.actionName.equalsIgnoreCase("useMap")) {
					ArrayList<String[]> mapReturnValueList = null;
					if ((mapReturnValueList = staticFieldTranslate(nodeNameNDL, inputValue)) != null) {
						for(String[] mapReturnValue : mapReturnValueList)
						if (!mapReturnValue[0].equals("remove")) {
							response.add(new String[] { mapReturnValue[0], mapReturnValue[1] });
						}
						return response;
					}
				}
				
				if (action.actionName.equalsIgnoreCase("copyData")) {
					if(!action.targetField.isEmpty())
						response.add(new String[] { action.targetField, inputValue });
					else
						response.add(new String[] { nodeNameNDL, inputValue });
					return response;
				}
			}
		return response;
	}
ArrayList<String[]> staticFieldTranslate(String input_fieldName, String input_fieldValue) {
	ArrayList<String[]> result = null;
	input_fieldValue = input_fieldValue.toLowerCase();
	if(fieldtranslationList.containsKey(input_fieldName)) {
	HashMap<String, ArrayList<String[]>> valueMap = fieldtranslationList.get(input_fieldName);
	if (valueMap.containsKey(input_fieldValue)) {
			result = valueMap.get(input_fieldValue);
		}
}
	return result;
}

ArrayList<String[]> FieldTranslatewithSourceAction(String input_fieldName, String input_fieldValue) {
	String[] result = null;
	boolean matchelement = false;
	ArrayList<String[]> response = new ArrayList<String[]>();
	if(lookupSet.containsKey(input_fieldName)) {
		for(String token : lookupSet.get(input_fieldName))	{
			token = token.replaceAll("([\\W&&\\S])", "\\\\$1");
			if(input_fieldValue.matches("(?i).*\\b"+token+"\\b.*")) {
				matchelement = true;
				break;
			}
		}

		if(matchelement) {
			for (Map.Entry<String[], String[]> entry : lookup.entrySet()) {
				String src_fieldName = entry.getKey()[0].trim();
				String src_fieldAction = entry.getKey()[1].trim();
				String srcValue = entry.getKey()[2].trim();
				srcValue = srcValue.replaceAll("([\\W&&\\S])", "\\\\$1");
				if (src_fieldName.equalsIgnoreCase(input_fieldName) && input_fieldValue.matches("(?i).*(^|\\s)" + srcValue + "(\\s|$).*")) {
					if (src_fieldAction.equalsIgnoreCase("contains")){
						result = new String[2];
						result[0] = entry.getValue()[0].trim();
						result[1] = entry.getValue()[1].trim();
						response.add(result);
						/**
						 * TODO need to optimize full iteration situation by smart loading the Map DS. break can't be removed.
						 */
						//break;
					} else if (src_fieldAction.equalsIgnoreCase("starts with")){
						if(input_fieldValue.matches("(?i)^" + srcValue + "\\s.*")) {
							result = new String[2];
							result[0] = entry.getValue()[0].trim();
							result[1] = entry.getValue()[1].trim();
							response.add(result);
							/**
							 * TODO need to optimize full iteration situation by smart loading the Map DS. break can't be removed.
							 */
							//break;
						}
					} else if (src_fieldAction.equalsIgnoreCase("ends with")){
						if(input_fieldValue.matches("(?i).*\\s" + srcValue + "$.*")) {
							result = new String[2];
							result[0] = entry.getValue()[0].trim();
							result[1] = entry.getValue()[1].trim();
							response.add(result);
							/**
							 * TODO need to optimize full iteration situation by smart loading the Map DS. break can't be removed.
							 */
							//break;
						}
					} else if (src_fieldAction.equalsIgnoreCase("matches")){
						if(input_fieldValue.matches("(?i)" + srcValue)) {
							result = new String[2];
							result[0] = entry.getValue()[0].trim();
							result[1] = entry.getValue()[1].trim();
							response.add(result);
							/**
							 * TODO need to optimize full iteration situation by smart loading the Map DS. break can't be removed.
							 */
							//break;
						}
					} 
				}
			}
	}
}
	return response;
}
	
	/*
	 * public static void main(String[] args) throws Exception{ // TODO
	 * Auto-generated method stub
	 * 
	 * }
	 */
}
