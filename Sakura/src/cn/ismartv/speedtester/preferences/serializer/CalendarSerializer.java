package cn.ismartv.speedtester.preferences.serializer;

import java.util.Calendar;

/**
 * Created by huaijie on 11/18/14.
 */
public class CalendarSerializer extends TypeSerializer {
    @Override
    public Class<?> getDeserializedType() {
        return Calendar.class;
    }

    @Override
    public Class<?> getSerializedType() {
        return long.class;
    }

    @Override
    public Object serialize(Object data) {
        return ((Calendar) data).getTimeInMillis();
    }

    @Override
    public Object deserialize(Object data) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((Long) data);
        return calendar;
    }
}
