package hu.durfi.freecell.solver;

import java.util.List;

/**
 * States in the search space must implements this interface.
 * @author durfi
 *
 */
public interface State {
	public List getNextStates();
	public Object getHash();
	public void setParent(State parent);
	public State getParent();
	public void setDepth(long depth);
	public long getDepth();
}
