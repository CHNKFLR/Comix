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

package de.jackwhite20.comix.network;

import de.jackwhite20.comix.Comix;
import de.jackwhite20.comix.strategy.BalancingStrategy;
import de.jackwhite20.comix.util.TargetData;
import de.jackwhite20.comix.util.Util;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

import java.net.InetSocketAddress;
import java.util.logging.Level;

/**
 * Created by JackWhite20 on 17.07.2015.
 */
public class UpstreamHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private ComixClient client;

    private BalancingStrategy strategy;

    private Channel upstreamChannel;

    private Channel downstreamChannel;

    private boolean downstreamConnected;

    public DownstreamHandler downstreamHandler;

    public UpstreamHandler(BalancingStrategy strategy) {
        this.strategy = strategy;
    }

    public void connectDownstream(ByteBuf initPacket) {
        InetSocketAddress address = (InetSocketAddress) upstreamChannel.remoteAddress();
        TargetData target = this.strategy.selectTarget(address.getHostName(), address.getPort());

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(upstreamChannel.eventLoop())
                .channel(upstreamChannel.getClass())
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.AUTO_READ, false)
                .option(ChannelOption.SO_TIMEOUT, 5000)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .handler(downstreamHandler = new DownstreamHandler(client, upstreamChannel));

        ChannelFuture f = bootstrap.connect(target.getHost(), target.getPort());
        downstreamChannel = f.channel();

        f.addListener((future) -> {
            if (future.isSuccess()) {
                downstreamChannel.writeAndFlush(initPacket.retain());

                downstreamConnected = true;

                Comix.getLogger().log(Level.INFO, "[" + ((client == null) ? Util.formatSocketAddress(upstreamChannel.remoteAddress()) : client.getName()) + "] <-> UpstreamHandler has connected");
                Comix.getLogger().log(Level.INFO, "[" + ((client == null) ? Util.formatSocketAddress(upstreamChannel.remoteAddress()) : client.getName()) + "] <-> [Comix] <-> [" + target.getName() + "] tunneled");
            } else {
                upstreamChannel.close();
            }
        });
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        upstreamChannel = ctx.channel();

        // Start reading
        upstreamChannel.read();
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        if(downstreamConnected) {
            downstreamChannel.writeAndFlush(byteBuf.retain()).addListener((future) -> {
                if(future.isSuccess()) {
                    ctx.channel().read();
                }else {
                    ctx.channel().close();
                }
            });
        }else {
            ctx.channel().read();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (downstreamChannel != null) {
            if (downstreamChannel.isActive()) {
                downstreamChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }

            if(client != null)
                Comix.getInstance().removeClient(client);

            Comix.getLogger().log(Level.INFO, "[" + ((client != null) ? client.getName() : Util.formatSocketAddress(upstreamChannel.remoteAddress())) + "] -> UpstreamHandler has disconnected");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        Channel ch = ctx.channel();
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    public Channel getUpstreamChannel() {
        return upstreamChannel;
    }

    public DownstreamHandler getDownstreamHandler() {
        return downstreamHandler;
    }

    public void setClient(ComixClient client) {
        this.client = client;
    }

}
