public class TranspositionEntry 
{
    int value; 
    int depth;
    boolean isExact;

    public TranspositionEntry(int value, int depth, boolean isExact) 
    {
        this.value = value;
        this.depth = depth;
        this.isExact = isExact;
    }
}
