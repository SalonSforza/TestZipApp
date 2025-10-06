package com.nikitin.app.data.fetcher;

import com.nikitin.app.db.connection.manager.PropertiesUtil;
import com.nikitin.app.user.imput.interpreter.UserRequestData;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;

public class DataFetcher {

    private static final String BASE_URL = PropertiesUtil.get("base.url");
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final UserRequestData userRequestData = new UserRequestData();

    public String sendRequestToApi( ) {
        try {
            String url = String.format("%s,%s", BASE_URL, userRequestData.interpret());

            CloseableHttpResponse response = httpClient.execute(new HttpGet(url));
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String jsonResponse = EntityUtils.toString(entity);
                System.out.println("Ответ: \n" + jsonResponse);
                return jsonResponse;
            } else {
                System.out.println("Ответ пуст");
                return null;
            }

        } catch (IOException e) {
            System.out.println("При обработке запроса выброшено исключение" + e);
            return null;
        } catch (ParseException e) {
            System.out.println("Не удалось обработать ответ от httpclient при выполнении запроса" + e);
            return null;
        }
    }
}
