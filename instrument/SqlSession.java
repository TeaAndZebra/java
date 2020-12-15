package instrument;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

public class SqlSession {
    private static Logger logger = LogManager.getLogger(SqlSession.class.getName());
    public SqlSessionFactory getSqlSessionFactory() {
        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            logger.error(resource, "conf/mybatis-config.xml occurs something wrong");
        }
        return new SqlSessionFactoryBuilder().build(inputStream);
    }
}
