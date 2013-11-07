package hu.durfi.freecell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

import javax.print.attribute.HashAttributeSet;

import org.json.simple.JSONArray;

import sun.font.CreatedFontTracker;
import hu.durfi.FreeCell.FoundationStack;
import hu.durfi.FreeCell.ReserveStack;
import hu.durfi.FreeCell.TableauStack;
import hu.durfi.card.Card;
import hu.durfi.card.ClassicCard;
import hu.durfi.card.ClassicDeck;
import hu.durfi.card.Suit;

public class FreeCellModel {
	private int numberOfTableauStacks = 8;
	private int numberOfReserveStacks = 4;
	private final int numberOfFoundationStacks = 4;
	
	/**
	 * The Tableau stacks.
	 */
	public TableauStack[] tableauStacks = new TableauStack[numberOfTableauStacks];
	/**
	 * The Reserve "stacks".
	 */
	public ReserveStack[] reserveStacks = new ReserveStack[numberOfReserveStacks];
	/**
	 * The Foundation stacks.
	 */
	public FoundationStack[] foundationStacks = new FoundationStack[numberOfFoundationStacks];
	
	public HashMap<String, ClassicCard> cards;
	
	public FreeCellModel() {
		/*
		 * Put all cards in a HashMap with their string representation
		 * as key. The JSON contains these strings to represent the cards.
		 */
		cards = new HashMap<String, ClassicCard>();
		eraseStacks();
		ClassicDeck deck = new ClassicDeck(null);
		deck.shuffle(0);
		for (ClassicCard card : deck ) {
			cards.put(card.toString(), card);
		}
	}
	
	public  void distributeCards(int seed) {
		eraseStacks();
		
		// Create a new deck
		ClassicDeck deck = new ClassicDeck(null);
		deck.shuffle(seed);
		
		// Distribute the deck to the stacks
		int i = 0;
		while (!deck.isEmpty()) {
			tableauStacks[i % tableauStacks.length].push(deck.pop());
			i++;
		}
	}
	
	/**
	 * Create the initial state of the board from the given JSON array.
	 * This is to be used in the M3W-JS environment.
	 * @param json
	 */
	public void createFromJSON(JSONArray board) {
		eraseStacks();
		for (int i = 0; i < board.size(); i ++) {
			JSONArray stack = (JSONArray)board.get(i);
			for (int j = 0; j < stack.size(); j ++) {
				String cardStr = stack.get(j).toString();
				ClassicCard card = cards.get(cardStr);
				tableauStacks[i].add(card);
			}
		}
	}
	
	/**
	 * This returns the tableau stacks as a JSON array. This is useful
	 * as representing an initial state. The output of this function can
	 * be given to FcLime in a replay.
	 * @return JSON Array of tableau stacks. Same format as in
	 * <code>createFromJSON</code>
	 * @see createFromJSON
	 */
	public JSONArray tableauToJSON() {
		JSONArray ret = new JSONArray();
		
		for (int i = 0; i < tableauStacks.length; i ++) {
			JSONArray stack = new JSONArray();
			for (int j = 0; j < tableauStacks[i].size(); j ++) {
				stack.add(tableauStacks[i].elementAt(j).toString());
			}
			ret.add(stack);
		}
		
		return ret;
	}
	
	/**
	 * Erases all the stacks
	 */
	private void eraseStacks() {
		for (int i = 0; i < tableauStacks.length; i ++) {
			tableauStacks[i] = new TableauStack("t"+i);
		}
		for (int i = 0; i < reserveStacks.length; i ++) {
			reserveStacks[i] = new ReserveStack("r"+i);
		}
		for (int i = 0; i < foundationStacks.length; i ++) {
			foundationStacks[i] = new FoundationStack("f"+i);
		}
	}
	
	/**
	 * Do a move based on the log. This is called from the XML handler.
	 * @param fromType Type of the source stack. 't' = tableau, 'r' = reserve.
	 * @param fromNum Index of the source stack.
	 * @param numOfCards Number of cards moved.
	 * @param toType Type of the destination stack. 't' = tableau, 'r' = reserve, 'f' = foundation.
	 * @param toNum Index of the destination stack.
	 */
	public void doMove(char fromType, int fromNum, int numOfCards, char toType, int toNum) throws Exception {
		// Get the moved cards to a new stack
		Stack<ClassicCard> tmp = new Stack<ClassicCard>();
		if (fromType == 't') {
			for (int i = 0; i < numOfCards; i ++) {
				tmp.push(tableauStacks[fromNum].pop());
			}
		} else if (fromType == 'r') {
			tmp.push(reserveStacks[fromNum].pop());
		} else {
			tmp.push(foundationStacks[fromNum].pop());
		}
		
		// Put that stack to its new place
		if (toType == 'f') {
			foundationStacks[toNum].push(tmp.pop());
		} else if (toType == 'r') {
			reserveStacks[toNum].push(tmp.pop());
		} else {
			while (!tmp.isEmpty()) {
				tableauStacks[toNum].push(tmp.pop());
			}
		}
	}
	
	/**
	 * Do a move. Return FcShortMove to be used in the undo log.
	 * @param from
	 * @param cardStr
	 * @param to
	 * @throws Exception
	 */
	public FcMoveShort doMove(String from, String cardStr, String to) throws Exception {
		char fromType = from.charAt(0);
		int fromNum = from.charAt(1) - '0';
		char toType = to.charAt(0);
		int toNum = to.charAt(1) - '0';
		int numOfCards = 0;
		ClassicCard card = cards.get(cardStr);
		if (fromType == 't') {
			numOfCards = tableauStacks[fromNum].size() - tableauStacks[fromNum].indexOf(card);
		} else if (fromType == 'r') {
			numOfCards = reserveStacks[fromNum].size() - reserveStacks[fromNum].indexOf(card);
		} else {
			numOfCards = foundationStacks[fromNum].size() - foundationStacks[fromNum].indexOf(card);
		}
		doMove(fromType, fromNum, numOfCards, toType, toNum);
		return new FcMoveShort(fromType, fromNum, numOfCards, toType, toNum);
	}
	
	/**
	 * Do an automatic move from the given place.
	 * @param fromType Type of the source stack. 't' = tableau, 'r' = reserve.
	 * @param fromNum Index of the source stack.
	 */
	public void doAutoMove(char fromType, int fromNum) {
		ClassicCard card;
		if (fromType == 't') {
			card = tableauStacks[fromNum].pop();
		} else {
			card = reserveStacks[fromNum].pop();
		}
		for (int i = 0; i < foundationStacks.length; i ++) {
			if (foundationStacks[i].isValid(card)) {
				foundationStacks[i].push(card);
				break;
			}
		}
	}
	
	/**
	 * Returns the current state of the board (the stacks) in 
	 * a table form. This can be passed to FreeCell Java Solver
	 * to solve.
	 * @return
	 */
	public String boardToString() {
		if (tableauStacks == null || foundationStacks == null || reserveStacks == null)
			return "";
		
		StringBuffer board = new StringBuffer();
		
		for (int i = 0; i < reserveStacks.length; i ++) {
			if (!reserveStacks[i].isEmpty()) {
				board.append(reserveStacks[i].getCard().toStringVS()).append(" ");
			} else {
				board.append("__ ");
			}
		}
		board.append(" ");
		
		// Foundation stacks in a given order:
		
		LinkedList<Suit> foundationSuits = new LinkedList<Suit>();
		foundationSuits.add(Suit.DIAMOND);
		foundationSuits.add(Suit.HEART);
		foundationSuits.add(Suit.CLUB);
		foundationSuits.add(Suit.SPADE);
		for (Suit suit : foundationSuits) {
			boolean found = false;
			for (int i = 0; i < foundationStacks.length; i ++) {
				if (foundationStacks[i].getTopCard() != null && foundationStacks[i].getTopCard().getSuit() == suit) {
					board.append(foundationStacks[i].getTopCard().toStringVS()).append(" ");
					found = true;
				}
			}
			if (!found) {
				board.append("__ ");
			}
		}
		board.setLength(board.length() - 1); // Trim the last space.
		board.append("\n\n");
		
		// Tableau Stacks:
		
		// Get the length of the longest stack:
		int length = 0;
		for (int i = 0; i < tableauStacks.length; i ++) {
			if (tableauStacks[i].size() > length) {
				length = tableauStacks[i].size();
			}
		}
		
		for (int index = 0; index < length; index ++) {
			for (int i = 0; i < tableauStacks.length; i ++) {
				if (tableauStacks[i].size() > index) {
					board.append(tableauStacks[i].elementAt(index).toStringVS()).append(" ");
				} else {
					board.append("__ ");
				}
			}
			
			board.setLength(board.length() - 1);
			if (index < length - 1)
				board.append("\n");
		}
		
		return board.toString();
	}
	
	/**
	 * <p>Returns the current state of the board (the stacks) in 
	 * a table form. This can be passed to FreeCell Java Solver
	 * to solve. 
	 * 
	 * <p>This differs from boardToString by returning the same
	 * string for equivalent boards (where only the order of stacks differ).
	 * This is achieved by ordering the stacks by their first card.
	 * @return
	 */
	public String boardToEqString() {
		if (tableauStacks == null || foundationStacks == null || reserveStacks == null)
			return "";
		
		StringBuffer board = new StringBuffer();
		
		/*
		 * Free cells first:
		 */
		ArrayList<String> reserves = new ArrayList<String>();
		for (int i = 0; i < reserveStacks.length; i ++) {
			if (!reserveStacks[i].isEmpty()) {
				reserves.add(reserveStacks[i].getCard().toStringVS());
			} else {
				reserves.add("__");
			}
		}
		Collections.sort(reserves);
		for (String r : reserves) {
			board.append(r).append(" ");
		}
		board.append(" ");
		
		/*
		 * Foundations:
		 */
		LinkedList<Suit> foundationSuits = new LinkedList<Suit>();
		foundationSuits.add(Suit.DIAMOND);
		foundationSuits.add(Suit.HEART);
		foundationSuits.add(Suit.CLUB);
		foundationSuits.add(Suit.SPADE);
		for (Suit suit : foundationSuits) {
			boolean found = false;
			for (int i = 0; i < foundationStacks.length; i ++) {
				if (foundationStacks[i].getTopCard() != null && foundationStacks[i].getTopCard().getSuit() == suit) {
					board.append(foundationStacks[i].getTopCard().toStringVS()).append(" ");
					found = true;
				}
			}
			if (!found) {
				board.append("__ ");
			}
		}
		board.setLength(board.length() - 1); // Trim the last space.
		board.append("\n\n");
		
		/*
		 * Tableau stacks
		 */
		// Create a list from the tableau stacks
		ArrayList<TableauStack> tsList = new ArrayList<TableauStack>();
		for (TableauStack ts : tableauStacks) {
			tsList.add(ts);
		}
		// Sort that list
		Collections.sort(tsList, new Comparator<TableauStack>() {
			@Override
			public int compare(TableauStack o1, TableauStack o2) {
				// Sort by first card
				String c1 = o1.isEmpty() ? "__" : o1.firstElement().toStringVS();
				String c2 = o2.isEmpty() ? "__" : o2.firstElement().toStringVS();
				return c1.compareTo(c2);
			}
		});
		// Get the length of the longest stack:
		int length = 0;
		for (int i = 0; i < tableauStacks.length; i ++) {
			if (tableauStacks[i].size() > length) {
				length = tableauStacks[i].size();
			}
		}
		
		for (int index = 0; index < length; index ++) {
			for (int i = 0; i < tsList.size(); i ++) {
				if (tsList.get(i).size() > index) {
					board.append(tsList.get(i).elementAt(index).toStringVS()).append(" ");
				} else {
					board.append("__ ");
				}
			}
			
			board.setLength(board.length() - 1);
			if (index < length - 1)
				board.append("\n");
		}
		
		
		return board.toString();
	}
	
	/**
	 * Is the game stored in this model an already won game? Game
	 * is considered won, if all stacks are correctly ordered.
	 * @return
	 */
	public boolean isWon() {
		boolean won = true;
		
		// Game is considered won if all tableau stacks are in the right order.
		if (tableauStacks != null) {
			for(int i = 0; i < tableauStacks.length; i ++) {
				if (tableauStacks[i] != null
						&& !tableauStacks[i].isEmpty()
						&& tableauStacks[i].firstValidIndex() != 0)
				{
					won = false;
				}
			}
		}
		return won;
	}


}
