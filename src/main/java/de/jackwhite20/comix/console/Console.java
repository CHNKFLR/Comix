/*
 * Copyright (c) 2015 "JackWhite20"
 *
 * This file is part of Comix.
 *
 * Comix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.jackwhite20.comix.console;

import de.jackwhite20.comix.util.Color;
import de.jackwhite20.comix.util.ThreadEvent;
import jline.console.ConsoleReader;
import jline.console.CursorBuffer;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by JackWhite20 on 14.07.2015.
 */
public class Console implements Runnable {

    private static Console console;

    private boolean running;

    private ConsoleReader consoleReader;

    private PrintWriter printWriter;

    private String prompt;

    private String color;

    private ThreadEvent threadEvent;

    private CursorBuffer stashed;

    public Console(String prompt, String color, ThreadEvent threadEvent) {
        console = this;
        this.prompt = prompt;
        this.color = color;
        this.threadEvent = threadEvent;
    }

    public void stop() {
        running = false;
        try {
            consoleReader.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void println(String line) {
        synchronized (stashed) {
            stashLine();

            printWriter.println(line);
            printWriter.flush();

            unstashLine();
        }
    }

    private void stashLine() {
        stashed = consoleReader.getCursorBuffer().copy();
        try {
            printWriter.write("\u001b[1G\u001b[K");
            printWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unstashLine() {
        try {
            consoleReader.resetPromptLine(consoleReader.getPrompt(), this.stashed.toString(), this.stashed.cursor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            running = true;

            consoleReader = new ConsoleReader();
            consoleReader.setPrompt(color + prompt + Color.RESET);

            stashed = consoleReader.getCursorBuffer().copy();

            printWriter = new PrintWriter(consoleReader.getOutput());

            threadEvent.signal();

            String line = null;
            while (running && (line = consoleReader.readLine()) != null) {
                println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        stop();
    }

    public static Console getConsole() {
        return console;
    }

}
