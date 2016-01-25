import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Loans {
	private static final int Step=2;
	private static JSONParser parser = new JSONParser();
	private static JSONObject jsonObject;

	private static int totalLoans;
	private static int totalFunded;
	private static int totalInRepayment;
	private static int totalPaid;
	private static int totalFundraising;
	private static int totalRefunded;
	private static int totalDefaults;
	private static int totalDefaultsWithPayment;
	private static int totalDefaultsWithPartialPayment;
	private static int percentRepayed[] = new int[100/Step];
	private static int numMen;
	private static int numWomen;
	private static int numMenDefaulted;
	private static int numWomenDefaulted;
	private static int numGroup;
	private static int numGroupDefaulted;
	private static double totalAmountFunded;
	private static double totalAmountDefaulted;
	private static HashMap<String, Sector> sectorMap =  new HashMap<String, Sector>();
	private static TreeMap<Integer , Integer> loanAmountMap = new TreeMap<Integer, Integer>();
	private static TreeMap<Integer , Integer> loanAmountDefaultedMap = new TreeMap<Integer, Integer>();

	
	static void GeneratePlotInput(String path) throws FileNotFoundException, UnsupportedEncodingException{
		PrintWriter writer = new PrintWriter(path+"/../plots/RepaymentPercent.dat", "UTF-8");
		
		writer.println("#The is percentage of loans repayed by howmany percent of defaulters");
		for (int i=0; i<percentRepayed.length; i++) {
			writer.println((i*Step)+" "
					+((float) percentRepayed[i]/(totalDefaults-totalDefaultsWithPayment))*100);
		}		
		writer.close();
		
		//System.out.println ("Loan amount and frequency is : ");
		writer = new PrintWriter(path+"/../plots/LoanAmountFrequency.dat", "UTF-8");
		Iterator<Integer> itr1 = loanAmountMap.keySet().iterator();
		while (itr1.hasNext()){
			Integer key = (Integer) itr1.next();
			writer.println (key+" "+loanAmountMap.get(key));
		}
		writer.close();
		
		//System.out.println ("Loan amount Defaulted and frequency is : ");
		writer = new PrintWriter(path+"/../plots/LoanAmountOfDefaultedFrequency.dat", "UTF-8");
		itr1 = loanAmountDefaultedMap.keySet().iterator();
		while (itr1.hasNext()){
			Integer key = (Integer) itr1.next();
			writer.println (key+" "+loanAmountDefaultedMap.get(key));
		}
		writer.close();

	}
	
	static void PrintStat(){
		System.out.println("Total loans                                 : "+totalLoans);
		System.out.println("Total Fundraising                           : "+totalFundraising);
		System.out.println("Total Funded                                : "+totalFunded);
		System.out.println("Total In Repayment                          : "+totalInRepayment);
		System.out.println("Total Paid                                  : "+totalPaid);
		System.out.println("Total Refunded                              : "+totalRefunded);
		System.out.println("Total Defaults                              : "+totalDefaults);
		System.out.println("Total Defaults with full payment            : "+totalDefaultsWithPayment);
		System.out.println("Total Defaults with no payment              : "+(totalDefaults - totalDefaultsWithPartialPayment));
		System.out.println("Total Defaults with partial payment         : "+totalDefaultsWithPartialPayment);
		System.out.printf ("Total Amount Funded                         : %.2f$\n",totalAmountFunded);
		System.out.printf ("Total Amount Defaulted                      : %.2f$\n",totalAmountDefaulted);
		System.out.printf ("Percentage of Defaluted amount              : %.2f\n", (totalAmountDefaulted/totalAmountFunded * 100));

		System.out.println("Total groups                                : "+numGroup);
		System.out.println("Total Defaulted groups                      : "+numGroupDefaulted);

		System.out.println("Total men                                   : "+numMen);
		System.out.println("Total men defaulted                         : "+numMenDefaulted);
		System.out.println("Total women                                 : "+numWomen);
		System.out.println("Total women defaulted                       : "+numWomenDefaulted);
		
		System.out.println("The total number of sectors are : " + sectorMap.size());
		System.out.println("The sectors are : ");
		
		ArrayList<String> sectorList=new ArrayList<String> (sectorMap.keySet());
		Collections.sort(sectorList, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return (sectorMap.get(o1).compareTo(sectorMap.get(o2)));
			}
		});
		
		Iterator<String> itr = sectorList.iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			Sector sector = sectorMap.get(key);
			
			System.out.print  ("Sector : "    +key);
			System.out.print  (" Total : "    +sector.getNumLoans());
			System.out.println(" Percent : "  +(float)sector.getNumLoans()/totalLoans*100);
			System.out.print  (" Defaulted : "+sector.getNumLoansDefaulted());;
			System.out.println(" Percent : "  +(float)sector.getNumLoansDefaulted()/sector.getNumLoans()*100);
		}
	}
	
	private static void PrintHeader(){
		JSONObject header = (JSONObject) jsonObject.get("header");
		System.out.println("Number of Json Objects in header : "+header.size());
		System.out.println ("Total records = " + header.get("total"));
		System.out.println ("Page = " + header.get("page"));
		System.out.println ("date = " + header.get("date"));	
		System.out.println ("page_size = " + header.get("page_size"));
	}
	
	private static void UpdateTotalAmountFunded(JSONObject loan){
		String tmp = loan.get("funded_amount").toString();
		if (tmp != null)
			totalAmountFunded += Double.parseDouble(tmp);
		
		JSONArray borrowers = (JSONArray)loan.get("borrowers");			
		Iterator itr_g = borrowers.iterator();

		int M=0, F=0; //male and female borrowers
		while (itr_g.hasNext()) {
			JSONObject payment = (JSONObject) itr_g.next();
			if ("M".equals(payment.get("gender")))
				M++;
			else if ("F".equals(payment.get("gender")))
				F++;
		}
		if ((M+F)>1)
			numGroup++;
		
		numMen 		+= M;
		numWomen 	+= F;
		
		//update sector
		String sectorkey = (String) loan.get("sector");
		if (sectorMap.containsKey(sectorkey)) {
			Sector sector = sectorMap.get(sectorkey);
			sector.incrementLoans();
			sectorMap.put(sectorkey, sector);
		} else {
			Sector sector = new Sector();
			sector.incrementLoans();
			sectorMap.put(sectorkey, sector);
		}
		
		//update loan amount
		Integer loanAmount = Integer.parseInt((loan.get("funded_amount")).toString());
		if (loanAmountMap.containsKey(loanAmount)){
			loanAmountMap.put(loanAmount, loanAmountMap.get(loanAmount)+1);
		} else {
			loanAmountMap.put(loanAmount, 1);
		}
	}
	
	static void ProcessLoans(Boolean print) {
		JSONArray loans = (JSONArray) jsonObject.get("loans");
		
		if (print)
			System.out.println("The number of loans is : "+loans.size());
		totalLoans += loans.size();
		
		Iterator itr = loans.iterator();
		while (itr.hasNext()) {
			JSONObject loan = (JSONObject) itr.next();
			String status = (String) loan.get("status");
			
			switch (status) {
			case "fundraising"  : totalFundraising++;totalLoans--;break;
			case "refunded"     : totalRefunded++;totalLoans--;break;
			case "funded"       : totalFunded++; UpdateTotalAmountFunded(loan); break;
			case "in_repayment" : totalInRepayment++; UpdateTotalAmountFunded(loan); break;
			case "paid"         : totalPaid++; UpdateTotalAmountFunded(loan); break;
			case "defaulted"    : ProcessDefaultedLoans(loan, print);break;
			}
		}
	}
	
	private static void ProcessDefaultedLoans(JSONObject loan, Boolean print) {
		UpdateTotalAmountFunded(loan);
		 
		totalDefaults++;
		Double funded = Double.parseDouble(loan.get("funded_amount").toString());

		if (print) {
			System.out.print("partner_id:" + loan.get("partner_id")+" ");
			System.out.print("Id:" + loan.get("id")+" ");
			System.out.print("name:" + loan.get("name")+" ");
		}

		JSONArray borrowers = (JSONArray)loan.get("borrowers");
		if (print)
			System.out.print("#borrowers:"+borrowers.size()+" ");
			
		Iterator itr_g = borrowers.iterator();

		int M=0, F=0; //male and female borrowers
		while (itr_g.hasNext()) {
			JSONObject payment = (JSONObject) itr_g.next();
			if ("M".equals(payment.get("gender")))
				M++;
			else if ("F".equals(payment.get("gender")))
				F++;
		}
		if ((M+F)>1)
			numGroupDefaulted++;
		
		if (print){
			System.out.print("Gender:M:"+M+"F:"+F+" ");
			System.out.print("Paid:" + loan.get("paid_amount")+" ");
		}

		JSONArray payments = (JSONArray) loan.get("payments");
		Iterator itr_p = payments.iterator();			
		Double paymentMade = 0.0;
		while (itr_p.hasNext()) {
			JSONObject payment = (JSONObject) itr_p.next();
			paymentMade += Double.parseDouble(payment.get("amount").toString());
		}
		if (print) {
			System.out.print("Paid_calculated:" + paymentMade+" ");
			System.out.print("Funded:" + funded +" ");
			System.out.print("defaulted:"+(funded - paymentMade)+" ");
			System.out.println("Sector:" + loan.get("sector")+" ");
		}
		if ((funded - paymentMade) <= 0) {
			totalDefaultsWithPayment++;
		} else {
			numMenDefaulted 		+= M;
			numWomenDefaulted 		+= F;
			totalAmountDefaulted 	+= (funded - paymentMade);

			if (paymentMade > 0)
				totalDefaultsWithPartialPayment++;
			
			int percent = (int) Math.floor((paymentMade/funded)*100/Step);
			(percentRepayed[percent])++;
			
			//update sector
			String sectorkey = (String) loan.get("sector");
			if (sectorMap.containsKey(sectorkey)) {
				Sector sector = sectorMap.get(sectorkey);
				sector.incrementLoansDefaulted();
				sectorMap.put(sectorkey, sector);
			} else {
				Sector sector = new Sector();
				sector.incrementLoansDefaulted();
				sectorMap.put(sectorkey, sector);
			}
			
			//update loan amount
			Integer tmp = funded.intValue();
			if (loanAmountDefaultedMap.containsKey(tmp)){
				loanAmountDefaultedMap.put(tmp, loanAmountDefaultedMap.get(tmp)+1);
			} else {
				loanAmountDefaultedMap.put(tmp, 1);
			}

		}
	}

	public static void ProcessLoansDir(String dname) throws FileNotFoundException, IOException, ParseException {
		File folder = new File(dname);
		File[] listOfFiles = folder.listFiles();
		System.out.print("Processing  ");
		for (int i = 0; i < listOfFiles.length; i++) {
			ShowProgress.Show();

			Object obj = parser.parse(new FileReader(listOfFiles[i]));
			jsonObject = (JSONObject) obj;

			//				PrintHeader();
			Loans.ProcessLoans(false);
			parser.reset(); //doing this instead of file close is it OK?
		}			

	}
}

