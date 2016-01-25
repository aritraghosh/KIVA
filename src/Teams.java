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


public class Teams {
	private static JSONParser parser = new JSONParser();
	private static JSONObject jsonObject;
	private static HashMap<Integer,ArrayList<Integer>> lenderTeamMap = new HashMap<Integer,ArrayList<Integer>>();
	private static HashMap<String, Integer> LenderIndexMap;
	
	public static void createLenderTeamsFile(String dname) throws FileNotFoundException, IOException, ParseException {
		System.out.println("creatin lender-teams.txt");
		String Tdir  = dname+"team_members/";
		String fname   = dname+"work_file/lender-teams.txt";
		String fnamelm = dname+"work_file/lenders_map.txt";

		File folder = new File(Tdir);
		File[] listOfFiles = folder.listFiles();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			ShowProgress.Show();
			//System.err.println(i+":"+listOfFiles[i]);
			FileReader fr = new FileReader(listOfFiles[i]);
			Object obj = parser.parse(fr);
			jsonObject = (JSONObject) obj;

			String name = listOfFiles[i].getName();
			name = name.substring(7, name.length()-5);

			int teamNumber = Integer.parseInt(name);
			ProcessTeams(false, teamNumber, fnamelm);
			parser.reset(); //fixme doing this instead of file close is it OK?
			fr.close();
		}
		
		PrintWriter writer = new PrintWriter(fname, "UTF-8");
		for (int key : lenderTeamMap.keySet()){
			ArrayList<Integer> teams = lenderTeamMap.get(key);
			
			writer.print(key+" ");
			for (int team:teams){
				writer.print(team+" ");
			}
			writer.println();
		}
		writer.close();
	}
	
	static void ProcessTeams(Boolean print,int teamNumber, String fname) throws NumberFormatException, IOException {
		JSONArray members = (JSONArray) jsonObject.get("lenders");
		
		if (print)
			System.out.println("The number of lenders is : "+ members.size());
				
		Iterator itr = members.iterator();
		while (itr.hasNext()) {
			JSONObject lender = (JSONObject) itr.next();
			String lenderID = (String) lender.get("lender_id");
			Integer lenderIndex = GetLenderIndex (lenderID, fname);
			
			if (lenderIndex != null){
				if(!lenderTeamMap.containsKey(lenderIndex)){
					ArrayList<Integer> teams = new ArrayList<Integer>();
					teams.add(teamNumber);
					lenderTeamMap.put(lenderIndex, teams);
				} else{
					ArrayList<Integer> teams = lenderTeamMap.get(lenderIndex);
					teams.add(teamNumber);
				}
			}
		}
	}

	private static Integer GetLenderIndex(String lenderID, String fname) throws NumberFormatException, IOException {		
		if (LenderIndexMap == null){
			LenderIndexMap = new HashMap<>();
			BufferedReader reader;
			String line;
			
			reader = new BufferedReader(new FileReader(fname));
			while ((line=reader.readLine())!=null) {
				String []S = line.split("\\s+");
				
				int lenderIndex   = Integer.parseInt(S[0]);
				String lenderName = S[1];
				
				LenderIndexMap.put(lenderName, lenderIndex);
			}
			reader.close();
		}
		
		return (LenderIndexMap.get(lenderID));
	}
}
