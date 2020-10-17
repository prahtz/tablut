import java.util.LinkedList;

public class TablutAction {
    private Coordinates coordinates;
    private LinkedList<Capture> captured;
    private Pawn pawn;

    public TablutAction(Coordinates coordinates, Pawn pawn) {
        this.coordinates = coordinates;
        this.captured = new LinkedList<>();
        this.pawn = pawn;
    }

    public void addCapture(Capture capture) {
        captured.add(capture);
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }
    
    public Pawn getPawn() {
        return pawn;
    }

    @Override
    public String toString() {
        byte pawnType = pawn.getPawnType();
        String result = "";
        if(pawnType == TablutState.BLACK)
            result = "BLACK: ";
        else if(pawnType == TablutState.WHITE) 
            result = "WHITE: ";
        else
            result = "KING: ";
        result += "(" + pawn.getPosition().getRow() + ", " + pawn.getPosition().getColumn() + ") - > ";
        result += "(" + coordinates.getRow() + ", " + coordinates.getColumn() + ")";
        return result;
    }

	public LinkedList<Capture> getCaptured() {
		return captured;
	}
}
