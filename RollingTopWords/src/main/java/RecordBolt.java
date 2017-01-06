import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;

import java.util.List;

/**
 * Created by ivensli on 2017/1/6.
 */
public class RecordBolt extends BaseBasicBolt {

    private static final Logger LOG = Logger.getLogger(IntermediateRankingsBolt.class);

    public final void execute(Tuple tuple, BasicOutputCollector collector) {
        List<Object> fields = Lists.newArrayList(tuple.getValues());
        LOG.info("RecordBolt Output: word:" + fields.get(0) + "count:" + fields.get(1));
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {}
}
