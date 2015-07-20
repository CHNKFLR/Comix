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
import de.jackwhite20.comix.network.ComixClient;
import de.jackwhite20.comix.network.Protocol;
import de.jackwhite20.comix.network.ProtocolState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * Created by JackWhite20 on 18.07.2015.
 */
public class HandshakeHandler extends MessageToMessageDecoder<ByteBuf> {

    private UpstreamHandler upstreamHandler;

    private ProtocolState protocolMode = ProtocolState.HANDSHAKE;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) throws Exception {
        ByteBuf copy = byteBuf.copy();

        Protocol.readVarInt(byteBuf);

        int packetId = Protocol.readVarInt(byteBuf);

        if(packetId == 0) {
            if(protocolMode == ProtocolState.HANDSHAKE) {
                Protocol.readVarInt(byteBuf);
                Protocol.readString(byteBuf);
                byteBuf.readUnsignedShort();
                protocolMode = ProtocolState.valueOf((byte) Protocol.readVarInt(byteBuf));
            }

            if(protocolMode == ProtocolState.STATUS) {
                ByteBuf responseBuffer = Unpooled.buffer();
                String response = Comix.getInstance().getStatusResponseString();
                response = response.replace("%online%", "" + Comix.getInstance().getClientsOnline()); // Replace online count
                Protocol.writeVarInt(3 + response.length(), responseBuffer); // Size
                Protocol.writeVarInt(0, responseBuffer); // Packet id
                Protocol.writeString(response, responseBuffer); // Data as json string
                channelHandlerContext.writeAndFlush(responseBuffer);

                // Read 2 bytes to prevent problems!
                if(byteBuf.readableBytes() == 2)
                    byteBuf.readBytes(2);

                // Sending Pong instant because otherwise the pong will not receive properly!
                ByteBuf pongBuffer = Unpooled.buffer();
                Protocol.writeVarInt(9, pongBuffer);
                Protocol.writeVarInt(1, pongBuffer);
                pongBuffer.writeLong(0);
                channelHandlerContext.writeAndFlush(pongBuffer);

                channelHandlerContext.close();
            }

            if(protocolMode == ProtocolState.LOGIN) {
                //TODO: Fix this!
                if(byteBuf.readableBytes() == 0) {
                    kick(channelHandlerContext, "{\"text\": \"Pls rejoin!\",\"color\": \"dark_red\"}");
                    channelHandlerContext.close();
                    return;
                }

                String name = Protocol.readString(byteBuf);

                //TODO: Improve
                if(Comix.getInstance().getComixConfig().isMaintenance()) {
                    if(Comix.getInstance().isWhitelistEnabled()) {
                        if(!Comix.getInstance().isWhitelisted(name)) {
                            kick(channelHandlerContext, Comix.getInstance().getWhitelistKickMessage());
                        }
                    }else {
                        kick(channelHandlerContext, Comix.getInstance().getComixConfig().getMaintenanceKickMessage());
                    }

                    channelHandlerContext.close();
                    return;
                }else if(Comix.getInstance().isWhitelistEnabled() && !Comix.getInstance().isWhitelisted(name)) {
                    kick(channelHandlerContext, Comix.getInstance().getWhitelistKickMessage());

                    channelHandlerContext.close();
                    return;
                }

                upstreamHandler.connectDownstream(copy);

                ComixClient comixClient = new ComixClient(name, upstreamHandler.getDownstreamHandler(), upstreamHandler);
                Comix.getInstance().addClient(comixClient);
                upstreamHandler.setClient(comixClient);

                channelHandlerContext.channel().pipeline().remove(this);

                Comix.getLogger().info("Player logged in: " + name);

                out.add(copy.retain());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        Channel ch = ctx.channel();
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void kick(ChannelHandlerContext channelHandlerContext, String text) {
        ByteBuf test = Unpooled.buffer();
        Protocol.writeVarInt(2 + text.length(), test);
        Protocol.writeVarInt(0, test);
        Protocol.writeString(text, test);
        channelHandlerContext.writeAndFlush(test);
    }

    public void setUpstreamHandler(UpstreamHandler upstreamHandler) {
        this.upstreamHandler = upstreamHandler;
    }

}