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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.jackwhite20.comix.config.ComixConfig;
import de.jackwhite20.comix.config.Config;
import de.jackwhite20.comix.console.Console;
import de.jackwhite20.comix.util.Color;
import de.jackwhite20.comix.util.TargetData;
import de.jackwhite20.comix.util.ThreadEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by JackWhite20 on 13.07.2015.
 */
public class Main {

    public static void main(String[] args) {
        ThreadEvent threadEvent = new ThreadEvent();

        Console console = new Console("Comix > ", Color.CYAN, threadEvent);
        new Thread(console, "Console").start();

        try {
            threadEvent.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Console.getConsole().println("------ Comix v.0.1 ------");

        ComixConfig comixConfig = null;
        try {
            comixConfig = Config.loadConfig("");

            Console.getConsole().println("Config loaded...");
        } catch (Exception e) {
            Console.getConsole().println("Unable to load Comix Config file!");
            System.exit(1);
        }

        Comix comix = new Comix(comixConfig);
        comix.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            console.stop();
            comix.shutdown();
        }));
    }

}
