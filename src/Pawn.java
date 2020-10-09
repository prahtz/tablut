public class Pawn {
    private Coordinates position;
    private byte pawnType;

    public Pawn(byte pawnType, Coordinates position) {
        this.pawnType = pawnType;
        this.position = position;
    }

    public Coordinates getPosition() {
        return position;
    }

    public byte getPawnType() {
        return pawnType;
    }
}
