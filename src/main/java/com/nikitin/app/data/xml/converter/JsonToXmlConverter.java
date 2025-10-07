package com.nikitin.app.data.xml.converter;

import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class JsonToXmlConverter {

    public File convertJsonStringToXmlFile(String jsonString, String folderPath, String fileName) throws Exception {
        JSONObject jsonObject = new JSONObject(jsonString);

        String xmlContent = "<root>" + XML.toString(jsonObject) + "</root>";

        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();

        File xmlFile = new File(folder, fileName + ".xml");
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(Files.newOutputStream(xmlFile.toPath()), StandardCharsets.UTF_8))) {
            writer.write(xmlContent);
        }

        return xmlFile;
    }
}
