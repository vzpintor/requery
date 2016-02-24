/*
 * Copyright 2016 requery.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.requery.android.sqlite;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.Statement;

/**
 * {@link java.sql.Statement} implementation using Android's local SQLite database.
 *
 * @author Nikhil Purushe
 */
class StatementAdapter implements Statement {

    protected final DatabaseConnection connection;
    protected ResultSet queryResult;
    protected ResultSet insertResult;
    protected int updateCount;
    private boolean closed;
    private int timeout;
    private int maxRows;
    private int maxFieldSize;
    private int fetchSize;

    StatementAdapter(DatabaseConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("null connection");
        }
        this.connection = connection;
    }

    protected void throwIfClosed() throws SQLException {
        if (isClosed()) {
            throw new SQLException("closed");
        }
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void cancel() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void clearBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public void close() throws SQLException {
        if (queryResult != null) {
            queryResult.close();
        }
        closed = true;
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        throwIfClosed();
        try {
            connection.getDatabase().execSQL(sql);
        } catch (android.database.SQLException e) {
            DatabaseConnection.throwSQLException(e);
        }
        return false;
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throwIfClosed();
        SQLiteStatement statement = null;
        try {
            statement = connection.getDatabase().compileStatement(sql);
            if (autoGeneratedKeys == RETURN_GENERATED_KEYS) {
                long rowId = statement.executeInsert();
                if (rowId == -1) {
                    throw new SQLException("executeInsert failed");
                }
                insertResult = new SingleResultSet(this, rowId);
                return true;
            } else {
                statement.execute();
            }
        } catch (android.database.SQLException e) {
            DatabaseConnection.throwSQLException(e);
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
        return false;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        throwIfClosed();
        try {
            @SuppressLint("Recycle") // released with the queryResult
            Cursor cursor = connection.getDatabase().rawQuery(sql, null);
            return queryResult = new CursorResultSet(this, cursor, true);
        } catch (android.database.SQLException e) {
            DatabaseConnection.throwSQLException(e);
        }
        return null;
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return executeUpdate(sql, NO_GENERATED_KEYS);
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throwIfClosed();
        SQLiteStatement statement = null;
        try {
            statement = connection.getDatabase().compileStatement(sql);
            if (autoGeneratedKeys == RETURN_GENERATED_KEYS) {
                long rowId = statement.executeInsert();
                if (rowId == -1) {
                    throw new SQLException("executeInsert failed");
                }
                insertResult = new SingleResultSet(this, rowId);
                return 1;
            } else {
                return updateCount = statement.executeUpdateDelete();
            }
        } catch (android.database.SQLException e) {
            DatabaseConnection.throwSQLException(e);
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
        return 0;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Connection getConnection() throws SQLException {
        throwIfClosed();
        return connection;
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return ResultSet.FETCH_FORWARD;
    }

    @Override
    public int getFetchSize() throws SQLException {
        return fetchSize;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return insertResult;
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return maxFieldSize;
    }

    @Override
    public int getMaxRows() throws SQLException {
        return maxRows;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return getMoreResults(CLOSE_CURRENT_RESULT);
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return timeout;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        throwIfClosed();
        return queryResult;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return connection.getHoldability();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return ResultSet.TYPE_SCROLL_INSENSITIVE;
    }

    @Override
    public int getUpdateCount() throws SQLException {
        throwIfClosed();
        return updateCount;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void setCursorName(String name) throws SQLException {
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {

    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        if (direction != ResultSet.FETCH_FORWARD) {
            throw new SQLFeatureNotSupportedException("only FETCH_FORWARD is supported");
        }
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        this.fetchSize = rows;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        this.maxFieldSize = max;
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        this.maxRows = max;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        this.timeout = seconds;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return closed;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
