import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;


public class GraphMatrix {
	private static HashSet<Integer> Loans;
	//hashmaps for the index of loanS,borrowers,LENDERS
	private static HashMap<Integer, Integer> LoanMap     = new HashMap<>();
	private static HashMap<Integer, Integer> LenderMap   = new HashMap<>();
	private static HashMap<Integer, Integer> BorrowerMap = new HashMap<>();

	public static void createMatrix(String dname) throws NumberFormatException, IOException {
		//creating three files for unique index mapping
		CreateIndex  (dname);
		CreateMatrix (dname);
	}

	private static void CreateMatrix(String dname) throws IOException {
		CreateMatrixLoanLender   (dname);
		CreateMatrixLoanBorrower (dname);
	}

	private static void CreateMatrixLoanBorrower(String dname) throws IOException {
		String fnameLB  = dname+"work_file/loan-borrower.txt";
		int row = LoanMap.size();
		int col = BorrowerMap.size();
		int [][]Matrix = new int [row][col];
		
		System.out.println(row+":"+col);
		
		BufferedReader reader;
		String line;
		
		reader = new BufferedReader(new FileReader(fnameLB));
		while ((line=reader.readLine())!=null) {
			String []S = line.split("\\s+");
			
			int     LoanID    = Integer.parseInt(S[0]);
			Integer loanIndex = LoanMap.get(LoanID);
			
			if (loanIndex != null) {
				for (int i=1; i<S.length; i++){
					int borrowerID        = Integer.parseInt(S[i]);
					Integer borrowerIndex = BorrowerMap.get(borrowerID);
					
					if (borrowerIndex!= null) {
						//now we need to make an entry into the matrix
						Matrix[loanIndex][borrowerIndex]=1;
					}
				}
			}
		}
		reader.close();
		
		PrintWriter writer = new PrintWriter(dname+"Matrix/loans_borrowers.txt", "UTF-8");
		for (int i=0; i<row; i++){
			for (int j=0; j<col; j++) {
				writer.print(Matrix[i][j]+" ");
			}
			writer.println();
		}
		writer.close();
	}

	private static void CreateMatrixLoanLender(String dname) throws IOException {
		String fnameLL  = dname+"work_file/lender-loans.txt";
		int row = LoanMap.size();
		int col = LenderMap.size();
		int [][]Matrix = new int [row][col];
		
		System.out.println(row+":"+col);
		
		BufferedReader reader;
		String line;
		
		reader = new BufferedReader(new FileReader(fnameLL));
		while ((line=reader.readLine())!=null) {
			String []S = line.split("\\s+");
			
			int     LenderID    = Integer.parseInt(S[0]);
			Integer lenderIndex = LenderMap.get (LenderID);
			
			if (lenderIndex != null) {
				for (int i=1; i<S.length; i++){
					int loanID        = Integer.parseInt(S[i]);
					Integer loanIndex = LoanMap.get (loanID);
					
					if (loanIndex != null) {
						//now we need to make an entry into the matrix
						Matrix[loanIndex][lenderIndex]=1;
					}
				}
			}
		}
		reader.close();
		
		PrintWriter writer = new PrintWriter(dname+"Matrix/loans_lenders.txt", "UTF-8");
		for (int i=0; i<row; i++){
			for (int j=0; j<col; j++) {
				writer.print(Matrix[i][j]+" ");
			}
			writer.println();
		}
		writer.close();
	}

	private static void CreateIndex(String dname) throws NumberFormatException, IOException {
		CreateIndexMapLoans    (dname);
		CreateIndexMapBorrowers(dname);
		CreateIndexMapLenders  (dname);
	}

	private static void CreateIndexMapLenders(String dname) throws IOException {
		String fnameLL  = dname+"work_file/lender-loans.txt";
		String fname    = dname+"/Matrix/lenders.txt";
		
		BufferedReader reader;
		String line;
		int index=0;
		
		reader = new BufferedReader(new FileReader(fnameLL));
		PrintWriter writer = new PrintWriter(fname, "UTF-8");
		while ((line=reader.readLine())!=null) {
			String []S = line.split("\\s+");
			
			int LenderID = Integer.parseInt(S[0]);
			boolean flag = false;
			
			for (int i=1; i<S.length; i++){
				int loadID = Integer.parseInt(S[i]);
				
				if (Loans.contains(loadID)) {
					flag = true;
					break;
				}
			}
			
			if (flag){
				writer.println(LenderID);
				LenderMap.put(LenderID, index);
				index++;
			}
		}
		reader.close();
		writer.close();
	}

	private static void CreateIndexMapBorrowers(String dname) throws IOException {
		HashSet<Integer> borrowers = new HashSet<>();
		
		String fname    = dname+"/Matrix/borrowers.txt";
		String fnameLB  = dname+"work_file/loan-borrower.txt";
		
		BufferedReader reader;
		String line;
		
		reader = new BufferedReader(new FileReader(fnameLB));
		PrintWriter writer = new PrintWriter(fname, "UTF-8");
		
		while ((line=reader.readLine())!=null) {
			String []S = line.split("\\s+");
			
			int LoanId = Integer.parseInt(S[0]);
			if (Loans.contains(LoanId)) {
				for (int i=1; i<S.length; i++){
					int borrowerID = Integer.parseInt(S[i]);
					borrowers.add(borrowerID);
				}
			}
		}
		
		int index=0;
		for (Integer borrowerID: borrowers){
			writer.println(borrowerID);
			BorrowerMap.put(borrowerID, index);
			index++;
		}

		writer.close();
		reader.close();
	}

	private static void CreateIndexMapLoans(String dname) throws NumberFormatException, IOException {
		HashSet<Integer> LoansFromBorrowers = new HashSet<>();
		HashSet<Integer> LoansFromLenders   = new HashSet<>();
		String Mdname   = dname+"/Matrix/";
		String fnameLB  = dname+"work_file/loan-borrower.txt";
		String fnameLL  = dname+"work_file/lender-loans.txt";

		BufferedReader reader;
		String line;
		
		reader = new BufferedReader(new FileReader(fnameLB));
		while ((line=reader.readLine())!=null) {
			String []S = line.split("\\s+");
			
			int LoanId = Integer.parseInt(S[0]);
			LoansFromBorrowers.add(LoanId);
		}
		reader.close();
		
		reader = new BufferedReader(new FileReader(fnameLL));
		while ((line=reader.readLine())!=null) {
			String []S = line.split("\\s+");
			
			int LenderId = Integer.parseInt(S[0]);
			for (int i=1; i<S.length; i++){
				int LoanId = Integer.parseInt(S[i]);			
				LoansFromLenders.add(LoanId);
			}
		}
		reader.close();

		System.out.println("LB : "+LoansFromBorrowers.size()+" LL : "+LoansFromLenders.size());
		
		Loans = new HashSet<>(LoansFromLenders);
		Loans.retainAll(LoansFromBorrowers);
		
		System.out.println("LOANS : "+Loans.size());
		PrintWriter writer = new PrintWriter(Mdname+"loans.txt", "UTF-8");
		int index=0;
		for (Integer LoanID: Loans){
			writer.println(LoanID);
			LoanMap.put(LoanID, index);
			index++;
		}
		writer.close();
	}
}
