/*
 * Copyright © 2026 cfksoftware@proton.me, https://cfksoftware.org
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the “Software”), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR  COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.cfksoftware.gui.common;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.cfksoftware.common.logging.CfkLogger;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import com.github.weisj.darklaf.theme.Theme;

public class GuiUtils {
  public static final String DEFAULT_TITLE = ".:: org.cfksoftware ::.";

  public static void installDarculaLaf() {
    GuiUtils.installLafTheme(new DarculaTheme());
  }

  public static void installLafTheme(Theme theme) {
    LafManager.setTheme(theme);
    LafManager.install();
  }

  public static void info(String message) {
    GuiUtils.info(null, message);
  }

  public static void info(Component parent, String message) {
    CfkLogger.info("GUI message: %s", message);
    GuiUtils.prompt(parent, message, JOptionPane.INFORMATION_MESSAGE);
  }

  public static void message(String message) {
    GuiUtils.message(null, message);
  }

  public static void message(Component parent, String message) {
    CfkLogger.info("GUI message: %s", message);
    GuiUtils.prompt(parent, message, JOptionPane.PLAIN_MESSAGE);
  }

  public static void warn(String message) {
    GuiUtils.warn(null, message);
  }

  public static void warn(Component parent, String message) {
    CfkLogger.warn("GUI message: %s", message);
    GuiUtils.prompt(parent, message, JOptionPane.WARNING_MESSAGE);
  }

  public static void error(String message) {
    GuiUtils.error(null, message);
  }

  public static void error(Component parent, String message) {
    CfkLogger.error("GUI message: %s", message);
    GuiUtils.prompt(parent, message, JOptionPane.ERROR_MESSAGE);
  }

  private static void prompt(Component parent, String message, int type) {
    JOptionPane.showMessageDialog(parent, message, GuiUtils.DEFAULT_TITLE, type);
  }

  public static File[] promptForFiles(boolean multiselect) {
    return GuiUtils.promptForFiles(multiselect, null);
  }

  public static File[] promptForFiles(boolean multiselect, Component parent) {
    return GuiUtils.promptForFiles(multiselect, null, null);
  }

  public static File[] promptForFiles(boolean multiselect, Component parent, File parentDirectory) {
    JFileChooser dlg = new JFileChooser(parentDirectory);
    dlg.setDialogTitle(GuiUtils.DEFAULT_TITLE);
    dlg.setFileSelectionMode(JFileChooser.FILES_ONLY);
    dlg.setMultiSelectionEnabled(multiselect);

    switch (dlg.showDialog(parent, "Choose File")) {
    case JFileChooser.APPROVE_OPTION:
      return dlg.getSelectedFiles();
    default:
      return null;
    }
  }
}
