package com.oul.mHipster.util;

import com.oul.mHipster.layerconfig.LayersConfig;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class ConfigUtil {

    public static String getLayersConfig() {
        return "C:\\Users\\jovan\\Documents\\mi\\mHipster\\src\\main\\resources\\layersConfig.xml";
//        return "/Users/mihajlo/Documents/best_in_class/mHipster/src/main/resources/layersConfig.xml";
    }

    public static LayersConfig readConfig() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(LayersConfig.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (LayersConfig) unmarshaller.unmarshal(new File(getLayersConfig()));
    }
}
