/**
 * 
 */
package hu.durfi.freecell.solver;

/**
 * Common interface of Search Space implementations.
 * @author durfi
 */
public interface SearchSpace {
	public State getNextState();
	public void putState(State state);
}
