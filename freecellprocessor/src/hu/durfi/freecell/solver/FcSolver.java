package hu.durfi.freecell.solver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import hu.durfi.freecell.FreeCellModel;

public class FcSolver {

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		String jsonStr = 
				"[[\"D8\",\"H2\",\"CQ\",\"C2\",\"D4\",\"CK\",\"H5\"]," +
				"[\"C5\",\"C4\",\"D9\",\"D6\",\"C3\",\"HA\",\"DK\"]," +
				"[\"C9\",\"D2\",\"S4\",\"H7\",\"S8\",\"HJ\",\"DA\"]," +
				"[\"D7\",\"H10\",\"SA\",\"SK\",\"C10\",\"D5\",\"C8\"]," +
				"[\"S3\",\"C6\",\"CJ\",\"S7\",\"S6\",\"S10\"]," +
				"[\"CA\",\"S5\",\"HQ\",\"C7\",\"D3\",\"H6\"]," +
				"[\"DJ\",\"SQ\",\"H8\",\"S9\",\"S2\",\"H4\"]," +
				"[\"D10\",\"DQ\",\"SJ\",\"H3\",\"HK\",\"H9\"]]";
		String jsonStr7921427 =
				"[[\"C10\",\"CJ\",\"HK\",\"C2\",\"H8\",\"D3\",\"S8\"]," +
				"[\"SK\",\"CQ\",\"C7\",\"H10\",\"S7\",\"DQ\",\"SQ\"]," +
				"[\"SJ\",\"S10\",\"S9\",\"D5\",\"D2\",\"C9\",\"CA\"]," +
				"[\"C5\",\"HJ\",\"H3\",\"C4\",\"C8\",\"S4\",\"DA\"]," +
				"[\"HQ\",\"CK\",\"C3\",\"DK\",\"S2\",\"DJ\"]," +
				"[\"S5\",\"D9\",\"D7\",\"H4\",\"H9\",\"SA\"]," +
				"[\"H5\",\"C6\",\"D8\",\"S6\",\"HA\",\"D4\"]," +
				"[\"D10\",\"H6\",\"S3\",\"H7\",\"D6\",\"H2\"]]";
		String jsonStr9104713 = "[[\"C9\",\"S10\",\"H4\",\"S5\",\"D3\",\"SA\",\"DJ\"],[\"HJ\",\"C4\",\"CQ\",\"HK\",\"DK\",\"D10\",\"D8\"],[\"C2\",\"D5\",\"S4\",\"CK\",\"S8\",\"SQ\",\"S2\"],[\"H7\",\"S6\",\"CA\",\"C5\",\"C6\",\"H5\",\"S3\"],[\"C10\",\"H3\",\"HQ\",\"S9\",\"DA\",\"S7\"],[\"D2\",\"H2\",\"D4\",\"DQ\",\"D7\",\"C3\"],[\"HA\",\"SK\",\"D9\",\"SJ\",\"C7\",\"H6\"],[\"H10\",\"D6\",\"C8\",\"H8\",\"H9\",\"CJ\"]]";
		JSONParser parser = new JSONParser();
		JSONArray board = (JSONArray)parser.parse(jsonStr7921427);
		FcState initialState = new FcState();
		initialState.createFromJSON(board);

		FcSearchSpace sp = new FcSearchSpace(new FcBreadthFirstStrategy());
		/*
		 * Start looking for a solution
		 */
		FcState solution = (FcState)sp.search(initialState, 1000);
		
		/*
		 * If solution was found, print the transitions
		 */
		if (solution != null) {
			List<String> transitions = solution.getTransitions();
			// Create JSON from the solution, with reversed transitions
			StringBuffer str = new StringBuffer();
			str.append("{\"init\":"+jsonStr9104713+", \"moves\": [\n");
			ListIterator<String> li = transitions.listIterator();
			while (li.hasNext()) {
				str.append(li.next()+",\n");
			}
			str.setLength(str.length()-2);
			str.append("]}");
	
			System.out.println(str);
		}
	}

}
