package org.training.core.pagecache;

import com.meetup.memcached.MemcachedClient;
import com.meetup.memcached.SockIOPool;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.thoughtworks.xstream.XStream;

import java.net.UnknownHostException;
import java.util.*;

import static com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table;

/**
 * Created by Rauf_Aliev on 7/29/2016.
 */
public class MemcachedService implements ICacheService {
    MemcachedClient memcachedClient = null;
    public void test() {
        String[] servers = { "127.0.0.1:11211"};
        SockIOPool pool = SockIOPool.getInstance();
        pool.setServers( servers );
        pool.setFailover( true );
        pool.setInitConn( 10 );
        pool.setMinConn( 5 );
        pool.setMaxConn( 250 );
        pool.setMaintSleep( 30 );
        pool.setNagle( false );
        pool.setSocketTO( 3000 );
        pool.setAliveCheck( true );
        pool.initialize();
        com.meetup.memcached.Logger.getLogger( MemcachedClient.class.getName() ).setLevel( com.meetup.memcached.Logger.LEVEL_WARN );
        MemcachedClient mcc = new MemcachedClient();

        for ( int i = 0; i < 10; i++ ) {
            boolean success = mcc.set( "" + i, "Hello!" );
            String result = (String)mcc.get( "" + i );
            System.out.println( String.format( "set( %d ): %s", i, success ) );
            System.out.println( String.format( "get( %d ): %s", i, result ) );
        }

        System.out.println( "\n\t -- sleeping --\n" );
        try { Thread.sleep( 10000 ); } catch ( Exception ex ) { }

        for ( int i = 0; i < 10; i++ ) {
            boolean success = mcc.set( "" + i, "Hello!" );
            String result = (String)mcc.get( "" + i );
            System.out.println( String.format( "set( %d ): %s", i, success ) );
            System.out.println( String.format( "get( %d ): %s", i, result ) );
        }
    }


    public void connect() throws UnknownHostException {
        if (memcachedClient == null) {
            String[] servers = { "127.0.0.1:11211"};
            SockIOPool pool = SockIOPool.getInstance();
            pool.setServers( servers );
            pool.setFailover( true );
            pool.setInitConn( 10 );
            pool.setMinConn( 5 );
            pool.setMaxConn( 250 );
            pool.setMaintSleep( 30 );
            pool.setNagle( false );
            pool.setSocketTO( 3000 );
            pool.setAliveCheck( true );
            pool.initialize();
            com.meetup.memcached.Logger.getLogger( MemcachedClient.class.getName() ).setLevel( com.meetup.memcached.Logger.LEVEL_WARN );
            memcachedClient = new MemcachedClient();
            if (memcachedClient == null ) {
                System.out.println("memcached client is null");
                return;
            }
        }
    }

    public Map<String, String> getMap(String key)
    {
        String value = (String) memcachedClient.get(key);
        String fieldsStr = (String) memcachedClient.get(key+"*fields");
        Map<String, String> map = new HashMap<>();
        map.put("key", key);
        map.put("value", value);
        if (fieldsStr != null && !fieldsStr.equals("")) {
            List<String> fields = Arrays.asList(fieldsStr.split(","));
            String s = "";
            Iterator<String> iter = fields.iterator();
            while (iter.hasNext()) {
                String k = iter.next();
                map.put(k, (String) memcachedClient.get(key + "*" + k));
            }
        }
        return map;
    }

    public String get(String key)
    {
        String value = (String) memcachedClient.get(key);
        if (value == null) { return ""; }
        return value;
    }

    public String put(String key, String value, Map<String, String> attributes)
    {
        if (get(key).equals("")) {

            memcachedClient.set( key, value);

            long ctime = System.currentTimeMillis();
            memcachedClient.set(key+"*ctime", ctime+"");
            String fields = "";
            for (String keyattr : attributes.keySet())
            {
                if (!fields.equals("")) { fields = fields + ","; }
                memcachedClient.set(key+"*" + keyattr, attributes.get(keyattr));
                fields = fields + keyattr;
            }
            memcachedClient.set(key+"*fields", fields);
        }
        return value;
    }


    public void removeAll(String key, String value) {
        //mongo.dropDatabase("testdb");
        //BasicDBObject query = new BasicDBObject(key, value);
        //mongo.getDB("testdb").getCollection("cache").findAndRemove(query);
    }

}
