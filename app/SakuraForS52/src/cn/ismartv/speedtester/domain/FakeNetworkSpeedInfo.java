package cn.ismartv.speedtester.domain;

public class FakeNetWorkSpeedInfo extends NetworkSpeedInfo {
	public void setSpeed(float speed) {
		this.speed = 0.8f * speed;
	}
}
