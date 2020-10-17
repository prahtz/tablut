public class Metrics {
    private int result;
    private int movesNumber;
    private int pawnsCaptured;
	private int pawnsLost;
	
	public static final int WIN = 1;
	public static final int DRAW = 0;
	public static final int LOOSE = -1;

    public Metrics() {
        
    }

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public int getMovesNumber() {
		return movesNumber;
	}

	public void setMovesNumber(int movesNumber) {
		this.movesNumber = movesNumber;
	}

	public int getPawnsCaptured() {
		return pawnsCaptured;
	}

	public void setPawnsCaptured(int pawnsCaptured) {
		this.pawnsCaptured = pawnsCaptured;
	}

	public int getPawnsLost() {
		return pawnsLost;
	}

	public void setPawnsLost(int pawnsLost) {
		this.pawnsLost = pawnsLost;
	}
}
