package ngo.nabarun.common.props.source;

import java.io.FileReader;
import java.util.Properties;

import ngo.nabarun.common.props.PropertySource;

public class FileSource implements PropertySource {
    private final Properties props = new Properties();
    public FileSource(String filePath) {
        try (var reader = new FileReader(filePath)) {
            props.load(reader);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load props: " + filePath, e);
        }
    }
    public String getProperty(String key) { return props.getProperty(key); }
}
