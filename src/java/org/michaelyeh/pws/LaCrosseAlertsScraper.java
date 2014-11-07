package org.michaelyeh.pws;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
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
        BasicCookieStore cookieStore = new BasicCookieStore();
        Header header = new BasicHeader(HttpHeaders.USER_AGENT,
                "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_2 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8H7 Safari/6533.18.5");
        List<Header> headers = new ArrayList<Header>(1);
        headers.add(header);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setDefaultHeaders(headers)
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
        try
        {
            HttpGet httpget = new HttpGet("https://www.lacrossealerts.com/");
            CloseableHttpResponse response1 = httpclient.execute(httpget);
            try
            {
                HttpEntity entity = response1.getEntity();

                System.out.println("Login page get: " + response1.getStatusLine());
                EntityUtils.consume(entity);
            }
            finally
            {
                response1.close();
            }

            HttpUriRequest login = RequestBuilder.post()
                    .setUri(new URI("https://www.lacrossealerts.com/login"))
                    .addParameter("username", Config.LACROSSE_USERNAME)
                    .addParameter("password", Config.LACROSSE_PASSWORD)
                    .addParameter("remember[]", "remember")
                    .addParameter("login", "")
                    .build();
            CloseableHttpResponse response2 = httpclient.execute(login);
            try
            {
                HttpEntity entity = response2.getEntity();
                System.out.println("Login form: " + response2.getStatusLine());
                int statusCode = response2.getStatusLine().getStatusCode();

                if (statusCode != HttpStatus.SC_OK)
                    throw new Exception("Unexpected response");

                EntityUtils.consume(entity);
            }
            finally
            {
                response2.close();
            }

            httpget = new HttpGet("https://www.lacrossealerts.com/v1/observations/" + Config.LACROSSE_DEVICE_ID + "?expand=stats1hr,stats24hr,stats1wk,stats1mo,stats1yr&filter=outdoor.temp,outdoor.rainRelative,outdoor.rainTotal,outdoor.windGust");
            CloseableHttpResponse response3 = httpclient.execute(httpget);
            try
            {
                HttpEntity entity = response3.getEntity();
                int statusCode = response3.getStatusLine().getStatusCode();


                System.out.println("Scrape data: " + response3.getStatusLine());

                if (statusCode == HttpStatus.SC_OK)
                {
                    String bodyAsString = EntityUtils.toString(entity);
                    EntityUtils.consume(entity);
                    return bodyAsString;
                }

            }
            finally
            {
                response3.close();
            }
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

    public static void main(String[] args) throws Exception
    {
        System.out.println("scrapeData() = " + scrapeData());
    }

}
