package hu.durfi.freecell.solver;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * <p>Basic FreeCell search space. This uses a greedy method to find the solution.
 * Always go the direction where there are the most cards in the foundations.
 * 
 * <p>Extend this to create a better search strategy!
 * @author durfi
 *
 */
public class FcSearchSpace implements SearchSpace {	
	
	private SearchStrategy strategy;
	
	public FcSearchSpace(SearchStrategy strategy) {
		this.strategy = strategy;
	}
	
	/**
	 * Returns and removes the state with the lowest score in the
	 * search space.
	 */
	@Override
	public State getNextState() {
		return strategy.getNextState();
	}

	@Override
	public void putState(State state) {
		strategy.putState(state);
	}
	
	@Override
	public State search(State initialState, long maxRounds) {
		boolean searchOver = false;
		boolean solutionFound = false;
		long round = 0;
		FcState currentState = (FcState)initialState;
		long startTime = (new Date()).getTime();
		while (!searchOver) {
			// System.out.println("Checking round "+round+". Depth: "+currentState.getDepth()+", score: "+currentState.getScore()+".");
			// System.out.println(currentState.boardToString());
			round ++;
			if (round >= maxRounds) {
				searchOver = true;
				solutionFound = false;
				break;
			}
			if (currentState.isWon()) {
				searchOver = true;
				solutionFound = true;
				break;
			}
			ArrayList<FcState> nextStates = currentState.getNextStates();
			for (FcState state : nextStates) {
				this.putState(state);
			}
			currentState = (FcState)this.getNextState();
		}
		long endTime = (new Date()).getTime();
		// If a solution was found:
		if (solutionFound) {
			System.out.println("Solution found in "+(endTime-startTime)/1000+"s, after checking "+round+" states.");
			return currentState;
		}
		// If solution was not found:
		System.out.println("Solution NOT found in "+(endTime-startTime)/1000+"s, after checking "+round+" states.");
		return null;
	}
}
