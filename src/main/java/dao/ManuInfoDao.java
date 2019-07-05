package dao;

import model.ManuInfo;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;


@Repository
public class ManuInfoDao {
    private static Logger logger = Logger.getLogger(ManuInfo.class);
    private final String COUNT_PRODUCT = "select count(*) from t_manuinfo where serial = ?";
    private final String GET_MANUINFO_BY_SERIAL = "select * from t_manuinfo where serial =?";
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ManuInfo getManuInfoBySerial(String serial) {
        ManuInfo manuInfo = new ManuInfo();
        try {
            jdbcTemplate.query(GET_MANUINFO_BY_SERIAL, new Object[]{serial}, new RowCallbackHandler() {

                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    manuInfo.setDescription(rs.getString("description"));
                    manuInfo.setHwType(rs.getString("hwType"));
                    manuInfo.setSerial(rs.getString("serial"));
                    manuInfo.setManuDate(rs.getString("manuDate"));
                }
            });
        } catch (Exception e) {
            logger.error("Failed to get ManuInfo about " + serial + " , SQL query error");
        }
        return manuInfo;
    }

    public boolean existSerial(String serial) {
        int count = 0;
        try {
            count = jdbcTemplate.queryForObject(COUNT_PRODUCT, new Object[]{serial}, Integer.class);
        } catch (Exception e) {
            logger.error("Count product by serial error ,serial: " + serial);
        }
        return count > 0;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
