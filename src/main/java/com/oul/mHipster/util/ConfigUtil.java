package com.oul.mHipster.util;

import com.oul.mHipster.layerconfig.LayersConfig;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ConfigUtil {

    public static LayersConfig readConfig(InputStream inputStream) throws JAXBException, IOException {
        JAXBContext jaxbContext = JAXBContext.newInstance(LayersConfig.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (LayersConfig) unmarshaller.unmarshal(getLayersConfig(inputStream));
    }

    private static File getLayersConfig(InputStream inputStream) throws IOException {
        File tempFile = File.createTempFile(String.valueOf(inputStream.hashCode()), ".tmp");
        tempFile.deleteOnExit();

        FileOutputStream out = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        return tempFile;
    }
}
