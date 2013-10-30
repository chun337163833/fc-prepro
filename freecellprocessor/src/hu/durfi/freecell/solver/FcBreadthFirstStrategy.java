package hu.durfi.freecell.solver;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class FcBreadthFirstStrategy extends FcGreedyStrategy {
	/**
	 * Map of depths. This structure stores the boards with the depth as
	 * key to make getting the board with the lowest depth faster.
	 */
	protected TreeMap<Long, TreeSet<String>> depths = new TreeMap<Long, TreeSet<String>>();
	
	@Override
	public State getNextState() {
		if (states.isEmpty()) {
			return null;
		}
		// Get entry state with lowest depth
		String board = depths.firstEntry().getValue().pollFirst();
//		System.out.println("Eddigi boardok es depth-ek: \n ----------------------------------------------------------------------------");
//		for (Map.Entry e : states.entrySet()) {
//			FcState st = (FcState)e.getValue();
//			System.out.println(st.boardToString().substring(0, st.boardToString().indexOf('\n')) + " depth: " + st.getDepth());
//		}
		
		// If this was the last entry with this depth, delete
		// this depth.
		if (depths.firstEntry().getValue().isEmpty()) {
			depths.pollFirstEntry();
		}
		// Mark this space as visited!
		visitedStates.add(board);
		return states.get(board);
	}

	@Override
	public void putState(State state) {
		// Do the usual stuff
		super.putState(state);
		
		// Add the depth to the tree
		FcState fs = (FcState)state;
		Long depth = fs.getDepth();
		String board = fs.boardToEqString();

		// If this depth hasn't occurred before, create list for the states
		// with this depth.
		if (!depths.containsKey(depth)) {
			depths.put(depth, new TreeSet<String>());
		}
		depths.get(depth).add(board);
	}
}
