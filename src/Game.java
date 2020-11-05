import java.util.LinkedList;

public interface Game<S, A> {
    public double getUtility(S state);
    public boolean goalTest(S state);
    public LinkedList<A> getActions(S state);
	public S getResult(S state, A action);
}
