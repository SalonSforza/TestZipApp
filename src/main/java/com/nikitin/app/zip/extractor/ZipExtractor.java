package com.nikitin.app.zip.extractor;

import com.nikitin.app.data.db.filler.DataBaseFiller;
import com.nikitin.app.data.db.filler.StringToDayTimeFormatter;
import com.nikitin.app.db.connection.manager.ConnectionManager;
import com.nikitin.app.db.connection.manager.PropertiesUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipExtractor {
    private static final String ZIP_PATH = "zip.archive.path";
    private static final String SELECT_QUERY = "SELECT * FROM organizations WHERE load_date BETWEEN ? AND ?";

    private final DataBaseFiller dbFiller = new DataBaseFiller();
    private final StringToDayTimeFormatter dayTimeFormatter = new StringToDayTimeFormatter();

    public void downloadAndArchiveToZip() {
        dbFiller.fillDataBase();
        String csvFileName = "organizations.csv";

        try (
                Connection conn = ConnectionManager.get();
                PreparedStatement stmt = conn.prepareStatement(SELECT_QUERY)
        ) {
            String startOfSearch = dbFiller.getDataFetcher().getUserRequestData().getStartDate();
            LocalDateTime startTime = dayTimeFormatter.formatTimeOfStartFromString(startOfSearch);
            stmt.setTimestamp(1, Timestamp.valueOf(startTime));

            String endOfSearch = dbFiller.getDataFetcher().getUserRequestData().getEndDate();
            LocalDateTime endTime = dayTimeFormatter.formatTimeOfStartFromString(endOfSearch);
            stmt.setTimestamp(2, Timestamp.valueOf(endTime));

            try (
                    ResultSet rs = stmt.executeQuery();
                    FileOutputStream fos = new FileOutputStream(csvFileName);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                    BufferedWriter csvWriter = new BufferedWriter(osw)
            ) {
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

            zipFile(csvFileName);

            System.out.printf("Выполнен экспорт данных за период с %s по %s по адресу %s%n",
                    startOfSearch, endOfSearch, PropertiesUtil.get(ZIP_PATH));

        } catch (Exception e) {
            throw new RuntimeException(e);
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

    private void zipFile(String sourceFilePath) {
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

        String zipFilePath = folderPath + startOfSearch + "-" + endOfSearch + ".zip";

        try (
                FileOutputStream fos = new FileOutputStream(zipFilePath);
                ZipOutputStream zipOut = new ZipOutputStream(fos)
        ) {
            File fileToZip = new File(sourceFilePath);
            try (FileInputStream fis = new FileInputStream(fileToZip)) {
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка при архивировании в zip-файл: " + e.getMessage());
        }
    }
}

