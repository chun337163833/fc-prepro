package hu.durfi.freecell.solver;

import java.util.ArrayList;
import java.util.Date;

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
		String jsonStr = "[[\"D8\",\"H2\",\"CQ\",\"C2\",\"D4\",\"CK\",\"H5\"]," +
				"[\"C5\",\"C4\",\"D9\",\"D6\",\"C3\",\"HA\",\"DK\"]," +
				"[\"C9\",\"D2\",\"S4\",\"H7\",\"S8\",\"HJ\",\"DA\"]," +
				"[\"D7\",\"H10\",\"SA\",\"SK\",\"C10\",\"D5\",\"C8\"]," +
				"[\"S3\",\"C6\",\"CJ\",\"S7\",\"S6\",\"S10\"]," +
				"[\"CA\",\"S5\",\"HQ\",\"C7\",\"D3\",\"H6\"]," +
				"[\"DJ\",\"SQ\",\"H8\",\"S9\",\"S2\",\"H4\"]," +
				"[\"D10\",\"DQ\",\"SJ\",\"H3\",\"HK\",\"H9\"]]";
		JSONParser parser = new JSONParser();
		JSONArray board = (JSONArray)parser.parse(jsonStr);
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
				System.out.println("Game won!");
				break;
			}
		}
		long endTime = (new Date()).getTime();
		System.out.println("Time spent: " + (endTime - startTime)/1000 + "s");
		
		// System.out.println(initialState.boardToEqString());
		// initialState.getNextStates();
	}

}
