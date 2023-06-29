package test.netty.demo.config;

import test.netty.demo.protocol.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static Properties properties;

    static {
        try (InputStream in = Config.class.getResourceAsStream("/application.properties")) {
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Serializer.Algorithm getSerializerAlgorithm() {
        String s = properties.getProperty("serializer.algorithm", "JAVA");
        return Serializer.Algorithm.valueOf(s);
    }

}
