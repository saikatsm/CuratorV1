package curation.release;

import java.util.HashMap;

public class NDLSchemaDetails {

	final HashMap<String, HashMap<String,Object>> NDLSchemaInfo = new HashMap<String, HashMap<String,Object>>();
	
	public NDLSchemaDetails() {
		// TODO Auto-generated constructor stub
		HashMap<String, Object> nodeProperties = new HashMap<String, Object>();
		nodeProperties.put("multiValued", false);
		NDLSchemaInfo.put("learningResourceType", nodeProperties);
		NDLSchemaInfo.put("language.iso", nodeProperties);
		NDLSchemaInfo.put("title", nodeProperties);
	}
	
	public HashMap<String, Object> getAtrributeType (String nodeNameNDL) {
		if(NDLSchemaInfo.containsKey(nodeNameNDL))
			return NDLSchemaInfo.get(nodeNameNDL);
		else
			return null;
		
	}
	
}
