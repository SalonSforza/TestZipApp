package com.nikitin.app.data.fetcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikitin.app.db.connection.manager.PropertiesUtil;
import com.nikitin.app.user.imput.interpreter.UserRequestData;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;

public class DataFetcher {
    private static final String BASE_URL = PropertiesUtil.get("base.url");
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final UserRequestData userRequestData = new UserRequestData();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String resultingJson;


    public String getResultingJson() {
        if (resultingJson == null) {
            resultingJson = fetchJsonFromApi();
        }
        return resultingJson;
    }

    private String fetchJsonFromApi() {
        ArrayNode combinedData = objectMapper.createArrayNode();
        int pageCount = 1;

        try {
            String firstUrl = String.format("%s%s", BASE_URL, userRequestData.getParameters());
            System.out.println(firstUrl);
            try (CloseableHttpResponse firstResponse = httpClient.execute(new HttpGet(firstUrl))) {
                String jsonResponse = EntityUtils.toString(firstResponse.getEntity());
                JsonNode root = objectMapper.readTree(jsonResponse);
                pageCount = root.get("pageCount").asInt();
                combinedData.addAll((ArrayNode) root.get("data"));
                System.out.println("Загружена страница 1/" + pageCount);
            }

            for (int i = 2; i <= pageCount; i++) {
                String pagedUrl = String.format("%s&pageNum=%d",firstUrl, i);
                System.out.println(pagedUrl);
                try (CloseableHttpResponse response = httpClient.execute(new HttpGet(pagedUrl))) {
                    String jsonResponse = EntityUtils.toString(response.getEntity());
                    JsonNode root = objectMapper.readTree(jsonResponse);
                    combinedData.addAll((ArrayNode) root.get("data"));
                    System.out.println("Загружена страница " + i + "/" + pageCount);
                }
            }

            ObjectNode finalJson = objectMapper.createObjectNode();
            finalJson.set("data", combinedData);
            finalJson.put("pageCount", pageCount);
            return objectMapper.writeValueAsString(finalJson);

        } catch (IOException | ParseException e) {
            throw new RuntimeException("Ошибка при выполнении запроса или парсинге JSON", e);
        }
    }

    public UserRequestData getUserRequestData() {
        return userRequestData;
    }
}