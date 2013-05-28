package net.davidtanzer.babysteps.ui;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.davidtanzer.babysteps.TimerThread;

final class TimerWindowCommandsHyperlinkListener implements HyperlinkListener {
	public static final String COMMAND_QUIT = "command://quit";
	public static final String COMMAND_RESET = "command://reset";
	public static final String COMMAND_STOP = "command://stop";
	public static final String COMMAND_START = "command://start";
	
	private final long secondsInCycle;
	private final TimerPresentationModel presentationModel;
	private TimerThread timerThread;
	private final TimerView timerView;

	TimerWindowCommandsHyperlinkListener(final TimerView timerView, final long secondsInCycle, final TimerPresentationModel presentationModel) {
		this.timerView = timerView;
		this.secondsInCycle = secondsInCycle;
		this.presentationModel = presentationModel;
	}

	@Override
	public void hyperlinkUpdate(final HyperlinkEvent e) {
		if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			if(COMMAND_START.equals(e.getDescription())) {
				timerView.updateTimerFrame(true);
				
				timerThread = new TimerThread(timerView.timer, presentationModel, secondsInCycle);
				timerThread.start();
			} else if(COMMAND_STOP.equals(e.getDescription())) {
				timerThread.stopTimer();
				
				timerView.updateTimerFrame(false);
			} else  if(COMMAND_RESET.equals(e.getDescription())) {
				timerThread.resetTimer();
			} else  if(COMMAND_QUIT.equals(e.getDescription())) {
				System.exit(0);
			}
		}
	}
}