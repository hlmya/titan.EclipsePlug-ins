package org.eclipse.titan.runtime.core;

import org.eclipse.titan.runtime.core.TitanVerdictType.VerdictTypeEnum;

/**
 * TTCN-3 runtime class
 * 
 * TODO: lots to implement
 * 
 * @author Kristof Szabados
 */
public class TTCN_Runtime {

	public static void begin_testcase(final boolean hasTimer, final TitanFloat timerValue) {
		//FIXME this is much more complex

		if (hasTimer) {
			TitanTimer.testcaseTimer.start(timerValue.getValue());
		}
	}

	public static TitanVerdictType end_testcase() {
		TitanTimer.testcaseTimer.stop();

		//FIXME this is more complex
		return new TitanVerdictType(VerdictTypeEnum.NONE);
	}

	public static void mapPort(final String sourePort, final String destinationPort) {
		//FIXME implement
		TitanPort.mapPort(sourePort, destinationPort);
	}

	public static void unmapPort(final String sourePort, final String destinationPort) {
		//FIXME implement
		TitanPort.unmapPort(sourePort, destinationPort);
	}
}
