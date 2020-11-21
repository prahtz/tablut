package domain;

public class Side implements BoardSet{
    public static final int ROWS_NUMBER = 2;
    public static final int Q_NUMBER = 2;

    public static final int UP = 0;
    public static final int RIGHT = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;

    public static final Side[] sides = initSides();

    private int sideType;

    private Side(int sideType) {
        this.sideType = sideType;
    }

    private static Side[] initSides() {
        Side[] sides = new Side[4];
        sides[UP] = new Side(UP);
        sides[RIGHT] = new Side(RIGHT);
        sides[DOWN] = new Side(DOWN);
        sides[LEFT] = new Side(LEFT);
        return sides;
    }

	public static Side getSideFromMiddle(Coordinates coord) {
        int r = coord.row - 4;
        int c = coord.column - 4;
        if(r == 0) {
            if(c > 0)
                return sides[RIGHT];
            else if (c < 0)
                return sides[LEFT];
        }
        else if(c == 0) {
            if(r > 0)
                return sides[DOWN];
            else if(r < 0)
                return sides[UP];
        }
		return null;
    }

    public boolean contains(Coordinates coord) {
        int r = coord.row - 4;
        int c = coord.column - 4;
        switch(sideType) {
            case UP:
                return r <= 0;
            case RIGHT:
                return c >= 0;
            case DOWN:
                return r >= 0;
            case LEFT:
                return c <= 0;
        }
        return false;
	}
}
