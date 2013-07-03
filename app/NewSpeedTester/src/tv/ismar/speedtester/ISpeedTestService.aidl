// ISpeedTestService.aidl
package tv.ismar.speedtester;

import tv.ismar.speedtester.ISpeedTestCallback;

/*
 * Copyright by iSmartv Inc.
 * Author: Bob Yuan
 * Version: 1.0
 */

/**
 * 
 */

interface ISpeedTestService {

	void registerClient(ISpeedTestCallback callback);
	
	/**
	 * Prepare meta data for speed test.
	 */
	void prepareSpeedTest();
	
	/**
	 * Get the current progress represents by a integer with a range from 0 to 100.
	 * this is the progress of current step.
	 * @return a integer with a range from 0 to 100.
	 */
	int getCurrentProgressOfStep();
	
	/**
	 * Get current step, a speed test may contain several steps.
	 * @return current step (start from zero, up to total steps minus one).
	 */
	int getCurrentStep();
	
	/**
	 * Get total steps count.
	 * @return total steps count.
	 */
	int getTotalSteps();
	
	/**
	 * Get currunt job represents the status of test in integer number.
	 * @return current job defined in {@link AppConstant}
	 */
	int getCurrentJob();
	
	/**
	 * Get the current speed in bytes per second
	 */
	float getCurrentSpeed();
	
	/**
	 * Stop the whole test task. May interrupt current running step.
	 */
	void stopSpeedTest();
	
	/**
	 * get speed test result. It's an average value of all the steps with display property enabled.
	 * @return a float value represents average speed.
	 */
	float getAverageSpeed();
}