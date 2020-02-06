package com.guna.ocrsample;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Api {
	static boolean DEBUG = false;
	static int _THRESHOLD_ = 5;

	private String data;
	private String jsonString = null;
	private boolean loaded = false;
	private ArrayList<String> nutrient_header = new ArrayList<String>();
	private HashSet<String> filtered_foods = new HashSet<String>();
	private HashMap<String, ArrayList<Double>> nutrient_info = new HashMap<String, ArrayList<Double>>();
	private HashMap<String, ArrayList<String>> food_by_type = new HashMap<String, ArrayList<String>>();
	private Random r = new Random();
	
	Api(String data, String jsonString){
	    this.data = data;
		this.jsonString = jsonString;
		load_api(jsonString);
		if (data != null) {
            String lines[] = data.split("\\r?\\n");
            for (String s : lines) {
                Pair<String, ArrayList<Double>> cur_result = getFood(s, "");
                if (cur_result != null) {
                    filtered_foods.add(cur_result.first);
                }
            }
        }
	}
	
	String getJson() {
		return readFile("api.json");
	}
	
	private String readFile(String filename) {
	    String result = "";
	    try {
	        BufferedReader br = new BufferedReader(new FileReader(filename));
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        while (line != null) {
	            sb.append(line);
	            line = br.readLine();
	        }
	        result = sb.toString();
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	    return result;
	}
	
	private void load_api(String jsonString){
	    if (loaded) return;
	    else loaded = true;
		//Where our filtering goes (data -> HashMap)
		JSONObject json = null;
		try {
			json = new JSONObject(jsonString);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONArray jarr;
		JSONObject jdic;
		try {
			if (DEBUG) System.out.println(json.get("data"));
			if (DEBUG) System.out.println(json.get("nutrients"));
			jarr = new JSONArray(json.get("nutrients").toString());
			for (int i=0;i<jarr.length();++i) {
                nutrient_header.add(jarr.getString(i));
			}
			if (DEBUG) System.out.println(nutrient_header);
			jdic = new JSONObject(json.get("data").toString());
			Iterator<?> keys = jdic.keys();
			while(keys.hasNext()) {	//Food classifications
				String key = (String)keys.next();
				food_by_type.put(key.toLowerCase(), new ArrayList<String>());
				JSONObject tmpObj = jdic.getJSONObject(key);
				Iterator<?> foodNames = tmpObj.keys();
				while(foodNames.hasNext()) {	//Individual foods
					String foodName = (String)foodNames.next();
					ArrayList<Double> dbl_li = new ArrayList<Double>();
					JSONArray tmpArr = new JSONArray(tmpObj.get(foodName).toString());
					for (int i=0;i<tmpArr.length();++i) {
						dbl_li.add(tmpArr.getDouble(i));
					}
					nutrient_info.put(foodName.toLowerCase(), dbl_li);
					food_by_type.get(key.toLowerCase()).add(foodName.toLowerCase());
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getFoodNames(){
		ArrayList<String> foodNames = new ArrayList<String>();
		for (String s: food_by_type.keySet()) {
			foodNames.addAll(food_by_type.get(s));
		}
		return foodNames;
	}
	
	static int editDistance(String x, String y) {
	    int[][] dp = new int[x.length() + 1][y.length() + 1];
	    
	    for (int i = 0; i <= x.length(); i++) {
	        for (int j = 0; j <= y.length(); j++) {
	            if (i == 0) {
	                dp[i][j] = j;
	            }
	            else if (j == 0) {
	                dp[i][j] = i;
	            }
	            else {
	                dp[i][j] = Math.min(Math.min(dp[i - 1][j - 1] + (x.charAt(i-1) == y.charAt(j-1)?0:1), 
	                  dp[i - 1][j] + 1), 
	                  dp[i][j - 1] + 1);
	            }
	        }
	    }
	 
	    if (DEBUG) System.out.println(String.format("Strings %s & %s have distance: %d", x, y, dp[x.length()][y.length()]));
	    
	    return dp[x.length()][y.length()];
	}
	
	public Pair<String, ArrayList<Double>> getFood(String query, String type) {
		ArrayList<String> searchSpace = new ArrayList<String>();
		//Restrict by available search space
		if (type != null && food_by_type.keySet().contains(type)) {
			searchSpace = food_by_type.get(type);
		}
		else {
			for (String s: food_by_type.keySet()) {
				searchSpace.addAll(food_by_type.get(s));
			}
		}
		//Get Closest Matching name
		if (searchSpace.contains(query)) return new Pair<>(query, nutrient_info.get(query));	//Augment with Levenshtein Distance
		
		ArrayList<Pair<Integer, Integer>> sortByEditDistance = new ArrayList<Pair<Integer, Integer>>();
		
		for (int i=0;i<searchSpace.size();++i) {
			int cur_distance = editDistance(searchSpace.get(i), query);
			if (cur_distance > Math.min(query.length(), _THRESHOLD_)) continue;
			sortByEditDistance.add(new Pair<Integer, Integer>(cur_distance, i));
		}
		
		Collections.sort(sortByEditDistance, new Comparator<Pair<Integer, Integer>>() {
		   @Override
		   public int compare(Pair<Integer, Integer> p1, Pair<Integer, Integer> p2) {
		       return p1.first.compareTo(p2.first);
//		       return p2.first.compareTo(p1.first);
		   }
		});
		
		if (sortByEditDistance.size() < 1) return null;
		if (DEBUG) Log.e("pickhacks", "Found most similar to: " + query + "| " + searchSpace.get(sortByEditDistance.get(0).second) + " = " + sortByEditDistance.get(0).first);
		return new Pair<>(searchSpace.get(sortByEditDistance.get(0).second), nutrient_info.get(searchSpace.get(sortByEditDistance.get(0).second)));
	}
	
	public void debug() {
		if (!DEBUG) return;
		System.out.println(nutrient_info);
		System.out.println(food_by_type);
	}
	
	public ArrayList<Double> genRandGoal(){
		ArrayList<Double> randGoal = new ArrayList<Double>();
		for (int i=0;i<food_by_type.keySet().size();++i){
			randGoal.add(r.nextDouble()*300);
		}
		return randGoal;
	}
	
	public ArrayList<ArrayList<Double>> genRandHistory(){
		ArrayList<ArrayList<Double>> randHistory = new ArrayList<ArrayList<Double>>();
		int randTot = r.nextInt(3);
		for (int i=0;i<randTot;++i) {
			ArrayList<Double> tmpArr = new ArrayList<Double>();
			for (int j=0;j<food_by_type.keySet().size();++j){
				tmpArr.add(r.nextDouble()*100);
			}
			randHistory.add(tmpArr);
		}
		return randHistory;
	}
	
	public HashMap<String, ArrayList<Double>> getHashMap(){
		return nutrient_info;
	}

	public HashSet<String> getValidFoods(){
        return filtered_foods;
    }

    public ArrayList<String> getHeaders(){
	    return nutrient_header;
    }

	/*Test Code*/
//	public static void main(String args[]) {
//		String sample_data = "";
//		Api api = new Api(sample_data);
//		api.debug();
//		System.out.println(api.getFoodNames());
//		System.out.println(api.getFood("grildfj echildk sandwlkdj with a fjsui and yaogur tsoothi", null));
//	}
	
}
