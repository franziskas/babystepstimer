package net.davidtanzer.babysteps;

import org.junit.Before;
import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoRule;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import java.awt.event.MouseMotionListener;

import static javax.swing.event.HyperlinkEvent.EventType.*;
import static net.davidtanzer.babysteps.BabystepsTimer.BACKGROUND_COLOR_NEUTRAL;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.junit.MockitoJUnit.rule;

@RunWith(Theories.class)
public class HyperlinkListenerCreatorShould {
    @Rule
    public MockitoRule mockitoRule = rule();

    @DataPoints
    public static EventType[] eventTypes = new EventType[]{ENTERED, EXITED, ACTIVATED};

    @Mock(answer = RETURNS_DEEP_STUBS)
    private JFrame timerFrame;
    @Mock
    private JTextPane timerPane;
    @Mock
    private java.awt.Container contentPane;
    @Mock
    private MouseMotionListener mouseMotionListener;
    @Mock
    private HyperlinkEvent hyperlinkEvent;
    @Mock
    private BabystepsTimer.TimerThread timerThread;

    private BabystepsTimer babystepsTimer;

    @Before
    public void setUp() {
        when(timerFrame.getContentPane()).thenReturn(contentPane);

        babystepsTimer = new BabystepsTimer() {
            @Override
            protected JFrame getJFrame() {
                return timerFrame;
            }

            @Override
            protected JTextPane getJTextPane() {
                return timerPane;
            }

            @Override
            protected MouseMotionListener getMouseMotionListener() {
                return mouseMotionListener;
            }

            @Override
            protected BabystepsTimer.TimerThread getTimerThread() {
                return timerThread;
            }
        };
    }

    @Theory
    public void
    create_hyperlink_listener_ignoring_events_that_are_not_activated(EventType eventType) {
        assumeThat(eventType, is(not(ACTIVATED)));

        BabystepsTimer.timerRunning = true;
        BabystepsTimer.currentCycleStartTime = 0L;
        BabystepsTimer.bodyBackgroundColor = BACKGROUND_COLOR_NEUTRAL;
        when(hyperlinkEvent.getEventType()).thenReturn(eventType);

        HyperlinkListener hyperlinkListener = babystepsTimer.getHyperlinkListener();
        hyperlinkListener.hyperlinkUpdate(hyperlinkEvent);

        verifyZeroInteractions(timerFrame);
        verifyZeroInteractions(timerPane);
        verifyZeroInteractions(timerThread);
        assertThat(BabystepsTimer.timerRunning, is(true));
        assertThat(BabystepsTimer.currentCycleStartTime, is(0L));
        assertThat(BabystepsTimer.bodyBackgroundColor, is(BACKGROUND_COLOR_NEUTRAL));
    }
}
