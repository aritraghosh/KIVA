import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Lenders {
	private static JSONParser parser = new JSONParser();
	private static JSONObject jsonObject;
	private static HashMap<String,Integer> lenderMap = new HashMap<>();
	private static PrintWriter writer;

	public static void ProcessLendersDir(String dname) throws FileNotFoundException, IOException, ParseException {
		initLenderMap(dname);
		File folder = new File(dname+"/lenders/");
		File[] listOfFiles = folder.listFiles();
		System.out.print("Processing  ");
		
		writer = new PrintWriter(dname+"work_file/lender_Country.txt", "UTF-8");
		for (int i = 0; i < listOfFiles.length; i++) {
			ShowProgress.Show();

			Object obj = parser.parse(new FileReader(listOfFiles[i]));
			jsonObject = (JSONObject) obj;

			//				PrintHeader();
			ProcessLenders(dname);
			parser.reset(); //doing this instead of file close is it OK?
		}
		writer.close();
	}
	
	
	static void ProcessLenders(String dname) throws FileNotFoundException, UnsupportedEncodingException {
		JSONArray lenders = (JSONArray) jsonObject.get("lenders");
		Iterator itr = lenders.iterator();
		while (itr.hasNext()) {
			JSONObject lender = (JSONObject) itr.next();
			String lenderID =(String) lender.get("lender_id");
			String countrycode = (String) lender.get("country_code");
		    
			Integer le = lenderMap.get(lenderID);
			if (le !=null) {
				writer.println(le + " "+ countrycode);
			}
		}
	}

	
    private static void initLenderMap(String dname) throws IOException{
    	
       String fname = dname+"/work_file/lenders_map.txt";
		
		BufferedReader read = new BufferedReader(new FileReader(fname));
		String line = read.readLine();
		
		while(line!=null){
			String s[] = line.split("\\s+");
			int lenderID =Integer.parseInt( s[0]);
			String lenderName = s[1];
			lenderMap.put(lenderName, lenderID);
			line = read.readLine();
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
	
	

}
