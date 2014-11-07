package org.michaelyeh.pws;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.List;


/**
 * // See http://www.cookiecentral.com/faq/#3.5
 *
 * @author Michael Yeh <myeh@hotmail.com>
 * @version $Revision: $
 *          Date: 11/7/14
 *          Time: 6:58 PM
 *          Last Modified by: $Author: $ on $Date:  $
 */
public class PersistentCookieStore implements CookieStore, Runnable
{
    static File file;

    static
    {
        try
        {
            file = new File(System.getProperty("java.io.tmpdir") + "/cookies.txt");
            file.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    CookieStore store;

    public PersistentCookieStore()
    {
        // get the default in memory cookie store
        store = new BasicCookieStore();

        // read in cookies from persistent storage and add them store
        synchronized (file)
        {
            FileReader fr = null;
            try
            {
                fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String s;
                while ((s = br.readLine()) != null)
                {
                    String[] value = s.split("\t");
                    BasicClientCookie cookie = new BasicClientCookie(value[5], value[6]);
                    cookie.setVersion(0);
                    cookie.setDomain(value[0]);
                    cookie.setPath(value[2]);
                    if (value[4] != null && !value[4].equals("0"))
                        cookie.setExpiryDate(new Date(value[4]));
                    cookie.setSecure(Boolean.valueOf(value[3]));

                    store.addCookie(cookie);
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
                    if (fr != null)
                        fr.close();
                }
                catch (IOException ignored)
                {
                }
            }
        }

        // add a shutdown hook to write out the in memory cookies
        Runtime.getRuntime().addShutdownHook(new Thread(this));
    }


    /**
     *  Write cookies in store to persistent storage
     */
    @Override
    public void run()
    {
        synchronized (file)
        {
            FileWriter writer = null;
            try
            {
                writer = new FileWriter(file);
                List<Cookie> cookies = getCookies();
                for (Cookie cookie : cookies)
                {
                    write(writer, cookie.getDomain());  // Index[0]
                    writer.write("\t");
                    writer.write("TRUE"); //1
                    writer.write("\t");
                    write(writer, cookie.getPath()); //2
                    writer.write("\t");
                    write(writer, String.valueOf(cookie.isSecure()).toUpperCase());  //3
                    writer.write("\t");
                    write(writer, (cookie.getExpiryDate() != null ? cookie.getExpiryDate().toString() : "0")); //4
                    writer.write("\t");
                    write(writer, cookie.getName()); //5
                    writer.write("\t");
                    write(writer, cookie.getValue()); //6
                    writer.write("\n");
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
                    if (writer != null)
                    {
                        writer.flush();
                        writer.close();
                    }
                }
                catch (IOException ignored)
                {
                }
            }
        }


    }

    private void write(Writer writer, String value) throws IOException
    {
        if (value != null)
            writer.write(value);
    }


    @Override
    public void addCookie(Cookie cookie)
    {
        store.addCookie(cookie);
    }

    public List<Cookie> getCookies()
    {
        return store.getCookies();
    }

    @Override
    public boolean clearExpired(Date date)
    {
        return store.clearExpired(date);
    }

    @Override
    public void clear()
    {
        store.clear();
    }

    public static void main(String[] args)
    {
        BasicClientCookie cookie = new BasicClientCookie("name", "value");
        cookie.setVersion(0);
        cookie.setDomain(".mycompany.com");
        cookie.setPath("/");

        PersistentCookieStore cookieStore = new PersistentCookieStore();
        cookieStore.addCookie(cookie);
    }
}
