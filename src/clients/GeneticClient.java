package clients;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import aima.core.search.local.Individual;
import domain.*;
import genetic.*;

public class GeneticClient {

    private Integer[] weights;
    private byte player;
    private long endTime;

    private static final int WEIGHTS_NUMBER = Weights.values().length;
    private static final int POPULATION_NUMBER = 7;
    private static final int WEIGHTS_LIMIT = 100;
    private static final int MAX_ITERATION = 4;
    private static final int MAX_MOVES = 60;
    private static final String IN_PATH = "out/";

    private static String inFile;
    private static boolean file = false;
    private static double[] firstFValues;

    public GeneticClient(Integer[] weights, byte player, int minutes) {
        this.weights = weights;
        this.player = player;
        this.endTime = minutes * 60 * 1000;
    }

    public TablutMetrics run() {
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        TablutMetrics result = new TablutMetrics();
        TablutGame train = new TablutGame(weights);
        TablutGame enemy = new TablutGame(new Integer[] {1, 1, 1, 1, 1});
        TablutSearch mctsTrain = new TablutSearch(train, 10);
        TablutSearch mctsEnemy = new TablutSearch(enemy, 10);
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
            moves++;
            end = System.currentTimeMillis();
        }
        if ((s.isWhiteWin() && player == TablutState.WHITE) || (s.isBlackWin() && player == TablutState.BLACK))
            result.setResult(TablutMetrics.WIN);
        else if (s.isDraw())
            result.setResult(TablutMetrics.DRAW);
        else
            result.setResult(TablutMetrics.LOOSE);
        return result;
    }

    public static void main(String[] args) {
        if(args.length > 2) {
            System.out.println("Too many arguments");
            System.exit(-1);
        }

        if(args.length >= 1) {
            if(args[0].toLowerCase().equals("-f"))
                file = true;
            else {
                System.out.println("Invalid argument");
                System.exit(-1);
            } 
            if(args.length == 2)
                inFile = args[1];
            else {
                System.out.println("Missing filename");
                System.exit(-1);
            }
        }
        
        List<Integer> finiteAlphabet = new ArrayList<>();
        for (int i = 1; i <= WEIGHTS_LIMIT; i++)
            finiteAlphabet.add(i);
        
        List<Individual<Integer>> population;
        if(file)
            population = getPopulationFromFile(IN_PATH + inFile);
        else 
            population = getPopulation();
        TablutGenetic<Integer> g = new TablutGenetic<>(WEIGHTS_NUMBER, finiteAlphabet, 0.3, firstFValues);
        Individual<Integer> result = g.geneticAlgorithm(population, new TablutFitness(), MAX_ITERATION);
        for (int i = 0; i < 5; i++) 
            System.out.println("RESULT: " + result.getRepresentation().get(i));
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
        return new Individual<>(Arrays.asList(Weights.getWeights()));
    }

    private static Individual<Integer> getIndividualRandom() {
        List<Integer> weights = new ArrayList<>(WEIGHTS_NUMBER);
        weights.add(Weights.STANDARD_ACTION.value(), getRandom(10));
        weights.add(Weights.KING_CHECK.value(), getRandom(WEIGHTS_LIMIT));
        weights.add(Weights.BLACK_ATTACK.value(), getRandom(WEIGHTS_LIMIT));
        weights.add(Weights.WHITE_BORDER.value(), getRandom(WEIGHTS_LIMIT));
        weights.add(Weights.CAPTURE.value(), getRandom(WEIGHTS_LIMIT));    
        return new Individual<>(weights);
    }

    private static int getRandom(int limit) {
        int result = 0;
        Random rand = new Random();
        while(result < 1) {
            result = 1 + rand.nextInt(limit);
        }
        return result;
    }
}
