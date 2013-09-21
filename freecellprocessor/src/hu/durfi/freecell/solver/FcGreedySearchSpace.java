package hu.durfi.freecell.solver;

import java.util.TreeMap;

public class FcGreedySearchSpace implements SearchSpace {
	/**
	 * Map of states. The key is the score associated with the state.
	 */
	private TreeMap<Long, FcState> states = new TreeMap<Long, FcState>();
	
	/**
	 * Returns and removes the state with the lowest score in the
	 * search space.
	 */
	@Override
	public State getNextState() {
		if (states.isEmpty()) {
			return null;
		}
		return states.pollFirstEntry().getValue();
	}

	@Override
	public void putState(State state) {
		states.put(state.getScore(), (FcState)state);
	}

}
