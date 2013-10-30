package hu.durfi.freecell.solver;

public interface SearchStrategy {
	public State getNextState();
	public void putState(State state);
}
