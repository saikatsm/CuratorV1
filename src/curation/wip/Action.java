package curation.wip;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.opencsv.CSVReader;

public class Action {
	
	public String actionName = "";
	public String targetField = "";
	public String targetValue = "";
	public int executionOrder;
	public ArrayList<String> targetvalueList = new ArrayList<String>();
	protected static HashMap<String,HashMap<String, ArrayList<String[]>>> ftListGen = new HashMap<String, HashMap<String, ArrayList<String[]>>>();
	protected static HashMap<String[], String[]> lookup = new HashMap<String[], String[]>();
	protected static HashMap<String, HashSet<String>> lookupSet = new HashMap<String, HashSet<String>>();
	protected void loadMapFile() throws Exception {
		if(ftListGen.isEmpty()) {
			CSVReader crFT = new CSVReader(new FileReader(Base.configPath + "/fieldtranslation.csv"), '|');
			for (String[] row : crFT.readAll()) {
				String keyField = row[0].trim();
				String keyvalue = row[1].toLowerCase().trim();
				// System.out.println(keyvalue);
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
	protected void loadLookup() throws Exception{	
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
	}
}
