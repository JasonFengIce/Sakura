package cn.ismartv.speedtester.preferences.serializer;


/**
 * Created by huaijie on 11/18/14.
 */
public abstract class TypeSerializer {
    public abstract Class<?> getDeserializedType();

    public abstract Class<?> getSerializedType();

    public abstract Object serialize(Object data);

    public abstract Object deserialize(Object data);
}
