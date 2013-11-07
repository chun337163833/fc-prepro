package hu.durfi.freecell.solver;

public interface SearchStrategy {
	/**
	 * <p>Get the next state to process. This is where the strategy happens.
	 * @return
	 */
	public State getNextState();
	/**
	 * <p>Put a state into the search space. Returns true if this state
	 * was a new state. Return false if this state was already in the
	 * search space.
	 * <p>Returning false doesn't mean that the already stored state wasn't
	 * changed by this method! If the path which lead to this state is better in
	 * this case, it might have been overwritten!
	 * 
	 * @param state
	 * @return
	 */
	public boolean putState(State state);
}
