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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.logging.Level;

/**
 * Created by JackWhite20 on 13.07.2015.
 */
public class DownstreamHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private ComixClient client;

    private Channel upstreamChannel;

    public DownstreamHandler(ComixClient client, Channel upstreamChannel) {
        this.client = client;
        this.upstreamChannel = upstreamChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.read();
        ctx.write(Unpooled.EMPTY_BUFFER);

        Comix.getLogger().log(Level.INFO, "[" + client.getName() + "] <-> DownstreamHandler has connected");
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        upstreamChannel.writeAndFlush(byteBuf.retain()).addListener((future) -> {
            if(future.isSuccess()) {
                ctx.channel().read();
            }else {
                ctx.channel().close();
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (upstreamChannel != null) {
            if (upstreamChannel.isActive()) {
                upstreamChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }

            Comix.getLogger().log(Level.INFO, "[" + client.getName() + "] -> DownstreamHandler has disconnected");
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

}
