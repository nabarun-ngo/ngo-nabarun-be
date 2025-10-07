package ngo.nabarun.common.props.source;

import ngo.nabarun.common.props.PropertySource;

public class EnvironmentSource implements PropertySource {
    public Object getProperty(String key) { return System.getenv(key); }
}

