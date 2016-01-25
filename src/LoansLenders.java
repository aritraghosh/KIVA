import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class LoansLenders {
	private static final int MINLOANS=200;
	private static final int MAXLOANS=1000000;
	private static JSONParser parser = new JSONParser();
	private static JSONObject jsonObject;
	private static HashMap<String, Integer> LenderID = new HashMap<>(); //hash use to give each lender a unique id
	private static int GlobalLenderID=0;
	private static HashMap<Integer, ArrayList<Integer>> lenderLoansMap = new HashMap<>(); //maps a lender to his loans
	private static PrintWriter writer;
	private static PrintWriter writer_lender;

	public static void ProcessLoansLendersDir(String dname) throws FileNotFoundException, IOException, ParseException {
		File folder = new File(dname);
		File[] listOfFiles = folder.listFiles();
		//System.out.print("Processing  ");
		String fname        = dname+"/../work_file/loans_lenders.txt";
		String fname_lender = dname+"/../work_file/lenders_map.txt";

		writer        = new PrintWriter(fname,        "UTF-8");
		writer_lender = new PrintWriter(fname_lender, "UTF-8");
		
		for (int i = 0; i < listOfFiles.length; i++) {
			//ShowProgress.Show();
			Object obj = parser.parse(new FileReader(listOfFiles[i]));
			jsonObject = (JSONObject) obj;

			ProcessLoansLenders(false);
			parser.reset(); //doing this instead of file close is it OK?
		}
		writer.close();
		writer_lender.close();
	}

	private static void ProcessLoansLenders(boolean print) {
		JSONArray loans_lenders = (JSONArray) jsonObject.get("loans_lenders");
		
		if (print)
			System.out.println("The number of loans_lenders is : "+loans_lenders.size());
		
		Iterator itr = loans_lenders.iterator();
		while (itr.hasNext()) {
			JSONObject loan_lenders = (JSONObject) itr.next();
			long id = (long) loan_lenders.get("id");
			
			JSONArray lenders = (JSONArray) loan_lenders.get("lender_ids");
			if ((lenders == null) || 
					(lenders.size()) == 0) {
				//there are no lenders so do nothing
				continue;
			}
			
			Iterator _itr = lenders.iterator();
			writer.print(id+" ");
			while (_itr.hasNext()) {
				String lender = (String) _itr.next();
				int id_lender = GetLenderID (lender);
				writer.print(" "+id_lender);
			}
			writer.println("");
		}
	}

	private static int GetLenderID(String lender) {
		int ret=0;
		if (LenderID.containsKey(lender))
			ret = LenderID.get(lender);
		else {
			GlobalLenderID++;
			LenderID.put(lender, GlobalLenderID);
			ret = GlobalLenderID;
			
			writer_lender.println(GlobalLenderID+" "+lender);
		}
		
		return ret;
	}

	public static void ProcessLoansLendersFile(String fname) throws IOException {
		//ProcessLoansLendersFileGetHistLendersPerLoan(fname);
		//ProcessLoansLendersFileGetHistLoansPerLender(fname);
		CreateLenderLoansFileFromLonsLender(fname);
	}

	private static void CreateLenderLoansFileFromLonsLender(String dname) throws IOException {
		//redirect the output to a file
		BufferedReader reader;
		String line;
		String fname = dname+"loans_lenders.txt";
		
		reader = new BufferedReader(new FileReader(fname));
		while ((line=reader.readLine())!=null) {
			String []S = line.split("\\s+");
			int loan = Integer.parseInt(S[0]);
			
			for (int i=1; i<S.length; i++){
				int lender = Integer.parseInt(S[i]);
				AddLoanToLendersMap(lender, loan);
			}
		}
		reader.close();
		
		fname = dname+"lender-loans.txt";
		
		PrintWriter writer = new PrintWriter(fname, "UTF-8");
		for (int lender : lenderLoansMap.keySet()){
			ArrayList<Integer> loans = lenderLoansMap.get(lender);
			int s = loans.size();
			
			if ((s >= MINLOANS) &&
					(s <= MAXLOANS)){
				writer.print(lender+" ");
				for (int l : loans) 
					writer.print(l+" ");
				writer.println();
			}
		}
		writer.close();
	}

	private static void AddLoanToLendersMap(int lender, int loan) {
		if (lenderLoansMap.containsKey(lender)) {
			ArrayList<Integer> loans = lenderLoansMap.get(lender);
			loans.add(loan);
		} else {
			ArrayList<Integer> loans = new ArrayList<>();
			loans.add(loan);
			lenderLoansMap.put(lender, loans);
		}
	}

	private static void ProcessLoansLendersFileGetHistLoansPerLender(
			String fname) throws IOException {
		// This function generates a hist of how many loans a lender gives
		BufferedReader reader;
		String line;
		int NumLenders=0;
		
		// find the number of lenders
		reader = new BufferedReader(new FileReader(fname));
		while ((line=reader.readLine())!=null) {
			String []S = line.split("\\s+");
			
			for (int i=1; i<S.length; i++){
				int id = Integer.parseInt(S[i]);
				if (id > NumLenders) {
					NumLenders = id;
				}
			}
		}
		reader.close();
		System.out.println("Num of lendes : "+NumLenders);
		
		int []numLoansArray = new int[NumLenders];
		reader = new BufferedReader(new FileReader(fname));
		while ((line=reader.readLine())!=null) {
			String []S = line.split("\\s+");
			
			for (int i=1; i<S.length; i++){
				int id = Integer.parseInt(S[i]);
				numLoansArray[id-1]++;
			}
		}
		
		//now we have how many loans given by each lender
		//lets now construc a hist
		int maxLoans=0, id=0;
		int maxLoans_2=0,id_2=0;
		
		for (int i=0; i<NumLenders; i++){
			if (numLoansArray[i] > maxLoans) {
				maxLoans_2 = maxLoans;
				maxLoans   = numLoansArray[i];
				
				id_2= id;
				id  = i+1;
			} else if (numLoansArray[i] > maxLoans_2) {
				maxLoans_2 = numLoansArray[i];
				id_2 = i+1;
			}
		}
		
		//System.out.println("Max num of loans : "+maxLoans+":"+id+"&"+maxLoans_2+":"+id_2);
		int []histNumLoans = new int[maxLoans];
		for (int i=0; i<NumLenders; i++){
			int numLoans = numLoansArray[i];
			
			//if (numLoans != maxLoans)
			histNumLoans[numLoans-1]++;			
		}
		
		for (int i=0; i<maxLoans; i++)
			System.out.println(histNumLoans[i]+",");
	}

	private static void ProcessLoansLendersFileGetHistLendersPerLoan(String fname) throws IOException {
		// This function generates a hist of how many lenders in a loan gives
		BufferedReader reader;
		String line;
		int maxNumLender = 0;
		
		reader = new BufferedReader(new FileReader(fname));
		while ((line=reader.readLine())!=null) {
			String []S = line.split("\\s+");
			
			int numLenders = S.length-1;
			
			if (maxNumLender < numLenders)
				maxNumLender = numLenders;
		}
		reader.close();
		
		//System.out.println("Max number of lenders in a loan : "+maxNumLender);
			
		int []numLendersArray = new int[maxNumLender];
		reader = new BufferedReader(new FileReader(fname));
		while ((line=reader.readLine())!=null) {
			String []S = line.split("\\s+");
			
			int numLenders = S.length-1;
			
			numLendersArray[numLenders-1]++;
		}
		reader.close();
		
		for (int i=0;i<maxNumLender;i++){
			System.out.println(numLendersArray[i]+",");
		}
	}
}
