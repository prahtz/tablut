import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

public class TablutGame implements MonteCarloGame<TablutState, TablutAction> {
    private final double WHITE_WIN = 0;
    private final double BLACK_WIN = 1;
    
    private final double DRAW = 0.5;

    private final double WIN_WEIGHT = 1;
    private final double LOOSE_WEIGHT = 0;
    @Override
    public LinkedList<TablutAction> getActions(TablutState state) {
        // TODO Auto-generated method stub
        return state.getLegalActions();
    }

    @Override
    public TablutState getNextState(TablutState state, TablutAction action) {
        TablutState newState = state.clone();
        newState.makeAction(action);
        return newState;
    }

    @Override
    public double getPlayoutResult(TablutState state) {
        // TODO Auto-generated method stub
        while(!state.isWhiteWin() && !state.isBlackWin()) {
            LinkedList<TablutAction> actions = state.getLegalActions();
            if(!actions.isEmpty()) {
                TablutAction action = actions.get(ThreadLocalRandom.current().nextInt(actions.size()));
                state = state.clone();
                state.makeAction(action);
            } 
        }
        double result = DRAW;
        if(state.isWhiteWin())
            result = WHITE_WIN;
        else if(state.isBlackWin())
            result = BLACK_WIN;
        return result;
    }

    @Override
    public double getUtility(TablutState state, double result) {
        byte playerTurn = state.getPlayerTurn();
        if(result == DRAW)
            return result;
        if((result == WHITE_WIN && playerTurn == TablutState.WHITE)
            || (result == BLACK_WIN && playerTurn == TablutState.BLACK))
            return LOOSE_WEIGHT;
        return WIN_WEIGHT;
    }
    
}
