package domain;

public class SimulateAction extends TablutAction {

    private double prob;

    public SimulateAction(TablutAction action, double prob) {
        super(action.coordinates, action.pawn);
        this.captured = action.getCaptured();
        this.prob = prob;
    }

    public double getProb() {
        return prob;
    }

    public void setProb(double prob) {
        this.prob = prob;
    }
    
}
