import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.apache.storm.Config;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.TupleUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ivensli on 2017/1/6.
 */
public class IntermediateRankingsBolt extends BaseBasicBolt {

    private static final long serialVersionUID = 4931640198501530202L;
    private static final Logger LOG = Logger.getLogger(IntermediateRankingsBolt.class);

    private static final int DEFAULT_COUNT = 10;
    private static final int DEFAULT_EMIT_FREQUENCY_IN_SECONDS = 60;

    private final int emitFrequencyInSeconds;
    private final int top_n;

    private final List<RankNode> rankedItems = Lists.newArrayList();

    public IntermediateRankingsBolt() {
        this(DEFAULT_COUNT, DEFAULT_EMIT_FREQUENCY_IN_SECONDS);
    }

    public IntermediateRankingsBolt(int topN) {
        this(topN, DEFAULT_EMIT_FREQUENCY_IN_SECONDS);
    }

    public IntermediateRankingsBolt(int topN, int emitFrequencyInSeconds) {
        if (topN < 1) {
            throw new IllegalArgumentException("topN must be >= 1 (you requested " + topN + ")");
        }
        if (emitFrequencyInSeconds < 1) {
            throw new IllegalArgumentException(
                    "The emit frequency must be >= 1 seconds (you requested " + emitFrequencyInSeconds + " seconds)");
        }
        top_n = topN;
        this.emitFrequencyInSeconds = emitFrequencyInSeconds;
    }

    public final void execute(Tuple tuple, BasicOutputCollector collector) {
        if (TupleUtils.isTick(tuple)) {
            LOG.debug("Received tick tuple, triggering emit of current rankings");
            emitRankings(collector);
        } else {
            updateRankingsWithTuple(tuple);
        }
    }

    private void emitRankings(BasicOutputCollector collector) {
        collector.emit(new Values(rankedItems)); //may have problem!!
        LOG.debug("Rankings: " + rankedItems.toString());
    }

    private void updateRankingsWithTuple(Tuple tuple) {
        List<Object> fields = Lists.newArrayList(tuple.getValues());
        RankNode node = new RankNode();
        node.obj = fields.remove(0);
        node.count = (Long) fields.remove(0);

        //replace old
        Integer index = 0;
        for (RankNode r : rankedItems) {
            if (r.obj.equals(node.obj)) {
                rankedItems.set(index, node);
                break;
            }
            ++index;
        }

        //add new
        if (index+1 >= rankedItems.size())
            rankedItems.add(node);

        //sort
        Collections.sort(rankedItems);
        Collections.reverse(rankedItems);

        //shrink
        if (rankedItems.size() > this.top_n)
            rankedItems.remove(top_n); //remove the last one
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("intermediate_rankings"));
    }

    public Map<String, Object> getComponentConfiguration() {
        Map<String, Object> conf = new HashMap<String, Object>();
        conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, emitFrequencyInSeconds);
        return conf;
    }
}
