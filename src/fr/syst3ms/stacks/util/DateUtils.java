package fr.syst3ms.stacks.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by ARTHUR on 28/08/2017.
 */
public class DateUtils {
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
}
