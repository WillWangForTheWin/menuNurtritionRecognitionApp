package com.guna.ocrsample;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class NutritionGoal {
	
	public static void main(String []args)
	{
		Api api = new Api("", "");
		ArrayList<Double> goal = api.genRandGoal();
		ArrayList<ArrayList<Double>> history = api.genRandHistory();
		HashMap<String, ArrayList<Double>> available_foods = api.getHashMap();
		
		algo(goal, history, available_foods);
	}
	
	static double score(ArrayList<Double> sum, ArrayList<Double> goal)
	{
		double sumTotal = 0.0;
		double goalTotal = 0.0;
		double 	dotProduct = 0.0;
		for(int x = 0; x < sum.size(); x++)
		{
			sumTotal += sum.get(x);
			goalTotal += goal.get(x);
		}
		for(int x = 0; x < sum.size(); x++)
		{
			dotProduct += (sum.get(x)/sumTotal) * (goal.get(x)/goalTotal);
		}
		return dotProduct;
	}

	static ArrayList<String> algo(ArrayList<Double> goal, ArrayList<ArrayList<Double>> history, HashMap<String, ArrayList<Double>> available_foods)
	{
		//Initializes potentialGoals full of 0.0's based on the size of the HashMap
		Double[]arr = new Double[goal.size()];
		ArrayList<Double> potentialGoals = new ArrayList<Double>(Arrays.asList(arr));
		Collections.fill(potentialGoals, 0.0);
		ArrayList<Double> sum = new ArrayList<Double>();
		
		
		//Store Top 3
		ArrayList<String> retArr = new ArrayList<String>();
		ArrayList<Pair<Double,Integer>> scores = new ArrayList<Pair<Double,Integer>>();
		
		//Increment
		//Iterates through the parameter HashMap and adds each ArrayList to intakeHistory
		for(String key : available_foods.keySet())
		{
			ArrayList<Double> dataCopy = available_foods.get(key);
			for(int x = 0; x < dataCopy.size(); x++)
				sum.add(potentialGoals.get(x) + dataCopy.get(x));
			double tempScore = score(sum,goal);
			Pair dq = new Pair(tempScore,key);
			scores.add(dq);
			sum.clear();
		}

		Collections.sort(scores, new Comparator<Pair<Double,Integer>>()
		{
			public int compare(final Pair<Double, Integer> o1, final Pair<Double, Integer> o2)
			{
				return o1.first.compareTo(o2.first);
			}
		});
		
		for(Pair p: scores) {
			System.out.println(p.second);
		}
		
		//return list with values most close to goal
		return retArr;
	}
}