package org.training.core.pagecache;

import java.net.UnknownHostException;
import java.util.Map;

/**
 * Created by Rauf_Aliev on 7/29/2016.
 */
public class PageCacheService {

    MemcachedService memcachedService;

    public void connect() throws UnknownHostException {
            if (memcachedService == null) { memcachedService = new MemcachedService(); }
            memcachedService.connect();
    }
    public Map<String, String> getMap(String key)
    {
        //System.out.print("check map cache[\""+key+"\"]...");
        Map<String, String> value= memcachedService.getMap(key);
        //System.out.print(value.get("value"));
        return value;
    }

    public String get(String key)
    {
        //System.out.print("check cache[\""+key+"\"]...");
        String value = memcachedService.get(key);
        //System.out.print(value);
        return  value;
    }

    public String put(String key, String value, Map<String, String> attributes)
    {
        //System.out.println("cache[\""+key+"\"]="+value.substring(1,10)+"...");
        return memcachedService.put(key,value,attributes);
    }

    public void removeAll(String key, String key1) {

    }
}
