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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.LogRecord;

/**
 * Created by JackWhite20 on 17.07.2015.
 */
public class LogDispatcher extends Thread {

    private ComixLogger logger;

    private final BlockingQueue<LogRecord> queue = new LinkedBlockingQueue<>();

    public LogDispatcher(ComixLogger logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            LogRecord record;
            try {
                record = queue.take();
            } catch (InterruptedException ex) {
                continue;
            }

            logger.realLog(record);
        }

        for (LogRecord record : queue) {
            logger.realLog(record);
        }
    }

    public void queue(LogRecord record) {
        if (!isInterrupted()) {
            queue.add(record);
        }
    }

}
