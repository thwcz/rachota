/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://rachota.sourceforge.net/license.txt.
 * 
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://rachota.sourceforge.net/license.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * The Original Software is Rachota.
 * The Initial Developer of the Original Software is Jiri Kovalsky
 * Portions created by Jiri Kovalsky are Copyright (C) 2006
 * All Rights Reserved.
 *
 * Contributor(s): Jiri Kovalsky
 * Created on April 9, 2005  9:18 PM
 * Tools.java
 */

package org.cesilko.rachota.gui;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.JSpinner;
import javax.swing.JFormattedTextField;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.border.MatteBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import org.cesilko.rachota.core.Day;
import org.cesilko.rachota.core.Settings;
import org.cesilko.rachota.core.Task;
import org.cesilko.rachota.core.Translator;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Dimension;

/** Helper class providing support for time conversion between
 * long, Date and String formats and other static functions.
 * @author Jiri Kovalsky
 */
public class Tools {
    
    /** Name and version of application. */
    public static final String title = "Rachota " + getApplicationVersion();
    /** Build number. */
    public static final String build = getApplicationVersion();
    /** Warning type of beep. */
    public static final int BEEP_WARNING = 0;
    /** Notification type of beep. */
    public static final int BEEP_NOTIFICATION = 1;
    /** Font that should be used for all UI elements. */
    private static Font font = null;
    /** Application version loaded from generated build metadata. */
    private static String applicationVersion = null;
    
    /** Transforms time in milliseconds to text string.
     * @param time Time in milliseconds.
     * @return Textual representation of time in format hh:mm:ss.
     */
    public static String getTime(double time) {
        long hours = (long) time/(1000*60*60);
        String text = ((hours > 9) ? "" : "0") + hours;
        time = time - hours * (1000*60*60);
        long minutes = (long) time/(1000*60);
        text = text + ":" + ((minutes > 9) ? "" : "0") + minutes;
        time = time - minutes*(1000*60);
        long seconds = (long) time/1000;
        text = text + ":" + ((seconds > 9) ? "" : "0") + seconds;
        return text;
    }
    
    /** Transforms time in milliseconds to text string.
     * @param time Time in milliseconds.
     * @return Textual representation of time in format hh:mm.
     */
    public static String getTimeShort(double time) {
        long hours = (long) time/(1000*60*60);
        String text = ((hours > 9) ? "" : "0") + hours;
        time = time - hours * (1000*60*60);
        long minutes = (long) time/(1000*60);
        text = text + ":" + ((minutes > 9) ? "" : "0") + minutes;
        time = time - minutes*(1000*60);
        return text;
    }
    
    /** Transforms time of Date to text string.
     * @param time Time in Date object.
     * @return Textual representation of time in format hh:mm.
     */
    public static String getTime(Date time) {
        if (time == null) return "00:00";
        SimpleDateFormat df = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
        String timeFormat = Translator.getTranslation("FORMAT.TIME");
        df.applyPattern(timeFormat);
        return df.format(time);
    }
    
    /** Transforms text string to time in milliseconds.
     * @return Time in milliseconds.
     * @param text Textual representation of time in format hh:mm or hh:mm:ss.
     * @throws NumberFormatException in case format of time does not comply with hh:mm:ss format.
     */
    public static long getTime(String text) throws NumberFormatException {
        long time = 0;
        if (text.length() == 5) {
            SimpleDateFormat df = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
            String timeFormat = Translator.getTranslation("FORMAT.TIME");
            df.applyPattern(timeFormat);
            try {
                time = df.parse(text).getTime();
            } catch (ParseException ex) {
                throw new NumberFormatException("Error: Time does not comply with hh:mm format: " + text);
            }
        } else {
            int firstColon = text.indexOf(":");
            int secondColon = text.lastIndexOf(":");
            int hours = Integer.parseInt(text.substring(0, firstColon));
            int minutes = Integer.parseInt(text.substring(firstColon + 1, secondColon));
            int seconds = Integer.parseInt(text.substring(secondColon + 1, text.length()));
            time = seconds * 1000;
            time = time + minutes * 1000 * 60;
            time = time + hours * 1000 * 60 * 60;
        }
        return time;
    }
    
    /** Returns total time measured in selected period.
     * @param includeIdleTime Should idle time be included in the total time?
     * @param includePrivateTime Should private time be included in the total time?
     * @param days Vector of days whose total time should be counted.
     * @return Total time measured in selected period including idle time and private time if desired.
     */
    public static long getTotalTime(boolean includeIdleTime, boolean includePrivateTime, Vector days) {
        long totalTime = 0;
        Iterator iterator = days.iterator();
        while (iterator.hasNext()) {
            Day day = (Day) iterator.next();
            totalTime = totalTime + day.getTotalTime(includePrivateTime);
            if (includeIdleTime) {
                Task idleTask = day.getIdleTask();
                if (idleTask != null) totalTime = totalTime + idleTask.getDuration();
            }
        }
        return totalTime;
    }

    /** Returns text string that has all occurences of oldText strings replaced by newText string.
     * @param text String where all occurences of oldText should be replaced.
     * @param oldText Substring to be searched for in text string.
     * @param newText New replacement string for all occurences of oldText string.
     * @return Text string with all occurences of oldText replaced by newText strings.
     */
    public static String replaceAll(String text, String oldText, String newText) {
        int index = text.indexOf(oldText);
        while (index != -1) {
            text = text.substring(0, index) + newText + text.substring(index + oldText.length());
            index = text.indexOf(oldText, index + 1);
        }
        return text;
    }
    
    /** Produce couple of warning beeps at user when necessary.
     * @param type Type of beep i.e. BEEP_NOTIFICATION or BEEP_WARNING.
     */
    public static void beep(int type) {
        int[] notify = {100, 100, 100, 200, 200, 100, 100, 100, 100};
        int[] delays = {200, 200, 200, 100, 100, 200, 200, 200};
        switch (type) {
            case BEEP_NOTIFICATION:
                delays = notify;
                break;
        }
        for (int i = 0; i < delays.length; i++) {
            Toolkit.getDefaultToolkit().beep();
            try { Thread.sleep(delays[i]); } catch (InterruptedException ex) {}
        }
    }

    /** Returns unique Rachota identification string.
     * @return Rachota identification string.
     */
    public static String getRID() {
        return title + "|" + build + "|" +
              System.getProperty("os.name") + "|" +
              System.getProperty("os.arch") + "|" +
              System.getProperty("os.version") + "|" +
              System.getProperty("java.version") + "|" +
              Locale.getDefault().getDisplayCountry(Locale.US) + "|" +
              System.getProperty("user.name") + "|" +
              System.getProperty("user.dir");
    }
    
    /** Returns font that should be used for all UI components
     * based on the language preferences or specified by user.
     * @return Font to be used across Rachota UI components.
     */
    public static Font getFont() {
        if (font == null)
            font = new Font((String) Settings.getDefault().getSetting("fontName"), java.awt.Font.PLAIN, Integer.parseInt((String) Settings.getDefault().getSetting("fontSize")));
        return font;
    }

    public static void setupLookAndFeel() {
        FlatLightLaf.setup();
        Font uiFont = getFont().deriveFont(14f);
        Color panelBackground = new Color(248, 250, 252);
        Color cardBackground = new Color(255, 255, 255);
        Color infoBackground = new Color(237, 244, 255);
        Color separatorColor = new Color(210, 217, 224);
        Color selectionBackground = new Color(56, 117, 215);
        Color selectionForeground = new Color(255, 255, 255);
        Color tabStripBackground = new Color(226, 232, 240);
        Color tabBorderColor = new Color(178, 188, 200);
        UIManager.put("defaultFont", uiFont);
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ProgressBar.arc", 10);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("TabbedPane.tabInsets", new Insets(8, 14, 8, 14));
        UIManager.put("Panel.background", panelBackground);
        UIManager.put("Viewport.background", cardBackground);
        UIManager.put("ScrollPane.background", cardBackground);
        UIManager.put("Table.background", cardBackground);
        UIManager.put("TableHeader.background", new Color(242, 246, 250));
        UIManager.put("Table.gridColor", separatorColor);
        UIManager.put("Table.selectionBackground", selectionBackground);
        UIManager.put("Table.selectionForeground", selectionForeground);
        // Tab strip (inactive tabs) gets a clearly darker/grayer background than
        // the content area, so tabs stay visually separated even without relying
        // on the underline alone. Selected tab matches the actual content panel
        // background (panelBackground, set via Tools.stylePanel()) so it visually
        // "merges" with the card below it (card look) instead of showing a seam.
        UIManager.put("TabbedPane.tabType", "card");
        UIManager.put("TabbedPane.tabsOpaque", true);
        UIManager.put("TabbedPane.background", tabStripBackground);
        UIManager.put("TabbedPane.selectedBackground", panelBackground);
        UIManager.put("TabbedPane.focusColor", panelBackground);
        UIManager.put("TabbedPane.hoverColor", new Color(214, 222, 232));
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("TabbedPane.tabSeparatorColor", tabBorderColor);
        UIManager.put("TabbedPane.contentAreaColor", tabBorderColor);
        UIManager.put("TabbedPane.contentSeparatorHeight", 2);
        UIManager.put("TabbedPane.hasFullBorder", true);
        UIManager.put("TextField.inactiveBackground", infoBackground);
        UIManager.put("FormattedTextField.inactiveBackground", infoBackground);
    }

    public static void styleTable(JTable table) {
        table.setFont(getFont().deriveFont(14f));
        table.setRowHeight(26);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(210, 217, 224));
        table.setIntercellSpacing(new java.awt.Dimension(1, 1));
        table.setSelectionBackground(new Color(56, 117, 215));
        table.setSelectionForeground(Color.WHITE);
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setFont(getFont().deriveFont(Font.BOLD, 13f));
            header.setReorderingAllowed(false);
        }
    }

    public static void styleInfoField(JTextComponent field) {
        field.setBackground(new Color(237, 244, 255));
        field.setOpaque(true);
    }

    public static void stylePanel(Component component) {
        if (component instanceof javax.swing.JComponent) {
            ((javax.swing.JComponent) component).setOpaque(true);
            component.setBackground(new Color(248, 250, 252));
        }
    }

    public static void styleTabbedPane(JTabbedPane tabbedPane) {
        // Intentionally left without per-instance background/border overrides:
        // FlatLaf's TabbedPane.* UIManager defaults (set in setupLookAndFeel())
        // already control the tab strip color and the content area border.
        // Overriding the component's own background here would hide the
        // distinct tab-strip color FlatLaf paints for inactive tabs.
    }

    public static EmptyBorder createPanelPadding() {
        return new EmptyBorder(12, 12, 12, 12);
    }

    private static String getApplicationVersion() {
        if (applicationVersion != null) {
            return applicationVersion;
        }
        try {
            InputStream stream = Tools.class.getResourceAsStream("/version.properties");
            if (stream != null) {
                Properties properties = new Properties();
                try {
                    properties.load(stream);
                } finally {
                    stream.close();
                }
                String version = properties.getProperty("app.version");
                if (version != null && version.length() > 0) {
                    applicationVersion = version;
                    return applicationVersion;
                }
            }
        } catch (IOException ex) {}
        applicationVersion = "dev";
        return applicationVersion;
    }

    public static void recordActivity() {
        System.setProperty("rachota.lastInteraction", "" + new Date().getTime());
    }

    public static long getInactivity() {
        String lastActivity = System.getProperty("rachota.lastInteraction");
        if (lastActivity == null) return 0;
        return new Date().getTime() - Long.parseLong(lastActivity);
    }

    /** Sets up a focus gained listener to the {@code JSpinner} that selects all text
     * currently in the text field of the supplied {@code JSpinner}.
     * @param spinner The {@code JSpinner} to add a select all listener to.
     */
    public static void setupSelectAllListener(JSpinner spinner) {
        FocusAdapter spinnerFocus = new FocusAdapter() {

            @Override
            public void focusGained(final FocusEvent e) {
                // Make sure that the event is from a JTextField
                if (e.getSource() instanceof JTextField) {
                    Runnable runnable = new Runnable() {

                        public void run() {
                            ((JTextField) e.getSource()).selectAll();
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
                }
            }
        };
        // Make sure that the editor is a instance of JSpinner.DefaultEditor.
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor) {
            // Get the text field from the JSpinner, and add the focus listener to it.
            final JTextField textField = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
            textField.addFocusListener(spinnerFocus);
        }
    }

    public static void setupCommitAndCloseOnEnter(final JSpinner spinner, final Runnable onEnter) {
        if (!(spinner.getEditor() instanceof JSpinner.DefaultEditor)) {
            return;
        }
        final JTextField textField = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        textField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "commit-and-close");
        textField.getActionMap().put("commit-and-close", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    spinner.commitEdit();
                } catch (ParseException ex) {}
                onEnter.run();
            }
        });
    }

    public static void setupActionOnAltKey(JSpinner spinner, int mnemonic, final Runnable action) {
        if (!(spinner.getEditor() instanceof JSpinner.DefaultEditor)) {
            return;
        }
        final JTextField textField = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        String actionKey = "alt-" + mnemonic + "-action";
        textField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(mnemonic, java.awt.event.InputEvent.ALT_DOWN_MASK), actionKey);
        textField.getActionMap().put(actionKey, new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                action.run();
            }
        });
    }

    public static void styleSpinner(JSpinner spinner) {
        Dimension size = spinner.getPreferredSize();
        spinner.setPreferredSize(new Dimension(size.width, Math.max(size.height, 34)));
        spinner.setBorder(new MatteBorder(1, 1, 1, 1, new Color(210, 217, 224)));
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor) {
            JFormattedTextField textField = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
            textField.setBorder(new EmptyBorder(0, 6, 0, 6));
            textField.setBackground(Color.WHITE);
        }
        for (Component child : spinner.getComponents()) {
            if (child instanceof JButton) {
                JButton button = (JButton) child;
                String name = button.getName();
                if ("Spinner.nextButton".equals(name)) {
                    button.setBorder(new CompoundBorder(
                            new MatteBorder(0, 1, 1, 0, new Color(210, 217, 224)),
                            new EmptyBorder(1, 2, 1, 2)));
                } else if ("Spinner.previousButton".equals(name)) {
                    button.setBorder(new CompoundBorder(
                            new MatteBorder(1, 1, 0, 0, new Color(210, 217, 224)),
                            new EmptyBorder(1, 2, 1, 2)));
                } else {
                    button.setBorder(new CompoundBorder(
                            new MatteBorder(0, 1, 0, 0, new Color(210, 217, 224)),
                            new EmptyBorder(1, 2, 1, 2)));
                }
                button.setBackground(new Color(245, 247, 250));
            }
        }
    }

    /** Generates random three digits identifier.
     * @return Returns random three digits as a string. e.g. "537"
     */
    static String getRandomID() {
        Double randomID = new Double(Math.random());
        return randomID.toString().substring(2, 5);
    }

    static void showURL(String webPage) {
        if (System.getProperty("java.version").startsWith("1.6")) {
            try { 
                URI url = new URI(webPage);
                Desktop.getDesktop().browse(url);
                return;
            }
            catch (Exception e) { e.printStackTrace(); }
        }
        String[] commands = null;
        String os = System.getProperty("os.name");
        if (os.indexOf("Unix") != -1) commands = new String[] {"firefox ", "konqueror ", "mozilla ", "opera "};
        if (os.indexOf("Linux") != -1) commands = new String[] {"firefox ", "konqueror ", "mozilla ", "opera "};
        if (os.indexOf("Windows") != -1) commands = new String[] {"cmd.exe /c start "};
        if (os.indexOf("Macintosh") != -1) commands = new String[] {"open "};
        if (commands == null) return;
        for (int i = 0; i < commands.length; i++) {
            String command = commands[i] + webPage;
            System.out.println("Executing \"" + command + "\" command.");
            try {
                Process process = Runtime.getRuntime().exec(command);
                process.waitFor();
                int exit = process.exitValue();
                if (exit == 0) return;
            } catch(Exception e) { e.printStackTrace(); }
        }
    }
}