package org.training.storefront.cache;

import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.springframework.stereotype.Component;
import org.training.core.pagecache.MongoDbService;
import org.training.core.pagecache.MongoDbService;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.DynamicAttributes;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class CacheTags extends BodyTagSupport implements DynamicAttributes
{

    MongoDbService mongoDBService;

    ConfigurationService configurationService;

    long startTime = 0;

    int ttl = 10;

    HashMap<String, Boolean> cacheControl;

    private static final String FRAGMENTCACHE = "FragmentCache";

    private Map<String,String> dynamicAttributes;


    public Map<String, String> getDynamicAttributes() {
        return dynamicAttributes;
    }



    @Override
    public int doStartTag() throws JspException {
        //return super.doStartTag();

        if (cacheControl == null) { cacheControl = new HashMap<String, Boolean>(); }

        if (configurationService == null ) {
            configurationService = (ConfigurationService) de.hybris.platform.core.Registry.getApplicationContext().getBean("configurationService");
        }
        if (configurationService.getConfiguration().getBoolean("cache.cms", true) == false) { return EVAL_BODY_TAG; }
        startTime = System.currentTimeMillis();
        try {
            try {
            mongoDBService = (MongoDbService) de.hybris.platform.core.Registry.getApplicationContext().getBean("mongoDBService");
            } catch (Exception e) { }
            if (mongoDBService == null) { mongoDBService = new MongoDbService(); }
            mongoDBService.connect();

            String key = buildKey();

            cacheControl.put(key, false);

            Map<String, String> datamap = mongoDBService.getMap(key);
            String data = "";
            if (datamap!=null) {
                data = datamap.get("value");
                if (datamap != null) {
                    String ttlStr = datamap.get("ttl");

                    if (ttlStr == null || ttlStr.equals("")) {
                        ttlStr = "5";
                    }
                    int ttl = Integer.parseInt(ttlStr);
                    String ctimeStr = datamap.get("ctime");

                    Long ctime = 0L;
                    if (ctimeStr != null && !ctimeStr.equals("")) {
                        ctime=Long.parseLong(ctimeStr);
                    }
                    if (startTime > ctime + ttl * 1000) {
                        // stale
                        Date ctimeDate = new Date(ctime);
                        Date startTimeDate = new Date(startTime);
                        System.out.println("MongoDB cache item is expired. ctime is " + ctime + "(" + ctimeDate.toString() + "), current time is " + startTime + "(" + startTimeDate + "), key=" + key + " ttl=" + (ttl * 1000));
                        mongoDBService.removeAll("key", key);
                        data = "";
                    }
                }
            }
            if (!data.equals("")) {
                long endTime = System.currentTimeMillis();
                //pageContext.getOut().print(markAs("from cache   : "+diffTime(endTime, startTime) + " ms", data) );
                //pageContext.getOut().print("<esi:include src=\"/cache/get?key="+key+"\"/>");
                includeCommand(pageContext.getOut(),key);

                cacheControl.put(key, true);
                System.out.println("found in mongoDB cache. Generating ESI include: "+"<esi:include src=\"/cache/get?key="+key+"\"/>");
                return SKIP_BODY;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return EVAL_BODY_TAG;
    }

    private void includeCommand(JspWriter out, String key) throws IOException {
        out.print("<!--# include file=\"/cache/get?key="+key+"\" -->");
    }

    private long diffTime(long endTime, long startTime) {
        return endTime-startTime;
    }

    private String buildKey() {
        String key = "";
        for (Map.Entry<String, String> dynamicAttribute : dynamicAttributes.entrySet()) {
            key = key + dynamicAttribute.getValue() + "_";
        }
        return key;
    }

    @Override
    public int doEndTag() throws JspException {

        if (configurationService == null ) {
            configurationService = (ConfigurationService) de.hybris.platform.core.Registry.getApplicationContext().getBean("configurationService");
        }
        if (configurationService.getConfiguration().getBoolean("cache.cms", true) == false) {
            try {
                pageContext.getOut().print(bodyContent.getString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return super.doEndTag(); }

        if (bodyContent != null) {
            try {
                try {
                    mongoDBService = (MongoDbService) de.hybris.platform.core.Registry.getApplicationContext().getBean("mongoDBService");
                } catch (Exception e) { }
                if (mongoDBService == null) { mongoDBService = new MongoDbService(); }
                mongoDBService.connect();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            String key = buildKey();
            mongoDBService.put(key, bodyContent.getString(), dynamicAttributes);
            String prevValueStr = ((HttpServletResponse) ((CacheTags) this).pageContext.getResponse()).getHeader("X-Cache-Control");
            if (prevValueStr != null && !prevValueStr.equals(""))
            {
                int prevValue = Integer.parseInt(prevValueStr);
                int newValue  = ttl;
                if (newValue < prevValue)
                {
                   // ((HttpServletResponse) ((CacheTags) this).pageContext.getResponse()).setHeader("X-VarnishCache", newValue + "");
                    ((HttpServletResponse) ((CacheTags) this).pageContext.getResponse()).setHeader("X-Cache-Control", newValue + "");
                }
            } else
            {
                ((HttpServletResponse) ((CacheTags) this).pageContext.getResponse()).setHeader("X-Cache-Control", ttl+"");
            }
            ((HttpServletResponse) ((CacheTags) this).pageContext.getResponse()).setHeader("Cache-Control", "public, max-age:10");
            String bodyText = bodyContent.getString();
            long endTime = System.currentTimeMillis();
            //bodyText = markAs("non-cached (saved):" +diffTime(endTime, startTime)+ "ms", bodyText);
            try {
                pageContext.getOut().print(bodyText);
                if (!cacheControl.get(key))
                 {
                        //pageContext.getOut().print("<esi:include src=\"/cache/get?key="+key+"\"/>");
                        //includeCommand(pageContext.getOut(), key);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return super.doEndTag();
    }

    private String markAs(String s, String bodyText) {
        return "<table border=1 cellpadding=0 cellspacing=0 style=\"border:1px dotted red\"><tr><td><img width=20 src=\"http://www.clipartbest.com/cliparts/LiK/opB/LiKopB9ia.png\">"+s+"</td></tr></table>"+bodyText+
                "<table border=1 cellpadding=0 cellspacing=0 style=\"border:1px dotted red\"><tr><td>[[END of the cached block]]</td></tr></table>";
    }

    @Override
    public BodyContent getBodyContent() {

        BodyContent bodyContent = super.getBodyContent();
        System.out.println("!!"+bodyContent.toString());
        return bodyContent;
    }

    @Override
    public int doAfterBody() throws JspException {
       // System.out.println("!"+bodyContent.toString());
        return super.doAfterBody();
    }

    @Override
    public void setDynamicAttribute(String s, String s1, Object o) throws JspException {
        if (dynamicAttributes == null) { dynamicAttributes = new HashMap<>(); }
        if (o == null) { System.out.println("value=null"); return; }

        if (s1.equals("ttl"))
        {
            this.dynamicAttributes.put("ttl", o.toString());
            ttl = Integer.parseInt(o.toString());
            return;
        }

        if (o.equals("url"))
        {
            ServletRequest sr = ((CacheTags) this).pageContext.getRequest();
            String url = ((HttpServletRequest) sr ).getRequestURI();
            this.dynamicAttributes.put("url", url);
            return;
        }

        if (o.equals("url+get"))
        {
            ServletRequest sr = ((CacheTags) this).pageContext.getRequest();
            String qs = ((HttpServletRequest) sr ).getQueryString();
            String url = ((HttpServletRequest) sr ).getRequestURL().toString();
            this.dynamicAttributes.put("url+get", url+"?"+qs);
            return;
        }
        if (o.equals("sessionid"))
        {
            System.out.println("sessionid...");
            ServletRequest sr = ((CacheTags) this).pageContext.getRequest();
            Cookie[] cookie = ((HttpServletRequest) sr ).getCookies();
            for (int i=0;i<cookie.length;i++)
            {
                if (cookie[i].getName().equals("JSESSIONID"))
                {
                    System.out.println("JSESSIONID="+cookie[i].getValue());
                    this.dynamicAttributes.put("sessionid", cookie[i].getValue());
                    return;
                }
            }
            return;
        }

        this.dynamicAttributes.put(s1, o.toString());
    }
}


