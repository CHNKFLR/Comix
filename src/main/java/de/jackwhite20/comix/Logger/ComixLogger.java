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

package de.jackwhite20.comix.logger;

import jline.console.ConsoleReader;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Created by JackWhite20 on 17.07.2015.
 */
public class ComixLogger extends Logger {

    private final LogFormatter formatter = new LogFormatter();

    private final LogDispatcher dispatcher = new LogDispatcher(this);

    public ComixLogger(ConsoleReader console) {
        super("Comix", null);

        setLevel(Level.ALL);

        try {
            LogWriter consoleHandler = new LogWriter(console);
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(formatter);
            addHandler(consoleHandler);
        } catch (Exception e) {
            System.err.println("Failed to initialize ComixLogger!");
            e.printStackTrace();
        }
        dispatcher.start();
    }

    @Override
    public void log(LogRecord record) {
        dispatcher.queue(record);
    }

    public void realLog(LogRecord logRecord) {
        super.log(logRecord);
    }

}
