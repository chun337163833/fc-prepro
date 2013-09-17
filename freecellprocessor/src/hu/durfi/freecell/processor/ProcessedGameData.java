package hu.durfi.freecell.processor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * A ProcessedGameData object holds the results of the
 * analysis of a logged FreeCell game. These objects are created
 * when parsing the log in the FreeCellProcessor.  
 * @author durfi
 *
 */
public class ProcessedGameData {
	/**
	 * Random seed of the game. Stored in the constructor.
	 */
	private int seed = -1;
	
	/**
	 * Optimal number of moves needed to solve the game. A new number is calculated
	 * after every valid move.
	 */
	public LinkedList<Integer> numberOfMovesNeeded = new LinkedList<Integer>();
	
	public String userId = "unknown";

	public String version = "unknown";
	
	public String startTime = "";
	
	public long duration = -1;
	
	public String disturbType;
	
	public String log;
	
	public ArrayList<Long> moveTimes = new ArrayList<Long>();
	
	public int validMoves = 0;
	public int misplacedMoves = 0;
	public int clickMoves = 0;
	
	public long averageResponse = -1;
	public long shortestResponse = -1;
	public long longestResponse = -1;
	public long averageResponse90 = -1;
	public long shortestResponse90 = -1;
	public long longestResponse90 = -1;
	
	public int clubsInFoundations = 0;
	public int heartsInFoundations = 0;
	public int spadesInFoundations = 0;
	public int diamondsInFoundations = 0;
	
	public boolean clickMode = false;
	
	public boolean won = false;
	
	public boolean autoPlay = false;
	
	public ProcessedGameData(int seed, int initialMoves) {
		this.seed = seed;
		numberOfMovesNeeded.add(initialMoves);
	}
	
	/**
	 * Difficulty of the game is the initial number of length of the optimal solution.
	 * @return
	 */
	public int getDifficulty() {
		return numberOfMovesNeeded.element();
	}
	
	/**
	 * A wrong move is a move that increases the length of the optimal solution
	 * @return
	 */
	public int getWrongMoves() {
		int count = 0;
		
		ListIterator<Integer> iterator = numberOfMovesNeeded.listIterator();
		int prev = iterator.next();
		
		while (iterator.hasNext()) {
			int curr = iterator.next();
			if (curr != -1 && curr > prev) {
				count ++;
			}
		}
		
		return count;
	}

	/**
	 * A right move is a move that decreases the length of the optimal solution
	 * @return
	 */
	public int getRightMoves() {
		int count = 0;
		
		ListIterator<Integer> iterator = numberOfMovesNeeded.listIterator();
		int prev = iterator.next();
		
		while (iterator.hasNext()) {
			int curr = iterator.next();
			if (curr != -1 && curr < prev) {
				count ++;
			}
		}
		
		return count;
	}
	
	/**
	 * A neutral move is a move that doesn't change the length of the optimal solution.
	 * @return
	 */
	public int getNeutralMoves() {
		int count = 0;
		
		ListIterator<Integer> iterator = numberOfMovesNeeded.listIterator();
		int prev = iterator.next();
		
		while (iterator.hasNext()) {
			int curr = iterator.next();
			if (curr != -1 && curr == prev) {
				count ++;
			}
		}
		
		return count;
	}
	
	/**
	 * Unwinnable move is when the length of the optimal solution is -1.
	 * @return
	 */
	public int getUnwinnableMoves() {
		int count = 0;
		
		ListIterator<Integer> iterator = numberOfMovesNeeded.listIterator();
		
		while (iterator.hasNext()) {
			int curr = iterator.next();
			if (curr == -1) {
				count ++;
			}
		}
		
		return count;
	}
	
	/**
	 * A game was winnable (without using Undo) if the last optimal solution's length
	 * is not -1.
	 * @return
	 */
	public boolean wasWinnableInTheEnd() {
		return numberOfMovesNeeded.getLast() != -1;
	}
	
	
	public static String headerString() {
		return "UserId; Version; " +
				"Seed; ClickMode?; AutoPlay; StartTime; Duration; DisturbType; IsWon?; Valid; Misplaced; Click; " +
				"ClubsInFou; DiamondsIF; HeartsIF; SpadesIF; " +
				"AvgResp; ShoResp; LongResp; AvgResp90; ShoResp90; LongResp90; WrongMoves; NeutralMoves; RightMoves; " +
				"WasWinnable; UnwinnableMoves; All moves";
	}
	
	// Returns all the stored data in CSV format.
	public String toString() {
		StringBuffer row = new StringBuffer();
		row.append(userId).append("; ");
		row.append(version).append("; ");
		row.append(seed).append("; ");
		row.append(clickMode).append("; ");
		row.append(autoPlay).append("; ");
		row.append(startTime).append("; ");
		row.append(duration).append("; ");
		row.append(disturbType).append("; ");
		row.append(won).append("; ");
		row.append(validMoves).append("; ");
		row.append(misplacedMoves).append("; ");
		row.append(clickMoves).append("; ");
		row.append(clubsInFoundations).append("; ");
		row.append(diamondsInFoundations).append("; ");
		row.append(heartsInFoundations).append("; ");
		row.append(spadesInFoundations).append("; ");
		row.append(averageResponse).append("; ");
		row.append(shortestResponse).append("; ");
		row.append(longestResponse).append("; ");
		row.append(averageResponse90).append("; ");
		row.append(shortestResponse90).append("; ");
		row.append(longestResponse90).append("; ");

		row.append(getWrongMoves()).append("; ");
		row.append(getNeutralMoves()).append("; ");
		row.append(getRightMoves()).append("; ");
		row.append(wasWinnableInTheEnd()).append("; ");
		row.append(getUnwinnableMoves()).append("; ");
		
		
		// Initial number of moves:
//		row.append(numberOfMovesNeeded.element()).append("; ");
//		All other numbers:
		while (!numberOfMovesNeeded.isEmpty()) {
			row.append(numberOfMovesNeeded.poll()).append(",");
		}
		row.setLength(row.length()-1);
		return row.toString();
	}
}
