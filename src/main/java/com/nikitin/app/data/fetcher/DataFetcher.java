package com.nikitin.app.data.fetcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public String sendRequestToApi() {
        StringBuilder allResponses = new StringBuilder();
        try {
            String firstUrl = String.format("%s,%s", BASE_URL, userRequestData.getParameters());
            CloseableHttpResponse firstResponse = httpClient.execute(new HttpGet(firstUrl));
            String jsonResponse = EntityUtils.toString(firstResponse.getEntity());
            firstResponse.close();

            JsonNode root = objectMapper.readTree(jsonResponse);
            int pageCount = root.get("pageCount").asInt();
            allResponses.append(jsonResponse);
            allResponses.append("\n");
            System.out.println(allResponses);
            System.out.println("Загружена страница 1/" + pageCount);

            for (int i = 2; i <= pageCount; i++) {
                String pagedUrl = String.format("%s,%s,&pageNum=%d", BASE_URL,
                        (userRequestData.getStartDate() + userRequestData.getEndDate()), i);
                CloseableHttpResponse response = httpClient.execute(new HttpGet(pagedUrl));
                String pageJson = EntityUtils.toString(response.getEntity());
                response.close();
                allResponses.append(pageJson);
                allResponses.append("\n");
                System.out.println("Загружена страница " + i + "/" + pageCount);
            }
            return allResponses.toString();

        } catch (IOException e) {
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
            return null;
        } catch (ParseException e) {
            System.out.println("Ошибка при парсинге JSON: " + e.getMessage());
            return null;
        }
    }

    public UserRequestData getUserRequestData() {
        return userRequestData;
    }
}