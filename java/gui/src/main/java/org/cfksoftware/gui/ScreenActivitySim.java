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

public class ScreenActivitySim {
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
        System.out.println(p2);
        Thread.sleep(foo);

        r.mouseMove(p1.x, p1.y);
        System.out.println(p1);
        Thread.sleep(sleep);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
