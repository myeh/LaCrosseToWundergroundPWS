LaCrosseToWundergroundPWS
=========================

Help post LaCrosse weather data to wunderground.com.

Just a little background.  I picked up a http://lacrossetechnology.com/c84612/ from Costco for less than a $100.  I was
a little bummed to find out the internet gateway only talks to the LaCrosse website and I wanted to have my data
available on the Wunderground website.  I wrote this app to help scrape the data off the LaCrosse website and post
it on the Wunderground website.  Once the data is uploaded, there's a great iPad app to view all the data
https://itunes.apple.com/us/app/wunderstation-weather-from/id906099986?mt=8


Once the binary is created, you can setup a cronjob to run every hour to continually post new data to the Wunderground website.
*/60 * * * * java -jar pws.jar
