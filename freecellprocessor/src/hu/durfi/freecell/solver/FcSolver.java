package hu.durfi.freecell.solver;

import java.util.ArrayList;
import java.util.Date;
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
		JSONParser parser = new JSONParser();
		JSONArray board = (JSONArray)parser.parse(jsonStr7921427);
		FcState initialState = new FcState();
		initialState.createFromJSON(board);
		
		FcState currentState = initialState;
		FcGreedySearchSpace sp = new FcGreedySearchSpace();
		long startTime = (new Date()).getTime();
		for (int i = 0; i < 10000; i ++) {
			ArrayList<FcState> nextStates = currentState.getNextStates();
			for (FcState state : nextStates) {
				sp.putState(state);
			}
			currentState = (FcState)sp.getNextState();
			System.out.println("Current("+i+") state is: ");
			System.out.println(currentState.boardToEqString());
			if (currentState.isWon()) {
				System.out.println("Game won!\n\n\n\n");
				
				// Put the transactions in a list
				ArrayList<String> transitions = new ArrayList<String>();
				FcState st = (FcState)currentState;
				while (st.getParent() != null) {
					transitions.add(st.getTransition());
					st = (FcState)st.getParent();
				}
				
				// Create JSON from the solution, with reversed transitions
				StringBuffer str = new StringBuffer();
				str.append("{\"init\":"+jsonStr7921427+", \"moves\": [");
				ListIterator<String> li = transitions.listIterator(transitions.size());
				while (li.hasPrevious()) {
					str.append(li.previous()+",\n");
				}
				str.setLength(str.length()-2);
				str.append("]}");
				
				System.out.println(str);
				break;
			}
		}
		long endTime = (new Date()).getTime();
		System.out.println("Time spent: " + (endTime - startTime)/1000 + "s");
		
		// System.out.println(initialState.boardToEqString());
		// initialState.getNextStates();
	}

}
