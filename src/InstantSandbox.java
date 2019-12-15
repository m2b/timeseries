import org.apache.commons.lang.time.DateUtils;

import java.text.ParseException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class InstantSandbox {
    public static void main(String[] args) {
        String[] formats= {
                "yyyy-MM-dd H:m:s",
                "yyyy-MM-dd H:m:s.S",
                "yyyy-MM-dd h:m:s a",
                "yyyy-MM-dd h:m:s.S a",
                "yyyy/MM/dd H:m:s",
                "yyyy/MM/dd H:m:s.S",
                "yyyy/MM/dd h:m:s a",
                "yyyy/MM/dd h:m:s.S a",
                "MMM d, yyyy H:m:s",
                "MMM d, yyyy H:m:s.S",
                "MMM d, yyyy H:m:s a",
                "MMM d, yyyy H:m:s.S a"
        };
        String[] sds= {
                "1959-01-01 23:59:02",
                "1959-01-01 23:59:01.999",
                "1959-01-01 11:59:01.999 AM",
                "1959/01/01 23:59:01.999",
                "1959/01/01 11:59:01.999 AM",
                "Jan 01, 1959 11:59:01.999 AM",
                "2007-12-03T10:15:30.00Z",
                "2018-07-20T19:00:00.00Z",
                "2018-7-20T19:00:00.00Z",  // this will fail expect 0 padded
                "2007-12-03T10:15:30-08:00",
                "2007-12-03T10:15:30+05:00",
                "2018-03-11 02:23:46.973047",
                "2018-03-11 02:23:46.973",
                "2018-03-11T02:23:46.973Z"
        };
        System.out.println("System ZoneId="+ZoneId.systemDefault());
        for(String s:sds)
        {
            Date df=null;
            Instant ist=null;
            /*
            for(String format:formats)
            {
                DateTimeFormatter formatter=DateTimeFormatter.ofPattern(format);
                try
                {
                    df=LocalDateTime.parse(s,formatter);
                    break;
                }
                catch(DateTimeParseException pe)
                { }
            }
            */
            try
            {
                df=DateUtils.parseDateStrictly(s,formats);
            }
            catch(ParseException pe){}
            if(df==null) {
                try {
                    ZonedDateTime zdt=ZonedDateTime.parse(s,DateTimeFormatter.ISO_DATE_TIME);
                    ist=zdt.toInstant();
                } catch (DateTimeException pe) {
                    System.out.println("Unable to parse: " + s);
                    continue;
                }
            }
            else
            {
                try {
                    ist=df.toInstant();
                } catch (DateTimeException pe) {
                    System.out.println("Unable to parse: " + s);
                    continue;
                }
            }
            System.out.println("Got instant: "+ist);
        }
    }
}