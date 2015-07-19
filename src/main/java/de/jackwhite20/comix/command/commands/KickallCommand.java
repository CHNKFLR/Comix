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

package de.jackwhite20.comix.command.commands;

import de.jackwhite20.comix.Comix;
import de.jackwhite20.comix.command.Command;

/**
 * Created by JackWhite20 on 17.07.2015.
 */
public class KickallCommand extends Command {

    public KickallCommand(String name, String[] aliases, String description) {
        super(name, aliases, description);
    }

    @Override
    public boolean execute(String[] args) {
        if(Comix.getInstance().getClientsOnline() == 0) {
            Comix.getLogger().info("No players online!");
            return true;
        }

        Comix.getLogger().info("Kicking " + Comix.getInstance().getClientsOnline() + " players!");

        Comix.getInstance().kickAll();

        Comix.getLogger().info("All players kicked!");

        return true;
    }

}
