package cn.ismartv.speedtester.utils;

public class StringUtilities {
    private static final String TAG = "StringUtilities";
    private static final String NORTH_CHINA_STRING = "北京市, 天津市, 河北省, 山西省, 内蒙古自治区";
    private static final String EAST_CHINA_STRING = "上海市,江苏省,安徽省,浙江省,福建省,江西省,山东省";
    private static final String SOUTH_CHINA_STRING = "广东省,广西壮族自治区,海南省,香港特区,澳门特区";
    private static final String CENTRAL_CHINA_STRING = "河南省,湖北省,湖南省";

    private static final int NORTH_CHINA_CODE = 1;
    private static final int EAST_CHINA_CODE = 2;
    private static final int SOUTH_CHINA_CODE = 3;
    private static final int CENTRAL_CHINA_CODE = 4;
    private static final int OTHERS_CODE = -100;

    private static final String NORTH_CHINA = "华北";
    private static final String EAST_CHINA = "华东";
    private static final String SOUTH_CHINA = "华南";
    private static final String CENTRAL_CHINA = "华中";

    private static final String CHINA_NET = "电信";
    private static final String CHINA_UNICOM = "联通";
    private static final String CHINA_MOBILE = "移动";

    private static final int CHINA_NET_CODE = 1;
    private static final int CHINA_UNICOM_CODE = 2;
    private static final int CHINA_MOBILE_CODE = 3;


    /**
     * string is empty
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(Object str) {

        return (str == null || "".equals(str));
    }

    public static int getAreaCodeByProvince(String province) {
        if (NORTH_CHINA_STRING.indexOf(province) != -1)
            return NORTH_CHINA_CODE;
        else if (EAST_CHINA_STRING.indexOf(province) != -1)
            return EAST_CHINA_CODE;
        else if (SOUTH_CHINA_STRING.indexOf(province) != -1)
            return SOUTH_CHINA_CODE;
        else if (CENTRAL_CHINA_STRING.indexOf(province) != -1)
            return CENTRAL_CHINA_CODE;
        return OTHERS_CODE;
    }

    public static int getAreaCodeByNode(String node) {
        if (node.indexOf(NORTH_CHINA) != -1) {
            return NORTH_CHINA_CODE;
        } else if (node.indexOf(EAST_CHINA) != -1)
            return EAST_CHINA_CODE;
        else if (node.indexOf(SOUTH_CHINA) != -1)
            return SOUTH_CHINA_CODE;
        else if (node.indexOf(CENTRAL_CHINA) != -1)
            return CENTRAL_CHINA_CODE;
        return OTHERS_CODE;
    }

    public static int getOperatorByNode(String node) {
        if (node.indexOf(CHINA_NET) != -1)
            return CHINA_NET_CODE;
        else if (node.indexOf(CHINA_UNICOM) != -1)
            return CHINA_UNICOM_CODE;
        else if (node.indexOf(CHINA_MOBILE) != -1)
            return CHINA_MOBILE_CODE;
        else return OTHERS_CODE;
    }
}