package cn.ismartv.speedtester.setting;

public class FakeNetworkSpeedInfo extends NetworkSpeedInfo {
	public void setSpeed(float speed) {
		this.speed = speed * 0.8f;
	}
}
