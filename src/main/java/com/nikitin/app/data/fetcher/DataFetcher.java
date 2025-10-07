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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataFetcher {
    private static final String BASE_URL = PropertiesUtil.get("base.url");
    private static final int THREAD_POOL_SIZE = Integer.parseInt(PropertiesUtil.get("thread.pool.size"));
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final UserRequestData userRequestData = new UserRequestData();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
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

            List<CompletableFuture<JsonNode>> futures = new ArrayList<>();

            for (int i = 2; i <= pageCount; i++) {
                final int pageNum = i;
                int finalPageCount = pageCount;
                CompletableFuture<JsonNode> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        String pagedUrl = String.format("%s&pageNum=%d", firstUrl, pageNum);
                        System.out.println("Загрузка страницы " + pageNum + ": " + pagedUrl);

                        try (CloseableHttpResponse response = httpClient.execute(new HttpGet(pagedUrl))) {
                            String jsonResponse = EntityUtils.toString(response.getEntity());
                            JsonNode root = objectMapper.readTree(jsonResponse);
                            System.out.println("Загружена страница " + pageNum + "/" + finalPageCount);
                            return root.get("data");
                        }
                    } catch (IOException | ParseException e) {
                        throw new RuntimeException("Ошибка при загрузке страницы " + pageNum, e);
                    }
                }, executor);
                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            for (CompletableFuture<JsonNode> future : futures) {
                JsonNode pageData = future.get();
                combinedData.addAll((ArrayNode) pageData);
            }

            ObjectNode finalJson = objectMapper.createObjectNode();
            finalJson.set("data", combinedData);
            finalJson.put("pageCount", pageCount);
            return objectMapper.writeValueAsString(finalJson);

        } catch (IOException | ParseException e) {
            throw new RuntimeException("Ошибка при выполнении запроса или парсинге JSON", e);

        } catch (ExecutionException e) {
            System.out.println("ExecutionException: " + e.getCause());
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            System.out.println("InterruptedException: " + e.getCause());
            throw new RuntimeException(e);
        } finally {
            executor.shutdown(); // НЕ ЗАБЫЛ!
        }

    }

    public UserRequestData getUserRequestData() {
        return userRequestData;
    }
}