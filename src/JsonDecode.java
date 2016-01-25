import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;



public class JsonDecode {
	
	public static void main (String []args) throws IOException, ParseException, org.json.simple.parser.ParseException{		
		do {
			if (args.length < 1) {
				System.out.println("Provide the Following arguments to the program");
				System.out.println("1) Location of directory conataining \"Loan\" json directory");
				break;
			}

			//process_loans(args[0]);
			process_lenders(args[0]);
			//process_loans_lenders      (args[0]);
			//process_loans_lenders_file (args[0]);
			//createMatLenderLoan        (args[0]);
			//createLenderTeamsFile      (args[0]);
		} while(false);
	}
	
	private static void createMatLenderLoan(String dname) throws NumberFormatException, IOException {
		GraphMatrix.createMatrix(dname);
	}
	
	private static void createLenderTeamsFile (String dname) throws FileNotFoundException, IOException, org.json.simple.parser.ParseException{
		Teams.createLenderTeamsFile(dname);
		
	}

	private static void process_loans_lenders_file(String dname) throws IOException {
		LoansLenders.ProcessLoansLendersFile(dname+"/work_file/");
	}

	private static void process_loans_lenders(String dname) throws FileNotFoundException, IOException, org.json.simple.parser.ParseException {
		// the function will map each loan to the set of lenders it is associated with.
		// each lender is associated with a numeric id
		//redirect the output to a file
		LoansLenders.ProcessLoansLendersDir(dname+"/loans_lenders/");
	}

	private static void process_lenders(String dname) throws FileNotFoundException, IOException, org.json.simple.parser.ParseException {
		Lenders.ProcessLendersDir(dname);
	}

	private static void process_loans(String dname) throws IOException, ParseException, org.json.simple.parser.ParseException {
		Loans.ProcessLoansDir(dname+"/loans/");
		System.out.println();

		Loans.PrintStat();
		Loans.GeneratePlotInput(dname);
	}
}

