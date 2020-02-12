package curation.release;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVReader;

import curation.release.Base;

public class Action {
	
	public String actionName = "";
	public String targetField = "";
	public String targetValue = "";
	public int executionOrder;
	protected ArrayList<Action> subActionSequence = new ArrayList<Action>();
	public ArrayList<String> targetvalueList = new ArrayList<String>();
	protected Boolean authorRender = true, removeTokens = true, caseValidation = false;
	protected String datatype = "";
	protected ArrayList<String> deleteKeyList = new ArrayList<String>();
	protected static HashMap<String,HashMap<String, ArrayList<String[]>>> ftListGen = new HashMap<String, HashMap<String, ArrayList<String[]>>>();
	protected static HashMap<String[], String[]> lookup = new HashMap<String[], String[]>();
	protected static HashMap<String, HashSet<String>> lookupSet = new HashMap<String, HashSet<String>>();
	protected static HashMap<String, HashMap<String,HashMap<String, ArrayList<String[]>>>> moveField = new HashMap<String, HashMap<String,HashMap<String, ArrayList<String[]>>>>();
	protected static HashMap<String, HashMap<String,ArrayList<String>>> moveSet = new HashMap<String, HashMap<String, ArrayList<String>>>();
	protected HashMap<String, String> fieldMap = new HashMap<String, String>();
	protected HashMap<String, ArrayList<String>> remTokensMap = new HashMap<String,ArrayList<String>>();
	protected ArrayList<String> genTokens = new ArrayList<String>(); 
	
	protected void loadMapFile() throws Exception {
		if(ftListGen.isEmpty()) {
			CSVReader crFT = new CSVReader(new FileReader(Base.configPath + "/fieldtranslation.csv"), '|');
			for (String[] row : crFT.readAll()) {
				String keyField = row[0].trim();
				String keyvalue = row[1].toLowerCase().trim();
				if (ftListGen.containsKey(keyField)) {
					if (ftListGen.get(keyField).containsKey(keyvalue)) {
						ftListGen.get(keyField).get(keyvalue).add(new String[] { row[2].trim(), row[3].trim() });
					} else {
						ftListGen.get(keyField).put(keyvalue, new ArrayList<String[]>() {
							{
								add(new String[] { row[2].trim(), row[3].trim() });
							}
						});
					}
				} else {
					HashMap<String, ArrayList<String[]>> valueMap = new HashMap<String, ArrayList<String[]>>();
					valueMap.put(keyvalue, new ArrayList<String[]>() {
						{
							add(new String[] { row[2].trim(), row[3].trim() });
						}
					});
					ftListGen.put(keyField, valueMap);
				}
			}
			crFT.close();
		}
	}
	
	protected void loadDict(String dictvalue) {
		JsonParser parser = new JsonParser();
		Object obj = parser.parse(dictvalue);
		JsonObject jsonObject = (JsonObject) obj;
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) 
			fieldMap.put(entry.getKey().toLowerCase().trim(), entry.getValue().toString().toLowerCase().trim());
	}
	
	protected void loadLookup() throws Exception {
		if (lookup.isEmpty()) {
			CSVReader crFTSA = new CSVReader(new FileReader(Base.configPath + "/lookup.csv"), '|');
			for (String[] row : crFTSA.readAll()) {
				String key = row[0].trim();
				lookup.put(new String[] { key, row[1].trim(), row[2].trim() },
						new String[] { row[3].trim(), row[4].trim() });
				if (lookupSet.containsKey(key)) {
					lookupSet.get(key).add(row[2].trim());
				} else {
					HashSet<String> set = new HashSet<String>();
					set.add(row[2].trim());
					lookupSet.put(key, set);
				}
			}
			crFTSA.close();
		}
	}
	
	protected void loadmoveField() throws Exception {
		if (moveField.isEmpty()) {
			CSVReader crauthMove = new CSVReader(new FileReader(Base.configPath + "/moveField.csv"), '|', '"');
			for (String[] row : crauthMove.readAll()) {
				String keyField = row[0].trim();
				String searchSpace = row[1].trim();
				String searchValue = row[2].trim().toLowerCase();
				if (moveField.containsKey(keyField)) {
					if (moveField.get(keyField).containsKey(searchSpace)) {
						if (moveField.get(keyField).get(searchSpace).containsKey(searchValue)) {
							moveField.get(keyField).get(searchSpace).get(searchValue).add(new String[] {row[3].trim(), row[4].trim()});
						} else {
							moveField.get(keyField).get(searchSpace).put(searchValue, new ArrayList<String[]>() {{
								add (new String[] {row[3].trim(), row[4].trim()});
							}});
							moveSet.get(keyField).put(searchValue, new ArrayList<String>() {{ add(searchSpace); }});
						}
					} else {
						moveField.get(keyField).put(searchSpace, new HashMap<String, ArrayList<String[]>>() {{
								put(searchValue, new ArrayList<String[]> () {{
									add(new String[] { row[3].trim(), row[4].trim()});
								}});
							}});
						if(moveSet.get(keyField).containsKey(searchValue))
							moveSet.get(keyField).get(searchValue).add(searchSpace);
						else
							moveSet.get(keyField).put(searchValue, new ArrayList<String>() {{ add(searchSpace); }} );
					}
				} else {
					moveField.put(keyField, new HashMap<String, HashMap<String, ArrayList<String[]>>>() {{ 
					put(searchSpace, new HashMap<String, ArrayList<String[]>>() {{ 
						put(searchValue, new ArrayList<String[]> () {{
							add(new String[] { row[3].trim(), row[4].trim() });
							}});
						}});
					}});
					moveSet.put(keyField, new HashMap<String, ArrayList<String>>() {{
						put(searchValue, new ArrayList<String>() {{ add(searchSpace); }} );
					}});
				}
			}
			crauthMove.close();
		}
	}
	
	protected void loadTokensFile(String tokensFilePath) throws Exception{
		CSVReader crremToken = new CSVReader(new FileReader(Base.configPath + "/moveField.csv"), '|', '"');
		for (String[] row : crremToken.readAll()) {
			String pattern = row[0].trim();
			String value = row[1].trim();
			value = value.replaceAll("([\\W&&\\S])", "\\\\$1");
			if(!remTokensMap.containsKey(pattern)) {
				ArrayList<String> __temp = new ArrayList<String>();
				__temp.add(value);
				remTokensMap.put(pattern, __temp );
			} else {
				remTokensMap.get(pattern).add(value);
			}
		}
		crremToken.close();
	}
}
