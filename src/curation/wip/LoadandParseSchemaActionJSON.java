package curation.wip;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;

public class LoadandParseSchemaActionJSON {

	HashMap<String,ArrayList<Action>> schemaAction = new HashMap<String, ArrayList<Action>>();
	HashMap<String, HashMap<String,ArrayList<Action>>> schemaActionByHandle = new HashMap<String, HashMap<String, ArrayList<Action>>>();
	
	public LoadandParseSchemaActionJSON() {
		try {
		
			JsonParser parser = new JsonParser();
		
		if(Base.runType.equalsIgnoreCase("-h")){
			
			CSVReader cr = new CSVReader(new FileReader(Base.configPath + "/"+Base.schemaActionFile), '|');
			for (String[] row : cr.readAll()) {
				String handle = row[0].trim();
				String logicString = row[1].trim();
				//System.out.println(logicString);
				Object obj = parser.parse(logicString);
				JsonObject jsonObject = (JsonObject) obj;
				Type arrType = new TypeToken<ArrayList<String>>() {}.getType();
				Gson convert = new Gson();
				HashMap<String,ArrayList<Action>> logic = new HashMap<String, ArrayList<Action>>();
				for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					String fieldName = entry.getKey();
					JsonObject fieldLogic =  entry.getValue().getAsJsonObject();
					JsonArray action_jsarr = fieldLogic.getAsJsonArray("action");
					ArrayList<String> fieldActions = convert.fromJson(action_jsarr, arrType);
					ArrayList<Action> fieldActionObjects = new ArrayList<Action>();
					
					if(schemaActionByHandle.containsKey(handle))
						logic = schemaActionByHandle.get(handle);
					
					boolean validAction = true;
					String errAction = "";
					int exOrder = 0;
					actionList : for(String actionName : fieldActions)
						switch (actionName) {
						case "useMap":
							Action useMap = new Action();
							useMap.actionName = "useMap"; 
							useMap.executionOrder = exOrder++;
							fieldActionObjects.add(useMap);
							break;
						case "curate":
							Action curate = new Action();
							curate.actionName = "curate";
							curate.executionOrder = exOrder++;
							if (fieldLogic.has("curate"))
								if (fieldLogic.getAsJsonObject("curate").get("targetField") != null)
									curate.targetField = fieldLogic.getAsJsonObject("curate").get("targetField").getAsString();
							fieldActionObjects.add(curate);
							break;
						case "delete":
							Action delete = new Action();
							delete.actionName = "delete";
							delete.executionOrder = exOrder++;
							fieldActionObjects.add(delete);
							break;
						case "deleteField":
							Action deleteField = new Action();
							deleteField.actionName = "deleteField";
							deleteField.executionOrder = exOrder++;
							fieldActionObjects.add(deleteField);
							break;
						case "copyData":
							Action copyData = new Action();
							copyData.actionName = "copyData";
							copyData.executionOrder = exOrder++;
							if (fieldLogic.has("copyData"))
								if (fieldLogic.getAsJsonObject("copyData").get("targetField") != null)
									copyData.targetField = fieldLogic.getAsJsonObject("copyData").get("targetField").getAsString();
							fieldActionObjects.add(copyData);
							break;
						case "replaceTokens":
							Action replaceTokens = new Action();
							replaceTokens.actionName = "replaceTokens";
							replaceTokens.executionOrder = exOrder++;
							fieldActionObjects.add(replaceTokens);
							break;
						case "add":
							Action add = new Action();
							add.actionName = "add";
							add.executionOrder = exOrder++;
							if (fieldLogic.has("add"))
								if (fieldLogic.getAsJsonObject("add").get("targetValue") != null) {
									if(fieldLogic.getAsJsonObject("add").get("targetValue") instanceof JsonArray)
										add.targetvalueList = convert.fromJson(fieldLogic.getAsJsonObject("add").getAsJsonArray("targetValue"), arrType);
									else
									add.targetValue = fieldLogic.getAsJsonObject("add").get("targetValue").getAsString();
								fieldActionObjects.add(add);
							}
							break;
						case "moveField":
							Action moveField = new Action();
							moveField.actionName = "moveField";
							moveField.executionOrder = exOrder++;
							fieldActionObjects.add(moveField);
							break;
						case "lookup":
							Action lookup = new Action();
							lookup.actionName = "lookup";
							lookup.executionOrder = exOrder++;
							fieldActionObjects.add(lookup);
							break;
						case "setValue":
							Action setValue = new Action();
							setValue.actionName = "setValue";
							setValue.executionOrder = exOrder++;
							if (fieldLogic.has("setValue"))
								if (fieldLogic.getAsJsonObject("setValue").get("targetValue") != null) {
									setValue.targetValue = fieldLogic.getAsJsonObject("setValue").get("targetValue").getAsString();
									fieldActionObjects.add(setValue);
									
									Action add_additional = new Action();
									add_additional.actionName = "add";
									add_additional.executionOrder = exOrder++;
									add_additional.targetValue = setValue.targetValue;
									fieldActionObjects.add(add_additional);
								}
							break;
						default:
							validAction = false;
							errAction = actionName;
							break actionList;
						}
					if(validAction) {
						logic.put(fieldName, fieldActionObjects);
					}
					else {
						System.out.println("Invalid Operation "  + fieldName +"@"+ errAction + " in schemaAction.json file.\nTerminating Program.");
						System.exit(0);
					}
				}
				schemaActionByHandle.put(handle, logic);
			}
			cr.close();			
		} else {
			
		Object obj = parser.parse(new FileReader(Base.configPath + "/" + Base.schemaActionFile));
		JsonObject jsonObject = (JsonObject) obj;
		Type arrType = new TypeToken<ArrayList<String>>() {}.getType();
		Gson convert = new Gson();
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String fieldName = entry.getKey();
			JsonObject fieldLogic =  entry.getValue().getAsJsonObject();
			JsonArray action_jsarr = fieldLogic.getAsJsonArray("action");
			ArrayList<String> fieldActions = convert.fromJson(action_jsarr, arrType);
			ArrayList<Action> fieldActionObjects = new ArrayList<Action>();
			boolean validAction = true;
			String errAction = "";
			int exOrder = 0;
			actionList : for(String actionName : fieldActions) {
				switch (actionName) {
				case "useMap":
					Action useMap = new Action();
					useMap.actionName = "useMap";
					useMap.executionOrder = exOrder++;
					if(fieldLogic.has("useMap")) {
						if(fieldLogic.getAsJsonObject("useMap").get("loadFile").getAsBoolean()) {
							String mapFileName = fieldLogic.getAsJsonObject("useMap").get("mapFileName").getAsString();
							useMap.loadMapFile(mapFileName);
						}
					} else 
						useMap.loadMapFile();
					fieldActionObjects.add(useMap);
					break;
				case "curate":
					Action curate = new Action();
					curate.actionName = "curate";
					curate.executionOrder = exOrder++;
					if (fieldLogic.has("curate"))
						if (fieldLogic.getAsJsonObject("curate").get("targetField") != null)
							curate.targetField = fieldLogic.getAsJsonObject("curate").get("targetField").getAsString();
					fieldActionObjects.add(curate);
					break;
				case "delete":
					Action delete = new Action();
					delete.actionName = "delete";
					delete.executionOrder = exOrder++;
					fieldActionObjects.add(delete);
					break;
				case "deleteField":
					Action deleteField = new Action();
					deleteField.actionName = "deleteField";
					deleteField.executionOrder = exOrder++;
					fieldActionObjects.add(deleteField);
					break;
				case "copyData":
					Action copyData = new Action();
					copyData.actionName = "copyData";
					copyData.executionOrder = exOrder++;
					if (fieldLogic.has("copyData"))
						if (fieldLogic.getAsJsonObject("copyData").get("targetField") != null)
							copyData.targetField = fieldLogic.getAsJsonObject("copyData").get("targetField").getAsString();
					fieldActionObjects.add(copyData);
					break;
				case "replaceTokens":
					Action replaceTokens = new Action();
					replaceTokens.actionName = "replaceTokens";
					replaceTokens.executionOrder = exOrder++;
					fieldActionObjects.add(replaceTokens);
					break;
				case "add":
					Action add = new Action();
					add.actionName = "add";
					add.executionOrder = exOrder++;
					if (fieldLogic.has("add"))
						if (fieldLogic.getAsJsonObject("add").get("targetValue") != null) {
							if(fieldLogic.getAsJsonObject("add").get("targetValue") instanceof JsonArray)
								add.targetvalueList = convert.fromJson(fieldLogic.getAsJsonObject("add").getAsJsonArray("targetValue"), arrType);
							else
							add.targetValue = fieldLogic.getAsJsonObject("add").get("targetValue").getAsString();
						fieldActionObjects.add(add);
					}
					break;
				case "moveField":
					Action moveField = new Action();
					moveField.actionName = "moveField";
					moveField.executionOrder = exOrder++;
					fieldActionObjects.add(moveField);
					break;
				case "lookup":
					Action lookup = new Action();
					lookup.actionName = "lookup";
					lookup.executionOrder = exOrder++;
					lookup.loadLookup();
					fieldActionObjects.add(lookup);
					break;
				case "setValue":
					Action setValue = new Action();
					setValue.actionName = "setValue";
					setValue.executionOrder = exOrder++;
					if (fieldLogic.has("setValue"))
						if (fieldLogic.getAsJsonObject("setValue").get("targetValue") != null) {
							setValue.targetValue = fieldLogic.getAsJsonObject("setValue").get("targetValue").getAsString();
							fieldActionObjects.add(setValue);
							
							Action add_additional = new Action();
							add_additional.actionName = "add";
							add_additional.executionOrder = exOrder++;
							add_additional.targetValue = setValue.targetValue;
							fieldActionObjects.add(add_additional);
						}
					break;
				default:
					validAction = false;
					errAction = actionName;
					break actionList;
				}
		}
			if(validAction) {
				schemaAction.put(fieldName, fieldActionObjects);
			}
			else {
				System.out.println("Invalid Operation "  + fieldName +"@"+ errAction + " in schemaAction.json file.\nTerminating Program.");
				System.exit(0);
			}
		}
		}
	} catch (Exception e) {
		e.printStackTrace();
		System.out.println("Schema Action File is invalid(empty/corrupt). Nothing to change in files."
				+ "\nTerminating Progam.");
		System.exit(0);
	}
		
		
	}
	
/*	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
*/
}
