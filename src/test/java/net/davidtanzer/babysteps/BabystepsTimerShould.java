package net.davidtanzer.babysteps;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.swing.*;

import static javax.swing.JFrame.EXIT_ON_CLOSE;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BabystepsTimerShould {
    private static final String FIRST_CONTENT = "<html><body style=\"border: 3px solid #555555; background: #ffffff; margin: 0; padding: 0;\"><h1 style=\"text-align: center; font-size: 30px; color: #333333;\">02:00</h1><div style=\"text-align: center\"><a style=\"color: #555555;\" href=\"command://start\">Start</a> <a style=\"color: #555555;\" href=\"command://quit\">Quit</a> </div></body></html>";

    @Mock(answer = RETURNS_DEEP_STUBS)
    private JFrame timerFrame;
    @Mock
    private JTextPane timerPane;
    @Mock
    private java.awt.Container contentPane;

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
        };
    }

    @Test
    public void
    test() {
        InOrder executionOrder = inOrder(timerFrame, timerPane, contentPane);

        babystepsTimer.execute();

        executionOrder.verify(timerFrame).setUndecorated(true);
        executionOrder.verify(timerFrame).setSize(250, 120);
        executionOrder.verify(timerFrame).setDefaultCloseOperation(EXIT_ON_CLOSE);

        executionOrder.verify(timerPane).setContentType("text/html");
        executionOrder.verify(timerPane).setText(FIRST_CONTENT);
        executionOrder.verify(timerPane).setEditable(false);
        executionOrder.verify(contentPane).add(timerPane);
        executionOrder.verify(timerFrame).setVisible(true);
    }
}
