/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

import org.eclipse.titan.runtime.core.TitanLoggerApi.DefaultEvent_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.DefaultOp;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Dualface__mapped;
import org.eclipse.titan.runtime.core.TitanLoggerApi.FunctionEvent_choice_random;
import org.eclipse.titan.runtime.core.TitanLoggerApi.LogEventType_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingEvent_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingFailureType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingProblemType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingSuccessType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingTimeout;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Msg__port__recv;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Msg__port__send;
import org.eclipse.titan.runtime.core.TitanLoggerApi.PortEvent_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Port__Misc;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Port__Queue;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Port__State;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Proc__port__in;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Proc__port__out;
import org.eclipse.titan.runtime.core.TitanLoggerApi.SetVerdictType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.StatisticsType_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TestcaseEvent_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TimerEvent_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TimerGuardType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TimerType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.VerdictOp_choice;
import org.eclipse.titan.runtime.core.TitanVerdictType.VerdictTypeEnum;
import org.eclipse.titan.runtime.core.TtcnLogger.Severity;

/**
 * A logger plugin implementing the legacy logger behaviour.
 *
 * FIXME lots to implement here, this is under construction right now
 *
 * @author Kristof Szabados
 */
public class LegacyLogger implements ILoggerPlugin {
	/**
	 * This function represents the entry point for the legacy style logger plugin.
	 * (still embedded in this generic class while transitioning the design)
	 * */
	public void log(final TitanLoggerApi.TitanLogEvent event, final boolean log_buffered, final boolean separate_file, final boolean use_emergency_mask) {
		if (separate_file) {
			//FIXME implement
		}

		final int severityIndex = event.getSeverity().getInt();
		final Severity severity = Severity.values()[severityIndex];
		if (use_emergency_mask) {
			//FIXME implement file logging
			if (TtcnLogger.should_log_to_console(severity)) {
				log_console(event, severity);
			}
		} else {
			//FIXME implement file logging
			if (TtcnLogger.should_log_to_console(severity)) {
				log_console(event, severity);
			}
		}
	}

	/**
	 * The log_console function from the legacy logger.
	 *
	 * Not the final implementation though.
	 * */
	private static boolean log_console(final TitanLoggerApi.TitanLogEvent event, final Severity msg_severity) {
		//FIXME once we have objects calculating the time will have to be moved earlier.
		//FIXME a bit more complicated in reality

		//Time
		final String event_str = event_to_string(event, true);
		if (event_str == null) {
			//FIXME write warning
			return false;
		}

		System.out.println(event_str);

		return true;
	}

	private static void append_header(final StringBuilder returnValue, final int seconds, int microseconds) {
		TtcnLogger.mputstr_timestamp(returnValue, TtcnLogger.get_timestamp_format(), seconds, microseconds);

		returnValue.append(' ');
		//FIXME implement rest of the header
	}

	private static String event_to_string(final TitanLoggerApi.TitanLogEvent event, final boolean without_header) {
		//FIXME implement proper header handling
		final StringBuilder returnValue = new StringBuilder();

		append_header(returnValue, event.getTimestamp().getSeconds().getInt(), event.getTimestamp().getMicroSeconds().getInt());

		final LogEventType_choice choice = event.getLogEvent().getChoice();
		switch(choice.get_selection()) {
		case UNBOUND_VALUE:
			return returnValue.toString();
		case ALT_UnhandledEvent:
			returnValue.append(choice.getUnhandledEvent().getValue());
			break;
		case ALT_TimerEvent:
			timer_event_str(returnValue, choice.getTimerEvent().getChoice());
			break;
		case ALT_VerdictOp:
			verdictop_str(returnValue, choice.getVerdictOp().getChoice());
			break;
		case ALT_Statistics:
			statistics_str(returnValue, choice.getStatistics().getChoice());
			break;
		case ALT_TestcaseOp:
			testcaseop_str(returnValue, choice.getTestcaseOp().getChoice());
			break;
		case ALT_DefaultEvent:
			defaultop_event_str(returnValue, choice.getDefaultEvent().getChoice());
			break;
		case ALT_MatchingEvent:
			matchingop_str(returnValue, choice.getMatchingEvent().getChoice());
			break;
		case ALT_PortEvent:
			portevent_str(returnValue, choice.getPortEvent().getChoice());
			break;
		case ALT_FunctionEvent: {
			switch (choice.getFunctionEvent().getChoice().get_selection()) {
			case ALT_Random : {
				final FunctionEvent_choice_random ra = choice.getFunctionEvent().getChoice().getRandom();
				switch (ra.getOperation().enum_value) {
				case seed:
					returnValue.append(MessageFormat.format( "Random number generator was initialized with seed {0}: {1}", ra.getRetval().getValue(), ra.getIntseed().getInt()));
					break;
				case read__out:
					returnValue.append(MessageFormat.format("Function rnd() returned {0}.", ra.getRetval().getValue()));
					break;
				case UNBOUND_VALUE:
				case UNKNOWN_VALUE:
				default:
					break;
				}
				break;
			}
			default:
				break;
			}
			break;
		}
		//FIXME implement missing branches
		}
		return returnValue.toString();
	}

	private static void timer_event_str(final StringBuilder returnValue, final TimerEvent_choice choice) {
		switch (choice.get_selection()) {
		case ALT_ReadTimer:{
			final TimerType timer = choice.getReadTimer();
			returnValue.append(MessageFormat.format("Read timer {0}: {1} s", timer.getName().getValue(), timer.getValue__().getValue()));
			break;}
		case ALT_StartTimer: {
			final TimerType timer = choice.getStartTimer();
			returnValue.append(MessageFormat.format("Start timer {0}: {1} s", timer.getName().getValue(), timer.getValue__().getValue()));
			break;}
		case ALT_GuardTimer: {
			final TimerGuardType timer = choice.getGuardTimer();
			returnValue.append(MessageFormat.format("Test case guard timer was set to {0} s", timer.getValue__().getValue()));
			break;}
		case ALT_StopTimer: {
			final TimerType timer = choice.getStopTimer();
			returnValue.append(MessageFormat.format("Stop timer {0}: {1} s", timer.getName().getValue(), timer.getValue__().getValue()));
			break;}
		case ALT_TimeoutTimer: {
			final TimerType timer = choice.getTimeoutTimer();
			returnValue.append(MessageFormat.format("Timeout {0}: {1} s", timer.getName().getValue(), timer.getValue__().getValue()));
			break;}
		case ALT_TimeoutAnyTimer: {
			returnValue.append("Operation `any timer.timeout' was successful.");
			break;}
		case ALT_UnqualifiedTimer: {
			returnValue.append(choice.getUnqualifiedTimer().getValue());
			break;}
		//FIXME implement missing branches
		}
	}

	private static void defaultop_event_str(final StringBuilder returnValue, final DefaultEvent_choice choice) {
		switch(choice.get_selection()) {
		case ALT_DefaultopActivate: {
			final DefaultOp dflt = choice.getDefaultopActivate();
			returnValue.append(MessageFormat.format("Altstep {0} was activated as default, id {1}", dflt.getName().getValue(), dflt.getId().getInt()));
			break;
		}
		//FIXME implement missing branches
		}
	}

	private static void verdictop_str(final StringBuilder returnValue, final VerdictOp_choice choice) {
		final SetVerdictType set = choice.getSetVerdict();
		final int newOrdinal = set.getNewVerdict().enum_value.ordinal();
		final String newVerdictName = VerdictTypeEnum.values()[newOrdinal].getName();
		final int oldOrdinal = set.getOldVerdict().enum_value.ordinal();
		final String oldVerdictName = VerdictTypeEnum.values()[oldOrdinal].getName();
		final int localOrdinal = set.getLocalVerdict().enum_value.ordinal();
		final String localVerdictName = VerdictTypeEnum.values()[localOrdinal].getName();

		if (set.getNewVerdict().isGreaterThan(set.getOldVerdict())) {
			if (!set.getOldReason().isPresent() || !set.getNewReason().isPresent()) {
				returnValue.append(MessageFormat.format("setverdict({0}): {1} -> {2}", newVerdictName, oldVerdictName, localVerdictName));
			} else {
				returnValue.append(MessageFormat.format("setverdict({0}): {1} -> {2} reason: \"{3}\", new component reason: \"{4}\"", newVerdictName, oldVerdictName, localVerdictName, set.getOldReason().get().getValue(), set.getNewReason().get().getValue()));
			}
		} else {
			if (!set.getOldReason().isPresent() || !set.getNewReason().isPresent()) {
				returnValue.append(MessageFormat.format("setverdict({0}): {1} -> {2} component reason not changed", newVerdictName, oldVerdictName, localVerdictName));
			} else {
				returnValue.append(MessageFormat.format("setverdict({0}): {1} -> {2} reason: \"{3}\", component reason not changed", newVerdictName, oldVerdictName, localVerdictName, set.getOldReason().get().getValue()));
			}
		}
	}

	private static void statistics_str(final StringBuilder returnValue, final StatisticsType_choice choice) {
		switch(choice.get_selection()) {
		case ALT_ControlpartStart:
			returnValue.append(MessageFormat.format("Execution of control part in module {0} started.", choice.getControlpartStart().getValue()));
			break;
		case ALT_ControlpartFinish:
			returnValue.append(MessageFormat.format("Execution of control part in module {0} finished.", choice.getControlpartFinish().getValue()));
			break;
			//FIXME implement the rest of the branches
		}
	}

	private static void testcaseop_str(final StringBuilder returnValue, final TestcaseEvent_choice choice) {
		switch(choice.get_selection()) {
		case ALT_TestcaseStarted:
			returnValue.append(MessageFormat.format("Test case {0} started.", choice.getTestcaseStarted().getTestcase__name().getValue()));
			break;
		case ALT_TestcaseFinished:
			final int ordinal = choice.getTestcaseFinished().getVerdict().enum_value.ordinal();
			final String verdictName = VerdictTypeEnum.values()[ordinal].getName();
			returnValue.append(MessageFormat.format("Test case {0} finished. Verdict: {1}", choice.getTestcaseFinished().getName().getTestcase__name().getValue(), verdictName));
			break;
		case UNBOUND_VALUE:
		default:
			break;
		}
	}

	private static void matchingop_str(final StringBuilder returnValue, final MatchingEvent_choice choice) {
		switch (choice.get_selection()) {
		case ALT_MatchingTimeout: {
			final MatchingTimeout mt = choice.getMatchingTimeout();
			if (mt.getTimer__name().isPresent()) {
				returnValue.append(MessageFormat.format("Timeout operation on timer {0} failed: The timer is not started.", mt.getTimer__name().get().getValue()));
			} else {
				returnValue.append("Operation `any timer.timeout' failed: The test component does not have active timers.");
			}
			break;
		}
		case ALT_MatchingFailure: {
			final MatchingFailureType mf = choice.getMatchingFailure();
			boolean is_call = false;
			switch (mf.getReason().enum_value) {
			case message__does__not__match__template:
				returnValue.append(MessageFormat.format("Matching on port {0} {1}: First message in the queue does not match the template: ", mf.getPort__name().getValue(), mf.getInfo().getValue()));
				break;
			case exception__does__not__match__template:
				returnValue.append(MessageFormat.format("Matching on port {0} failed: The first exception in the queue does not match the template: {1}", mf.getPort__name().getValue(), mf.getInfo().getValue()));
				break;
			case parameters__of__call__do__not__match__template:
				is_call = true; // fall through
			case parameters__of__reply__do__not__match__template:
				returnValue.append(MessageFormat.format("Matching on port {0} failed: The parameters of the first {1} in the queue do not match the template: {2}", mf.getPort__name().getValue(), is_call ? "call" : "reply", mf.getInfo().getValue()));
				break;
			case sender__does__not__match__from__clause:
				returnValue.append(MessageFormat.format("Matching on port {0} failed: Sender of the first entity in the queue does not match the from clause: {1}", mf.getPort__name().getValue(), mf.getInfo().getValue()));
				break;
			case sender__is__not__system:
				returnValue.append(MessageFormat.format("Matching on port {0} failed: Sender of the first entity in the queue is not the system.", mf.getPort__name().getValue()));
				break;
			case not__an__exception__for__signature:
				returnValue.append(MessageFormat.format("Matching on port {0} failed: The first entity in the queue is not an exception for signature {1}.", mf.getPort__name().getValue(), mf.getInfo().getValue()));
				break;
			default:
				break;
			}
			break;
		}
		case ALT_MatchingSuccess: {
			final MatchingSuccessType ms = choice.getMatchingSuccess();
			returnValue.append(MessageFormat.format("Matching on port {0} succeeded: {1}", ms.getPort__name().getValue(), ms.getInfo().getValue()));
			break;
		}
		case ALT_MatchingProblem: {
			final MatchingProblemType mp = choice.getMatchingProblem();
			returnValue.append("Operation `");
			if (mp.getAny__port().getValue()) {
				returnValue.append("any port.");
			}

			if (mp.getCheck__().getValue()) {
				returnValue.append("check(");
			}
			switch (mp.getOperation().enum_value) {
			case receive__:
				returnValue.append("receive");
				break;
			case trigger__:
				returnValue.append("trigger");
				break;
			case getcall__:
				returnValue.append("getcall");
				break;
			case getreply__:
				returnValue.append("getreply");
				break;
			case catch__:
				returnValue.append("catch");
				break;
			case check__:
				returnValue.append("check");
				break;
			default:
				break;
			}
			if (mp.getCheck__().getValue()) {
				returnValue.append(')');
			}
			returnValue.append("' ");

			if (mp.getPort__name().isBound()) {
				returnValue.append(MessageFormat.format("on port {0} ", mp.getPort__name().getValue()));
			}
			// we could also check that any__port is false

			returnValue.append("failed: ");

			switch (mp.getReason().enum_value) {
			case component__has__no__ports:
				returnValue.append("The test component does not have ports.");
				break;
			case no__incoming__signatures:
				returnValue.append("The port type does not have any incoming signatures.");
				break;
			case no__incoming__types:
				returnValue.append("The port type does not have any incoming message types.");
				break;
			case no__outgoing__blocking__signatures:
				returnValue.append("The port type does not have any outgoing blocking signatures.");
				break;
			case no__outgoing__blocking__signatures__that__support__exceptions:
				returnValue.append("The port type does not have any outgoing blocking signatures that support exceptions.");
				break;
			case port__not__started__and__queue__empty:
				returnValue.append("Port is not started and the queue is empty.");
				break;
			default:
				break;
			}
			break;
		}
		//FIXME implement the rest of the branches
		}
	}

	private static void portevent_str(final StringBuilder returnValue, final PortEvent_choice choice) {
		switch (choice.get_selection()) {
		case ALT_PortQueue: {
			final Port__Queue portQueue = choice.getPortQueue();
			switch (portQueue.getOperation().enum_value) {
			case enqueue__msg:{
				final String comp_str = TitanComponent.get_component_string(portQueue.getCompref().getInt());
				returnValue.append(MessageFormat.format("Message enqueued on {0} from {1}{2}{3} id {4}", portQueue.getPort__name().getValue() , comp_str, portQueue.getAddress__().getValue(), portQueue.getParam__().getValue(), portQueue.getMsgid().getInt()));
				break;}
			case enqueue__call:{
				final String comp_str = TitanComponent.get_component_string(portQueue.getCompref().getInt());
				returnValue.append(MessageFormat.format("Call enqueued on {0} from {1}{2}{3} id {4}", portQueue.getPort__name().getValue() , comp_str, portQueue.getAddress__().getValue(), portQueue.getParam__().getValue(), portQueue.getMsgid().getInt()));
				break;}
			case enqueue__reply:{
				final String comp_str = TitanComponent.get_component_string(portQueue.getCompref().getInt());
				returnValue.append(MessageFormat.format("Reply enqueued on {0} from {1}{2}{3} id {4}", portQueue.getPort__name().getValue() , comp_str, portQueue.getAddress__().getValue(), portQueue.getParam__().getValue(), portQueue.getMsgid().getInt()));
				break;}
			case enqueue__exception:{
				final String comp_str = TitanComponent.get_component_string(portQueue.getCompref().getInt());
				returnValue.append(MessageFormat.format("Exception enqueued on {0} from {1}{2}{3} id {4}", portQueue.getPort__name().getValue() , comp_str, portQueue.getAddress__().getValue(), portQueue.getParam__().getValue(), portQueue.getMsgid().getInt()));
				break;}
			case extract__msg:
				returnValue.append(MessageFormat.format("Message with id {0} was extracted from the queue of {1}.", portQueue.getMsgid().getInt(), portQueue.getPort__name().getValue()));
				break;
			case extract__op:
				returnValue.append(MessageFormat.format("Operation with id {0} was extracted from the queue of {1}.", portQueue.getMsgid().getInt(), portQueue.getPort__name().getValue()));
				break;
			default:
				break;
			}
			break;
		}
		case ALT_PortState: {
			final Port__State ps = choice.getPortState();
			String what = "";
			switch (ps.getOperation().enum_value) {
			case started:
				what = "started";
				break;
			case stopped:
				what = "stopped";
				break;
			case halted:
				what = "halted";
				break;
			default:
				return;
			}
			returnValue.append(MessageFormat.format("Port {0} was {1}.", ps.getPort__name().getValue(), what));
			break;
		}
		case ALT_ProcPortSend: {
			final Proc__port__out ps = choice.getProcPortSend();
			final String dest;
			if (ps.getCompref().getInt() == TitanComponent.SYSTEM_COMPREF) {
				dest = ps.getSys__name().getValue().toString();
			} else {
				dest = TitanComponent.get_component_string(ps.getCompref().getInt());
			}
			
			switch (ps.getOperation().enum_value) {
			case call__op:
				returnValue.append("Called");
				break;
			case reply__op:
				returnValue.append("Replied");
				break;
			case exception__op:
				returnValue.append("Raised");
			default:
				return;
			}

			returnValue.append(MessageFormat.format(" on {0} to {1} {2}", ps.getPort__name().getValue(), dest, ps.getParameter().getValue()));
			break;
		}
		case ALT_ProcPortRecv: {
			final Proc__port__in ps = choice.getProcPortRecv();
			String op2 = "";
			switch (ps.getOperation().enum_value) {
			case call__op:
				returnValue.append(ps.getCheck__().getValue() ? "Check-getcall" : "Getcall");
				op2 = "call";
				break;
			case reply__op:
				returnValue.append(ps.getCheck__().getValue() ? "Check-getreply" : "Getreply");
				op2 = "reply";
			case exception__op:
				returnValue.append(ps.getCheck__().getValue() ? "Check-catch" : "Catch");
				op2 = "exception";
			default:
				return;
			}

			final String source = TitanComponent.get_component_string(ps.getCompref().getInt());
			returnValue.append(MessageFormat.format(" operation on port {0} succeeded, {1} from {2}: {3} id {4}", ps.getPort__name().getValue(), op2, source, ps.getParameter().getValue(), ps.getMsgid().getInt()));
			break;
		}
		case ALT_MsgPortSend: {
			final Msg__port__send ms = choice.getMsgPortSend();
			final String dest = TitanComponent.get_component_string(ms.getCompref().getInt());
			returnValue.append(MessageFormat.format("Sent on {0} to {1}{2}", ms.getPort__name().getValue(), dest, ms.getParameter().getValue()));
			break;
		}
		case ALT_MsgPortRecv: {
			final Msg__port__recv ms = choice.getMsgPortRecv();
			switch (ms.getOperation().enum_value) {
			case receive__op:
				returnValue.append("Receive");
				break;
			case check__receive__op:
				returnValue.append("Check-receive");
				break;
			case trigger__op:
				returnValue.append("Trigger");
				break;
			default:
				return;
			}

			returnValue.append(MessageFormat.format(" operation on port {0} succeeded, message from ", ms.getPort__name().getValue()));
			if (ms.getCompref().getInt() == TitanComponent.SYSTEM_COMPREF) {
				returnValue.append(MessageFormat.format("system({0})", ms.getSys__name().getValue()));
			} else {
				final String dest = TitanComponent.get_component_string(ms.getCompref().getInt());
				returnValue.append(dest);
			}

			returnValue.append(MessageFormat.format("{0} id {1}", ms.getParameter().getValue(), ms.getMsgid().getInt()));
			break;
		}
		case ALT_DualMapped: {
			final Dualface__mapped dual = choice.getDualMapped();
			returnValue.append(MessageFormat.format("{0} message was mapped to {1} : {2}", (dual.getIncoming().getValue() ? "Incoming" : "Outgoing"), dual.getTarget__type().getValue(), dual.getValue__().getValue()));
			if (dual.getIncoming().getValue()) {
				returnValue.append(MessageFormat.format(" id {0}", dual.getMsgid().getInt()));
			}
			break;
		}
		case ALT_PortMisc: {
			final Port__Misc portMisc = choice.getPortMisc();
			final String comp_str = TitanComponent.get_component_string(portMisc.getRemote__component().getInt());
			switch (portMisc.getReason().enum_value) {
			case removing__unterminated__connection:
				returnValue.append(MessageFormat.format("Removing unterminated connection between port {0} and {1}:{2}.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case removing__unterminated__mapping:
				returnValue.append(MessageFormat.format("Removing unterminated mapping between port {0} and system:{1}.", portMisc.getPort__name().getValue(), portMisc.getRemote__port().getValue()));
				break;
			case port__was__cleared:
				returnValue.append(MessageFormat.format("Port {0} was cleared.", portMisc.getPort__name().getValue()));
				break;
			case local__connection__established:
				returnValue.append(MessageFormat.format("Port {0} has established the connection with local port {1}.", portMisc.getPort__name().getValue(), portMisc.getRemote__port().getValue()));
				break;
			case local__connection__terminated:
				returnValue.append(MessageFormat.format("Port {0} has terminated the connection with local port {1}.", portMisc.getPort__name().getValue(), portMisc.getRemote__port().getValue()));
				break;
			case port__is__waiting__for__connection__tcp:
				returnValue.append(MessageFormat.format("Port {0} is waiting for connection from {1}:{2} on TCP port {3}:{4}.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue(), portMisc.getIp__address().getValue(), portMisc.getTcp__port().getInt()));
				break;
			case port__is__waiting__for__connection__unix:
				returnValue.append(MessageFormat.format("Port {0} is waiting for connection from {1}:{2} on UNIX pathname {3}.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue(), portMisc.getIp__address().getValue()));
				break;
			case connection__established:
				returnValue.append(MessageFormat.format("Port {0} has established the connection with {1}:{2} using transport type {3}.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue(), portMisc.getIp__address().getValue()));
				break;
			case destroying__unestablished__connection:
				returnValue.append(MessageFormat.format("Destroying unestablished connection of port {0} to {1}:{2} because the other endpoint has terminated.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case terminating__connection:
				returnValue.append(MessageFormat.format("Terminating the connection of port {0} to {1}:{2}. No more messages can be sent through this connection.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case sending__termination__request__failed:
				returnValue.append(MessageFormat.format("Sending the connection termination request on port {0} to remote endpoint {1}:}{2} failed.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case termination__request__received:
				returnValue.append(MessageFormat.format("Connection termination request was received on port {0} from {1}:{2}. No more data can be sent or received through this connection.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case acknowledging__termination__request__failed:
				returnValue.append(MessageFormat.format("Sending the acknowledgment for connection termination request on port {0} to remote endpoint {1}:{2} failed.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case sending__would__block:
				returnValue.append(MessageFormat.format("Sending data on the connection of port {0} to {1}:{2} would block execution. The size of the outgoing buffer was increased from {3} to {4} bytes.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue(), portMisc.getTcp__port().getInt(), portMisc.getNew__size().getInt()));
				break;
			case connection__accepted:
				returnValue.append(MessageFormat.format("Port {0} has accepted the connection from {1}:{2}.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case connection__reset__by__peer:
				returnValue.append(MessageFormat.format("Connection of port {0} to {1}:{2} was reset by the peer.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case connection__closed__by__peer:
				returnValue.append(MessageFormat.format("Connection of port {0} to {1}:{2} was closed unexpectedly by the peer.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case port__disconnected:
				returnValue.append(MessageFormat.format("Port {0} was disconnected from {1}:{2}.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case port__was__mapped__to__system:
				returnValue.append(MessageFormat.format("Port {0} was mapped to system:{1}.", portMisc.getPort__name().getValue(), portMisc.getRemote__port().getValue()));
				break;
			case port__was__unmapped__from__system:
				returnValue.append(MessageFormat.format("Port {0} was unmapped from system:{1}.", portMisc.getPort__name().getValue(), portMisc.getRemote__port().getValue()));
				break;
			default:
				break;
			}
			break;
		}
		//FIXME implement rest
		}
	}
}