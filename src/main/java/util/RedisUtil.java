package util;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class RedisUtil {
    private static Logger logger = Logger.getLogger(RedisUtil.class);
    @Value("${core.redis.host}")
    private String host;
    @Value("${core.redis.port}")
    private int port;
    @Value("${core.redis.pass}")
    private String password;
    @Value("${core.redis.max_idle}")
    private int max_idle;
    @Value("${core.redis.max_active}")
    private int max_active;
    @Value("${core.redis.max_wait}")
    private int max_wait;
    @Value("${core.redis.test_on_borrow}")
    private boolean test_on_borrow;
    private JedisPool jedisPool;

    /*初始化连接池,在构造函数执行后执行*/
    @PostConstruct
    private void initJedisPool() {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(max_active);
            config.setMaxIdle(max_idle);
            config.setMaxWaitMillis(max_wait);
            config.setTestOnBorrow(test_on_borrow);
            jedisPool = new JedisPool(config, host, port, 3000, password);
            logger.info("初始化Redis连接池,最大允许连接数 :" + max_active);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("初始化连接池失败");
        }
    }

    /*获取连接*/
    public synchronized Jedis getJedis() {
        try {
            int connectionNum = jedisPool.getNumActive();
            /*如果当前Redis资源池连接数已经达到最大值,则不再分配新的连接*/
            if (connectionNum >= max_active) {
                logger.error("当前Redis资源池会话数超过限制");
                return null;
            }
            Jedis jedis = jedisPool.getResource();
            logger.debug("当前活跃连接数量 :" + connectionNum + 1);
            return jedis;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("无法获取redis实例");
            return null;
        }
    }

    /*释放redis连接*/
    public void returnResource(final Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    /*销毁连接池*/
    @PreDestroy
    private void releasePool() {
        logger.info("销毁Redis连接池");
        jedisPool.close();
    }
}
