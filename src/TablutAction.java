import java.util.LinkedList;

public class TablutAction {
    Coordinates coordinates;
    private LinkedList<Capture> captured;
    Pawn pawn;

    public TablutAction(Coordinates coordinates, Pawn pawn) {
        this.coordinates = coordinates;
        this.captured = new LinkedList<>();
        this.pawn = pawn;
    }

    public void addCapture(Capture capture) {
        captured.add(capture);
    }

    public boolean removeCapture(Capture capture) {
        return captured.remove(capture);
    }

    public LinkedList<Capture> getCaptured() {
		return captured;
    }
    
    public TablutAction copy() {
        TablutAction action = new TablutAction(coordinates, pawn);
        for(Capture c : captured) 
            action.addCapture(c);
        return action;
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
        result += "(" + pawn.position.row + ", " + pawn.position.column + ") - > ";
        result += "(" + coordinates.row + ", " + coordinates.column + ")";
        return result;
    }

    @Override
	public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        TablutAction a = (TablutAction) obj;
        if(a.coordinates.equals(this.coordinates) && a.pawn.equals(this.pawn))
            return true;
        return false;
    } 
}
