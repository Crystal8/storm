/**
 * Created by ivensli on 2017/1/3.
 */

import org.apache.log4j.Logger;

public class LogTest{

    final static Logger logger = Logger.getLogger(LogTest.class);

    public static void main(String[] args) {

        LogTest obj = new LogTest();
        obj.runMe("hahaha");

    }

    private void runMe(String parameter){

        if(logger.isDebugEnabled()){
            logger.debug("This is debug : " + parameter);
        }

        if(logger.isInfoEnabled()){
            logger.info("This is info : " + parameter);
        }

        logger.warn("This is warn : " + parameter);
        logger.error("This is error : " + parameter);
        logger.fatal("This is fatal : " + parameter);

    }

}