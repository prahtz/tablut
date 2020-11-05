import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import aima.core.search.local.FitnessFunction;
import aima.core.search.local.GeneticAlgorithm;
import aima.core.search.local.Individual;
import aima.core.util.Tasks;
import aima.core.util.Util;

public class Genetic<A> extends GeneticAlgorithm<A> {

	private final String OUT_PATH = "out/populations.txt";
	private double[] firstFValues;

    public Genetic(int individualLength, Collection<A> finiteAlphabet, double mutationProbability) {
		super(individualLength, finiteAlphabet, mutationProbability);
	}

	public Genetic(int individualLength, Collection<A> finiteAlphabet, double mutationProbability, double[] firstFValues) {
		super(individualLength, finiteAlphabet, mutationProbability);
		this.firstFValues = firstFValues;
	}

    @Override
    public Individual<A> geneticAlgorithm(Collection<Individual<A>> initPopulation, FitnessFunction<A> fitnessFn,
										  Predicate<Individual<A>> goalTest, long maxTimeMilliseconds) {
		Individual<A> bestIndividual = null;
        printPopulation(initPopulation, -1);
		// Create a local copy of the population to work with
        List<Individual<A>> population = new ArrayList<>(initPopulation);
		// Validate the population and setup the instrumentation
		validatePopulation(population);
		updateMetrics(population, 0, 0L);

		long startTime = System.currentTimeMillis();

		// repeat
		int itCount = 0;
		do {
            population = nextGeneration(population, fitnessFn);
            printPopulation(population, itCount);
			bestIndividual = retrieveBestIndividual(population, fitnessFn);

			updateMetrics(population, ++itCount, System.currentTimeMillis() - startTime);
			// until some individual is fit enough, or enough time has elapsed
			if (maxTimeMilliseconds > 0L && (System.currentTimeMillis() - startTime) > maxTimeMilliseconds)
				break;
			if (Tasks.currIsCancelled())
				break;
		} while (!goalTest.test(bestIndividual));

		notifyProgressTrackers(itCount, population);
		// return the best individual in population, according to FITNESS-FN
		return bestIndividual;
    }
    
    @Override
    protected List<Individual<A>> nextGeneration(List<Individual<A>> population, FitnessFunction<A> fitnessFn) {
		List<Individual<A>> newPopulation = new ArrayList<>(population.size());

		double[] fValues = new double[population.size()];
		if(firstFValues != null) {
			fValues = firstFValues;
			for (int i = 0; i < population.size(); i++)
				printFitnessValue(fValues[i], i);
			firstFValues = null;
		}
		else {
			for (int i = 0; i < population.size(); i++) {
				fValues[i] = fitnessFn.apply(population.get(i));
				printFitnessValue(fValues[i], i);
			}
		}
        fValues = Util.normalize(fValues);
		for (int i = 0; i < population.size(); i++) {
			// x <- RANDOM-SELECTION(population, FITNESS-FN)
			Individual<A> x = randomSelection(population, fValues);
			// y <- RANDOM-SELECTION(population, FITNESS-FN)
			Individual<A> y = randomSelection(population, fValues);
			// child <- REPRODUCE(x, y)
			Individual<A> child = reproduce(x, y);
			// if (small random probability) then child <- MUTATE(child)
			if (random.nextDouble() <= mutationProbability) {
				child = mutate(child);
			}
			// add child to new_population
			newPopulation.add(child);
		}
		notifyProgressTrackers(getIterations(), population);
		return newPopulation;
    }
    
    protected Individual<A> randomSelection(List<Individual<A>> population, double[] fValues) {
		Individual<A> selected = population.get(population.size() - 1);
		
		double prob = random.nextDouble();
		double totalSoFar = 0.0;
		for (int i = 0; i < fValues.length; i++) {
			totalSoFar += fValues[i];
			if (prob <= totalSoFar) {
				selected = population.get(i);
				break;
			}
		}
		selected.incDescendants();
		return selected;
    }
    
    @Override
    public Individual<A> retrieveBestIndividual(Collection<Individual<A>> population, FitnessFunction<A> fitnessFn) {
		Individual<A> bestIndividual = null;
		double bestSoFarFValue = Double.NEGATIVE_INFINITY;
		double[] fValues = new double[population.size()];
        int i = 0;
		for (Individual<A> individual : population) {
			double fValue = fitnessFn.apply(individual);
			fValues[i] = fValue;
            printFitnessValue(fValue, i);
            i++;
			if (fValue > bestSoFarFValue) {
				bestIndividual = individual;
				bestSoFarFValue = fValue;
            }
		}
		firstFValues = fValues;
		return bestIndividual;
	}

    private void printFitnessValue(double fValue, int index) {
        try {
            File myObj = new File(OUT_PATH);
            myObj.createNewFile();

            FileWriter myWriter = new FileWriter(myObj, true);
          
            myWriter.write("FValue of index " + index + ": " + fValue + "\n");
            myWriter.close();
          } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
	}

    private void printPopulation(Collection<Individual<A>> population, int itCount) {
        try {
            File myObj = new File(OUT_PATH);
            myObj.createNewFile();

            FileWriter myWriter = new FileWriter(myObj, true);
            if(itCount == -1)
                myWriter.write("Initial population:\n");
            else 
                myWriter.write("Population of iteration number: " + itCount + "\n");
            for(Individual<A> i : population) 
                myWriter.write(i.toString() + "\n");
            myWriter.close();
          } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
	}
}
