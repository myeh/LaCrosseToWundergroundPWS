package org.michaelyeh.pws;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Parses XML data received from the LaCrossAlerts website and
 * returns the data in a WeatherData object
 *
 * @author Michael Yeh <myeh@hotmail.com>
 * @version $Revision: $
 *          Date: 11/6/14
 *          Time: 11:34 PM
 *          Last Modified by: $Author: $ on $Date:  $
 */
public class ParseData
{
    static XPath xpathParser;
    static DocumentBuilder builder;

    static
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            xpathParser = XPathFactory.newInstance().newXPath();
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Parses XML and returns a WeatherData object
     * @param xml String containing weather values scraped from LaCrosse website
     * @return WeatherData object with the populated values
     * @throws Exception
     */
    public static WeatherData read(String xml) throws Exception
    {
        try
        {
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            String returnValue = xpathParser.evaluate("/data/success/text()", doc);

            if (!returnValue.equals("true"))
                throw new Exception("No Data");

            WeatherData data = new WeatherData();

            data.timeStamp = xpathParser.evaluate("/data/response/obs/ob/timeStamp/text()", doc);

            data.temperature = xpathParser.evaluate("/data/response/obs/ob/values/outdoor/temp/text()", doc);

            data.outdoorRelHum = xpathParser.evaluate("/data/response/obs/ob/values/outdoor/rh/text()", doc);

            data.dewPoint = xpathParser.evaluate("/data/response/obs/ob/values/outdoor/dewpoint/text()", doc);

            data.windSpeed = xpathParser.evaluate("/data/response/obs/ob/values/outdoor/windSpeed/text()", doc);

            data.windDirection = xpathParser.evaluate("/data/response/obs/ob/values/outdoor/windDirection/text()", doc);

            data.windGust = xpathParser.evaluate("/data/response/obs/ob/values/outdoor/windGust/text()", doc);

            data.rain1hr = xpathParser.evaluate("/data/response/obs/ob/values/outdoor/rain1hr/text()", doc);

            data.rain24hr = xpathParser.evaluate("/data/response/obs/ob/values/outdoor/rain24hr/text()", doc);

            data.rainWeek = xpathParser.evaluate("/data/response/obs/ob/values/outdoor/rainWeek/text()", doc);

            data.rainMonth = xpathParser.evaluate("/data/response/obs/ob/values/outdoor/rainMonth/text()", doc);

            data.rainTotal = xpathParser.evaluate("/data/response/obs/ob/values/outdoor/rainTotal/text()", doc);

            data.pressureRelative = xpathParser.evaluate("/data/response/obs/ob/values/outdoor/pressureRelative/text()", doc);

            return data;
        }
        catch (SAXException | XPathExpressionException | IOException e)
        {
            e.printStackTrace();
        }

        throw new Exception("Unable to parse data");
    }

    public static WeatherData main(String[] args)
    {
        try
        {
            // try parsing sample.xml passed in from command line
            byte[] encoded = Files.readAllBytes(Paths.get(args[0]));
            String xml = new String(encoded, StandardCharsets.UTF_8);
            return read(xml);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
