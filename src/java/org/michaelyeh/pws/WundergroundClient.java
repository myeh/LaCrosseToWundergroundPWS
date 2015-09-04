package org.michaelyeh.pws;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 *  Post weather data to the Wunderground website
 *
 *  http://wiki.wunderground.com/index.php/PWS_-_Upload_Protocol
 *
 *  example URL:
 *  http://weatherstation.wunderground.com/weatherstation/updateweatherstation.php?ID=KCASANFR5&PASSWORD=XXXXXX&dateutc=2000-01-01+10%3A32%3A35&winddir=230&windspeedmph=12&windgustmph=12&tempf=70&rainin=0&baromin=29.1&dewptf=68.2&humidity=90&weather=&clouds=&softwaretype=vws%20versionxx&action=updateraw
 *
 * @author Michael Yeh <myeh@hotmail.com>
 * @version $Revision: $
 *          Date: 11/6/14
 *          Time: 8:53 PM
 *          Last Modified by: $Author: $ on $Date:  $
 */
public class WundergroundClient
{

    /**
     * The CVS identification <code>String</code>. The Unix
     * <code>ident</code> and <code>strings</code> commands can be used to
     * display the contents of the <code>String</code> (and hence the
     * version), in either the .java or .class file.
     */
    public static final String Ident =
            "$Id: $";

    StringBuffer wundergroundUrl;

    public WundergroundClient(WeatherData data)
    {
        wundergroundUrl = new StringBuffer("http://weatherstation.wunderground.com/weatherstation/updateweatherstation.php?ID=");
        appendUrl(wundergroundUrl, Config.WUNDERGROUND_ID);

        wundergroundUrl.append("&PASSWORD=");
        appendUrl(wundergroundUrl, Config.WUNDERGROUND_PWD);

        wundergroundUrl.append("&dateutc=");
        appendUrl(wundergroundUrl, getGMT(Long.valueOf(data.timeStamp)));

        wundergroundUrl.append("&tempf=");
        appendUrl(wundergroundUrl, data.temperature);

        wundergroundUrl.append("&humidity=");
        appendUrl(wundergroundUrl, data.outdoorRelHum);

        wundergroundUrl.append("&winddir=");
        appendUrl(wundergroundUrl, data.windDirection);

        wundergroundUrl.append("&windspeedmph=");
        appendUrl(wundergroundUrl, data.windSpeed);

        wundergroundUrl.append("&windgustmph=");
        appendUrl(wundergroundUrl, data.windGust);

        wundergroundUrl.append("&rainin=");
        appendUrl(wundergroundUrl, data.rain1hr);

        wundergroundUrl.append("&dailyrainin=");
        appendUrl(wundergroundUrl, data.rain24hr);

        wundergroundUrl.append("&baromin=");
        appendUrl(wundergroundUrl, data.pressureRelative);

        wundergroundUrl.append("&dewptf=");
        appendUrl(wundergroundUrl, data.dewPoint);

        wundergroundUrl.append("&action=updateraw");
    }

    /**
     * URL encode parameters
     * @param sb StringBuffer containing URL
     * @param parameter String of parameter to be encoded
     */
    private void appendUrl(StringBuffer sb, String parameter)
    {
        if (sb == null || parameter == null)
            return;

        try
        {
            sb.append(URLEncoder.encode(parameter, "UTF-8"));
        }
        catch (UnsupportedEncodingException ignored)
        {
        }
    }

    /**
     * Wunderground expects the timestamp to be in GMT
     * @param time timestamp to be converted
     * @return String containing formatted time stamp in GMT
     */
    private static String getGMT(long time)
    {
        Date localTime = new Date(time * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(localTime);
    }

    /**
     * Post(Get request) the data to the Wunderground website
     */
    public void postData()
    {
        CloseableHttpClient httpclient = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
        try
        {
            HttpGet httpget = new HttpGet(wundergroundUrl.toString());
            CloseableHttpResponse response1 = httpclient.execute(httpget);
            int statusCode = response1.getStatusLine().getStatusCode();
            try
            {
                HttpEntity entity = response1.getEntity();

                if (statusCode == HttpStatus.SC_OK)
                {
                    String bodyAsString = EntityUtils.toString(entity);
                    EntityUtils.consume(entity);
                    System.out.println("Wunderground response = " + bodyAsString);
                }

            }
            finally
            {
                response1.close();
            }
        }
        catch (IOException e)
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
    }

    public static void main(String[] args)
    {
        try
        {
            new WundergroundClient(ParseData.readJSON(LaCrosseAlertsScraper.scrapeData())).postData();

            // read cached sample.xml instead of real time data
            //new WundergroundClient(ParseData.parseSample(args)).postData();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //@TODO send out a notification on errors
        }
    }
}
