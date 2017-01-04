import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.io.*;
import java.util.Map;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


public class SentenceSpout extends BaseRichSpout {

    private SpoutOutputCollector collector;
    private ConcurrentHashMap<UUID, Values> pending;

    private String input_file_dir;

    private static AtomicLong count = new AtomicLong();
    private long ID = 0;

    /*
     声明spout会发射一个数据流，其中的tuple包含一个字段sentence
     */
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("sentence"));
    }

    /*
    spout初始化时调用这个方法
    map包含storm配置信息
    TopologyContext对象提供了topology中组件的信息
    SpoutOutputCollector对象提供了发射tuple的方法
     */
    public void open(Map config, TopologyContext context, SpoutOutputCollector collector) {
        input_file_dir = config.get("input_file_dir").toString();

        this.ID = count.addAndGet(1);
        this.collector = collector;
        this.pending = new ConcurrentHashMap<UUID, Values>();

    }

    /*
     Storm通过调用这个方法向输出的collector发射tuple
    */
    public void nextTuple() {
        LinkedList<File> file_list = new LinkedList<File>();
        File file_dir = new File(input_file_dir);
        if (file_dir.exists()) {
            File[] files = file_dir.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    //do nothing.
                } else {
                    try {
                        String file_name = f.getName();
                        int name_len = file_name.length();

                        String file_type = file_name.substring(name_len - 3, name_len);

                        if (!file_type.equalsIgnoreCase("txt")) {
                            continue;
                        }

                        BufferedReader reader = new BufferedReader(new FileReader(f));
                        String read_in = null;
                        while ((read_in = reader.readLine()) != null) {
                            UUID msgId = UUID.randomUUID();
                            Values val = new Values(read_in);
                            this.pending.put(msgId, val);
                            this.collector.emit(val, msgId);
                            Utils.sleep(10);
                        }
                        reader.close();

                        if(f.delete()) {
                            System.out.printf("file delete fail.");
                        }
                        /*
                        String renameto = f.getPath();
                        String tmp = renameto.substring(0, renameto.lastIndexOf(".")) + ".xxx";
                        File rename_to_file = new File(tmp);
                        f.renameTo(rename_to_file);
                        */
                    } catch (IOException exp) {
                        System.out.println("file error.");
                    }
                }
            }
        } else {
            System.out.println("file_dir not exist, file_dir=" + input_file_dir);
        }
    }

    @Override
    public void ack(Object msgId) {
        this.pending.remove(msgId);
    }

    @Override
    public void fail(Object msgId) {
        this.collector.emit(this.pending.get(msgId), msgId);
    }
}