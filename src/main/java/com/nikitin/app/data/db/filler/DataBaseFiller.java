package com.nikitin.app.data.db.filler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikitin.app.data.fetcher.DataFetcher;
import com.nikitin.app.db.connection.manager.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataBaseFiller {

    private static final String SQL_REQUEST = "INSERT INTO organizations (" +
                                              "id, reg_num, code, full_name, short_name, inn, kpp, ogrn, okopf_name, okopf_code, " +
                                              "okfs_name, okfs_code, city_name, street_name, house, region_name, status_name, record_num, " +
                                              "authorities, activities, heads, facial_accounts, fo_accounts, non_participant_permissions, " +
                                              "procurement_permissions, contacts, load_date" +
                                              ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final DataFetcher dataFetcher = new DataFetcher();
    private final ObjectMapper mapper = new ObjectMapper();

    public void fillDataBase() {
        try {
            String json = dataFetcher.sendRequestToApi();
            JsonNode rootNode = mapper.readTree(json);
            JsonNode dataArray = rootNode.get("data");
            Connection conn = ConnectionManager.get();
            PreparedStatement stmt = conn.prepareStatement(SQL_REQUEST);


            for (JsonNode orgNode : dataArray) {
                JsonNode info = orgNode.get("info");

                stmt.setString(1, orgNode.get("id").asText());
                stmt.setString(2, info.get("regNum").asText());
                stmt.setString(3, info.get("code").asText());
                stmt.setString(4, info.get("fullName").asText());
                stmt.setString(5, info.get("shortName").asText());
                stmt.setString(6, info.get("inn").asText());
                stmt.setString(7, info.get("kpp").asText());
                stmt.setString(8, info.get("ogrn").asText());
                stmt.setString(9, info.get("okopfName").asText());
                stmt.setString(10, info.get("okopfCode").asText());
                stmt.setString(11, info.get("okfsName").asText());
                stmt.setString(12, info.get("okfsCode").asText());
                stmt.setString(13, info.get("cityName").asText());
                stmt.setString(14, info.get("streetName").asText());
                stmt.setString(15, info.get("house").asText());
                stmt.setString(16, info.get("regionName").asText());
                stmt.setString(17, info.get("statusName").asText());
                stmt.setString(18, info.get("recordNum").asText());

                stmt.setObject(19, orgNode.get("authorities").toString(), java.sql.Types.OTHER);
                stmt.setObject(20, orgNode.get("activities").toString(), java.sql.Types.OTHER);
                stmt.setObject(21, orgNode.get("heads").toString(), java.sql.Types.OTHER);
                stmt.setObject(22, orgNode.get("facialAccounts").toString(), java.sql.Types.OTHER);
                stmt.setObject(23, orgNode.get("foAccounts").toString(), java.sql.Types.OTHER);
                stmt.setObject(24, orgNode.get("nonParticipantPermissions").toString(), java.sql.Types.OTHER);
                stmt.setObject(25, orgNode.get("procurementPermissions").toString(), java.sql.Types.OTHER);
                stmt.setObject(26, orgNode.get("contacts").toString(), java.sql.Types.OTHER);

                stmt.setTimestamp(27, java.sql.Timestamp.valueOf(info.get("loadDate").asText().replace(" ", "T")));

                stmt.addBatch();
                stmt.executeBatch();
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Something went wrong during sql request for database filling");
        } catch (JsonProcessingException e) {
            System.out.println("Something went wrong during json parsing");
        }
    }



}
