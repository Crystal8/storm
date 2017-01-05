/**
 * Created by ivensli on 2017/1/4.
 */

import org.apache.storm.kafka.BrokerHosts;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.kafka.KafkaSpout;

import java.util.UUID;

public class MyKafkaConfig {
    private static final String default_brokerZkStr = "localhost:2181/kafka_test";
    private static final String default_brokerZkPath = "/brokers";
    private static final String default_topic_name = "topic_kafka_storm_test";
    private SpoutConfig spoutConfig;

    public MyKafkaConfig() {
        BrokerHosts hosts = new ZkHosts(default_brokerZkStr, default_brokerZkPath);
        spoutConfig = new SpoutConfig(hosts, default_topic_name, "/" + default_topic_name,
                UUID.randomUUID().toString());
        spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
    }

    public KafkaSpout getKafkaConfig() {
        return new KafkaSpout(spoutConfig);
    }
}
