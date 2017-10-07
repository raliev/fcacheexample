    <%@ page trimDirectiveWhitespaces="true"%>
    <%@ taglib prefix="cache" uri="/WEB-INF/cachetags.tld"%>

 <!--# set var="test" value="Hello nginx!" -->
 <!--# echo var="test" -->


    <script>
    function addleadingzero(s)
    {
     if ((s+"").length<2) { s = "0" + s; }
     return s;
    }
    var currentdate = new Date();
    var datetime =  addleadingzero(currentdate.getMonth()) + "/"+addleadingzero(currentdate.getDate())
    + "/" + currentdate.getFullYear() + " @ "
    + addleadingzero(currentdate.getHours()) + ":"
    + addleadingzero(currentdate.getMinutes()) + ":" + addleadingzero(currentdate.getSeconds());
    document.write("<li>the current time is "+datetime);
    </script>
    <li>time when this page was generated: ${datetime}

    <table border=1>
    <tr><td>
    <cache:cached addToKey="nav" ttl="10">

    <b>This block "NAV" is supposed to be cached by Varnish for <font color=red>10 seconds</font></b>
    <li> the time when this block was generated: ${datetime}
    </cache:cached>
    </td><td>
    <table border=1>
    <tr>
    <td>

    <cache:cached addToKey="1" ttl="3">

    <b>This block #1 is supposed to be cached by Varnish for <font color=red>3 seconds</font></b>
    <li> the time when this block was generated: ${datetime}
    </cache:cached>


    </td>
    <tr>
    <td>
    <cache:cached addToKey="2" ttl="5">

    <b>This block #2 is supposed to be cached by Varnish for <font color=red>5 seconds</font></b>
    <li> the time when this block was generated: ${datetime}
    </cache:cached>
    </td>
    </tr>
    </table>
    </td></tr></table>
    <script>
     //setTimeout(function(){ window.location.reload(1); }, 500);
    </script>
    <input type="submit" value="Reload page (auto reload 0.5s)" onClick="window.location.reload(1)"/>
<table width=100%><tr><td align=left><img src="https://s31.postimg.org/wt9zfr1e3/2016_07_24_13h48_23.png"></td></tr></table>