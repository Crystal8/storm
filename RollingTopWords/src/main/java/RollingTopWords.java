import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.storm.Config;
import org.apache.storm.testing.TestWordSpout;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

/**
 * Created by ivensli on 2017/1/5.
 */
public class RollingTopWords {
    private static final Logger LOG = Logger.getLogger(RollingTopWords.class);
    private static final int DEFAULT_RUNTIME_IN_SECONDS = 60;
    private static final int TOP_N = 5;

    private final TopologyBuilder builder;
    private final String topologyName;
    private final Config topologyConfig;
    private final int runtimeInSeconds;

    private static final String spoutId = "kafka_input";
    private static final String counterId = "counter";
    private static final String intermediateRankerId = "intermediateRanker";
    private static final String totalRankerId = "finalRanker";

    private RollingTopWords(String topologyName) throws InterruptedException {
        builder = new TopologyBuilder();
        this.topologyName = topologyName;
        topologyConfig = createTopologyConfiguration();
        runtimeInSeconds = DEFAULT_RUNTIME_IN_SECONDS;

        wireTopology();
    }

    private static Config createTopologyConfiguration() {
        Config conf = new Config();
        conf.setDebug(true);
        return conf;
    }

    private void wireTopology() throws InterruptedException {
        MyKafkaConfig kafkaConfig = new MyKafkaConfig();

        builder.setSpout(spoutId, kafkaConfig.getKafkaConfig(), 5);
        builder.setBolt(counterId, new RollingCountBolt(9, 3), 4)
                .shuffleGrouping(spoutId);
        builder.setBolt(intermediateRankerId, new IntermediateRankingsBolt(TOP_N), 4).shuffleGrouping(counterId);
        builder.setBolt(totalRankerId, new RecordBolt()).globalGrouping(intermediateRankerId);
    }

    private void runLocally() throws InterruptedException {
        StormRunner.runTopologyLocally(builder.createTopology(), topologyName, topologyConfig, runtimeInSeconds);
    }

    private void runRemotely() throws Exception {
        StormRunner.runTopologyRemotely(builder.createTopology(), topologyName, topologyConfig);
    }

    public static void main(String[] args) throws Exception {
        PropertyConfigurator.configure("./resources/log4j.properties");
        String topologyName = "slidingWindowCounts";
        if (args.length >= 1) {
            topologyName = args[0];
        }
        boolean runLocally = true;
        if (args.length >= 2 && args[1].equalsIgnoreCase("remote")) {
            runLocally = false;
        }

        LOG.info("Topology name: " + topologyName);
        RollingTopWords rtw = new RollingTopWords(topologyName);
        if (runLocally) {
            LOG.info("Running in local mode");
            rtw.runLocally();
        } else {
            LOG.info("Running in remote (cluster) mode");
            rtw.runRemotely();
        }
    }
}
