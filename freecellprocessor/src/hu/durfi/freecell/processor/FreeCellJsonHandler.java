package hu.durfi.freecell.processor;

import hu.durfi.FreeCell.FoundationStack;
import hu.durfi.card.Suit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Stack;

import mikejyg.freecellsolver.test.FCSolve;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

public class FreeCellJsonHandler {
	private boolean debug = false;

	private FCSolve solver = new FCSolve();
	public LinkedList<ProcessedGameData> processedGames = new LinkedList<ProcessedGameData>();
	private FreeCellModel model = new FreeCellModel();
	private Stack<FcMoveShort> pastMoves = new Stack<FcMoveShort>();
	
	public FreeCellJsonHandler(boolean debug) {
		this.debug = debug;
	}
	
	public void parseFile(File file) throws Exception {
		BufferedReader reader = new  BufferedReader(new FileReader(file) );
		String line;
		while ((line = reader.readLine()) != null) {
			parseLine(line);
		}
	}
	
	public void parseLine(String line) throws Exception {
		/*
		 * Init
		 */
		pastMoves = new Stack<FcMoveShort>();
		
		
		
		/*
		 *  Get first "move". It is the "New game" move. We get the initial state
		 *  of the board from that node.
		 */		
		JSONParser parser = new JSONParser();
		JSONArray moves = (JSONArray) parser.parse(line);
		JSONObject firstMove = (JSONObject)moves.get(0);
		if ((long)firstMove.get("code") != 301) {
			throw new Exception("First element is not a New Game (code 301)!");
		}
		JSONArray board = (JSONArray)((JSONObject)firstMove.get("descr")).get("board");
		int seed = 0;
		// int seed = ((Long)((JSONObject)firstMove.get("descr")).get("seed")).intValue();
		// Create the board
		this.model.createFromJSON(board);
		
		/*
		 * Calculate the number of optimal moves in the beginning
		 */
		int initialMoves = -1;
		try {
			initialMoves = solver.getNumberOfMoves(model.boardToString());
		} catch (Exception ex) {
			System.out.println("Error solving board: " + ex);
		}
		// Create the object holding the processed data
		ProcessedGameData currentGame = new ProcessedGameData(seed, initialMoves);
		
		/*
		 * Go through all other moves to "play" the game
		 */
		for (int i = 1; i < moves.size(); i ++) {
			// Go through all moves
			JSONObject move = (JSONObject)moves.get(i);
			long code = (long)move.get("code");
			
			/*
			 * VALID MOVE : Do the move.
			 */
			if (code >= 200 && code < 300) {
				// Get the description of the move (from where, what, and where to?)
				String from = (String)move.get("from");
				String to = (String)move.get("to");
				String card = (String)move.get("card");
				
				// Increment the valid move count
				currentGame.validMoves++;
				// Do the move
				FcMoveShort fcMoveShort = model.doMove(from, card, to);
				// Save the move, so it can be undone on an UNDO tag.
				pastMoves.push(fcMoveShort);
				
				int numnum = -2;
				try {
					numnum=solver.getNumberOfMoves(model.boardToString());
				} catch (Exception ex) {
					System.out.println("Error solving board: " + ex);
					throw new SAXException(ex);
				}
				//System.out.println("Ennyi: " + numnum);
				currentGame.numberOfMovesNeeded.add(numnum);
			}
			/*
			 * UNDO
			 */
			if (code == 307) {
				// Undo last move!
				FcMoveShort lastMove = pastMoves.pop();
				try {
					model.doMove(lastMove.toType, lastMove.toNum, lastMove.numberOfMovedCards, lastMove.fromType, lastMove.fromNum);
				} catch (Exception ex) {
					System.out.println("Error while doing move: " + ex);
				}
				try {
					currentGame.numberOfMovesNeeded.add(solver.getNumberOfMoves(model.boardToString()));
				} catch (Exception ex) {
					System.out.println("Error solving board: " + ex);
					throw new SAXException(ex);
				}
			}
			
			if (debug) {
				System.out.println(move.toJSONString());
				System.out.println(model.boardToString());
			}
			
		}
		
		
		
		/*
		 * End of game
		 */
		
		// Number of cards in the foundations
		for (int i = 0; i < model.foundationStacks.length; i++) {
			FoundationStack fou = model.foundationStacks[i];
			if (fou != null && !fou.isEmpty()) {
				Suit suit = fou.elementAt(0).getSuit();
				if (suit == Suit.CLUB) {
					currentGame.clubsInFoundations = fou.size();
				} else if (suit == Suit.DIAMOND) {
					currentGame.diamondsInFoundations = fou.size();
				} else if (suit == Suit.HEART) {
					currentGame.heartsInFoundations = fou.size();
				} else if (suit == Suit.SPADE) {
					currentGame.spadesInFoundations = fou.size();
				}
			}
		}
		
		/*
		 * Won or lost game?
		 */
		currentGame.won = model.isWon();
		
		processedGames.add(currentGame);
	}
}
