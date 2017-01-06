
/**
 * Created by ivensli on 2017/1/6.
 */
public class RankNode implements Comparable<RankNode> {
    public Object obj;
    public Long count;

    public int compareTo(RankNode other) {
        if (this.count > other.count)
            return 1;
        else if (this.count < other.count)
            return -1;
        else
            return 0;
    }
}
