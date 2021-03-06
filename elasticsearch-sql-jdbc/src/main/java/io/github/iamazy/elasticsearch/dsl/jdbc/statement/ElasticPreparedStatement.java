package io.github.iamazy.elasticsearch.dsl.jdbc.statement;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.iamazy.elasticsearch.dsl.jdbc.ElasticConnection;
import io.github.iamazy.elasticsearch.dsl.jdbc.cons.JdbcConstants;
import io.github.iamazy.elasticsearch.dsl.jdbc.result.ElasticResultSetMetaData;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * @author iamazy
 * @date 2019/12/21
 **/
public class ElasticPreparedStatement extends AbstractFeatureNotSupportedPreparedStatement {

    private Map<Integer, SqlParam> paramMap = Maps.newHashMap();

    private String sql;

    private boolean scroll = false;

    public ElasticPreparedStatement(ElasticConnection connection, String sql) {
        super(connection);
        this.sql = sql;
    }

    public ElasticPreparedStatement(ElasticConnection connection, String sql, int resultSetType, int resultSetConcurrency) {
        super(connection);
        this.sql = sql;
        if (resultSetType == ResultSet.TYPE_SCROLL_INSENSITIVE && resultSetConcurrency == ResultSet.CONCUR_READ_ONLY) {
            this.scroll = true;
        }
    }

    @Override
    public boolean execute() throws SQLException {
        executeQuery();
        return true;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        if (scroll) {
            try {
                return executeScrollQuery(sql, null);
            } catch (IOException e) {
                throw new SQLException(e.getMessage());
            }
        } else {
            if (paramMap.size() > 0) {
                return executeQuery(sql, composeParams(paramMap));
            }
            return executeQuery(sql);
        }
    }

    @Override
    public int executeUpdate() throws SQLException {
        sql = prepareExecute(sql, composeParams(paramMap));
        return executeUpdate(sql);
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return super.executeUpdate(sql);
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return super.executeUpdate(sql, columnNames);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        paramMap.put(parameterIndex, new SqlParam(parameterIndex, x));
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        paramMap.put(parameterIndex, new SqlParam(parameterIndex, x));
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        paramMap.put(parameterIndex, new SqlParam(parameterIndex, x));
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        paramMap.put(parameterIndex, new SqlParam(parameterIndex, x));
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        paramMap.put(parameterIndex, new SqlParam(parameterIndex, x));
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        paramMap.put(parameterIndex, new SqlParam(parameterIndex, x));
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        paramMap.put(parameterIndex, new SqlParam(parameterIndex, x));
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        paramMap.put(parameterIndex, new SqlParam(parameterIndex, x));
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        paramMap.put(parameterIndex, new SqlParam(parameterIndex, "'" + x + "'"));
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        String dateStr = JdbcConstants.DATE_FORMAT.format(x);
        paramMap.put(parameterIndex, new SqlParam(parameterIndex, dateStr));
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        String dateStr = JdbcConstants.DATE_FORMAT.format(x);
        paramMap.put(parameterIndex, new SqlParam(parameterIndex, dateStr));
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        String dateStr = JdbcConstants.DATE_FORMAT.format(x);
        paramMap.put(parameterIndex, new SqlParam(parameterIndex, dateStr));
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        super.setMaxRows(max);
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        super.setMaxFieldSize(max);
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        super.setFetchSize(rows);
    }

    @Override
    public void clearParameters() throws SQLException {
        paramMap.clear();
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        if (x instanceof Date) {
            setDate(parameterIndex, (Date) x);
        } else if (x instanceof Time) {
            setTime(parameterIndex, (Time) x);
        } else if (x instanceof java.util.Date) {
            String dateStr = JdbcConstants.DATE_FORMAT.format(x);
            paramMap.put(parameterIndex, new SqlParam(parameterIndex, dateStr));
        } else {
            paramMap.put(parameterIndex, new SqlParam(parameterIndex, x));
        }
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return new ElasticResultSetMetaData();
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return null;
    }

    private Object[] composeParams(Map<Integer, SqlParam> paramMap) {
        List<SqlParam> params = Lists.newArrayList(paramMap.values());
        params.sort((o1, o2) -> {
            if (o1.index < o2.index) {
                return -1;
            } else if (o1.index > o2.index) {
                return 1;
            }
            return 0;
        });
        return params.stream().map(input -> {
            Objects.requireNonNull(input);
            return input.value;
        }).toArray(Object[]::new);
    }

    private static class SqlParam {
        private int index;
        private Object value;

        SqlParam(int index, Object value) {
            this.index = index;
            this.value = value;
        }
    }
}
