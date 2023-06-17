package src;

/**
 * Class for comparing dates.
 * Supports ISO8601 formate dateTime strings, such as "2023-06-01T06:51:07.837837Z"
 */
public class ISODateTime implements Comparable<ISODateTime>{
    int year, month, day, hour, minute;
    float second;
    
    public ISODateTime(String timeString){
        String date = timeString.split("T")[0];
        String time = timeString.split("T")[1];
        time = time.substring(0, time.length() - 1); // chop off the ending z

        String[] dateSplit = date.split("-");
        year = Integer.parseInt(dateSplit[0]);
        month = Integer.parseInt(dateSplit[1]);
        day = Integer.parseInt(dateSplit[2]);

        String[] timeSplit = time.split(":");
        hour = Integer.parseInt(timeSplit[0]);
        minute = Integer.parseInt(timeSplit[1]);
        second = Float.parseFloat(timeSplit[2]);
    }

    public long toLong(){
        int yearDiff = year - 1970;
        int monthDiff = month - 1;
        int dayDiff = day - 1;

        long yearMillis = (long)yearDiff * 3155695200l;
        long monthMillis = (long)monthDiff * 2629746000l;
        long dayMillis = (long)dayDiff * 86400000l;
        long hourMillis = (long)hour * 3600000l;
        long minuteMillis = (long)minute * 60000l;
        long secondMillis = (long)second * 1000l;

        return yearMillis + monthMillis + dayMillis + hourMillis + minuteMillis + secondMillis;
    }

    public String toString(){
        return year + "-" + month + "-" + day + ", " + hour + ":" + minute + ":" + second;
    }

    @Override
    public int compareTo(ISODateTime o) {
        // -1 if this < o, 0 if equal, 1 if this > o
        if(this.year == o.year){ // if years are equal compare months
            if(this.month == o.month){
                if(this.day == o.day){
                    if(this.hour == o.hour){
                        if(this.minute == o.minute){
                            if(this.second == o.second){
                                return 0;
                            }
                            return (int)((int)this.second - o.second);
                        }
                        return this.minute - o.minute;
                    }
                    return this.hour - o.hour;
                }
                return this.day - o.day;
            }
            return this.month - o.month;
        }
        return this.year - o.year; // otherwise result is in the years
    }
}
