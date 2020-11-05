import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

import aima.core.util.Util;


public class TablutGame implements MonteCarloGame<TablutState, TablutAction> {
    private final double WHITE_WIN = 0;
    private final double BLACK_WIN = 1;
    private final double DRAW = 0.5;
    private final double ABORTED = -1;

    private final double WIN_WEIGHT = 1;
    private final double LOOSE_WEIGHT = 0;
    private final double NULL_WEIGHT = -1;

    private final int MAX_MOVES = 200;

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
        LinkedList<SimulateAction> actions = new LinkedList<>();
        byte player = state.getPlayerTurn();
        boolean abortSimulation = false;
        int moves = 0;
        while (!state.isWhiteWin() && !state.isBlackWin() && !state.isDraw() && !abortSimulation) {
            actions = state.getSimulatingActions();
            if (!actions.isEmpty()) {
                double[] probDist = new double[actions.size()];
                int i = 0;
                for(SimulateAction sa : actions) {
                    probDist[i] = sa.getProb();
                    i++;
                }

                probDist = Util.normalize(probDist);
                double prob = ThreadLocalRandom.current().nextDouble();
                double totalSoFar = 0.0;
                TablutAction action = actions.getLast();
                for (i = 0; i < probDist.length; i++) {
                    totalSoFar += probDist[i]; 
                    if (totalSoFar <= prob) {
                        action = actions.get(i);
                        break;
                    }
		        }
                state = state.copySimulation();
                state.makeAction(action);
                if(moves >= MAX_MOVES)
                    abortSimulation = true;
                moves++;
            }
            else
                break;
        }

        double result = DRAW;
        if (state.isWhiteWin())
            result = WHITE_WIN;
        else if (state.isBlackWin())
            result = BLACK_WIN;
        //else if(abortSimulation)
            //result = ABORTED;
        else if(actions.isEmpty() || abortSimulation) {
            if(player == TablutState.WHITE) 
                result = BLACK_WIN;
            else
                result = WHITE_WIN;
        }

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
        if(result == ABORTED)
            return NULL_WEIGHT;
        return WIN_WEIGHT;
    }

    @Override
    public double selectionPolicyValue(MonteCarloNode<TablutState, TablutAction> node) {
        double C = Math.sqrt(2);
        return (node.getUtility() / node.getPlayoutsNumber())
                + C * Math.sqrt(Math.log(node.getParent().getPlayoutsNumber()) / node.getPlayoutsNumber());
    }
}
