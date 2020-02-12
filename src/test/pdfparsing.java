package test;

import java.io.File;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
/**
 * formal parameter is to give parent location of the item.
 **/

public class pdfparsing {

	public static int no_of_Pages;
	public static long size;
	
	static void getInfo(String contentPath) throws Exception {
			PdfReader reader = new PdfReader(contentPath);
			PdfReaderContentParser parser  = new PdfReaderContentParser(reader);
			no_of_Pages = reader.getNumberOfPages();
			reader.getPageContent(0);
	
	}
	
	public static void main (String args[]) throws Exception{
		
		getInfo("/home/arunavo/Desktop/data/CiteSeerX/COnstantTimeSoln_GraphColoring.pdf");
		System.out.println("No Of Pages: " + no_of_Pages);
		
	}

}