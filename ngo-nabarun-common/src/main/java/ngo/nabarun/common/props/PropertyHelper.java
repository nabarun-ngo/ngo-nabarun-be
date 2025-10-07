package ngo.nabarun.common.props;
import java.util.*;

import ngo.nabarun.common.util.CommonUtil;

public class PropertyHelper {
    private final List<PropertySource> sources = new ArrayList<>();

    public PropertyHelper(PropertySource... sources) {
        this.sources.addAll(Arrays.asList(sources));
    }

    public <T> T get(PropertyKey key,Class<T> cls) {
        for (PropertySource source : sources) {
            Object value = source.getProperty(key.key());
            if (value != null) {
                return CommonUtil.convertToType(value, cls);
            }
        }
        return null;
    }
    
    public String get(PropertyKey key) {
        return get(key,String.class);
    }
}
