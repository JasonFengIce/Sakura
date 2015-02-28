package cn.ismartv.speedtester.core.qrcode;

/**
 * Created by huaijie on 2/28/15.
 */
public class QRCodeUtils {

//    public void createQRImage(String url) {

//
//        int QR_WIDTH = 200;
//        int QR_HEIGHT = 200;
//
//
//        try {
//            //判断URL合法性
//            if (url == null || "".equals(url) || url.length() < 1) {
//                return;
//            }
//            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
//            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
//            //图像数据转换，使用了矩阵转换
//            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
//
//            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
//
////            Matrix matrix = new Matrix();
////            matrix.setScale(LOGO_WIDTH, LOGO_HEIGHT);
//
//
//            Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
//
//            int LOGO_WIDTH = logo.getWidth();
//            int LOGO_HEIGHT = logo.getHeight();
////            logo = Bitmap.createBitmap(logo, 0, 0, LOGO_WIDTH, LOGO_HEIGHT);
//
//
//            Log.d(TAG, "bitmap x ---> " + logo.getWidth() + "bitmap y ---> " + logo.getHeight());
//
//
//            //下面这里按照二维码的算法，逐个生成二维码的图片，
//            //两个for循环是图片横列扫描的结果
//            for (int y = 0; y < QR_HEIGHT; y++) {
//                for (int x = 0; x < QR_WIDTH; x++) {
//                    if (x > QR_WIDTH / 2 - LOGO_WIDTH / 2 &&
//                            x < QR_WIDTH / 2 + LOGO_WIDTH / 2 &&
//                            y > QR_HEIGHT / 2 - LOGO_HEIGHT / 2 &&
//                            y < QR_HEIGHT / 2 + LOGO_HEIGHT / 2) {
//
//
//                        pixels[y * QR_WIDTH + x] = logo.getPixel(x - (QR_WIDTH - LOGO_WIDTH) / 2, y - (QR_HEIGHT - LOGO_HEIGHT) / 2);
//
//
//                    } else {
//                        pixels[y * QR_WIDTH + x] = bitMatrix.get(x, y) ? 0xff000000 : 0xffffffff;
//                    }
//
//                }
//            }
//            //生成二维码图片的格式，使用ARGB_8888
//            Bitmap mBitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
//            mBitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
////            //显示到一个ImageView上面
//
//
//            image.setImageBitmap(mBitmap);
//        } catch (WriterException e) {
//            e.printStackTrace();
//        }
//    }
}
