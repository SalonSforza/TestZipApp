package com.nikitin.app.zip.extractor;

import com.nikitin.app.data.db.postgres.filler.DataBaseFiller;
import com.nikitin.app.data.db.postgres.filler.StringToDayTimeFormatter;
import com.nikitin.app.data.fetcher.DataFetcher;
import com.nikitin.app.data.xml.converter.JsonToXmlConverter;
import com.nikitin.app.db.connection.manager.PostgresConnectionManager;
import com.nikitin.app.db.connection.manager.PropertiesUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipExtractor {
    private static final String ZIP_PATH = "zip.archive.path";
    private static final String SELECT_QUERY = "SELECT * FROM organizations WHERE load_date BETWEEN ? AND ?";

    private final DataBaseFiller dbFiller = new DataBaseFiller();
    private final StringToDayTimeFormatter dayTimeFormatter = new StringToDayTimeFormatter();
    private final JsonToXmlConverter xmlConverter = new JsonToXmlConverter();

    public void downloadAndArchiveToZip() {
        dbFiller.fillDataBase();

        String startOfSearch = dbFiller.getDataFetcher().getUserRequestData().getStartDate();
        String endOfSearch = dbFiller.getDataFetcher().getUserRequestData().getEndDate();

        String folderPath = PropertiesUtil.get(ZIP_PATH)
                .replace("\"", "")
                .replace("/", File.separator)
                .replace("\\\\", File.separator);

        if (!folderPath.endsWith(File.separator)) folderPath += File.separator;

        File folder = new File(folderPath);
        if (!folder.exists() && !folder.mkdirs()) {
            System.out.println("Не удалось создать папку: " + folderPath);
            return;
        }

        String csvFileName = folderPath + "organizations.csv";
        generateCsv(csvFileName, startOfSearch, endOfSearch);

        File xmlFile = generateXml(folderPath, dbFiller.getDataFetcher());

        String zipFilePath = folderPath + startOfSearch + "-" + endOfSearch + ".zip";
        List<File> filesToZip = new ArrayList<>();
        filesToZip.add(new File(csvFileName));
        if (xmlFile != null) filesToZip.add(xmlFile);
        zipFiles(filesToZip, zipFilePath);
        for (File f : filesToZip) {
            if (f.exists()) {
                boolean deleted = f.delete();
                if (!deleted) {
                    System.out.println("Не удалось удалить временный файл: " + f.getAbsolutePath());
                }
            }
        }

        System.out.printf(
                "Выполнен экспорт данных за период с %s по %s по адресу %s%n",
                startOfSearch, endOfSearch, zipFilePath
        );
    }

    private void generateCsv(String csvFileName, String startOfSearch, String endOfSearch) {
        try (
                Connection conn = PostgresConnectionManager.get();
                PreparedStatement stmt = conn.prepareStatement(SELECT_QUERY)
        ) {
            LocalDateTime startTime = dayTimeFormatter.formatTimeOfStartFromString(startOfSearch);
            LocalDateTime endTime = dayTimeFormatter.formatTimeOfStartFromString(endOfSearch)
                    .withHour(23).withMinute(59).withSecond(59);
            stmt.setTimestamp(1, Timestamp.valueOf(startTime));
            stmt.setTimestamp(2, Timestamp.valueOf(endTime));

            try (
                    ResultSet rs = stmt.executeQuery();
                    FileOutputStream fos = new FileOutputStream(csvFileName);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                    BufferedWriter csvWriter = new BufferedWriter(osw)
            ) {
                System.out.println(rs);
                fos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    csvWriter.append(escapeCsv(meta.getColumnName(i)));
                    if (i < columnCount) csvWriter.append(",");
                }
                csvWriter.append("\n");


                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        String value = rs.getString(i);
                        csvWriter.append(escapeCsv(value));
                        if (i < columnCount) csvWriter.append(",");
                    }
                    csvWriter.append("\n");
                }
                csvWriter.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании CSV: " + e.getMessage(), e);
        }
    }

    private File generateXml(String folderPath, DataFetcher dataFetcher) {
        try {
            String json = dataFetcher.getResultingJson();
            return xmlConverter.convertJsonStringToXmlFile(json, folderPath, "organizations_data");
        } catch (Exception e) {
            System.out.println("Ошибка при создании XML-файла: " + e.getMessage());
            return null;
        }
    }

    private void zipFiles(List<File> files, String zipFilePath) {
        try (
                FileOutputStream fos = new FileOutputStream(zipFilePath);
                ZipOutputStream zipOut = new ZipOutputStream(fos)
        ) {
            for (File file : files) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zipOut.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                    zipOut.closeEntry();
                }
            }
            System.out.println("ZIP-файл успешно создан: " + zipFilePath);
        } catch (IOException e) {
            System.out.println("Ошибка при архивировании в zip-файл: " + e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        value = value.replace("\"", "\"\"");
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            value = "\"" + value + "\"";
        }
        return value;
    }
}
