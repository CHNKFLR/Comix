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

package de.jackwhite20.comix.config;

import de.jackwhite20.comix.util.TargetData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JackWhite20 on 14.07.2015.
 */
public class ComixConfig {

    private String host;

    private int port;

    private List<TargetData> targets = new ArrayList<>();

    public ComixConfig(String host, int port, List<TargetData> targets) {
        this.host = host;
        this.port = port;
        this.targets = targets;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<TargetData> getTargets() {
        return targets;
    }

    public void setTargets(List<TargetData> targets) {
        this.targets = targets;
    }

    @Override
    public String toString() {
        return "ComixConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", targets=" + targets +
                '}';
    }

}
