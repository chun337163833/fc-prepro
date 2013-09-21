package hu.durfi.freecell;

public class FcMoveShort {
	public FcMoveShort(char fromType, int fromNum, int numOfCards, char toType, int toNum) {
		this.fromType = fromType;
		this.fromNum = fromNum;
		this.numberOfMovedCards = numOfCards;
		this.toType = toType;
		this.toNum = toNum;
	}
	
	public char fromType;
	public int fromNum;
	public int numberOfMovedCards;
	public char toType;
	public int toNum;
}