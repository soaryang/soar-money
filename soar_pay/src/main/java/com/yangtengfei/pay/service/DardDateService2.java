package com.yangtengfei.pay.service;

import com.alibaba.fastjson.JSON;
import com.yangtengfei.pay.bean.Card;
import com.yangtengfei.pay.payconst.BankTypeEnum;
import com.yangtengfei.pay.payconst.PayTypeEnum;
import com.yangtengfei.pay.util.CalendarUtil;
import com.yangtengfei.pay.util.DateUtil;
import com.yangtengfei.pay.view.CardView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Slf4j
@Service
public class DardDateService2 {

    @Autowired
    private CardService cardService;

    public void setPayDay(Calendar calendar, Card card, CardView cardView, int day) {
        if (card.getIsfixDate() == 0) {
            isFixDate(calendar, card, cardView, day);
        } else {
            isNotFixDate(card, cardView, calendar, cardView.getPayDate());
        }
    }

    private void isFixDate(Calendar calendar, Card card, CardView cardView, int day) {
        int subDay = 0;
        //log.info("card:{}",JSON.toJSONString(card));
        //还款日
        if (card.getPayDate() <= day) {
            //当前日期
            Calendar calendarTemp = Calendar.getInstance();
            CalendarUtil.addMonth(calendarTemp, 1);
            CalendarUtil.setSpecialDay(calendarTemp, card.getPayDate());
            cardView.setPayDay(DateUtil.calendarToString(calendarTemp, DateUtil.YYYY_MM_DD));

            System.out.print((calendarTemp.getTimeInMillis() - calendar.getTimeInMillis()) / (1000 * 60 * 60 * 24) - 1);
            subDay = (int) ((calendarTemp.getTimeInMillis() - calendar.getTimeInMillis()) / (1000 * 60 * 60 * 24) - 1);
            calendarTemp.add(Calendar.DATE, -1);
            cardView.setPutMonyDayStr(DateUtil.calendarToString(calendarTemp, DateUtil.YYYY_MM_DD));
        } else {
            subDay = card.getPayDate() - day;
            Calendar calendarTemp = Calendar.getInstance();
            CalendarUtil.setSpecialDay(calendarTemp, card.getPayDate());
            cardView.setPayDay(DateUtil.calendarToString(calendarTemp, DateUtil.YYYY_MM_DD));

            CalendarUtil.addDay(calendarTemp, -1);
            cardView.setPutMonyDayStr(DateUtil.calendarToString(calendarTemp, DateUtil.YYYY_MM_DD));
        }
        //距离还款时间
        cardView.setSubPayDay(subDay - 1);
        log.info("cardView:{}", JSON.toJSONString(cardView));
    }

    private void isNotFixDate(Card card, CardView cardView, Calendar calendar,int payDate) {
        Calendar calendarTemp = Calendar.getInstance();
        //设置账单日
        CalendarUtil.setSpecialDay(calendarTemp, card.getAccountDate());
        //获取当前账单日
        int accountDay = calendarTemp.get(Calendar.DAY_OF_MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        if (card.getCardName().contains("浦东")) {
            log.info("accountDay:{}", accountDay);
        }
        //设置还款日
        if(payDate!=0){
            CalendarUtil.addDay(calendarTemp, payDate);
        }else{
            CalendarUtil.addDay(calendarTemp, 20);
        }


        int subDay = 0;
        //if(calendarTemp.getTime().after(calendar.getTime())){
        if (accountDay > currentDay) {
            log.info("账单日在当前时间之后");
            //获取上个账单日。来计算本月的还款日
            Calendar currentCalendar = Calendar.getInstance();

            CalendarUtil.addMonth(currentCalendar, -1);
            CalendarUtil.setSpecialDay(currentCalendar, card.getAccountDate());

            CalendarUtil.addDay(currentCalendar, 20);
            cardView.setPayDay(DateUtil.calendarToString(currentCalendar, DateUtil.YYYY_MM_DD));
            //存钱日期
            CalendarUtil.addDay(currentCalendar, -1);
            cardView.setPutMonyDayStr(DateUtil.calendarToString(currentCalendar, DateUtil.YYYY_MM_DD));

            subDay = (int) ((currentCalendar.getTimeInMillis() - calendar.getTimeInMillis()) / (1000 * 60 * 60 * 24));
            cardView.setSubPayDay(subDay);
        } else {
            log.info("账单日在当前时间之前");
            Calendar calendarAccount = Calendar.getInstance();
            //判断账单日+ 指定的20天是否大于最后一天
            CalendarUtil.setSpecialDay(calendarAccount, card.getAccountDate());
            CalendarUtil.addDay(calendarAccount, 20);

            //还款日期
            cardView.setPayDay(DateUtil.calendarToString(calendarTemp, DateUtil.YYYY_MM_DD));

            //存钱日期
            CalendarUtil.addDay(calendarAccount, -1);
            cardView.setPutMonyDayStr(DateUtil.calendarToString(calendarAccount, DateUtil.YYYY_MM_DD));

            subDay = (int) ((calendarAccount.getTimeInMillis() - calendar.getTimeInMillis()) / (1000 * 60 * 60 * 24) - 1);
            cardView.setSubPayDay(subDay);





            /*CalendarUtil.addMonth(calendarAccount,1);
            CalendarUtil.setSpecialDay(calendarAccount,card.getAccountDate());
            CalendarUtil.addDay(calendarAccount,20);
            cardView.setPutMonyDay(DateUtil.calendarToString(calendarAccount,DateUtil.YYYY_MM_DD));
            subDay =(int)((calendarAccount.getTimeInMillis() - calendar.getTimeInMillis())/(1000*60*60*24)-1);
            cardView.setSubPayDay(subDay-1);
            log.info("cardName:{},before:{}",card.getCardName(),calendarTemp.getTime());
            CalendarUtil.addDay(calendarTemp,-1);
            cardView.setPayDay(DateUtil.calendarToString(calendarTemp,DateUtil.YYYY_MM_DD));*/
        }
    }

    public List<CardView> findCardViewList() {

        List<CardView> cardViewList = new ArrayList<>();
        List<Card> cards = cardService.findAll();
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (cards != null && cards.size() > 0) {
            for (Card card : cards) {
                CardView cardView = new CardView();
                BeanUtils.copyProperties(card, cardView);
                cardView.setBank(BankTypeEnum.getName(card.getBankType()));
                cardView.setPayType(PayTypeEnum.getName(card.getPayType()));
                int subDay = 0;
                setPayDay(calendar, card, cardView, day);
                //是否紧急
                subDay = cardView.getSubPayDay();
                //是否能取现
                if (card.getPayType() == PayTypeEnum.CREDIT_CARD.getIndex() || card.getPayType() == PayTypeEnum.JINGDONGBAITIAO.getIndex()) {
                    int subAccountDay = 0;
                    if (card.getAccountDate() < day) {
                        Calendar calendarTemp = Calendar.getInstance();
                        CalendarUtil.addMonth(calendarTemp, 1);
                        CalendarUtil.setSpecialDay(calendarTemp, card.getAccountDate());
                        System.out.print((calendarTemp.getTimeInMillis() - calendar.getTimeInMillis()) / (1000 * 60 * 60 * 24) - 1);
                        subAccountDay = (int) ((calendarTemp.getTimeInMillis() - calendar.getTimeInMillis()) / (1000 * 60 * 60 * 24) - 1);

                        cardView.setAccountDay(DateUtil.calendarToString(calendarTemp, DateUtil.YYYY_MM_DD));
                    } else {
                        subAccountDay = card.getAccountDate() - day;
                        Calendar calendarTemp = Calendar.getInstance();
                        CalendarUtil.setSpecialDay(calendarTemp, card.getAccountDate());
                        cardView.setAccountDay(DateUtil.calendarToString(calendarTemp, DateUtil.YYYY_MM_DD));
                    }

                    if (card.getPayType() == PayTypeEnum.CREDIT_CARD.getIndex()) {
                        if (subAccountDay > 20) {
                            cardView.setIsgetMoney(true);
                        }
                    }
                    cardView.setSubNexAccountDay(subAccountDay);
                }

                if (subDay <= 3 & subDay >= 0) {
                    cardView.setIsemergent(true);
                    //paycardViewList.add(cardView);
                } else {
                    //notPaycardViewList.add(cardView);
                }
                cardViewList.add(cardView);
            }
            //cardViewList.addAll(paycardViewList);
            //cardViewList.addAll(notPaycardViewList);
        }
        return cardViewList;
    }
}
