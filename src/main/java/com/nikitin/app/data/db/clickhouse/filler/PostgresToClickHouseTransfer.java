package com.nikitin.app.data.db.clickhouse.filler;

import com.nikitin.app.db.connection.manager.ClickHouseConnectionManager;
import com.nikitin.app.db.connection.manager.PostgresConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PostgresToClickHouseTransfer {

    private static final String SELECT_POSTGRES = "SELECT * FROM organizations WHERE load_date BETWEEN ? AND ?";
    private static final String INSERT_CLICKHOUSE = "INSERT INTO organizations VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public void transfer(LocalDateTime start, LocalDateTime end) {
        try (Connection pgConn = PostgresConnectionManager.get();
             Connection chConn = ClickHouseConnectionManager.get();
             PreparedStatement pgStmt = pgConn.prepareStatement(SELECT_POSTGRES);
             PreparedStatement chStmt = chConn.prepareStatement(INSERT_CLICKHOUSE)) {

            pgStmt.setTimestamp(1, Timestamp.valueOf(start));
            pgStmt.setTimestamp(2, Timestamp.valueOf(end));

            try (ResultSet rs = pgStmt.executeQuery()) {
                int batchSize = 0;
                while (rs.next()) {
                    for (int i = 1; i <= 27; i++) {
                        chStmt.setString(i, rs.getString(i));
                    }
                    chStmt.addBatch();
                    batchSize++;

                    if (batchSize >= 1000) {  // flush every 1000 rows
                        chStmt.executeBatch();
                        batchSize = 0;
                    }
                }
                if (batchSize > 0) chStmt.executeBatch();
            }

            System.out.println("Данные успешно перенесены в ClickHouse");

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при переносе данных в ClickHouse", e);
        }
    }
}
