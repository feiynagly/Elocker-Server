package test;

import constant.Constant;
import constant.Operation;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.UnsupportedEncodingException;

public class Test {
    public static void main(String args[]) throws UnsupportedEncodingException {

        JdbcTemplate jdbcTemplate = DbOperationUtils.getJdbcTemplate();
        String sql1 = "insert into t_locker(serial,phoneNum,description,createTime,lastOpenTime,hwType) " +
                "values (?,?,?,?,?,?)";

        String sql2 = "insert into t_manuinfo(serial,hwType,manuDate,description) values(?,?,?,?)";
        String sql3 = "insert into t_log(serial,phoneNum,operation,sTime,description) values(?,?,?,?,?)";

        for (int i = 0; i < 10; i++) {
            String serial = RandomStringUtils.random(8, "abcdefghijklmnopqrstuvwxyz1234567890");
            String phoneNum = "13167017116";
            String description = "智能锁" + (i + 1);
            String createTime = "2019-04-" + (i + 1) + " 14:" + (i + 2) + ":00";
            String lastOpenTime = createTime;
            String hwType = Constant.TYPE_BLE_LOCKER;
            Operation operation = Operation.Open;

            jdbcTemplate.update(sql1, new Object[]{serial, phoneNum, description, createTime, lastOpenTime, hwType});
            jdbcTemplate.update(sql2, new Object[]{serial, hwType, createTime, description});
            jdbcTemplate.update(sql3, new Object[]{serial, phoneNum, operation, createTime, operation});
        }

    }
}

class DbOperationUtils {

    public static JdbcTemplate getJdbcTemplate() {

        // com.alibaba.druid.pool.DruidDataSource
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUsername("root");
        dataSource.setPassword("Superuser001");
        dataSource.setUrl("jdbc:mysql://10.0.10.100:3306/elock?characterEncoding=utf-8");
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");

        // 获取spring的JdbcTemplate
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        // 设置数据源
        jdbcTemplate.setDataSource(dataSource);

        return jdbcTemplate;
    }

}

