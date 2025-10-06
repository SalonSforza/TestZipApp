package com.nikitin.app;

import com.nikitin.app.data.fetcher.DataFetcher;

public class Main {
    public static void main(String[] args) {
        DataFetcher dataFetcher = new DataFetcher();
        dataFetcher.sendRequestToApi();
    }
}