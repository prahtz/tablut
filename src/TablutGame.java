import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

import javax.management.RuntimeErrorException;

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
        //boolean first = state.isFirstMove();
        //LinkedList<TablutAction> debugResult = state.clone().getBestActionsDebug(first);
        LinkedList<TablutAction> result = state.getBestActionFirst(weights); 
        //int debugCaptures = countCaptures(debugResult);
        int realCaptures = countCaptures(result);
        /*
        if(result.size() != debugResult.size() || debugCaptures != realCaptures) {
            System.out.println("DebugCap: " + debugCaptures);
            System.out.println("RealCap: " + realCaptures);
            System.out.println("first: " + first);
            System.out.println("DEBUG");
            state.printList(debugResult);
            System.out.println("REAL");
            state.printList(result);
            throw new RuntimeException();
        }*/
        return state.getBestActionFirst(weights);
    }

    @Override
    public TablutState getNextState(TablutState state, TablutAction action) {
        TablutState newState = state.clone();
        newState.makeAction(action);
        return newState;
    }

    private int countCaptures(LinkedList<TablutAction> list) {
        int cap = 0;
        for(TablutAction a : list) 
            cap += a.getCaptured().size();
        return cap;
    }

    @Override
    public double getPlayoutResult(TablutState state) {
        state = state.clone();
        while (!state.isWhiteWin() && !state.isBlackWin() && !state.isDraw()) {
            //boolean first = state.isFirstMove();
            //TablutState cloneState = state.clone();
            //LinkedList<TablutAction> debugActions = cloneState.getBestActionsDebug(first);
            LinkedList<TablutAction> actions = state.getSimulatingActions();
            //int debugCaptures = countCaptures(debugActions);
            int realCaptures = countCaptures(actions);
            /*
            if(actions.size() != debugActions.size() || debugCaptures != realCaptures) {
                System.out.println("DebugCap: " + debugCaptures);
                System.out.println("RealCap: " + realCaptures);
                System.out.println("DEBUG - LEGAL SIZE " + cloneState.getLegalActionsDebug().size());
                System.out.println("REAL - LEGAL SIZE: " + state.getLegalActions().size());
                System.out.println("DEBUG");
                state.printList(debugActions);
                System.out.println("REAL");
                state.printList(actions);
                System.out.println(state);
                System.out.println(cloneState);
                throw new RuntimeException();
            }*/
            if (!actions.isEmpty()) {
                TablutAction action = actions.get(ThreadLocalRandom.current().nextInt(actions.size()));
                //TablutAction action = actions.getFirst();
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
