package ngo.nabarun.common.props.source;

import java.util.Map;

import ngo.nabarun.common.props.PropertySource;

public class MapSource implements PropertySource {
    private final Map<String, Object> map;
    public MapSource(Map<String, Object> map) { this.map = map; }
    public Object getProperty(String key) { return map.get(key); }
}

