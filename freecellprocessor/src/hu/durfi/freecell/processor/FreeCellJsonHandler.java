package hu.durfi.freecell.processor;

import hu.durfi.FreeCell.FoundationStack;
import hu.durfi.card.Suit;
import hu.durfi.freecell.FcMoveShort;
import hu.durfi.freecell.FreeCellModel;

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
		 * 
		 * Init
		 * 
		 */
		/**
		 * Stack of past moves used for undoing previous moves.
		 */
		pastMoves = new Stack<FcMoveShort>();
		
		/**
		 * Time of last action. Used to keep track of response times.
		 */
		long lastResponseTime;
		long startTime;
		
		/**
		 * Keeps response times in a list. This is used to calculate the
		 * final statistics in the end of the game.
		 */
		LinkedList<Long> responseTimes = new LinkedList<Long>();
		

		/*
		 * 
		 *  Get first "move". It is the "New game" move. We get the initial state
		 *  of the board from that node.
		 *  
		 */
		/**
		 * JSON parser, that processes the JSON string.
		 */
		JSONParser parser = new JSONParser();
		/**
		 * Array of the moves in the game. (Every action is considered a move.)
		 */
		JSONArray moves = (JSONArray) parser.parse(line);
		JSONObject firstMove = (JSONObject)moves.get(0);
		if ((Long)firstMove.get("code") != 301) {
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
		/**
		 * This keeps all data of the currently processed game. When finished processing
		 * the game this gets pushed into the <code>processedGames</code> list.
		 */
		ProcessedGameData currentGame = new ProcessedGameData(seed, initialMoves);
		// Set the starttime
		startTime = lastResponseTime = (Long)firstMove.get("time");
		currentGame.startTime = Long.toString((Long)firstMove.get("time"));
		
		
		
		
		/*
		 * 
		 * Go through all other moves to "play" the game
		 * 
		 */
		for (int i = 1; i < moves.size(); i ++) {
			/**
			 * Current move as a JSON object.
			 */
			JSONObject move = (JSONObject)moves.get(i);
			/**
			 * Code of the current game. See fclime's logentry.js for the meaning
			 * of log codes.
			 */
			long code = (Long)move.get("code");
			/**
			 * Time of move
			 */
			long time = (Long)move.get("time");
			
			// Add the response time to the list
			responseTimes.add(time-lastResponseTime);
			lastResponseTime = time;
			
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
				currentGame.numberOfMovesNeeded.add(numnum);
			}
			/*
			 * INVALID MOVE: Increment counter
			 */
			if (code >= 400 && code < 500) {
				// Increment the number of misplaced moves
				currentGame.misplacedMoves++;
			}
			/*
			 * UNDO: Undo last move
			 */
			if (code == 307) {
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
			
			
			// If in debugging mode, print current move in JSON string and print current setup of
			// the board.
			if (debug) {
				System.out.println(move.toJSONString());
				System.out.println(model.boardToString());
			}
			
		}
		
		
		
		/*
		 * End of game
		 */
		
		// Calculate the number of cards in the foundations
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
		
		// Calculate response times
		long longest = 0;
		long shortest = Long.MAX_VALUE;
		long sum = 0;
		for (int i = 0; i < responseTimes.size(); i ++) {
			long resp = responseTimes.get(i);
			if (resp > longest) {
				longest = resp;
			}
			if (resp < shortest) {
				shortest = resp;
			}
			sum += resp;
		}
		long average = sum / responseTimes.size();
		currentGame.averageResponse = average;
		currentGame.shortestResponse = shortest;
		currentGame.longestResponse = longest;
		
		// Calculate duration
		currentGame.duration = lastResponseTime - startTime;
		
		
		// Was this a won or a lost game?
		currentGame.won = model.isWon();
		
		// Add the game to the list of processed games.
		processedGames.add(currentGame);
	}
}
