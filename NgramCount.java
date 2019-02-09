
public class NgramCount implements Comparable<NgramCount> {

    public String ngram;
    public int count;

    public NgramCount(String name, int count)
    {
        this.count = count;
        this.ngram = name;
    }

    public String getNgram() {
        return ngram;
    }

    public int getCount() {
        return count;
    }

    public int compareTo(NgramCount o) {
        if(this.count == o.count) 
            return o.ngram.compareTo(this.ngram);
        return this.count-o.count;

    }

    public void incrementCount() {
        this.count++;
    }
}
