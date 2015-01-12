gradle build

adb connect 192.168.16.127

adb install -r /Users/huaijie/IdeaProjects/Iris/Sakura/build/outputs/apk/Sakura-debug.apk

adb shell am start -n "cn.ismartv.speedtester/cn.ismartv.speedtester.ui.activity.MenuActivity"


