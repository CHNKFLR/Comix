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

package de.jackwhite20.comix;

import de.jackwhite20.comix.config.ComixConfig;
import de.jackwhite20.comix.config.Config;
import de.jackwhite20.comix.console.Console;
import de.jackwhite20.comix.util.Color;
import de.jackwhite20.comix.util.ThreadEvent;

/**
 * Created by JackWhite20 on 13.07.2015.
 */
public class Main {

    public static void main(String[] args) {
        ComixConfig comixConfig = null;
        try {
            comixConfig = Config.loadConfig("");
        } catch (Exception e) {
            System.out.println("Unable to load Comix Config file!");
            System.exit(1);
        }

        ThreadEvent threadEvent = new ThreadEvent();

        Console console = new Console("Comix > ", Color.CYAN, threadEvent);
        new Thread(console, "Console").start();

        try {
            threadEvent.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Comix comix = new Comix(comixConfig);
        comix.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            console.stop();
            comix.shutdown();
        }));
    }

}
