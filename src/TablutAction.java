import java.util.LinkedList;

public class TablutAction implements Comparable<TablutAction>{
    Coordinates coordinates;
    private LinkedList<Capture> captured;
    Pawn pawn;
    private double value;

    int capturesDiff = 0;
    int lossDiff = 0;
    int kingMovesDiff = 0;
    boolean kingCheckmate = false;
    boolean willBeCaptured = false;

    public TablutAction(Coordinates coordinates, Pawn pawn) {
        this.coordinates = coordinates;
        this.captured = new LinkedList<>();
        this.pawn = pawn;
        this.value = 0;
    }

    public void addCapture(Capture capture) {
        if(!captured.contains(capture))
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
        result += "(" + coordinates.row + ", " + coordinates.column + ") ";
        if(!captured.isEmpty()) {
            result += "-- Captures: ";
        }
        for(Capture c : captured) {
            result += c.toString() + " ";
        }
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

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public int compareTo(TablutAction o) {
        if(o.getValue() > value)
            return 1;
        else if(o.getValue() < value)
            return -1;
        return 0;
    }

    public boolean isWillBeCaptured() {
        return willBeCaptured;
    }

    public void setWillBeCaptured(boolean willBeCaptured) {
        this.willBeCaptured = willBeCaptured;
    }

    @Override
    public int hashCode() {
        return coordinates.hashCode() * 100 + pawn.position.hashCode();
    }
}
