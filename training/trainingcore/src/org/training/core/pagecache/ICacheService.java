package org.training.core.pagecache;

import java.net.UnknownHostException;
import java.util.Map;

/**
 * Created by Rauf_Aliev on 7/29/2016.
 */
public interface ICacheService {
    public void connect() throws UnknownHostException;

    public Map<String, String> getMap(String key);

    public String get(String key);

    public String put(String key, String value, Map<String, String> attributes);
}
