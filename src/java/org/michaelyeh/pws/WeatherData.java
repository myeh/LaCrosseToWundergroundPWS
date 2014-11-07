package org.michaelyeh.pws;

/**
 * Value object to encapsulate weather data
 *
 * @author Michael Yeh <myeh@hotmail.com>
 * @version $Revision: $
 *          Date: 11/6/14
 *          Time: 7:30 PM
 *          Last Modified by: $Author: $ on $Date:  $
 */
public class WeatherData
{

    /**
     * The CVS identification <code>String</code>. The Unix
     * <code>ident</code> and <code>strings</code> commands can be used to
     * display the contents of the <code>String</code> (and hence the
     * version), in either the .java or .class file.
     */
    public static final String Ident =
            "$Id: $";

    public String timeStamp;
    public String temperature;
    public String outdoorRelHum;
    public String dewPoint;
    public String windSpeed;
    public String windDirection;
    public String windGust;
    public String rain1hr;
    public String rain24hr;
    public String rainWeek;
    public String rainMonth;
    public String rainTotal;
    public String pressureRelative;

}
