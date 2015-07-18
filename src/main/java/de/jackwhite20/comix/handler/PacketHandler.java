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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;
import java.util.logging.Level;

/**
 * Created by JackWhite20 on 17.07.2015.
 */
public class PacketHandler extends MessageToMessageDecoder<ByteBuf> {

    private UpstreamHandler upstreamHandler;

    public void setUpstreamHandler(UpstreamHandler upstreamHandler) {
        this.upstreamHandler = upstreamHandler;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        ByteBuf copy = byteBuf.copy();

        try {
            ByteBuf buffer = byteBuf.retain();

            if(buffer.readableBytes() <= 1)
                return;

            Protocol.readVarInt(buffer); // Length

            if(buffer.readableBytes() <= 0)
                return;

            int packetId = Protocol.readVarInt(buffer);

            if (packetId == 0 && buffer.readableBytes() > 0) {
                Protocol.readVarInt(buffer); // Protocol Version
                Protocol.readString(buffer); // Ip
                buffer.readUnsignedShort(); // Port
                int state = Protocol.readVarInt(buffer); // State

                if (state == 1) {
                    ByteBuf responseBuffer = Unpooled.buffer();
                    String response = Comix.getInstance().getStatusResponseString();
                    response = response.replace("%online%", "" + Comix.getInstance().getClientsOnline());
                    Protocol.writeVarInt(3 + response.length(), responseBuffer); // Size
                    Protocol.writeVarInt(0, responseBuffer); // Packet id
                    Protocol.writeString(response, responseBuffer); // Data as json string
                    channelHandlerContext.writeAndFlush(responseBuffer.retain());

                    ByteBuf pingResponse = Unpooled.buffer();
                    Protocol.writeVarInt(9, pingResponse); // Size
                    Protocol.writeVarInt(1, pingResponse); // Packet id
                    pingResponse.writeLong(System.currentTimeMillis()); // Read long from client
                    channelHandlerContext.writeAndFlush(pingResponse.retain());

                    channelHandlerContext.channel().pipeline().remove(this);
                    channelHandlerContext.close();
                    return;
                } else if (state == 2) {
                    if(buffer.readableBytes() <= 0) {
                        channelHandlerContext.close();
                        return;
                    }

                    String name = Protocol.readString(buffer);

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

                    ComixClient comixClient = new ComixClient(name, upstreamHandler.getDownstreamHandler(), upstreamHandler);
                    Comix.getInstance().addClient(comixClient);
                    upstreamHandler.setClient(comixClient);

                    upstreamHandler.connectDownstream(copy);

                    channelHandlerContext.channel().pipeline().remove(this);

                    Comix.getLogger().log(Level.INFO, "Player logged in: " + name);

                    list.add(copy.retain());
                }
            }

            copy = null;
        } finally {
            if (copy != null)
                copy.release();
        }
    }

    private void kick(ChannelHandlerContext channelHandlerContext, String text) {
        ByteBuf test = Unpooled.buffer();
        Protocol.writeVarInt(2 + text.length(), test);
        Protocol.writeVarInt(0, test);
        Protocol.writeString(text, test);
        channelHandlerContext.writeAndFlush(test);
    }

}
