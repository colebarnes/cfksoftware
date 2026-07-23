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

package org.cfksoftware.gui;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;

import org.cfksoftware.common.logging.CfkLogger;

public class ScreenActivitySim {
  // x delta
  private int xDelta;

  // y delta
  private int yDelta;

  // time delta for point shift
  private long shiftDelta;

  // time delta between shift operations
  private long threadDelta;

  // the thread
  private Thread th = null;

  // is stopped
  private boolean isStopped;

  public ScreenActivitySim() {
    this(1, 0, 0, 60000);
  }

  public ScreenActivitySim(int xDelta, int yDelta, long shiftDelta, long threadDelta) {
    this.setXDelta(xDelta);
    this.setYDelta(yDelta);
    this.setShiftDelta(shiftDelta);
    this.setThreadDelta(threadDelta);
  }

  public void setXDelta(int xDelta) {
    this.xDelta = xDelta;
  }

  public void setYDelta(int yDelta) {
    this.yDelta = yDelta;
  }

  public void setShiftDelta(long shiftDelta) {
    this.shiftDelta = shiftDelta;
  }

  public void setThreadDelta(long threadDelta) {
    this.threadDelta = threadDelta;
  }

  public void start() {
    // TODO: start?
  }

  public void stop() {
    // TODO: stop?
  }

  public static void main(String[] args) {
    int xOffset = 1;
    int yOffset = 0;
    int foo = 0;
    int sleep = 60000;

    try {
      while (true) {
        Point p1 = MouseInfo.getPointerInfo().getLocation();
        Point p2 = new Point(p1.x + xOffset, p1.y + yOffset);
        Robot r = new Robot();

        r.mouseMove(p2.x, p2.y);
        CfkLogger.info("%s", p2.toString());
        Thread.sleep(foo);

        r.mouseMove(p1.x, p1.y);
        CfkLogger.info("%s", p1.toString());
        Thread.sleep(sleep);
      }
    } catch (Exception e) {
      CfkLogger.warn(e);
    }
  }
}
