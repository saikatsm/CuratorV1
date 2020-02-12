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

public class Logics extends LoadandParseSchemaActionJSON {

	static NDL_DataService ndlDS = new NDL_DataService("http://10.4.8.239:65/services/", "normalizeDate");
	static NDL_DataService ndlDS_ddc = new NDL_DataService("http://10.4.8.239:65/services/", "getClassHierarchy");
	NDL_DataService ndlDS_lang = new NDL_DataService("http://10.4.8.239:65/services/", "normalizeLanguage");
	NDL_DataService ndlDS_text = new NDL_DataService("http://10.4.8.239:65/services/", "normalizeText");
	HashMap<String,HashMap<String, ArrayList<String[]>>> fieldtranslationList = new HashMap<String, HashMap<String, ArrayList<String[]>>>();
	HashMap<String, ArrayList<String>> startsWithTokensMap = new HashMap<String, ArrayList<String>>();
	HashMap<String, ArrayList<String>> endsWithTokensMap = new HashMap<String, ArrayList<String>>();
	HashMap<String, ArrayList<String>> containsTokensMap = new HashMap<String, ArrayList<String>>();
	HashMap<String, HashMap<String,String>> moveField = new HashMap<String, HashMap<String,String>>();

	public Logics() {

		try {
			//nn = new Name_Normalization(Base.configPath + "/author/"); 
			

			
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

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}	

	public ArrayList<String[]> nodeAction(String textContent, String nodeNameNDL) {
		String inputValue = textContent.replaceAll("\\s+", " ").trim();
		ArrayList<String[]> response = new ArrayList<>();		
		for (Action action : schemaAction.get(nodeNameNDL)) {		
			if (action.actionName.equalsIgnoreCase("useMap")) {
				fieldtranslationList = Action.ftListGen;
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
			
			if (action.actionName.equalsIgnoreCase("delete")) {		
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
	if(Action.lookupSet.containsKey(input_fieldName)) {
		for(String token : Action.lookupSet.get(input_fieldName))	{
			token = token.replaceAll("([\\W&&\\S])", "\\\\$1");
			if(input_fieldValue.matches("(?i).*\\b"+token+"\\b.*")) {
				matchelement = true;
				break;
			}
		}

		if(matchelement) {
			for (Map.Entry<String[], String[]> entry : Action.lookup.entrySet()) {
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
