import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class SimRankEntry {
	int i, j;
	double similarity;
	SimRankEntry(int i,int j, Double sim){
		this.i  = i;
		this.j = j;
		this.similarity = sim;
	}
	 void print(){
		 System.out.println(i + ","+ j + " : "+ similarity);
	 }
}

class LoanEntry{
	String sector;
	String country;
	long   amount;
	int    group; //will be set to one if a group loan
	
	void println() {
		System.out.print("sector  : "+ sector);
		System.out.print(" country : "+ country);
		System.out.print(" amount  : "+ amount);
		System.out.print(" group   : "+ group);
		System.out.println();
	}
	public LoanEntry(String s, String c, long a, int g) {
		sector  = s;
		country = c;
		amount  = a;
		
		if (g>1){
			group = 1;
		} else {
			group = 0;
		}
	}
}


public class TopK {
	private static ArrayList<Integer> LoanIDArray  = new ArrayList<>();
	private static ArrayList<Integer> LendersArray = new ArrayList<>();
	
	private static HashMap<Integer, HashSet<Integer>> LenderTeamsMap   = new HashMap<>();
	private static HashMap<Integer, String>           LenderCountryMap = new HashMap<>();
	
	private static HashMap<Integer, LoanEntry> LoanIDDetailsMap = new HashMap<>();
	
	private static int PairWithNoTeam_le    = 0;
	private static int PairWithNoCountry_le = 0;
	
	public static void main(String[] args) throws IOException, ParseException {
		if (args.length < 1) {
			System.out.println("Provide the Following arguments to the program");
			System.out.println("1) Location of directory conataining \"Loan\" json directory");
			System.out.println("2) K");
			return;
		}
	
		int k = Integer.parseInt(args[1]);

		AnalyseSimLe(args[0], k);
		//AnalyseSimLo(args[0], k);
		
	}
	
	private static void AnalyseSimLo(String dname, int k) throws IOException, ParseException {
		String file = dname+"/Matrix/Sim_Lo.txt";
		
		PriorityQueue<SimRankEntry> topK  = GetTopKFromSimFile(file,k);
		ArrayList<SimRankEntry> topKArray = GetTopKArrayFormPriorityQueu (topK);
				
		InitializeLoanMap(dname, topKArray);
		
		int numInSameSector      = 0;
		int numInSameCountry     = 0;
		int numInSameGroupStatus = 0;
		int numInSameAmountRange = 0;
		
		for (SimRankEntry s : topKArray) {
			if (InSameSector_lo (s.i,s.j))
				numInSameSector++;
			if (InSameCountry_lo(s.i, s.j))
				numInSameCountry++;
			if (InSameAmountRange_lo(s.i, s.j))
				numInSameAmountRange++;
			if (InSameGroupStatus_lo(s.i, s.j))
				numInSameGroupStatus++;
		}			
		
		System.out.println("%%%%%%%%%%% K = "+k+" %%%%%%%%%%");
		
		System.out.println("Accuracy                 : "+((float)numInSameSector/k));
		//System.out.println("AccuracyWithoutNOTeam    : "+((float)numInSameSector/(k-PairWithNoCountry_le)));
		//System.out.println("PairWithNoTeam           : "+PairWithNoTeam_le);
		System.out.println("numInSameSector          : "+numInSameSector);
		
		System.out.println("Accuracy                 : "+((float)numInSameCountry /k));
		System.out.println("numInSameCountry         : "+ numInSameCountry);
		
		System.out.println("Accuracy                 : "+((float)numInSameAmountRange /k));
		System.out.println("numInSameAmountRange     : "+ numInSameAmountRange);
		
		System.out.println("Accuracy                 : "+((float)numInSameGroupStatus /k));
		System.out.println("numInSameGroupStatus     : "+ numInSameGroupStatus);
	}
	
	
	private static ArrayList<SimRankEntry> GetTopKArrayFormPriorityQueu(
			PriorityQueue<SimRankEntry> topK) {
		ArrayList<SimRankEntry> topKArray = new ArrayList<>();
		
		while(!topK.isEmpty()){
			SimRankEntry s = topK.poll();
			topKArray.add(s);
		}
		return topKArray;
	}
	
	private static void InitializeLoanMap(String dname,
			ArrayList<SimRankEntry> topKArray) throws IOException, ParseException {
		//we need the details of the loans of the top K pairs of loans
		
		//initializing the LoanIDArray array
		{
			String fname = dname+"/Matrix/loans.txt";
			BufferedReader read = new BufferedReader(new FileReader(fname));
			String line = read.readLine();

			while(line!=null){
				LoanIDArray.add(Integer.parseInt(line));
				line = read.readLine();
			}
			read.close();
		}
		
		//creating a set of loanids whos information to be fetched
		HashSet<Integer> LoanIDsSet = new HashSet<>();
		for (SimRankEntry S : topKArray) {
			LoanIDsSet.add(LoanIDArray.get(S.i));
			LoanIDsSet.add(LoanIDArray.get(S.j));
		}
		//fetch loan details only of these loans
		InitializeLoanMap(dname, LoanIDsSet);
	}
	
	private static void InitializeLoanMap(String dname,
			HashSet<Integer> loanIDsSet) throws FileNotFoundException, IOException, ParseException {
		String loanDname = dname+"loans/";
		
		File folder        = new File(loanDname);
		File[] listOfFiles = folder.listFiles();
		JSONParser parser  = new JSONParser();
		JSONObject jsonObject;
		
		for (int i = 0; i < listOfFiles.length; i++) {
			ShowProgress.Show();

			Object obj = parser.parse(new FileReader(listOfFiles[i]));
			jsonObject = (JSONObject) obj;

			ProcessLoans(jsonObject, loanIDsSet);
			parser.reset(); //doing this instead of file close is it OK?
		}
		
	}

	private static void ProcessLoans(JSONObject jsonObject,
			HashSet<Integer> loanIDsSet) {
		JSONArray loans = (JSONArray) jsonObject.get("loans");
		
		Iterator itr = loans.iterator();
		while ((itr.hasNext()) &&
				(loanIDsSet.size() > 0)) {
			JSONObject loan = (JSONObject) itr.next();
			
			Integer loanID = (int)(long)(loan.get("id"));
			if (loanIDsSet.contains(loanID)){
				loanIDsSet.remove(loanID);
				String sector = (String) loan.get("sector");
				long amount   = (long) loan.get("loan_amount");
				
				JSONObject location = (JSONObject) loan.get    ("location");
				String country      = (String)     location.get("country_code");

				JSONArray borrowers = (JSONArray) loan.get("borrowers");
				
				LoanEntry LE = new LoanEntry(sector, country, amount, borrowers.size());
				
				LoanIDDetailsMap.put(loanID, LE);
			}
		}
	}

	private static boolean InSameSector_lo(int i, int j) {
		boolean ret = false;

		int loadID_i = LoanIDArray.get(i);
		int loadID_j = LoanIDArray.get(j);

		LoanEntry Li = LoanIDDetailsMap.get(loadID_i);
		LoanEntry Lj = LoanIDDetailsMap.get(loadID_j);
		
		if (Li != null && Lj != null)
			if ((Li.sector != null) && (Lj.sector != null)){
				ret = Li.sector.equals(Lj.sector);
				//System.out.println(Li.sector+":"+Lj.sector);
			}
		
		return ret;
	}
	private static boolean InSameCountry_lo(int i, int j) {
		boolean ret = false;

		int loadID_i = LoanIDArray.get(i);
		int loadID_j = LoanIDArray.get(j);

		LoanEntry Li = LoanIDDetailsMap.get(loadID_i);
		LoanEntry Lj = LoanIDDetailsMap.get(loadID_j);
		
		
		if (Li != null && Lj != null)
			if ((Li.country != null) && (Lj.country != null))
				ret = Li.country.equals(Lj.country);
		
		return ret;
	}
	private static boolean InSameGroupStatus_lo(int i, int j) {
		boolean ret = false;

		int loadID_i = LoanIDArray.get(i);
		int loadID_j = LoanIDArray.get(j);

		LoanEntry Li = LoanIDDetailsMap.get(loadID_i);
		LoanEntry Lj = LoanIDDetailsMap.get(loadID_j);
		
		if (Li != null && Lj != null)
			if(Li.group == Lj.group)
				ret = true;
		return ret;
	}
	private static boolean InSameAmountRange_lo(int i, int j) {
		boolean ret = false;

		int loadID_i = LoanIDArray.get(i);
		int loadID_j = LoanIDArray.get(j);

		LoanEntry Li = LoanIDDetailsMap.get(loadID_i);
		LoanEntry Lj = LoanIDDetailsMap.get(loadID_j);
		
		if (Li != null && Lj != null) {
			long a1 = Li.amount;
			long a2 = Lj.amount;
	
			//making a1 the lesser of the two
			if (a1 > a2){
				long t = a1;
				a2 = a1;
				a1 = t;
			}
			
			long diff = a2-a1;
			
			if (a2>0) {
				if (((float)diff/a2) < 0.25)
					ret = true;
			}
		}
		return ret;
	}
	private static void AnalyseSimLe(String dname, int k) throws IOException {
		String file = dname+"/Matrix/Sim_Le.txt";
		
		PriorityQueue<SimRankEntry> topK = GetTopKFromSimFile(file,k);
		
		InitializeLenderTeamMap   (dname);
		InitializeLenderCountryMap(dname);

		boolean flag = true;
		int numInSameTeam    = 0;
		int numInSameCountry = 0;
		while(!topK.isEmpty()){
			SimRankEntry s = topK.poll();
			
			if (flag) {
				if (InSameTeam_le (s.i,s.j))
					numInSameTeam++;

				if(InSameCountry_le (s.i,s.j))
					numInSameCountry++;
			}
		}
		
		if (flag) {
			System.out.println("Accuracy               : "+((float)numInSameTeam/k));
			System.out.println("AccuracyWithoutNOTeam  : "+((float)numInSameTeam/(k-PairWithNoTeam_le)));
			System.out.println("PairWithNoTeam         : "+PairWithNoTeam_le);
			System.out.println("numInSameTeam          : "+numInSameTeam);

			System.out.println("Accuracy                    : "+((float)numInSameCountry/k));
			System.out.println("AccuracyWithout No country  : "+((float)numInSameCountry/(k-PairWithNoCountry_le)));
			System.out.println("PairWithNoCountry           : "+PairWithNoCountry_le);
			System.out.println("numInSameCountry            : "+numInSameCountry);
		}
		
		
	}
	
	
	
	private static PriorityQueue<SimRankEntry> GetTopKFromSimFile(String file,
			int k) throws IOException {
		Comparator   <SimRankEntry> comparator = new SimilarityComparator();
		PriorityQueue<SimRankEntry> topK       = new PriorityQueue<>(k, comparator);
		
		BufferedReader read = new BufferedReader(new FileReader(file));
		String line = read.readLine();
		
		String sim[] = line.split(",");

		int i=0;
		while(line!=null){	
			sim = line.split(",");
			for(int j=0;j<sim.length;j++) {
				if (i>j){
					Double similarity = Double.parseDouble(sim[j]);
					if(topK.size()<k)
						topK.add(new SimRankEntry(i,j,similarity));
					else{
						// We use a min heap of k elements
						if(topK.peek().similarity< similarity){
							topK.add(new SimRankEntry(i, j, similarity));
							topK.poll();
						}
					}
				}
				else
					break;
			}
			    
			i++;
			line= read.readLine();
		}
		read.close();
		
		return topK;
	}
	
	private static boolean InSameCountry_le(int i, int j) {
		boolean ret = false;
		
		int lenderID_i = LendersArray.get(i);
		int lenderID_j = LendersArray.get(j);

		String country_i = LenderCountryMap.get(lenderID_i);
		String country_j = LenderCountryMap.get(lenderID_j);
		
		if ((country_i == null) || (country_j == null)) {
			PairWithNoCountry_le++;
		} else {
			if(!country_i.equals("null")&& !country_j.equals("null")) {
				if(country_i.equals(country_j))
					ret = true;
			}
			else {
				PairWithNoCountry_le++;
			}
		}
		
		return ret;
	}
	
	
	private static void InitializeLenderCountryMap(String dname) throws IOException {
		String fname = dname+"/work_file/lender_Country.txt";
		BufferedReader  read = new BufferedReader(new FileReader(fname));
		String line = read.readLine();
		
		while(line!=null){	
			String s[] = line.split("\\s+");
			
			int lenderID =  Integer.parseInt(s[0]);
			LenderCountryMap.put(lenderID,s[1]);
			line = read.readLine();
		}
		read.close();
	}	
	private static boolean InSameTeam_le(int i, int j) {
		boolean ret = false;
		
		int lenderID_i = LendersArray.get(i);
		int lenderID_j = LendersArray.get(j);

		HashSet<Integer> teams_i = LenderTeamsMap.get(lenderID_i);
		HashSet<Integer> teams_j = LenderTeamsMap.get(lenderID_j);

		if ((teams_i != null) && (teams_j != null)) {
			HashSet<Integer> teams = new HashSet<>(teams_i);
			teams.retainAll(teams_j);

			if (teams.size() > 0)
				ret = true;
		} else {
			PairWithNoTeam_le++;
		}
		return ret;
	}

	private static void InitializeLenderTeamMap(String dname) throws IOException {
		//creating a arraylist of lenders in the matrix order
		String fname = dname+"/Matrix/lenders.txt";
		
		BufferedReader read = new BufferedReader(new FileReader(fname));
		String line = read.readLine();
		
		while(line!=null){
			LendersArray.add(Integer.parseInt(line));
			line = read.readLine();
		}
		read.close();
		
		//create a lender teams hash map
		
		fname = dname+"/work_file/lender-teams.txt";
		read = new BufferedReader(new FileReader(fname));
		line = read.readLine();
		
		while(line!=null){	
			String s[] = line.split("\\s+");
			
			int lenderID =  Integer.parseInt(s[0]);
			
			HashSet<Integer> teams = new HashSet<>();
			
			for (int i=1; i < s.length; i++) {
				int team = Integer.parseInt(s[i]);
				teams.add(team);
			}
			
			LenderTeamsMap.put(lenderID, teams);
			line = read.readLine();
		}
	}	
}

class SimilarityComparator  implements Comparator<SimRankEntry>{	
	@Override
	public int compare(SimRankEntry s1,SimRankEntry s2){
		int ret=0;
		if(s1.similarity> s2.similarity)
			ret = 1;
		else if (s1.similarity < s2.similarity)
			ret = -1;
		return ret;
		
	}
}
