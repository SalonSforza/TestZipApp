package com.nikitin.app;

import com.nikitin.app.zip.extractor.ZipExtractor;

public class Main {
    public static void main(String[] args) {
        ZipExtractor zipExtractor = new ZipExtractor();
        zipExtractor.downloadAndArchiveToZip();
    }
}