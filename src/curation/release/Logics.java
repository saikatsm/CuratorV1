package curation.release;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
			CSVReader crauthswTokens = new CSVReader(
					new FileReader(Base.configPath + "/startsWithTokens.csv"), '|','"');
			for (String[] row : crauthswTokens.readAll()) {		
				//System.out.println(row[0].toLowerCase() + row[1].trim());
				startsWithTokensMap.put(row[0].trim(),new ArrayList<String>(Arrays.asList(new String[] {row[1],row[2]})));
			}
			crauthswTokens.close();
			
			CSVReader crauthewTokens = new CSVReader(
					new FileReader(Base.configPath + "/endsWithTokens.csv"), '|','"');
			for (String[] row : crauthewTokens.readAll()) {
				//System.out.println(row[0].toLowerCase() + row[1].trim());
				endsWithTokensMap.put(row[0].trim(),new ArrayList<String>(Arrays.asList(new String[] {row[1],row[2]})));
			}
			crauthewTokens.close();
			
			CSVReader crauthcnTokens = new CSVReader(
					new FileReader(Base.configPath + "/containsTokens.csv"), '|','"');
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
		ArrayList<String[]> response = new ArrayList<String[]>();
		for (Action action : schemaAction.get(nodeNameNDL)) {
			if (action.datatype.equals("json")) {
				JsonParser parser = new JsonParser();
				inputValue = inputValue.replaceAll("(?<![\\{:])\"(?![:\\}])", "\\\\\"");
				Object obj = parser.parse(inputValue);
				JsonObject jsonObject = (JsonObject) obj;
				JsonObject tempResult = new JsonObject();
				for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					String tempnodeNameNDL = nodeNameNDL + "@" + entry.getKey();
					inputValue = entry.getValue().getAsString();
					ArrayList<String[]> tempResponse = performAction(action, tempnodeNameNDL, inputValue);
					for (String[] eachtemp : tempResponse)
						if (eachtemp[0].equals(tempnodeNameNDL)) {
							String nodeKey = eachtemp[0].replaceAll(".*@", "");
							tempResult.addProperty(nodeKey, eachtemp[1]);
						} else {
							response.add(new String[] { eachtemp[0], eachtemp[1] });
						}
					if(tempResult.entrySet().size() != 0)
						response.add(new String[] { nodeNameNDL, tempResult.toString() });
				}
			} else {
				response.addAll(performAction(action, nodeNameNDL, inputValue));
			}
		}
		return response;
	}

	ArrayList<String[]> performAction(Action action, String nodeName, String nodeValue) {
		ArrayList<String[]> result = new ArrayList<String[]>();
		
		if (action.actionName.equalsIgnoreCase("useMap")) {
			ArrayList<String[]> response = new ArrayList<String[]>();
			if (!action.fieldMap.isEmpty()) {
				String keyValue = nodeValue;
				if (!action.caseValidation)
					keyValue = keyValue.toLowerCase();
				if (action.fieldMap.containsKey(keyValue))
					for (String eachValue : action.fieldMap.get(keyValue).split(";"))
						response.add(new String[] { nodeName, eachValue.trim() });
			} else {
				fieldtranslationList = Action.ftListGen;
				ArrayList<String[]> mapReturnValueList = null;
				if ((mapReturnValueList = staticFieldTranslate(nodeName, nodeValue)) != null) {
					for (String[] mapReturnValue : mapReturnValueList)
						if (!mapReturnValue[0].equals("remove")) {
							response.add(new String[] { mapReturnValue[0], mapReturnValue[1] });
						}
				}
			}
			result = response;
		}
		if (action.actionName.equalsIgnoreCase("lookup")) {
			ArrayList<String[]> response = new ArrayList<String[]>();
			ArrayList<String[]> lookupResponse = new ArrayList<String[]>();
			if (!(lookupResponse = getLookuptarget(nodeName, nodeValue)).isEmpty()) {
				for (String[] returnValue : lookupResponse)
						for (String mapSplitValue : returnValue[1].split(";"))
							response.add(new String[] { returnValue[0], mapSplitValue.trim() });
			}
			result = response;
		}
		
		if (action.actionName.equalsIgnoreCase("moveField")) {
			ArrayList<String[]> response = new ArrayList<String[]>();
			ArrayList<String[]> target = new ArrayList<String[]>();
			if (!(target = getmoveFieldtarget(nodeName, nodeValue)).isEmpty()) {
				for (String[] returnValue : target)
					if (!returnValue[0].equals("remove")) {
						for (String mapSplitValue : returnValue[1].split(";"))
							response.add(new String[] { returnValue[0], mapSplitValue.trim() });
					}
			}
			result = response;
		}
		if (action.actionName.equalsIgnoreCase("copyData")) {
			ArrayList<String[]> response = new ArrayList<String[]>();
			if (!action.targetField.isEmpty())
				response.add(new String[] { action.targetField, nodeValue });
			else
				response.add(new String[] { nodeName, nodeValue });
			result = response;
		}
		
		if (action.actionName.equalsIgnoreCase("deleteKey")) {
			ArrayList<String[]> response = new ArrayList<String[]>();
			String key = nodeName.replaceAll(".*@(.*)", "$1");
			if(!action.deleteKeyList.contains(key))
				response.add(new String[] { nodeName, nodeValue });
			
			result = response;
		}
		
		if (action.actionName.equalsIgnoreCase("setValue")) {
			ArrayList<String[]> response = new ArrayList<String[]>();
			
			if(response.isEmpty()) {
				if(!action.targetValue.isEmpty())
					response.add(new String[] { nodeName, action.targetValue });
				else
					response.add(new String[] { nodeName, nodeValue });
			}
			result = response;
		}
		
		if (action.actionName.equalsIgnoreCase("curate")) {
			ArrayList<String[]> response = new ArrayList<String[]>();
			nodeValue = nodeValue.trim();
			for(Action subaction : action.subActionSequence) {
				switch(subaction.actionName) {
				case "removeTokens":
					if(subaction.remTokensMap.isEmpty()) {
						for (String tokens : subaction.genTokens) {
							nodeValue = nodeValue.replaceAll(tokens, "");
						}
						nodeValue = nodeValue.replaceAll("\\s+", " ");
					} else {
						for(Map.Entry<String, ArrayList<String>> entry : subaction.remTokensMap.entrySet()) {
							if (entry.getKey().equalsIgnoreCase("starts With")) {
								boolean run = true;
								while(run) {
									run = false;
									for (String tokens : entry.getValue())
										if(nodeValue.matches("(?i)^"+tokens+"(\\s|$).*")) {
											run = true;
											nodeValue = nodeValue.replaceAll("(?:^|\\s)" + tokens + "\\s", " ").trim();
									}
								}
							}
							else if (entry.getKey().equalsIgnoreCase("ends With")) {
								boolean run = true;
								while(run) {
									run = false;
									for (String tokens : entry.getValue())
										if(nodeValue.matches("(?i).*\\s" + tokens + "$")) {
											run = true;
											nodeValue = nodeValue.replaceAll("\\s" + tokens + "(?:\\s|$)", " ").trim();
									}
								}
							}
							else if (entry.getKey().equalsIgnoreCase("ends With")) {
								boolean run = true;
								while(run) {
									run = false;
									for (String tokens : entry.getValue())
										if(nodeValue.matches("(?i).*\\s" + tokens + "\\s.*")) {
											run = true;
											nodeValue = nodeValue.replaceAll("(?:^|\\s)" + tokens + "(?:\\s|$)", " ").trim();
									}
								}
							}
						}
					}
						
				
				
				}
			if (!action.targetField.isEmpty())
				response.add(new String[] { action.targetField, nodeValue });
			else
				response.add(new String[] { nodeName, nodeValue });
		}
			result = response;
		}
		
		return result;
	}

	ArrayList<String[]> staticFieldTranslate(String input_fieldName, String input_fieldValue) {
		ArrayList<String[]> result = null;
		input_fieldValue = input_fieldValue.toLowerCase();
		if (fieldtranslationList.containsKey(input_fieldName)) {
			HashMap<String, ArrayList<String[]>> valueMap = fieldtranslationList.get(input_fieldName);
			if (valueMap.containsKey(input_fieldValue)) {
				result = valueMap.get(input_fieldValue);
			}
		}
		return result;
	}

	ArrayList<String[]> getLookuptarget(String input_fieldName, String input_fieldValue) {
		String[] result = null;
		boolean matchelement = false;
		ArrayList<String[]> response = new ArrayList<String[]>();
		if (Action.lookupSet.containsKey(input_fieldName)) {
			for (String token : Action.lookupSet.get(input_fieldName)) {
				token = token.replaceAll("([\\W&&\\S])", "\\\\$1");
				if (input_fieldValue.matches("(?i).*\\b" + token + "\\b.*")) {
					matchelement = true;
					break;
				}
			}

			if (matchelement) {
				for (Map.Entry<String[], String[]> entry : Action.lookup.entrySet()) {
					String src_fieldName = entry.getKey()[0].trim();
					String src_fieldAction = entry.getKey()[1].trim();
					String srcValue = entry.getKey()[2].trim();
					srcValue = srcValue.replaceAll("([\\W&&\\S])", "\\\\$1");
					if (src_fieldName.equalsIgnoreCase(input_fieldName)
							&& input_fieldValue.matches("(?i).*(^|\\s)" + srcValue + "(\\s|$).*")) {
						if (src_fieldAction.equalsIgnoreCase("contains")) {
							result = new String[2];
							result[0] = entry.getValue()[0].trim();
							result[1] = entry.getValue()[1].trim();
							response.add(result);
						} else if (src_fieldAction.equalsIgnoreCase("starts with")) {
							if (input_fieldValue.matches("(?i)^" + srcValue + "\\s.*")) {
								result = new String[2];
								result[0] = entry.getValue()[0].trim();
								result[1] = entry.getValue()[1].trim();
								response.add(result);
							}
						} else if (src_fieldAction.equalsIgnoreCase("ends with")) {
							if (input_fieldValue.matches("(?i).*\\s" + srcValue + "$.*")) {
								result = new String[2];
								result[0] = entry.getValue()[0].trim();
								result[1] = entry.getValue()[1].trim();
								response.add(result);
							}
						} else if (src_fieldAction.equalsIgnoreCase("matches")) {
							if (input_fieldValue.matches("(?i)" + srcValue)) {
								result = new String[2];
								result[0] = entry.getValue()[0].trim();
								result[1] = entry.getValue()[1].trim();
								response.add(result);
							}
						}
					}
				}
			}
		}
		return response;
	}
	
ArrayList<String[]> getmoveFieldtarget(String input_fieldName, String input_fieldValue) {
	boolean matchelement = false;
	ArrayList<String> searchSpace = new ArrayList<String>();
	ArrayList<String[]> response = new ArrayList<String[]>();
	if(Action.moveSet.containsKey(input_fieldName)) {
		for(Map.Entry<String, ArrayList<String>> entry : Action.moveSet.get(input_fieldName).entrySet()) {
			String token = entry.getKey().replaceAll("([\\W&&\\S])", "\\\\$1");
			if(input_fieldValue.matches("(?i).*"+token+".*")) {
				matchelement = true;
				searchSpace = Action.moveSet.get(input_fieldValue).get(token);
				break;
			}
		}
		if(matchelement) {
			for (String searchCriterion : searchSpace) {
				HashMap<String, ArrayList<String[]>> valueSet = Action.moveField.get(input_fieldName).
						get(searchCriterion);
				slabel : switch (searchCriterion) {
				case "contains":
					for (Map.Entry<String, ArrayList<String[]>> entry : valueSet.entrySet()) {
						String searchToken = entry.getKey().trim();
						searchToken = searchToken.replaceAll("([\\W&&\\S])", "\\\\$1");
						if(input_fieldValue.matches("(?i).*(^|\\s)" + searchToken + "(\\s|$).*")) {
							response.addAll(valueSet.get(searchToken));
							break slabel;
						}
					}
				case "starts with":		
					for (Map.Entry<String, ArrayList<String[]>> entry : valueSet.entrySet()) {
						String searchToken = entry.getKey().trim();
						searchToken = searchToken.replaceAll("([\\W&&\\S])", "\\\\$1");
						if(input_fieldValue.matches("(?i)^" + searchToken + "\\s.*")) {
							response.addAll(valueSet.get(searchToken));
							break slabel;
						}
					}
				case "ends with":		
					for (Map.Entry<String, ArrayList<String[]>> entry : valueSet.entrySet()) {
						String searchToken = entry.getKey().trim();
						searchToken = searchToken.replaceAll("([\\W&&\\S])", "\\\\$1");
						if(input_fieldValue.matches("(?i).*\\s" + searchToken + "$.*")) {
							response.addAll(valueSet.get(searchToken));
							break slabel;
						}
					}
				case "matches":		
					for (Map.Entry<String, ArrayList<String[]>> entry : valueSet.entrySet()) {
						String searchToken = entry.getKey().trim();
						searchToken = searchToken.replaceAll("([\\W&&\\S])", "\\\\$1");
						if(input_fieldValue.matches("(?i)" + searchToken)) {
							response.addAll(valueSet.get(searchToken));
							break slabel;
						}
					}
				default:		
					for (Map.Entry<String, ArrayList<String[]>> entry : valueSet.entrySet()) {
						String searchToken = entry.getKey().trim();
						searchToken = searchToken.replaceAll("([\\W&&\\S])", "\\\\$1");
						String[] regexPart = searchCriterion.split("r\\:");
						String matchString = "";
						if(regexPart.length == 2)
							matchString = regexPart[1] + searchToken;
						else if (regexPart.length == 3)
							matchString = regexPart[1] + searchToken + regexPart[2];
						else
							System.err.println("Invalid Regex definition.");
						if(input_fieldValue.matches(matchString)) {
							response.addAll(valueSet.get(searchToken));
							break slabel;
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
