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

package de.jackwhite20.comix.handler;

import de.jackwhite20.comix.Comix;
import de.jackwhite20.comix.strategy.BalancingStrategy;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.util.List;
import java.util.logging.Level;

/**
 * Created by JackWhite20 on 18.07.2015.
 */
public class ComixChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        // Simple IP-Blacklist
        if (Comix.getInstance().isIpBanned(ch.remoteAddress().getAddress().getHostAddress())) {
            ch.close();
            return;
        }

        PacketHandler packetHandler = new PacketHandler();
        p.addFirst(packetHandler);

        UpstreamHandler upstreamHandler = new UpstreamHandler(Comix.getInstance().getBalancingStrategy());
        p.addLast(upstreamHandler);

        packetHandler.setUpstreamHandler(upstreamHandler);

        Comix.getLogger().log(Level.INFO, "[" + ch.remoteAddress().getAddress().getHostAddress() + "] -> InitialHandler has connected");
    }

}
