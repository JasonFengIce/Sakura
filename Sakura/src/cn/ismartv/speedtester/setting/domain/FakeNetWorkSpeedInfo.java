package cn.ismartv.speedtester.setting.domain;

public class FakeNetWorkSpeedInfo extends NetworkSpeedInfo {
	public void setSpeed(float speed) {
		this.speed = 0.8f * speed;
	}
}
