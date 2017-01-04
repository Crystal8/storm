import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import java.io.*;
import java.util.*;

public class ReportBolt extends BaseRichBolt {
    private HashMap<String, Long> counts = null;
    private String output_file;
    FileOutputStream file_out = null;
    private long cc = 0;

    public void prepare(Map config, TopologyContext context, OutputCollector collector) {
        this.counts = new HashMap<String, Long>();
        output_file = config.get("output_file").toString();

        try {
            file_out = new FileOutputStream(output_file, false);
            file_out.write("--- FINAL COUNTS ---\n".getBytes("utf-8"));
        } catch (IOException exp) {
            System.out.println("open output file err!");
        }
    }

    public void execute(Tuple tuple) {
        String word = tuple.getStringByField("word");
        Long count = tuple.getLongByField("count");
        this.counts.put(word, count);
        ++cc;
        if(cc % 1000 == 0) {
            try {
                file_out.write("\n\n------------------------------------------------------------------------\n".getBytes("utf-8"));

                List<String> keys = new ArrayList<String>();
                keys.addAll(this.counts.keySet());
                Collections.sort(keys);
                for (String key : keys) {
                    String str_out = key + " : " + this.counts.get(key) + '\n';
                    file_out.write(str_out.getBytes("utf-8"));
                }
            } catch (IOException exp) {
                System.out.println("write output file err!");
            }
        }
    }

    /*
     该bolt位于末端,所以declareOutputFields为空
    **/
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

    /*
     cleanup方法用来释放bolt占用的资源
     */
    public void cleanup() {
        try {
            System.out.println("--- FINAL COUNTS ---\n");
            List<String> keys = new ArrayList<String>();
            keys.addAll(this.counts.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                String str_out = key + " : " + this.counts.get(key) + '\n';
                System.out.println(str_out);
                file_out.write(str_out.getBytes("utf-8"));
            }
        } catch (IOException exp) {
            System.out.println("IO err!");
        }
    }
}