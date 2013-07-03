// ISpeedTestService.aidl
package tv.ismar.speedtester;

import java.lang.String;

/*
 * Copyright by iSmartv Inc.
 * Author: Bob Yuan
 * Version: 1.0
 */

 
 interface ISpeedTestCallback {
 	void onSpeedTestStart(int repeat);
 	void onStepStart(long length, int step);
 	void onStepFinish(int step);
 	void onStepInterrupted(int step, int reason, String message);
 	void onSpeedTestFinished(float avgSpeed);
 	void onSpeedTestCancelled();
 	void onError(int phase, String message);
 }