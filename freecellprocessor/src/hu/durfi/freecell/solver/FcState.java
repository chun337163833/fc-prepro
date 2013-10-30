package hu.durfi.freecell.solver;

import hu.durfi.FreeCell.CardStack;
import hu.durfi.FreeCell.FoundationStack;
import hu.durfi.FreeCell.ReserveStack;
import hu.durfi.FreeCell.TableauStack;
import hu.durfi.card.ClassicCard;
import hu.durfi.freecell.FreeCellModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

public class FcState extends FreeCellModel implements State {

	/**
	 * Where did we get here from? This is updated if this state appears in
	 * a shorter path.
	 */
	private FcState parent;
	
	/**
	 * How deep did this occur in the search tree
	 */
	private long depth = 0L;
	
	/**
	 * How did we get here from the parent state? This is the JSON representation
	 * of the move. See Fclime for further details.
	 * 
	 * Example: {"code":202,"from":"t5","card":"SA","to":"r1","time":1382252373562}
	 * ("code" and "time" are irrelevant here)
	 */
	private String transition;
	
	/**
	 * Cost of getting to this state. This is set to 0 in the constructor
	 * and set to the correct value, when adding the parent state (in setParent).
	 */
	private long cost = 0L;
	
	public FcState() {
		super();
	}
	
	public FcState(FcState parent) {
		super();
		this.parent = parent;
		this.depth = parent.getDepth() + 1;
	}
	
	@Override
	public ArrayList<FcState> getNextStates() {
		// This will hold the results
		ArrayList<FcState> nextStates = new ArrayList<FcState>();
		// Keep the equivalent board representations, so the same board
		// wont show up in next states.
		HashSet<String> nextBoards = new HashSet<String>();
		
		/*
		 * Moves from the free cells
		 */
		for (int i = 0; i < reserveStacks.length; i ++) {
			ReserveStack from = reserveStacks[i];
			if (from.isEmpty()) {
				continue;
			}
			ClassicCard card = from.getCard();
			/*
			 * Move to foundations
			 */
			for (int j = 0; j < foundationStacks.length; j ++) {
				FoundationStack to = foundationStacks[j];
				if (to.isValid(card)) {
					FcState next = afterMove(from, 1, to);
					if (!nextBoards.contains(next.boardToEqString())) {
						nextBoards.add(next.boardToEqString());
						nextStates.add(next);
					}
				}
			}
			/*
			 * Move to tableau stacks
			 */
			for (int j = 0; j < tableauStacks.length; j ++) {
				TableauStack to = tableauStacks[j];
				TableauStack dummy = new TableauStack();
				dummy.push(card);
				if (to.isValid(dummy)) {
					FcState next = afterMove(from, 1, to);
					if (!nextBoards.contains(next.boardToEqString())) {
						nextBoards.add(next.boardToEqString());
						nextStates.add(next);
					}
				}
			}
		}
		/*
		 * Moves from the tableau
		 */
		for (int i = 0; i < tableauStacks.length; i ++) {
			TableauStack from = tableauStacks[i];
			if (from.isEmpty()) {
				continue;
			}
			// Try all movable cards in the stack
			for (int j = from.firstValidIndex(); j < from.size(); j ++) {
				 int numberOfCardsToMove = from.size() - j;
				 // Dummy stack contains the first card of the to-be-moved substack
				 TableauStack dummy = new TableauStack();
				 dummy.push(from.get(j));
				 /*
				  * Move to tableau stacks
				  */
				 for (int k = 0; k < tableauStacks.length; k ++) {
					 TableauStack to = tableauStacks[k];
					 // Skip same column (not really necessary)
					 if (i == k) {
						 continue;
					 }
					 // Check number of empty free cells
					 if (numberOfCardsToMove > 1) {
							int freeReserve = 0;
							int freeTableau = 0;
							// Count empty reserved stacks
							for (int l = 0; l < reserveStacks.length; l ++) {
								if (reserveStacks[l].isEmpty()) {
									freeReserve ++;
								}
							}
							// Count empty tableau stacks (not including the 'from' and 'to' stacks).
							for (int l = 0; l < tableauStacks.length; l ++) {
								if (tableauStacks[l].isEmpty() && tableauStacks[l] != from && tableauStacks[l] != to) {
									freeTableau ++;
								}
							}
							int numberOfMovableCards = (1 + freeReserve) * (int)Math.pow(2, freeTableau);
							// If not enough, continue!
							if (numberOfMovableCards < numberOfCardsToMove) {
								continue;
							}
					 }
					 if (to.isValid(dummy)) {
						FcState next = afterMove(from, numberOfCardsToMove, to);
						if (!nextBoards.contains(next.boardToEqString())) {
							nextBoards.add(next.boardToEqString());
							nextStates.add(next);
						}
					 }
				 }
				 
				 // If trying to move more than one card, don't even try
				 // the free cells and the foundations
				 if (numberOfCardsToMove > 1) {
					 continue;
				 }
				 
				 /*
				  * Move to free cells
				  */
				 for (int k = 0; k < reserveStacks.length; k ++) {
					 ReserveStack to = reserveStacks[k];
					 // Checking if empty is enough now
					 if (to.isEmpty()) {
						FcState next = afterMove(from, numberOfCardsToMove, to);
						if (!nextBoards.contains(next.boardToEqString())) {
							nextBoards.add(next.boardToEqString());
							nextStates.add(next);
						}
					 }
				 }
				 
				 /*
				  * Move to foundations
				  */
				 for (int k = 0; k < foundationStacks.length; k ++) {
					 FoundationStack to = foundationStacks[k];
					 if (to.isValid(dummy)) {
						FcState next = afterMove(from, numberOfCardsToMove, to);
						if (!nextBoards.contains(next.boardToEqString())) {
							nextBoards.add(next.boardToEqString());
							nextStates.add(next);
						}
					 }
				 }
			}
		}
		
		return nextStates;
	}
	
	/**
	 * Return the state after a given move.
	 * @param from From where move the cards.
	 * @param num How many cards to move.
	 * @param to Where to move the cards.
	 * @return A new FcState object that hold the state after the move.
	 */
	private FcState afterMove(CardStack from, int num, CardStack to) {
		Stack<ClassicCard> tmp = new Stack<ClassicCard>();
		for (int i = 0; i < num; i ++) {
			tmp.push(from.pop());
		}
		for (int i = 0; i < num; i ++) {
			to.push(tmp.pop());
		}
		FcState state = copy();
		for (int i = 0; i < num; i ++) {
			tmp.push(to.pop());
		}
		for (int i = 0; i < num; i ++) {
			from.push(tmp.pop());
		}
		
		state.setParent(this);
		state.setDepth(this.getDepth()+1);
		state.setTransition("{\"from\": \""+from.getName()+"\", \"num\": \""+num+"\", \"to\": \""+to.getName()+"\"}");
		return state;
	}
	
	/**
	 * Create a "deep" copy of this state. This won't duplicate cards, only
	 * copy their references.
	 * @return
	 */
	public FcState copy() {
		FcState state = new FcState();
		// Copy reserve stacks
		for (int i = 0; i < reserveStacks.length; i ++) {
			if (reserveStacks[i].isEmpty()) {
				continue;
			}
			state.reserveStacks[i].push(reserveStacks[i].getCard());
		}
		for (int i = 0; i < foundationStacks.length; i ++) {
			for (int j = 0; j < foundationStacks[i].size(); j ++) {
				state.foundationStacks[i].push(foundationStacks[i].get(j));
			}
		}
		for (int i = 0; i < tableauStacks.length; i ++) {
			for (int j = 0; j < tableauStacks[i].size(); j ++) {
				state.tableauStacks[i].push(tableauStacks[i].get(j));
			}
		}
		return state;
	}

	@Override
	public Object getHash() {
		return boardToEqString();
	}

	public Long getScore() {
		return getNumberOfCardsInFoundations();
	}
	
	private Long getNumberOfCardsInFoundations() {
		Long result = 0L;
		for (int i = 0; i < foundationStacks.length; i ++) {
			result += foundationStacks[i].size();
		}
		return result;
	}

	@Override
	public void setParent(State parent) {
		this.parent = (FcState)parent;
	}

	@Override
	public State getParent() {
		return this.parent;
	}

	@Override
	public void setDepth(long depth) {
		this.depth = depth;
	}
	
	@Override
	public long getDepth() {
		return this.depth;
	}

	public void setTransition(String transition) {
		this.transition = transition;
	}
	
	public String getTransition() {
		return this.transition;
	}
	
	public List<String> getTransitions() {
		// Put the transactions in a list (backwards)
		ArrayList<String> transitions = new ArrayList<String>();
		FcState st = this;
		while (st.getParent() != null) {
			transitions.add(0, st.getTransition());
			st = (FcState)st.getParent();
		}
		return transitions;
	}
}
