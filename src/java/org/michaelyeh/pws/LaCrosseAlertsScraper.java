package org.michaelyeh.pws;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Yeh <myeh@hotmail.com>
 * @version $Revision: $
 *          Date: 11/5/14
 *          Time: 9:55 PM
 *          Last Modified by: $Author: $ on $Date:  $
 */
public class LaCrosseAlertsScraper
{

    /**
     * The CVS identification <code>String</code>. The Unix
     * <code>ident</code> and <code>strings</code> commands can be used to
     * display the contents of the <code>String</code> (and hence the
     * version), in either the .java or .class file.
     */
    public static final String Ident =
            "$Id: $";

    public static String scrapeData() throws Exception
    {
        PersistentCookieStore cookieStore = new PersistentCookieStore();
        Header header = new BasicHeader(HttpHeaders.USER_AGENT,
                "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_2 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8H7 Safari/6533.18.5");
        List<Header> headers = new ArrayList<>(1);
        headers.add(header);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setDefaultHeaders(headers)
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
        try
        {
            // try to grab data using the remember me cookie without logging in
            String data = grabData(httpclient);
            if (data != null && data.startsWith("{\"device0\":{\"success\":true"))
                return data;

            // try logging in and grabbing data
            // as of 09/03/15 - doesn't look like we need to auth any more
            getLandingPage(httpclient);

            login(httpclient);

            return grabData(httpclient);

        }
        catch (URISyntaxException | IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                httpclient.close();
            }
            catch (IOException ignored)
            {
            }
        }

        throw new Exception("Unexpected response");
    }

    private static void getLandingPage(CloseableHttpClient httpclient) throws IOException
    {
        HttpGet httpget = new HttpGet("https://www.lacrossealerts.com/");
        CloseableHttpResponse response = httpclient.execute(httpget);
        try
        {
            HttpEntity entity = response.getEntity();

            System.out.println("Login page get: " + response.getStatusLine());
            EntityUtils.consume(entity);
        }
        finally
        {
            response.close();
        }
    }

    private static void login(CloseableHttpClient httpclient) throws Exception
    {
        HttpUriRequest login = RequestBuilder.post()
                .setUri(new URI("https://www.lacrossealerts.com/login"))
                .addParameter("username", Config.LACROSSE_USERNAME)
                .addParameter("password", Config.LACROSSE_PASSWORD)
                .addParameter("remember[]", "remember")
                .addParameter("login", "")
                .build();
        CloseableHttpResponse response = httpclient.execute(login);
        try
        {
            HttpEntity entity = response.getEntity();
            System.out.println("Login form: " + response.getStatusLine());
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK)
                throw new Exception("Unexpected response");

            EntityUtils.consume(entity);
        }
        finally
        {
            response.close();
        }

    }

    private static String grabData(CloseableHttpClient httpclient) throws Exception
    {
        HttpGet httpget = new HttpGet("http://lacrossealertsmobile.com/laxservices/device_info.php?&deviceid=" + Config.LACROSSE_DEVICE_ID);
        CloseableHttpResponse response = httpclient.execute(httpget);
        try
        {
            HttpEntity entity = response.getEntity();
            int statusCode = response.getStatusLine().getStatusCode();


            System.out.println("Scrape data: " + response.getStatusLine());

            if (statusCode == HttpStatus.SC_OK)
            {
                String bodyAsString = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
                return bodyAsString;
            }

        }
        finally
        {
            response.close();
        }
        throw new Exception("Unexpected response");
    }


    public static void main(String[] args) throws Exception
    {
        System.out.println("scrapeData() = " + scrapeData());
    }

}
