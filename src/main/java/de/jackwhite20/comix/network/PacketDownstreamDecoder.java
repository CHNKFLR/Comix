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

import de.jackwhite20.comix.console.Console;
import de.jackwhite20.comix.util.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * Created by JackWhite20 on 14.07.2015.
 */
public class PacketDownstreamDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        ByteBuf copy = byteBuf.copy();

        try {
            ByteBuf buffer = byteBuf.retain();

            int length = Protocol.readVarInt(buffer);
            int packetId = Protocol.readVarInt(buffer);

            if(packetId == 1) {
                Console.getConsole().println("Ping response - Length: " + length);
                //Console.getConsole().println("Bungee: " + ByteBufUtil.hexDump(buffer));

                ByteBuf pingResponse = Unpooled.buffer();
                Protocol.writeVarInt(9, pingResponse); // Size?
                Protocol.writeVarInt(1, pingResponse); // Packet id
                pingResponse.writeLong(buffer.readLong());
                //Console.getConsole().println("Mine: " + ByteBufUtil.hexDump(pingResponse));
                list.add(pingResponse.retain());

                Console.getConsole().println("Received: " + ByteBufUtil.hexDump(copy));
                Console.getConsole().println("Writed: " + ByteBufUtil.hexDump(pingResponse.copy()));
/*                ByteBuf out = Unpooled.buffer();
                ByteBuf data = Unpooled.buffer();
                Protocol.writeVarInt(1, data);
                out.writeLong(464646L);
                byte[] dataBytes = data.retain().array();
                Protocol.writeVarInt(dataBytes.length, out);
                out.writeBytes(dataBytes);
                Console.getConsole().println("Mine New: " + ByteBufUtil.hexDump(out));*/

            }else {
                list.add(copy);
            }
/*            if(length > 0) {
                int packetId = Protocol.readVarInt(buffer);

                Console.getConsole().println("Decoded packet ID: " + packetId);

                if(packetId == 2) {
                    UUID uuid = Protocol.readUUID(buffer);
                    String name = Protocol.readString(buffer);

                    Console.getConsole().println("LOGIN SUCCESS: " + uuid.toString() + " - " + name);
                }
            }*/
            //list.add(pingResponse.retain());
            copy = null;
        } finally {
            if(copy != null)
                copy.release();
        }
    }

}
