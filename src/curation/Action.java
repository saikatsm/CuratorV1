package curation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Action {
	
	protected String actionName = "";
	protected String targetField = "";
	protected String targetValue = "";
	protected int executionOrder;
	protected ArrayList<String> targetvalueList = new ArrayList<String>();
	protected Boolean authorRender = true, removeTokens = true;
	protected String datatype = "";
	protected ArrayList<String> deleteKeyList = new ArrayList<String>();
	protected LinkedHashMap<String[],String[]> filtermap = new LinkedHashMap<String[],String[]>();

}
