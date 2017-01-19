package net.davidtanzer.babysteps;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoRule;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import static javax.swing.event.HyperlinkEvent.EventType.*;
import static net.davidtanzer.babysteps.BabystepsTimer.*;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;
import static org.mockito.Mockito.*;
import static org.mockito.junit.MockitoJUnit.rule;

@RunWith(Theories.class)
public class HyperlinkListenerCreatorShould {
    private static final String START_HTML = "<html><body style=\"border: 3px solid #555555; background: #ffffff; margin: 0; padding: 0;\"><h1 style=\"text-align: center; font-size: 30px; color: #333333;\">02:00</h1><div style=\"text-align: center\"><a style=\"color: #555555;\" href=\"command://stop\">Stop</a> <a style=\"color: #555555;\" href=\"command://reset\">Reset</a> <a style=\"color: #555555;\" href=\"command://quit\">Quit</a> </div></body></html>";
    private static final String STOPPED_HTML = "<html><body style=\"border: 3px solid #555555; background: #ffffff; margin: 0; padding: 0;\"><h1 style=\"text-align: center; font-size: 30px; color: #333333;\">02:00</h1><div style=\"text-align: center\"><a style=\"color: #555555;\" href=\"command://start\">Start</a> <a style=\"color: #555555;\" href=\"command://quit\">Quit</a> </div></body></html>";
    private static final long FIXED_CURRENT_TIME = 12421421L;

    @Rule
    public MockitoRule mockitoRule = rule();

    @DataPoints
    public static EventType[] eventTypes = new EventType[]{ENTERED, EXITED, ACTIVATED};

    @Mock
    private JFrame timerFrame;
    @Mock
    private JTextPane timerPane;
    @Mock
    private HyperlinkEvent hyperlinkEvent;
    @Mock
    private BabystepsTimer.TimerThread timerThread;
    @Mock
    private Runnable exitAction;

    private BabystepsTimer babystepsTimer;

    @Before
    public void setUp() {
        babystepsTimer = new BabystepsTimer() {
            @Override
            protected BabystepsTimer.TimerThread getTimerThread() {
                return timerThread;
            }

            @Override
            protected long getCurrentTime() {
                return FIXED_CURRENT_TIME;
            }

            @Override
            protected Runnable getExitAction() {
                return exitAction;
            }
        };
        BabystepsTimer.timerFrame = timerFrame;
        BabystepsTimer.timerPane = timerPane;
    }

    @Theory
    public void
    create_hyperlink_listener_ignoring_events_that_are_not_activated(EventType eventType) {
        assumeThat(eventType, is(not(ACTIVATED)));
        timerRunning = true;
        currentCycleStartTime = 0L;
        bodyBackgroundColor = BACKGROUND_COLOR_NEUTRAL;
        when(hyperlinkEvent.getEventType()).thenReturn(eventType);

        HyperlinkListener hyperlinkListener = babystepsTimer.getHyperlinkListener();
        hyperlinkListener.hyperlinkUpdate(hyperlinkEvent);

        verifyZeroInteractions(timerFrame);
        verifyZeroInteractions(timerPane);
        verifyZeroInteractions(timerThread);
        verifyZeroInteractions(exitAction);
        assertThat(timerRunning, is(true));
        assertThat(currentCycleStartTime, is(0L));
        assertThat(bodyBackgroundColor, is(BACKGROUND_COLOR_NEUTRAL));
    }

    @Test
    public void
    create_hyperlink_listener_when_activated_with_unknown_event() {
        timerRunning = true;
        currentCycleStartTime = 0L;
        bodyBackgroundColor = BACKGROUND_COLOR_NEUTRAL;
        when(hyperlinkEvent.getEventType()).thenReturn(ACTIVATED);
        when(hyperlinkEvent.getDescription()).thenReturn("unknown");

        HyperlinkListener hyperlinkListener = babystepsTimer.getHyperlinkListener();
        hyperlinkListener.hyperlinkUpdate(hyperlinkEvent);

        verifyZeroInteractions(timerFrame);
        verifyZeroInteractions(timerPane);
        verifyZeroInteractions(timerThread);
        verifyZeroInteractions(exitAction);
        assertThat(timerRunning, is(true));
        assertThat(currentCycleStartTime, is(0L));
        assertThat(bodyBackgroundColor, is(BACKGROUND_COLOR_NEUTRAL));
    }

    @Test
    public void
    create_hyperlink_listener_when_activated_with_start_command() {
        InOrder inOrder = inOrder(timerFrame, timerPane, timerThread);
        timerRunning = true;
        currentCycleStartTime = 0L;
        bodyBackgroundColor = BACKGROUND_COLOR_NEUTRAL;
        when(hyperlinkEvent.getEventType()).thenReturn(ACTIVATED);
        when(hyperlinkEvent.getDescription()).thenReturn("command://start");

        HyperlinkListener hyperlinkListener = babystepsTimer.getHyperlinkListener();
        hyperlinkListener.hyperlinkUpdate(hyperlinkEvent);

        inOrder.verify(timerFrame).setAlwaysOnTop(true);
        inOrder.verify(timerPane).setText(START_HTML);
        inOrder.verify(timerFrame).repaint();
        inOrder.verify(timerThread).start();
        verifyZeroInteractions(exitAction);
        assertThat(timerRunning, is(true));
        assertThat(currentCycleStartTime, is(0L));
        assertThat(bodyBackgroundColor, is(BACKGROUND_COLOR_NEUTRAL));
    }

    @Test
    public void
    create_hyperlink_listener_when_activated_with_stop_command() {
        InOrder inOrder = inOrder(timerFrame, timerPane, timerThread);
        timerRunning = true;
        currentCycleStartTime = 0L;
        bodyBackgroundColor = BACKGROUND_COLOR_NEUTRAL;
        when(hyperlinkEvent.getEventType()).thenReturn(ACTIVATED);
        when(hyperlinkEvent.getDescription()).thenReturn("command://stop");

        HyperlinkListener hyperlinkListener = babystepsTimer.getHyperlinkListener();
        hyperlinkListener.hyperlinkUpdate(hyperlinkEvent);

        inOrder.verify(timerFrame).setAlwaysOnTop(false);
        inOrder.verify(timerPane).setText(STOPPED_HTML);
        inOrder.verify(timerFrame).repaint();
        verifyZeroInteractions(timerThread);
        verifyZeroInteractions(exitAction);
        assertThat(timerRunning, is(false));
        assertThat(currentCycleStartTime, is(0L));
        assertThat(bodyBackgroundColor, is(BACKGROUND_COLOR_NEUTRAL));
    }

    @Test
    public void
    create_hyperlink_listener_when_activated_with_reset_command() {
        timerRunning = true;
        currentCycleStartTime = 0L;
        bodyBackgroundColor = BACKGROUND_COLOR_NEUTRAL;
        when(hyperlinkEvent.getEventType()).thenReturn(ACTIVATED);
        when(hyperlinkEvent.getDescription()).thenReturn("command://reset");

        HyperlinkListener hyperlinkListener = babystepsTimer.getHyperlinkListener();
        hyperlinkListener.hyperlinkUpdate(hyperlinkEvent);

        verifyZeroInteractions(timerFrame);
        verifyZeroInteractions(timerPane);
        verifyZeroInteractions(timerThread);
        verifyZeroInteractions(exitAction);
        assertThat(timerRunning, is(true));
        assertThat(currentCycleStartTime, is(FIXED_CURRENT_TIME));
        assertThat(bodyBackgroundColor, is(BACKGROUND_COLOR_PASSED));
    }

    @Test
    public void
    create_hyperlink_listener_when_activated_with_quit_command() {
        timerRunning = true;
        currentCycleStartTime = 0L;
        bodyBackgroundColor = BACKGROUND_COLOR_NEUTRAL;
        when(hyperlinkEvent.getEventType()).thenReturn(ACTIVATED);
        when(hyperlinkEvent.getDescription()).thenReturn("command://quit");

        HyperlinkListener hyperlinkListener = babystepsTimer.getHyperlinkListener();
        hyperlinkListener.hyperlinkUpdate(hyperlinkEvent);

        verifyZeroInteractions(timerFrame);
        verifyZeroInteractions(timerPane);
        verifyZeroInteractions(timerThread);
        verify(exitAction).run();
        assertThat(timerRunning, is(true));
        assertThat(currentCycleStartTime, is(0L));
        assertThat(bodyBackgroundColor, is(BACKGROUND_COLOR_NEUTRAL));
    }
}
