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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.jackwhite20.comix.Comix;
import de.jackwhite20.comix.console.Console;
import de.jackwhite20.comix.util.Protocol;
import de.jackwhite20.comix.util.TargetData;
import de.jackwhite20.comix.util.ThreadEvent;
import de.jackwhite20.comix.util.Util;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by JackWhite20 on 14.07.2015.
 */
public class PacketDecoderNew extends MessageToMessageDecoder<ByteBuf> {

    private UpstreamHandler upstreamHandler;

    public PacketDecoderNew(UpstreamHandler upstreamHandler) {
        this.upstreamHandler = upstreamHandler;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        ByteBuf copy = byteBuf.copy();

        try {
            ByteBuf buffer = byteBuf.retain();

            /*int length = */Protocol.readVarInt2(buffer);

            int packetId = Protocol.readVarInt2(buffer);

            if (packetId == 0 && buffer.readableBytes() > 0) {
                //Console.getConsole().println("Handshake Packet: " + buffer.readableBytes() + "bytes");

                int version = Protocol.readVarInt(buffer);
                String ip = Protocol.readString(buffer);
                int port = buffer.readUnsignedShort();
                int state = Protocol.readVarInt(buffer);

                //Console.getConsole().println("State: " + state);

                if (state == 1) {
                    //Console.getConsole().println("Sending response...");
                    ByteBuf responseBuffer = Unpooled.buffer();
                    String response = Comix.getInstance().getStatusResponseString();
                    Protocol.writeVarInt(3 + response.length(), responseBuffer); // Size, not used but needed
                    Protocol.writeVarInt(0, responseBuffer); // Packet id
                    Protocol.writeString(response, responseBuffer); // Data as json string
                    channelHandlerContext.writeAndFlush(responseBuffer);
                    //Console.getConsole().println("Finished...");
                    //list.add(copy.retain());
                } else if (state == 2) {
                    upstreamHandler.startProxying(copy.retain());

                    Console.getConsole().println("HANDSHAKE: " + version + " - " + ip + ":" + port + " - " + "LOGIN");
                    String name = Protocol.readString(buffer);

                    channelHandlerContext.channel().pipeline().removeFirst();

                    Console.getConsole().println("AUTH WITH NAME: " + name);
                    Console.getConsole().println("--------------------------------------------------");

                    list.add(copy.retain());
                }
            } else if (packetId == 1) {
                //Console.getConsole().println("Ping request size: " + buffer.readableBytes());

                long pingLong = buffer.readLong();

                Console.getConsole().println("Ping: " + pingLong + " Time: " + (System.currentTimeMillis() - pingLong) + "ms");

                ByteBuf pingResponse = Unpooled.buffer();
                Protocol.writeVarInt(8, pingResponse); // Size?
                Protocol.writeVarInt(1, pingResponse); // Packet id
                pingResponse.writeLong(pingLong);
                channelHandlerContext.writeAndFlush(pingResponse);
                //Console.getConsole().println("Some ping request!!!!!!!!!!!!");

                channelHandlerContext.channel().close();

                //list.add(copy.retain());
            } else {
                list.add(copy.retain());
            }

            copy = null;
        } finally {
            if (copy != null)
                copy.release();
        }
    }

}
