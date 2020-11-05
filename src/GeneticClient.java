import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import aima.core.search.local.Individual;

public class GeneticClient {

    private int[] weights;
    private byte player;
    private long endTime;

    private static final int WEIGHTS_NUMBER = TablutState.Weights.values().length;
    private static final int POPULATION_NUMBER = 7;
    private static final int WEIGHTS_LIMIT = 50;
    private static final int MAX_ITERATION = 4;
    private static final int MAX_MOVES = 60;
    private static final String IN_PATH = "out/first.csv";
    private static double[] firstFValues;

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
        TablutGame enemy = new TablutGame(new int[] { 0, 0, 0, 0, 0 });
        MonteCarloTreeSearch<TablutState, TablutAction> mctsTrain = new MonteCarloTreeSearch<>(train, 10);
        MonteCarloTreeSearch<TablutState, TablutAction> mctsEnemy = new MonteCarloTreeSearch<>(enemy, 10);
        TablutState s = new TablutState(TablutState.WHITE);
        int moves = 0;
        while (!s.isWhiteWin() && !s.isBlackWin() && !s.isDraw() && end - start < endTime && moves < MAX_MOVES) {
            TablutAction a;
            if (s.getPlayerTurn() == player)
                a = mctsTrain.monteCarloTreeSearch(s);
            else
                a = mctsEnemy.monteCarloTreeSearch(s);
            if (a == null)
                break;
            s = s.clone();
            s.makeAction(a);
            // System.out.println(s.toString());
            moves++;
            end = System.currentTimeMillis();
        }
        if ((s.isWhiteWin() && player == TablutState.WHITE) || (s.isBlackWin() && player == TablutState.BLACK))
            result.setResult(Metrics.WIN);
        else if (s.isDraw())
            result.setResult(Metrics.DRAW);
        else
            result.setResult(Metrics.LOOSE);

        if (player == TablutState.WHITE) {
            result.setPawnsCaptured(TablutState.BLACK_PAWNS - s.getBlackPawns());
            result.setPawnsLost(TablutState.WHITE_PAWNS - s.getWhitePawns());
        } else {
            result.setPawnsCaptured(TablutState.WHITE_PAWNS - s.getWhitePawns());
            result.setPawnsLost(TablutState.BLACK_PAWNS - s.getBlackPawns());
        }

        result.setMovesNumber(moves);
        return result;
    }

    public static void main(String[] args) {
        List<Integer> finiteAlphabet = new ArrayList<>();
        for (int i = 0; i <= WEIGHTS_LIMIT; i++) {
            finiteAlphabet.add(i);
        }
        
        List<Individual<Integer>> population = getPopulationFromFile(IN_PATH);
        Genetic<Integer> g = new Genetic<>(WEIGHTS_NUMBER, finiteAlphabet, 0.3, firstFValues);
        //List<Individual<Integer>> population = getPopulation();
        Individual<Integer> result = g.geneticAlgorithm(population, new Fitness(), MAX_ITERATION);
        for (int i = 0; i < 5; i++) {
            System.out.println("RESULT: " + result.getRepresentation().get(i));
        }
    }

    private static List<Individual<Integer>> getPopulationFromFile(String path) {
        double[] fValues = new double[POPULATION_NUMBER];
        List<Individual<Integer>> result = new ArrayList<>();
        int i = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            while (true) {
                String line = br.readLine();
                if(line == null) break;
                String[] fields = line.split(";");
                ArrayList<Integer> weights = new ArrayList<>(WEIGHTS_NUMBER);
                for(int j = 0; j < WEIGHTS_NUMBER; j++) 
                    weights.add(Integer.parseInt(fields[j]));
                result.add(new Individual<>(weights));
                fValues[i] = Double.parseDouble(fields[WEIGHTS_NUMBER]);
                i++;
            }
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        firstFValues = fValues;
        return result;
    }

    private static List<Individual<Integer>> getPopulation() {
        List<Individual<Integer>> result = new ArrayList<>();
        result.add(getEmpiricalIndividual());
        for (int i = 1; i < POPULATION_NUMBER; i++)
            result.add(getIndividualRandom());
        return result;
    }

    private static Individual<Integer> getEmpiricalIndividual() {
        ArrayList<Integer> weights = new ArrayList<>(WEIGHTS_NUMBER);
        weights.add(TablutState.Weights.TOTAL_DIFF.value(), 10);
        weights.add(TablutState.Weights.ACTIVE_CAPTURES.value(), 15);
        weights.add(TablutState.Weights.KING_MOVES_DIFF.value(), 5);
        weights.add(TablutState.Weights.WILL_BE_CAPTURED.value(), 15);
        weights.add(TablutState.Weights.KING_CHECKMATE.value(), 20);    
        return new Individual<>(weights);
    }

    private static Individual<Integer> getIndividualRandom() {
        ArrayList<Integer> weights = new ArrayList<>(WEIGHTS_NUMBER);
        weights.add(TablutState.Weights.TOTAL_DIFF.value(), getNearRandom(10));
        weights.add(TablutState.Weights.ACTIVE_CAPTURES.value(), getNearRandom(15));
        weights.add(TablutState.Weights.KING_MOVES_DIFF.value(), getNearRandom(5));
        weights.add(TablutState.Weights.WILL_BE_CAPTURED.value(), getNearRandom(15));
        weights.add(TablutState.Weights.KING_CHECKMATE.value(), getNearRandom(20));    
        return new Individual<>(weights);
    }

    private static int getNearRandom(int i) {
        int offset = 50 - i;
        int result = -1;
        while(result < 0) {
            Random rand = new Random();
            int sign = rand.nextInt(2) == 1 ? 1 : -1; 
            result = i + sign * rand.nextInt(offset);
        }
        return result;
    }
}
