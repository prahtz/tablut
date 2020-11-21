package domain;

public class Quadrant implements BoardSet{
    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static final int THIRD = 2;
    public static final int FOURTH = 3;

    public static final Quadrant[] quadrants = initQuadrants();

    private int quadrantNumber;

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
    
    public static Quadrant getQuadrant(Coordinates coordinates) {
        if(coordinates.row == 4 || coordinates.column == 4)
            return null;
        for(Quadrant q : quadrants) 
            if(q.contains(coordinates))
                return q;
        return null;
    }
}
