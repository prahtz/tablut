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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
        return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        Pawn p = (Pawn) obj;
        if(p.getPawnType() == this.pawnType && p.getPosition().equals(this.position))
            return true;
        return false;
    }
}
