import java.util.LinkedList;

public class TablutGame implements MonteCarloGame<TablutState, TablutAction> {
    @Override
    public LinkedList<TablutAction> getActions(TablutState state) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TablutState getNextState(TablutState state, TablutAction action) {
        // TODO Auto-generated method stub
        TablutState newState = state.clone();
        newState.makeAction(action);
        return newState;
    }

    @Override
    public double getPlayoutResult(TablutState state) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getUtility(TablutState state, double result) {
        // TODO Auto-generated method stub
        return 0;
    }
    
}
