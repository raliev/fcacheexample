package org.training.core.pagecache;

import com.mongodb.*;
import com.mongodb.util.JSON;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rauf_Aliev on 7/15/2016.
 */

public class MongoDbService implements ICacheService {

    MongoClient mongo = null;
    DB db;
    DBCollection table;

    public MongoDbService() {
    }

    public void connect() throws UnknownHostException {
        if (mongo == null) {
            mongo = new MongoClient( "localhost" , 27017 );
            if (mongo == null ) {
            System.out.println("mongoClient is null");
            return;
            }
            db = mongo.getDB("testdb");
            if (mongo == null ) {
                System.out.println("db is null");
                return;
            }
            table = db.getCollection("cache");
            if (mongo == null ) {
                System.out.println("table is null");
                return;
            }
        }
    }

    public Map<String, String> getMap(String key)
    {
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("key", key);

        DBCursor cursor = table.find(searchQuery);

        String s = "";
        Map<String, String> map = new HashMap<>();
        while (cursor.hasNext()) {
            DBObject k = cursor.next();
            for (String akey : k.keySet()) {
                map.put(akey, k.get(akey).toString());
            }
            return map;
        }
        return null;

    }

    public String get(String key)
    {
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("key", key);

        DBCursor cursor = table.find(searchQuery);

        String s = "";
        while (cursor.hasNext()) {
            DBObject k = cursor.next();
            s = s + k.get("value");
        }
        return s;

    }

    public String put(String key, String value, Map<String, String> attributes)
    {
        if (get(key).equals("")) {

            BasicDBObject document = new BasicDBObject();
            document.put("key", key);

            document.put("value", value);
            //document.put("value", "test"    );
            long ctime = System.currentTimeMillis();
            document.put("ctime", ctime);

            for (String keyattr : attributes.keySet())
            {
                document.put(keyattr, attributes.get(keyattr));
            }

            table.insert(document);
            table.save(document);
        }
        return value;
    }

    public boolean lookup(String key, String value) {

        BasicDBObject query = new BasicDBObject(key, value);

        DBCursor cursor = table.find(query);

        try {
            while(cursor.hasNext()) {
                System.out.println(cursor.next());
                return true;
            }
        } finally {
            cursor.close();
        }
        return false;
    }

    public void removeAll(String key, String value) {
        //mongo.dropDatabase("testdb");
        BasicDBObject query = new BasicDBObject(key, value);

        mongo.getDB("testdb").getCollection("cache").findAndRemove(query);

    }
}
