package hu.durfi.freecell.processor;

import hu.durfi.FreeCell.FoundationStack;
import hu.durfi.card.Suit;

import java.util.LinkedList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mikejyg.freecellsolver.test.FCSolve;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class FreeCellXmlHandler extends DefaultHandler {
	private static enum Tags {
		USERID, VERSION,
		GAME, SEED, MODE, STARTTIME, DURATION, DISTURBTYPE, WON, STATISTICS, FOUNDATIONSINTHEEND,
		CLUBS, DIAMONDS, HEARTS, SPADES, RESPONSETIMES, RESPONSE90TIMES,
		AVERAGE, SHORTEST, LONGEST, NUMBEROFMOVES, LOG, VALID, MISPLACED, CLICK, INFO
	}
	
	private Tags currentTag;
	
	private boolean debug = false;
	
	private static final int undoId = 307;
	private static final int autoPlayFromTableauId = 501;
	private static final int autoPlayFromReserveId = 502;
	
	private FreeCellModel model = new FreeCellModel();
	
	private FCSolve solver = new FCSolve();
	
	private boolean response90 = false;
	
	private String step = "";
	
	private String version = "unknown";
	
	private String userId = "unknown";
	
	public LinkedList<ProcessedGameData> processedGames = new LinkedList<ProcessedGameData>();
	
	private ProcessedGameData currentGame;
	
	private Stack<FcMoveShort> pastMoves = new Stack<FcMoveShort>();

	
	
	public FreeCellXmlHandler(boolean debugMode) {
		debug = debugMode;
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase(Tags.USERID.toString())) {
			currentTag = Tags.USERID;
		}
		if (qName.equalsIgnoreCase(Tags.VERSION.toString())) {
			currentTag = Tags.VERSION;
		}
		
		// New game starts
		if (qName.equalsIgnoreCase(Tags.GAME.toString())) {
			this.model = new FreeCellModel();
			currentTag = Tags.GAME;
			// the currentGame element is constructed in the SEED tag's body
		}
		
		if (qName.equalsIgnoreCase(Tags.SEED.toString()))
			currentTag = Tags.SEED;
			
		if (qName.equalsIgnoreCase(Tags.MODE.toString()))
			currentTag = Tags.MODE;
		if (qName.equalsIgnoreCase(Tags.STARTTIME.toString()))
			currentTag = Tags.STARTTIME;
		if (qName.equalsIgnoreCase(Tags.DURATION.toString()))
			currentTag = Tags.DURATION;
		if (qName.equalsIgnoreCase(Tags.DISTURBTYPE.toString()))
			currentTag = Tags.DISTURBTYPE;
		if (qName.equalsIgnoreCase(Tags.WON.toString()))
			currentTag = Tags.WON;
		
		if (qName.equalsIgnoreCase(Tags.CLUBS.toString()))
			currentTag = Tags.CLUBS;
		if (qName.equalsIgnoreCase(Tags.DIAMONDS.toString()))
			currentTag = Tags.DIAMONDS;
		if (qName.equalsIgnoreCase(Tags.HEARTS.toString()))
			currentTag = Tags.HEARTS;
		if (qName.equalsIgnoreCase(Tags.SPADES.toString()))
			currentTag = Tags.SPADES;
		
		if (qName.equalsIgnoreCase(Tags.RESPONSETIMES.toString()))
			response90 = false;
		if (qName.equalsIgnoreCase(Tags.RESPONSE90TIMES.toString()))
			response90 = true;
		
		if (qName.equalsIgnoreCase(Tags.AVERAGE.toString()))
			currentTag = Tags.AVERAGE;
		if (qName.equalsIgnoreCase(Tags.SHORTEST.toString()))
			currentTag = Tags.SHORTEST;
		if (qName.equalsIgnoreCase(Tags.LONGEST.toString()))
			currentTag = Tags.LONGEST;
		
		if (qName.equalsIgnoreCase(Tags.VALID.toString())) {
			currentTag = Tags.VALID;
			
			currentGame.validMoves++;
			
			// Step attribute example: t6([C2, D2, SA, D5, CA]), [CJ] > r3:([])
			//                          ^from tableau 6           ^move this ^to reserve 3
			step = attributes.getValue("step");
			
			char fromType = step.charAt(0);
			int fromNum = step.charAt(1) - '0';
			
			String movedCards = step.split("\\[")[2].split("\\]")[0];
			int numberOfMovedCards = movedCards.split(",").length;
			
			char toType = step.charAt(step.indexOf('>')+2);
			int toNum = step.charAt(step.indexOf('>')+3) - '0';
			
			// Save the move, so it can be undone on an UNDO tag.
			pastMoves.push(new FcMoveShort(fromType, fromNum, numberOfMovedCards, toType, toNum));
			
			// Do the move.
			try {
				model.doMove(fromType, fromNum, numberOfMovedCards, toType, toNum);
			} catch (Exception ex) {
				System.out.println("Error while doing move: " + ex);
			}
			
			int numnum = -2;
			
			if (debug) {
				System.out.println("-------------------------------------\n" +
						"Move time: " + attributes.getValue("time") + "\n" + model.boardToString());
			}
			
			try {
				numnum=solver.getNumberOfMoves(model.boardToString());
			} catch (Exception ex) {
				System.out.println("Error solving board: " + ex);
				throw new SAXException(ex);
			}
			//System.out.println("Ennyi: " + numnum);
			currentGame.numberOfMovesNeeded.add(numnum);
			
			// System.out.println("Move " + numberOfMovedCards + " cards from " + fromType + fromNum + " to " + toType + toNum + ".");
			// System.out.println(model.boardToString());
		}
		
		if (qName.equalsIgnoreCase(Tags.MISPLACED.toString())) {
			currentTag = Tags.MISPLACED;
			currentGame.misplacedMoves++;
		}
		if (qName.equalsIgnoreCase(Tags.CLICK.toString())) {
			currentTag = Tags.CLICK;
			currentGame.clickMoves++;
		}
		if (qName.equalsIgnoreCase(Tags.INFO.toString())) {
			currentTag = Tags.INFO;
			int id = new Integer(attributes.getValue("id"));
			if (id == undoId) {
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
			
			/*
			 * Do autoplay
			 */
			if (id == autoPlayFromReserveId || id == autoPlayFromTableauId) {
				String step = attributes.getValue("description");
				char fromType = step.charAt(0);
				int fromNum = step.charAt(1) - '0';
				
				model.doAutoMove(fromType, fromNum);
				
				int numnum = -2;
				try {
					numnum=solver.getNumberOfMoves(model.boardToString());
				} catch (Exception ex) {
					System.out.println("Error solving board: " + ex);
					throw new SAXException(ex);
				}
				currentGame.numberOfMovesNeeded.add(numnum);
				currentGame.autoPlay = true;
			}
		}
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// Store this game to the processed games
		if (qName.equalsIgnoreCase(Tags.GAME.toString())) {
			// Calculated some statistic values:
			
			/*
			 * Number of cards in foundations
			 */
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
			pastMoves = new Stack<FcMoveShort>();
		}
		currentTag = null;
	}
	
	public void characters(char ch[], int start, int length) throws SAXException {

		String str = new String(ch, start, length);
		
		if (currentTag == Tags.USERID) {
			userId = str;
		}
		
		if (currentTag == Tags.VERSION) {
			version = str;
		}
		
		if (currentTag == Tags.SEED) {
			int seed = new Integer(str);
			model.distributeCards(seed);
			int initialMoves = -1;
			try {
				initialMoves = solver.getNumberOfMoves(model.boardToString());
			} catch (Exception ex) {
				System.out.println("Error solving board: " + ex);
			}
			currentGame = new ProcessedGameData(seed, initialMoves);
			currentGame.userId = userId;
			currentGame.version = version;
		}
		if (currentTag == Tags.MODE) {
			if (str.equalsIgnoreCase("drag")) {
				currentGame.clickMode = false;
			} else {
				currentGame.clickMode = true;
			}
		}
		if (currentTag == Tags.STARTTIME) {
			currentGame.startTime = str;
		}
		if (currentTag == Tags.DURATION) {
			currentGame.duration = new Long(str);
		}
		if (currentTag == Tags.DISTURBTYPE) {
			currentGame.disturbType = str;
		}
		
		/* 
		 * These are not used due to log problems in older FreeCell versions
		 * These values are extracted from the FreeCellModel object in the
		 * end of the game.
		 */
		
//		if (currentTag == Tags.WON) {
//			currentGame.won = new Boolean(str);
//		}
//		
//		if (currentTag == Tags.CLUBS) {
//			currentGame.clubsInFoundations = new Integer(str);
//		}
//		if (currentTag == Tags.DIAMONDS) {
//			currentGame.diamondsInFoundations = new Integer(str);
//		}
//		if (currentTag == Tags.HEARTS) {
//			currentGame.heartsInFoundations = new Integer(str);
//		}
//		if (currentTag == Tags.SPADES) {
//			currentGame.spadesInFoundations = new Integer(str);
//		}
		
		if (!response90) {
			if (currentTag == Tags.AVERAGE) {
				currentGame.averageResponse = new Integer(str);
			}
			if (currentTag == Tags.SHORTEST) {
				currentGame.shortestResponse = new Integer(str);
			}
			if (currentTag == Tags.LONGEST) {
				currentGame.longestResponse = new Integer(str);
			}
		} else {

			if (currentTag == Tags.AVERAGE) {
				currentGame.averageResponse90 = new Integer(str);
			}
			if (currentTag == Tags.SHORTEST) {
				currentGame.shortestResponse90 = new Integer(str);
			}
			if (currentTag == Tags.LONGEST) {
				currentGame.longestResponse90 = new Integer(str);
			}
		}
		
	}
}
