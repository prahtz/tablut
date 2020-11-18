package montecarlo;

import java.util.LinkedList;

public interface MonteCarloGame<S, A> {
	double getPlayoutResult(S state);
	double getUtility(S state, double result);
	public LinkedList<A> getActions(S state);
	public S getNextState(S state, A action);
	double selectionPolicyValue(MonteCarloNode<S, A> child);
}
