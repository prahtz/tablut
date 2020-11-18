package genetic;

public class TablutMetrics {
    private int result;
	
	public static final int WIN = 1;
	public static final int DRAW = 0;
	public static final int LOOSE = -1;

    public TablutMetrics() {
        
    }

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}
}
