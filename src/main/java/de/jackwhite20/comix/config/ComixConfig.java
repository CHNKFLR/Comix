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

    private int threads;

    private int backlog;

    private int checkTime;

    private List<TargetData> targets = new ArrayList<>();

    private boolean maintenance;

    private String maintenanceDescription;

    private String maintenanceKickMessage;

    private String maintenancePingMessage;

    public ComixConfig(String host, int port, int threads, int backlog, int checkTime, List<TargetData> targets, boolean maintenance, String maintenanceDescription, String maintenanceKickMessage, String maintenancePingMessage) {
        this.host = host;
        this.port = port;
        this.threads = threads;
        this.backlog = backlog;
        this.checkTime = checkTime;
        this.targets = targets;
        this.maintenance = maintenance;
        this.maintenanceDescription = maintenanceDescription;
        this.maintenanceKickMessage = maintenanceKickMessage;
        this.maintenancePingMessage = maintenancePingMessage;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getThreads() {
        return threads;
    }

    public int getBacklog() {
        return backlog;
    }

    public int getCheckTime() {
        return checkTime;
    }

    public List<TargetData> getTargets() {
        return targets;
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }

    public String getMaintenanceDescription() {
        return maintenanceDescription;
    }

    public String getMaintenanceKickMessage() {
        return maintenanceKickMessage;
    }

    public String getMaintenancePingMessage() {
        return maintenancePingMessage;
    }

    @Override
    public String toString() {
        return "ComixConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", targets=" + targets +
                ", maintenance=" + maintenance +
                ", maintenanceDescription='" + maintenanceDescription + '\'' +
                ", maintenanceKickMessage='" + maintenanceKickMessage + '\'' +
                ", maintenancePingMessage='" + maintenancePingMessage + '\'' +
                '}';
    }

}
