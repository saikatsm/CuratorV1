package test;

import java.io.File;

import curation.Base;
import curation.PostProcessing;
import utility.ValidateMappingFile;

public class RunPostProcessTest {
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		try {
			
			//source_root = "/home/arunavo/Desktop/data/Shodhganga/Inflibnet-Shodhganga-mapped-SIP/Shodhganga_postproc/";
			//target_root = "/home/arunavo/Desktop/data/Shodhganga/Inflibnet-Shodhganga-mapped-SIP/Shodhganga_postprocII/";
			
			PostProcessing locpp = new PostProcessing();
			
			Base.source_root = "/home/arunavo/Desktop/data/Shodhganga/sampleTrans/COLL@shodhganga_10603_100/";
			Base.target_root = "/home/arunavo/Desktop/data/Shodhganga/sampleTrans_pp/COLL@shodhganga_10603_100/";
			Base.logPath = "/home/arunavo/Desktop/data/Shodhganga/log/";
			
			locpp.traverse(new File(Base.source_root));
			
			locpp.set_logs("pp_out", "pp_err");
			locpp.set_logs_close();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}
	
}
