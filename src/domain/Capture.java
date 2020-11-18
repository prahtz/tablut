package domain;

public class Capture {
    private Pawn captured;

    public Capture(Pawn captured) {
        this.captured = captured;
    }

    public Pawn getCaptured() {
        return captured;
    }

    public void setCaptured(Pawn captured) {
        this.captured = captured;
    }

    @Override
    public String toString() {
        return captured.toString();
    }
    @Override
    public boolean equals(Object obj) {
        Capture c = (Capture) obj;
        return c.getCaptured().position.equals(captured.position);
    }

    @Override
    public int hashCode() {
        return captured.position.hashCode();
    }
}
