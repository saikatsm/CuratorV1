package utility;

import java.io.FileReader;

import com.opencsv.CSVReader;

public class ValidateMappingFile {
	
	boolean showdata = false;
	
	public ValidateMappingFile(boolean showdata) {
		// TODO Auto-generated constructor stub
		
		this.showdata = showdata;	
	}

	public boolean validate(String targetFilePath, int targetCount) throws Exception {
		
			int count = 0;

			if (!(targetCount == 0 || targetFilePath.isEmpty())) {

				CSVReader cr = new CSVReader(new FileReader(targetFilePath), '|', '"');

				for (String[] row : cr.readAll()) {
					++count;

					if (showdata) {

						String printString = "";

						for (int i = 0; i < row.length; i++)
							printString = printString + row[i] + "|";

						printString = printString.replaceAll("\\|$", "");
						System.out.println(printString);
					}
				}

				System.out.println("Total lines read = " + count);

				cr.close();

				if (targetCount != count) {

					throw new Exception(
							"CSV Parsing error in One to One Mapping File. Target Count and Parse Count doesn't match.");

				} else {

					System.out.println("CSV Parsing success.");

					return true;
				}

			} else {

				throw new Exception("Please check input parameters. Either the line count is 0 is the target path is empty.");
			}

	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		String targetFile = args[0];
		int targetCount = Integer.parseInt(args[1]);
		String showData = args[2];
		boolean show = false;
		if(showData.equalsIgnoreCase("true"))
			show = true;
		else if (showData.equalsIgnoreCase("false"))
			show = false;
		else
			throw new Exception ("argument 3 wrong. Value is between true/false");
		ValidateMappingFile vm = new ValidateMappingFile(show);
		vm.validate(targetFile, targetCount);

	}
}
