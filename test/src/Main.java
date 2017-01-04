/**
 * Created by ivensli on 2016/12/21.
 */

import java.io.File;

public class Main {
    public static void main(String[] args) {
        File old_name = new File("E:\\storm\\wordcount\\input\\test.txt");
        File new_name = new File("E:\\storm\\wordcount\\input\\test.xxx");
        old_name.renameTo(new_name);
    }
}
