package ngo.nabarun.common.props.source;

import ngo.nabarun.common.props.PropertySource;

public class SystemSource implements PropertySource {
    public Object getProperty(String key) { return System.getProperty(key); }
}

