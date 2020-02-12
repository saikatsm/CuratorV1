package test;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import curation.Action;

public class NSP1 {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
//		String title = "THIS IS A SAMPLE of 101.TXT TEXT. WHERE EVERY LETTER IS CAPITALIZED.";
//		String updTitle = "";
//		for (String titleParts : title.split("\\.(\\s|$)")) {
//			titleParts = titleParts.trim();
//			if(titleParts.length()!=0) {
//				int pst = 0, pend = 0;
//				Pattern pattern = Pattern.compile("[^\\s]*\\.[^\\s]*");
//				Matcher matcher = pattern.matcher(titleParts);
//				while(matcher.find()) {
//					pst = matcher.start();
//					pend = matcher.end();
//				}
//				titleParts = titleParts.substring(0, pst).toLowerCase()+titleParts.substring(pst, pend)+titleParts.substring(pend).toLowerCase();
//				updTitle += StringUtils.capitalize(titleParts) + ". ";
//			}
//		}
//		updTitle = updTitle.trim();
//		System.out.println(updTitle);
	
		String input= "example String";
		String regex = "";
		String output = input.replaceAll("^(.*)", "new Text $1");
		System.out.println(output);
	
	}
	
}
