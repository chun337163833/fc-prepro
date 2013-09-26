package hu.durfi.freecell.solver;

import hu.durfi.FreeCell.CardStack;
import hu.durfi.FreeCell.FoundationStack;
import hu.durfi.FreeCell.ReserveStack;
import hu.durfi.FreeCell.TableauStack;
import hu.durfi.card.ClassicCard;
import hu.durfi.freecell.FreeCellModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

public class FcState extends FreeCellModel implements State {

	public FcState() {
		super();
	}
	
	@Override
	public ArrayList<FcState> getNextStates() {
		ArrayList<FcState> nextStates = new ArrayList<FcState>();
		
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
					nextStates.add(afterMove(from, 1, to));
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
					nextStates.add(afterMove(from, 1, to));
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
				 // TODO: Check number of empty free cells before moving!
				 for (int k = 0; k < tableauStacks.length; k ++) {
					 TableauStack to = tableauStacks[k];
					 // Skip same column (not really necessary)
					 if (i == k) {
						 continue;
					 }
					 
					 if (to.isValid(dummy)) {
						 nextStates.add(afterMove(from, numberOfCardsToMove, to));
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
						 nextStates.add(afterMove(from, numberOfCardsToMove, to));
					 }
				 }
				 
				 /*
				  * Move to foundations
				  */
				 for (int k = 0; k < foundationStacks.length; k ++) {
					 FoundationStack to = foundationStacks[k];
					 if (to.isValid(dummy)) {
						 nextStates.add(afterMove(from, numberOfCardsToMove, to));
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
		return boardToString();
	}

	@Override
	public Long getScore() {
		// TODO Auto-generated method stub
		return 0L;
	}
	
	public Long getNumberOfCardsInFoundations() {
		Long result = 0L;
		for (int i = 0; i < foundationStacks.length; i ++) {
			result += foundationStacks[i].size();
		}
		return result;
	}

}
