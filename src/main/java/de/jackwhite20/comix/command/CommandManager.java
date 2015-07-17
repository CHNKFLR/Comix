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

package de.jackwhite20.comix.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by JackWhite20 on 17.07.2015.
 */
public class CommandManager {

    private HashMap<String, Command> commands = new HashMap<>();

    public CommandManager() {

    }

    public Command findCommand(String name) {
        if(commands.containsKey(name))
            return commands.get(name);
        else
            return commands.values().stream().filter((Command c) -> c.isValidAlias(name)).findFirst().orElse(null);
    }

    public void addCommand(Command command) {
        commands.put(command.getName(), command);
    }

    public List<Command> getCommands() {
        return new ArrayList<>(commands.values());
    }
}
