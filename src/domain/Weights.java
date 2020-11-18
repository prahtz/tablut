package domain;

public enum Weights {
    STANDARD_ACTION(0), 
    KING_CHECK(1), 
    BLACK_ATTACK(2), 
    WHITE_BORDER(3), 
    CAPTURE(4);

    private int value;

    private Weights(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static Integer[] getWeights() {
        return new Integer[]{5, 30, 30, 50, 75};
    }
}