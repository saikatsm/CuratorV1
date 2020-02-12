package test;

import java.io.FileReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.text.similarity.LevenshteinDetailedDistance;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.commons.text.similarity.LevenshteinResults;

import com.opencsv.CSVReader;

public class findpartialMatch {
static int count= 0;
static HashMap<String,String> result = new HashMap<String, String>();
static Set<String> ref = new HashSet<String>();
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		CSVReader cr = new CSVReader(new FileReader("/home/arunavo/Desktop/data/CiteSeerX/random/author_handle_Article_old.csv"),'|','"');
	for (String[] row : cr.readAll()) {
		ref.add(row[0]);
	}
	cr.close();
	System.out.println("Source Read complete.");
	LevenshteinDistance ld = new LevenshteinDistance();
	
	CSVReader cr1 = new CSVReader(new FileReader("/home/arunavo/Desktop/data/CiteSeerX/random/test"));
	for (String[] row : cr1.readAll()) {
		System.out.println(count + " : " + row[0]);
		if(!result.containsKey(row[0])) {
			result.put(row[0],"");
		int dist = 99999;
		for(String s_ref : ref) {
			int comp = ld.apply(row[0], s_ref);
			if(dist > comp) {
				dist = comp;
				result.put(row[0], s_ref);
			}
		}
		}
		count++;
	}
	System.out.println(count);
	PrintStream out = new PrintStream("/home/arunavo/Desktop/data/CiteSeerX/random/testresults");
	for(Map.Entry<String, String> entry : result.entrySet())
		out.println(entry.getKey() + "|" + entry.getValue());
	cr1.close();
	out.close();
	}

}
