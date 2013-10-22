// ISpeedTestService.aidl
package cn.ismartv.speedtester;

import java.lang.String;

/*
 * Copyright by iSmartv Inc.
 * Author: Bob Yuan
 * Version: 1.0
 */

 
 interface ISpeedTestCallback {
 	/**
 	 * When test data has ready after client invoke the {@link ISpeedTestService.prepareSpeedTest()} method.
 	 * @param repeatCount  total steps of this test.
 	 */
	void onTestReady(int repeatCount);
	
	/**
	 * When a step has started to download data.
	 * @param length  duration of current step
	 * @param step   current step (from 0)
	 */
 	void onStepStart(long length, int step);
 	/**
 	 * When a step has finished.
 	 * @param step current step (from 0)
 	 */
 	void onStepFinish(int step);
 	/**
 	 * When an error occurred, step has been interrupted.
 	 * @param step current step (from 0)
 	 * @param reason
 	 * @param message  error message
 	 */
 	void onStepInterrupted(int step, int reason, String message);
 	/**
 	 * When test has accomplished.
 	 * @param avgSpeed  the average speed of all steps.
 	 */
 	void onSpeedTestFinished(float avgSpeed);
 	/**
 	 * When test has been cancelled manually.
 	 */
 	void onSpeedTestCancelled();
 	/**
 	 * When a critical exception has occurred. the test task usually auto cancelled when this happens.
 	 * @param phase  indicate what JOB currently been deal with.
 	 */
 	void onError(int phase, String message);
 	/**
 	 * Update current speed with a solid interval.
 	 * @param current speed of the interval.
 	 */
 	void onUpdateSpeed(float speed);
 }