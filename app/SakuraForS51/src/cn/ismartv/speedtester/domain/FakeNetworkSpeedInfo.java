package cn.ismartv.speedtester.domain;

public class FakeNetworkSpeedInfo extends NetworkSpeedInfo {
	public void setSpeed(float speed) {
		this.speed = speed * 0.8f;
	}
}
