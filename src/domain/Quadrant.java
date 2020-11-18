package domain;

public class Quadrant {
    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static final int THIRD = 2;
    public static final int FOURTH = 3;
    public static final int S_NUMBER = 2;

    public static final Quadrant[] quadrants = initQuadrants();

    private int quadrantNumber;
    private Side[] sides = new Side[S_NUMBER];

    private Quadrant(int quadrantNumber) {
        this.quadrantNumber = quadrantNumber;
    }

    private static Quadrant[] initQuadrants() {
        Quadrant[] quadrants = new Quadrant[4];
        quadrants[FIRST] = new Quadrant(FIRST);
        quadrants[SECOND] = new Quadrant(SECOND);
        quadrants[THIRD] = new Quadrant(THIRD);
        quadrants[FOURTH] = new Quadrant(FOURTH);
        return quadrants;
    }

	public void addSide(Side side) {
        if(sides[0] != null)
            sides[0] = side;
        else if(sides[1] != null)
            sides[1] = side;
    }
    
    public boolean contains(Coordinates coord) {
        int r = coord.row - 4;
        int c = coord.column - 4;
        switch(this.quadrantNumber) {
            case FIRST:
                return r <= 0 && c >= 0;
            case SECOND:
                return r <= 0 && c <= 0;
            case THIRD:
                return r >= 0 && c <= 0;
            case FOURTH:
                return r >= 0 && c >= 0;
        }
        return false;
    }

	public static boolean isOnQuadrant(Coordinates coordinates, Quadrant q) {
		return false;
	}
}
