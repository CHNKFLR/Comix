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

package de.jackwhite20.comix.whitelist;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JackWhite20 on 18.07.2015.
 */
public class Whitelist {

    private boolean enabled;

    private String message;

    private List<String> names = new ArrayList<>();

    public Whitelist(boolean enabled, List<String> names) {
        this.enabled = enabled;
        this.names = names;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getNames() {
        return names;
    }

}
