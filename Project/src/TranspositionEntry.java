public class TranspositionEntry 
{
    int value; // The evaluation of the state
    int depth; // The depth at which this value was computed
    boolean isExact; // Whether this value is exact (not a bound)

    public TranspositionEntry(int value, int depth, boolean isExact) {
        this.value = value;
        this.depth = depth;
        this.isExact = isExact;
    }
}
