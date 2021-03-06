package com.yangtengfei.pay.util;

import java.util.Calendar;

public class CalendarUtil {

    public static Calendar getCalendar(int day){
        Calendar calendar  = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH,day);
        return calendar;
    }


    public static void setSpecialDay(Calendar calendar,int day){
        calendar.set(Calendar.DAY_OF_MONTH,day);
    }

    public static void addMonth(Calendar calendar,int month){
        calendar.add(Calendar.MONTH,month);
    }

    public static void addDay(Calendar calendar,int day){
        calendar.add(Calendar.DATE,day);
    }


    public static int getLastDayOfThisMonth(){
	    Calendar ca = Calendar.getInstance();
	    ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));

	    int day = ca.get(Calendar.DAY_OF_MONTH);
	    System.out.print(day);
	    return day;
    }

	/*public static void main(String[] args) {
		getLastDayOfThisMonth();
	}*/
}
