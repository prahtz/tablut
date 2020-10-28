import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

public class TablutGame implements MonteCarloGame<TablutState, TablutAction> {
    private final double WHITE_WIN = 0;
    private final double BLACK_WIN = 1;

    private final double DRAW = 0.5;
    private final double WIN_WEIGHT = 1;
    private final double LOOSE_WEIGHT = 0;

    private int[] weights;

    public TablutGame(int[] weights) {
        this.weights = weights;
    }

    @Override
    public LinkedList<TablutAction> getActions(TablutState state) {
        return state.getBestActionFirst(weights);
    }

    @Override
    public TablutState getNextState(TablutState state, TablutAction action) {
        TablutState newState = state.clone();
        newState.makeAction(action);
        return newState;
    }

    @Override
    public double getPlayoutResult(TablutState state) {
        state = state.clone();
        while (!state.isWhiteWin() && !state.isBlackWin() && !state.isDraw()) {
            LinkedList<TablutAction> actions = state.getSimulatingActions();
            if (!actions.isEmpty()) {
                TablutAction action = actions.get(ThreadLocalRandom.current().nextInt(actions.size()));
                state = state.copySimulation();
                state.makeAction(action);
            }
        }
        double result = DRAW;
        if (state.isWhiteWin())
            result = WHITE_WIN;
        else if (state.isBlackWin())
            result = BLACK_WIN;
        return result;
    }

    @Override
    public double getUtility(TablutState state, double result) {
        byte playerTurn = state.getPlayerTurn();
        
        if (result == DRAW)
            return result;
        if (result == DRAW || (result == WHITE_WIN && playerTurn == TablutState.WHITE)
                || (result == BLACK_WIN && playerTurn == TablutState.BLACK))
            return LOOSE_WEIGHT;
        return WIN_WEIGHT;
    }

    @Override
    public double selectionPolicyValue(MonteCarloNode<TablutState, TablutAction> node) {
        double C = Math.sqrt(2);
        return (node.getUtility() / node.getPlayoutsNumber())
                + C * Math.sqrt(Math.log(node.getParent().getPlayoutsNumber()) / node.getPlayoutsNumber());
    }
}
