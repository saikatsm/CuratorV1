package curation;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import com.opencsv.CSVReader;

public class FieldMethodMap extends Base {
	
	ArrayList<String> validFieldList = new ArrayList<String>();
	
	public FieldMethodMap(String schemaPath) throws Exception{
		
		// TODO Auto-generated constructor stub
		CSVReader cr = new CSVReader(new FileReader(schemaPath + "/NDLSchema"));
		
		for(String[] row : cr.readAll())
			validFieldList.add(row[0]);
		
		cr.close();
	}

	public ArrayList<String[]> curateField(String nodeNameNDL, String textContent) throws Exception {

			ArrayList<String[]> response = new ArrayList<String[]>();

			switch (nodeNameNDL) {

			case "dc.date.other":
				response = curateDateOther(textContent,nodeNameNDL);
				break;
				
		/*	case "dc.date.submitted":
			//CurateDate method to be modified according to generic signature signature.
				response = curateDate(textContent);
				break;
		*/	
			case "dc.date.awarded":
				response = curateDateAwarded(textContent,nodeNameNDL);
				break;

			case "dc.contributor.author":
				response = curateAuthor(textContent,nodeNameNDL);
				break;
				
			case "dc.contributor.advisor":
				response = curateAdvisor(textContent,nodeNameNDL);
				break;
				
			case "dc.creator.researcher":
				response = curateResearcher(textContent,nodeNameNDL);
				break;
				
			case "dc.language.iso":
				response = curateLanguage(textContent,nodeNameNDL);
				break;
				
			case "ndl.sourceMeta.additionalInfo":
				response = curateAdditionalInfo(textContent,nodeNameNDL);
				break;
				
			case "dc.subject":
				response = curateSubject(textContent,nodeNameNDL);
				break;
				
			case "dc.format.extent":
				response = curateFormatExtent(textContent,nodeNameNDL);
				break;
				
			case "dc.title":
				response = curateTitle(textContent,nodeNameNDL);
				break;
				
			case "dc.title.alternative":
				response = curateTitleAlternative(textContent,nodeNameNDL);
				break;
				
			case "dc.publisher.department":
				response = curatePublisherDepartment(textContent,nodeNameNDL);
				break;
				
			case "dc.publisher.institution":
				response = curatePublisherInstitution(textContent,nodeNameNDL);
				break;

			default:
				throw new Exception("Field Name did not match with any curation method.");
			}
		
		return response;
	}
	
	public void checkFieldValidity(String inputFieldName) throws Exception {

		if(!validFieldList.contains(inputFieldName))
			throw new Exception("Field Name " + inputFieldName + " did not match with NDL Schema.");
	}
}
