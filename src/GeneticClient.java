import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import aima.core.search.local.Individual;

public class GeneticClient {

    private int[] weights;
    private byte player;
    private long endTime;

    private static final int WEIGHTS_NUMBER = 5;
    private static final int POPULATION_NUMBER = 10;
    private static final int WEIGHTS_LIMIT = 500;
    private static final int MAX_ITERATION = 10;

    public GeneticClient(int[] weights, byte player, int minutes) {
        this.weights = weights;
        this.player = player;
        this.endTime = minutes * 60 * 1000;
    }

    public Metrics run() {
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        Metrics result = new Metrics();
        TablutGame train = new TablutGame(weights);
        TablutGame enemy = new TablutGame(new int[]{0,0,0,0,0});
        MonteCarloTreeSearch<TablutState, TablutAction> mctsTrain = new MonteCarloTreeSearch<>(train, 5);
        MonteCarloTreeSearch<TablutState, TablutAction> mctsEnemy = new MonteCarloTreeSearch<>(enemy, 5);
        TablutState s = new TablutState(TablutState.WHITE);
        int moves = 0;
        while(!s.isWhiteWin() && !s.isBlackWin() && !s.isDraw() && end - start < endTime) {
            TablutAction a;
            if(s.getPlayerTurn() == player) 
                a = mctsTrain.monteCarloTreeSearch(s);
            else
                a = mctsEnemy.monteCarloTreeSearch(s);
            if(a == null)
                break;
            s = s.clone();
            s.makeAction(a);
            System.out.println(s.toString());
            moves++;
            end = System.currentTimeMillis();
        }
        if((s.isWhiteWin() && player == TablutState.WHITE) || (s.isBlackWin() && player == TablutState.BLACK))
            result.setResult(Metrics.WIN);
        else if(s.isDraw())
            result.setResult(Metrics.DRAW);
        else
            result.setResult(Metrics.LOOSE);

        if(player == TablutState.WHITE) {
            result.setPawnsCaptured(TablutState.BLACK_PAWNS - s.getBlackPawns());
            result.setPawnsLost(TablutState.WHITE_PAWNS - s.getWhitePawns());
        }
        else {
            result.setPawnsCaptured(TablutState.WHITE_PAWNS - s.getWhitePawns());
            result.setPawnsLost(TablutState.BLACK_PAWNS - s.getBlackPawns());
        }

        result.setMovesNumber(moves);  
        return result;
    }

    public static void main(String[] args) {
        List<Integer> finiteAlphabet = new ArrayList<>();
        for (int i = -WEIGHTS_LIMIT; i <= WEIGHTS_LIMIT; i++) {
            finiteAlphabet.add(i);
        }
        Genetic g = new Genetic(WEIGHTS_NUMBER, finiteAlphabet, 0.3);
        List<Individual<Integer>> population = getPopulation();
        ArrayList<Integer> weights = new ArrayList<>();
        
        weights.add(TablutState.THRESHOLD, -102);
        weights.add(TablutState.CAPTURED, 106);
        weights.add(TablutState.KING_WHITE, 19);
        weights.add(TablutState.KING_BLACK, -51);
        weights.add(TablutState.KING_EMPTY, -2);
        population.add(0, new Individual<>(weights));
        Individual<Integer> result = g.geneticAlgorithm(population, new Fitness(), MAX_ITERATION);
        for(int i = 0; i < 5; i++) {
            System.out.println("RESULT: " + result.getRepresentation().get(i));
        }
    }

    private static List<Individual<Integer>> getPopulation() {
        List<Individual<Integer>> result = new ArrayList<>();
        for (int i = 0; i < POPULATION_NUMBER; i++) 
            result.add(getIndividualRandom());
        return result;
    }

    private static Individual<Integer> getIndividualRandom() {
        ArrayList<Integer> weights = new ArrayList<>();
        Random rand = new Random();

        weights.add(TablutState.THRESHOLD, -getNearRandom(100));
        weights.add(TablutState.CAPTURED, getNearRandom(100));
        weights.add(TablutState.KING_WHITE, getNearRandom(10));
        weights.add(TablutState.KING_BLACK,  -getNearRandom(50));
        weights.add(TablutState.KING_EMPTY, (rand.nextBoolean() ? 1 : -1) * getNearRandom(0));
        return new Individual<>(weights);
    }

    private static int getNearRandom(int i) {
        Random rand = new Random();
        return i + rand.nextInt(10);
    }
}
