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
import org.fusesource.jansi.Ansi;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by JackWhite20 on 17.07.2015.
 */
public class LogWriter extends Handler {

    private ConsoleReader console;

    public LogWriter(ConsoleReader console) {
        this.console = console;
    }

    private void println(String line) {
        try {
            console.print(ConsoleReader.RESET_LINE + line.replaceAll("\\p{C}", "") + Ansi.ansi().reset().toString() + "\n\r");
            console.drawLine();
            console.flush();
        } catch ( IOException ex ) {

        }
    }

    @Override
    public void publish(LogRecord record) {
        if(isLoggable(record)) {
            println(getFormatter().format(record));
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }

}
