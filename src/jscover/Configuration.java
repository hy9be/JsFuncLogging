package jscover;

/**
 * Created by hyou on 8/12/15.
 */
import java.util.Properties;

public class Configuration {
    private Properties properties;

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getVersion() {
        return properties.getProperty("version");
    }
}
