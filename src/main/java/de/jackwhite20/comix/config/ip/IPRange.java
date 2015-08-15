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

package de.jackwhite20.comix.config.ip;

import java.net.InetSocketAddress;

/**
 * Created by JackWhite20 on 15.08.2015.
 */
public class IPRange {

    private long lowIp;

    private long highIp;

    public IPRange(String lowIp, String highIp) {
        this.lowIp = ipToLong(new InetSocketAddress(lowIp, 0));
        this.highIp = ipToLong(new InetSocketAddress(highIp, 0));
    }

    public boolean isAllowed(InetSocketAddress ip) {
        long ipToTest = ipToLong(ip);

        return ipToTest >= lowIp && ipToTest <= highIp;
    }

    public boolean isAllowed(byte[] octets) {
        long ipToTest = ipToLong(octets);

        return ipToTest >= lowIp && ipToTest <= highIp;
    }

    private long ipToLong(byte[] octets) {
        long result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= octet & 0xff;
        }

        return result;
    }

    private long ipToLong(InetSocketAddress ip) {
        byte[] octets = ip.getAddress().getAddress();
        long result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= octet & 0xff;
        }

        return result;
    }
}
