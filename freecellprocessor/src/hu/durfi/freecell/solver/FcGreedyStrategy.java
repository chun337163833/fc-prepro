package hu.durfi.freecell.solver;

import java.util.TreeMap;
import java.util.TreeSet;

public class FcGreedyStrategy implements SearchStrategy {
	/**
	 * This holds the visited states.
	 */
	protected TreeSet<String> visitedStates = new TreeSet<String>();
	/**
	 * Map of states. The key is the (equivalent) string representation
	 * of the board.
	 */
	protected TreeMap<String, FcState> states = new TreeMap<String, FcState>();
	/**
	 * Map of scores. This structure stores the boards with the score as
	 * key to make getting the board with the best score faster.
	 */
	protected TreeMap<Long, TreeSet<String>> scores = new TreeMap<Long, TreeSet<String>>();
	
	/**
	 * Returns and removes the state with the lowest score in the
	 * search space.
	 */
	@Override
	public State getNextState() {
		if (states.isEmpty()) {
			return null;
		}
		// Get entry state with highest score
		String board = scores.lastEntry().getValue().pollFirst();
		// If this was the last entry with this score, delete
		// this score.
		if (scores.lastEntry().getValue().isEmpty()) {
			scores.pollLastEntry();
		}
		// Mark this space as visited!
		if (visitedStates.contains(board)) {
			System.out.println("ERROR: KETSZER NEZZUK MEG AZ ALLAPOTOT!!!");
		}
		visitedStates.add(board);
		return states.get(board);
	}

	@Override
	public void putState(State state) {
		FcState fs = (FcState)state;
		Long score = fs.getScore();
		String board = fs.boardToEqString();
		// If state was already visited, don't add it again.
		//if (visitedStates.contains(board)) {
		if (states.containsKey(board)) {
			return;
		}
		// If this score hasn't occurred before, create list for the states
		// with this score.
		if (!scores.containsKey(score)) {
			scores.put(score, new TreeSet<String>());
		}
		scores.get(score).add(board);
		states.put(board, fs);
	}
}
