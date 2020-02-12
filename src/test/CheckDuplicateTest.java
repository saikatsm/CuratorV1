package test;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import com.opencsv.CSVReader;

import curation.Base;
import curation.DuplicateRemoval;

public class CheckDuplicateTest {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		Base.source_root = "/home/arunavo/Desktop/data/Shodhganga/sampleTrans_pp/";
		Base.logPath = "/home/arunavo/Desktop/data/Shodhganga/log/";
		String[] fieldList = new String[] {"dc.contributor.advisor","dc.anything.absent", "dc.creator.researcher", "dc.date.awarded"};
		
		DuplicateRemoval dr = new DuplicateRemoval(true);
		dr.set_logs("out_dup","err_dup");
		dr.checkFields = new ArrayList<String>( Arrays.asList(fieldList) );
		
		String printString = "handle|parentHandle|duplicateCount|title|";
		
		for(String fields : dr.checkFields)
			printString += fields + "|";
		printString = printString.replaceAll("\\|$", "");
		
		Base.pr.println(printString);
		
		dr.traverse(new File(Base.source_root));		
		int parentCount = 0, duplicateCount = 0;
		
		for(Map.Entry<String, ArrayList<String>> entry : dr.itemMap.entrySet()) {
			ArrayList<String> duplicateList = entry.getValue(); 
			if(duplicateList.size() > 1) {
				++parentCount;
				for(int i = 1; i < duplicateList.size(); i++) {
					++duplicateCount;
					Base.pr.println(duplicateList.get(i) + "|" + duplicateList.get(0) + "|" + (duplicateList.size()-1) +"|" +entry.getKey());
				}
			}
		}		
		System.out.println(duplicateCount + " duplicates found in " + parentCount + " sets.");		
		dr.set_logs_close();
	}
	
}
