/*  Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package net.davidtanzer.babysteps;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.text.DecimalFormat;

public class BabystepsTimer {
    public static final String BACKGROUND_COLOR_NEUTRAL = "#ffffff";
    private static final String BACKGROUND_COLOR_FAILED = "#ffcccc";
    private static final String BACKGROUND_COLOR_PASSED = "#ccffcc";

    private static final long SECONDS_IN_CYCLE = 120;

    protected static JFrame timerFrame;
    protected static JTextPane timerPane;
    public static boolean timerRunning;
    public static long currentCycleStartTime;
    private static String lastRemainingTime;
    public static String bodyBackgroundColor = BACKGROUND_COLOR_NEUTRAL;

    private static DecimalFormat twoDigitsFormat = new DecimalFormat("00");

    public static void main(final String[] args) throws InterruptedException {
        new BabystepsTimer().execute();
    }

    public void execute() {
        timerFrame = getJFrame();
        timerFrame.setUndecorated(true);

        timerFrame.setSize(250, 120);
        timerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        timerPane = getJTextPane();
        timerPane.setContentType("text/html");
        timerPane.setText(createTimerHtml(getRemainingTimeCaption(0L), BACKGROUND_COLOR_NEUTRAL, false));
        timerPane.setEditable(false);
        timerPane.addMouseMotionListener(getMouseMotionListener());
        timerPane.addHyperlinkListener(getHyperlinkListener());
        timerFrame.getContentPane().add(timerPane);

        timerFrame.setVisible(true);
    }

    protected HyperlinkListener getHyperlinkListener() {
        return new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(final HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if ("command://start".equals(e.getDescription())) {
                        timerFrame.setAlwaysOnTop(true);
                        timerPane.setText(createTimerHtml(getRemainingTimeCaption(0L), BACKGROUND_COLOR_NEUTRAL, true));
                        timerFrame.repaint();
                        getTimerThread().start();
                    } else if ("command://stop".equals(e.getDescription())) {
                        timerRunning = false;
                        timerFrame.setAlwaysOnTop(false);
                        timerPane.setText(createTimerHtml(getRemainingTimeCaption(0L), BACKGROUND_COLOR_NEUTRAL, false));
                        timerFrame.repaint();
                    } else if ("command://reset".equals(e.getDescription())) {
                        currentCycleStartTime = System.currentTimeMillis();
                        bodyBackgroundColor = BACKGROUND_COLOR_PASSED;
                    } else if ("command://quit".equals(e.getDescription())) {
                        System.exit(0);
                    }
                }
            }
        };
    }

    protected TimerThread getTimerThread() {
        return new TimerThread();
    }

    protected MouseMotionListener getMouseMotionListener() {
        return new MouseMotionListener() {
            private int lastX;
            private int lastY;

            @Override
            public void mouseMoved(final MouseEvent e) {
                lastX = e.getXOnScreen();
                lastY = e.getYOnScreen();
            }

            @Override
            public void mouseDragged(final MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();

                timerFrame.setLocation(timerFrame.getLocation().x + (x - lastX), timerFrame.getLocation().y + (y - lastY));

                lastX = x;
                lastY = y;
            }
        };
    }

    protected JTextPane getJTextPane() {
        return new JTextPane();
    }

    protected JFrame getJFrame() {
        return new JFrame("Babysteps Timer");
    }

    private static String getRemainingTimeCaption(final long elapsedTime) {
        long elapsedSeconds = elapsedTime / 1000;
        long remainingSeconds = SECONDS_IN_CYCLE - elapsedSeconds;

        long remainingMinutes = remainingSeconds / 60;
        return twoDigitsFormat.format(remainingMinutes) + ":" + twoDigitsFormat.format(remainingSeconds - remainingMinutes * 60);
    }

    private static String createTimerHtml(final String timerText, final String bodyColor, final boolean running) {
        String timerHtml = "<html><body style=\"border: 3px solid #555555; background: " + bodyColor + "; margin: 0; padding: 0;\">" +
                "<h1 style=\"text-align: center; font-size: 30px; color: #333333;\">" + timerText + "</h1>" +
                "<div style=\"text-align: center\">";
        if (running) {
            timerHtml += "<a style=\"color: #555555;\" href=\"command://stop\">Stop</a> " +
                    "<a style=\"color: #555555;\" href=\"command://reset\">Reset</a> ";
        } else {
            timerHtml += "<a style=\"color: #555555;\" href=\"command://start\">Start</a> ";
        }
        timerHtml += "<a style=\"color: #555555;\" href=\"command://quit\">Quit</a> ";
        timerHtml += "</div>" +
                "</body></html>";
        return timerHtml;
    }

    public static synchronized void playSound(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                            BabystepsTimer.class.getResourceAsStream("/" + url));
                    clip.open(inputStream);
                    clip.start();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }).start();
    }

    public static class TimerThread extends Thread {
        @Override
        public void run() {
            timerRunning = true;
            currentCycleStartTime = System.currentTimeMillis();

            while (timerRunning) {
                long elapsedTime = System.currentTimeMillis() - currentCycleStartTime;

                if (elapsedTime >= SECONDS_IN_CYCLE * 1000 + 980) {
                    currentCycleStartTime = System.currentTimeMillis();
                    elapsedTime = System.currentTimeMillis() - currentCycleStartTime;
                }
                if (elapsedTime >= 5000 && elapsedTime < 6000 && !BACKGROUND_COLOR_NEUTRAL.equals(bodyBackgroundColor)) {
                    bodyBackgroundColor = BACKGROUND_COLOR_NEUTRAL;
                }

                String remainingTime = getRemainingTimeCaption(elapsedTime);
                if (!remainingTime.equals(lastRemainingTime)) {
                    if (remainingTime.equals("00:10")) {
                        playSound("2166__suburban-grilla__bowl-struck.wav");
                    } else if (remainingTime.equals("00:00")) {
                        playSound("32304__acclivity__shipsbell.wav");
                        bodyBackgroundColor = BACKGROUND_COLOR_FAILED;
                    }

                    timerPane.setText(createTimerHtml(remainingTime, bodyBackgroundColor, true));
                    timerFrame.repaint();
                    lastRemainingTime = remainingTime;
                }
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    //We don't really care about this one...
                }
            }
        }
    }
}
