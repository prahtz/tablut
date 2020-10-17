public class Capture {
    private Pawn captured;
    private Pawn blocker;

    public Capture(Pawn captured) {
        this.captured = captured;
    }

    public Pawn getCaptured() {
        return captured;
    }

    public void setCaptured(Pawn captured) {
        this.captured = captured;
    }

    public Pawn getBlocker() {
        return blocker;
    }

    public void setBlocker(Pawn blocker) {
        this.blocker = blocker;
    }
    
    
}
