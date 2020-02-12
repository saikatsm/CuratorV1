package curation;

import java.util.HashMap;

public class NDLSchemaDetails {

	HashMap<String, HashMap<String,Object>> NDLSchemaInfo = new HashMap<String, HashMap<String,Object>>();
	
	public NDLSchemaDetails() {
		// TODO Auto-generated constructor stub
		HashMap<String, Object> nodeProperties = new HashMap<String, Object>();
		nodeProperties.put("multiValued", false);
		nodeProperties.put("edited", false);
		NDLSchemaInfo.put("learningResourceType", nodeProperties);
		NDLSchemaInfo.put("language.iso", nodeProperties);
		NDLSchemaInfo.put("title", nodeProperties);
		NDLSchemaInfo.put("publisher.date", nodeProperties);
		NDLSchemaInfo.put("description.abstract", nodeProperties);
	}
	
	public HashMap<String, Object> getAtrributeType (String nodeNameNDL) {
		if(NDLSchemaInfo.containsKey(nodeNameNDL))
			return NDLSchemaInfo.get(nodeNameNDL);
		else
			return null;
		
	}
	
}
