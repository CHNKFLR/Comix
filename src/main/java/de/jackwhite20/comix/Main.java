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

import de.jackwhite20.comix.command.Command;

import java.util.logging.Level;

/**
 * Created by JackWhite20 on 13.07.2015.
 */
public class Main {

    public static void main(String[] consoleArgs) throws Exception {
        Comix comix = new Comix();
        new Thread(comix, "Comix").start();

        while (comix.isRunning()) {
            String line = Comix.getConsoleReader().readLine("Comix >");

            if(line == "")
                continue;

            String[] splitted = line.split(" ");
            int length = splitted.length - 1;
            String[] args = new String[0];
            if(length > 1) {
                args = new String[length];
                System.arraycopy(splitted, 1, args, 0, length);
            }
            String name = splitted[0];

            Command command = comix.getCommandManager().findCommand(name);

            if(command != null) {
                command.execute(args);
            }else {
                Comix.getLogger().log(Level.INFO, "Command not found!");
            }
        }
    }

}
