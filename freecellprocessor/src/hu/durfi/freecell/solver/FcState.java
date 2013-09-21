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
//		for (int i = 0; i < reserveStacks.length; i ++) {
//			ReserveStack reserve = reserveStacks[i];
//			if (reserve.isEmpty()) {
//				continue;
//			}
//			ClassicCard card = reserve.getCard();
//			for (int j = 0; j < foundationStacks.length; j ++) {
//				FoundationStack foundation = foundationStacks[j];
//				if (foundation.isValid(card)) {
//					
//				}
//				TableauStack tableau = tableauStacks[0];
//			}
//		}
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
					 
					 if (to.isValid(dummy)) {
						 nextStates.add(afterMove(from, numberOfCardsToMove, to));
						 System.out.println(nextStates.get(nextStates.size()-1).boardToString());
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
			state.reserveStacks[i].add(reserveStacks[i].getCard());
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

}
