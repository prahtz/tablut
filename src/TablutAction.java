public class TablutAction {
    private Coordinates coordinates;
    private Pawn pawn;

    public TablutAction(Coordinates coordinates, Pawn pawn) {
        this.coordinates = coordinates;
        this.pawn = pawn;
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
        result += "(" + coordinates.getRow() + ", " + coordinates.getColumn() + ")\n";
        return result;
    }
}
