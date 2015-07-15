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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;
import java.util.UUID;

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
            Console.getConsole().println("Length: " + length);
            if(length > 0) {
                int packetId = Protocol.readVarInt(buffer);

                Console.getConsole().println("Decoded packet ID: " + packetId);

                if(packetId == 2) {
                    UUID uuid = Protocol.readUUID(buffer);
                    String name = Protocol.readString(buffer);

                    Console.getConsole().println("LOGIN SUCCESS: " + uuid.toString() + " - " + name);
                }
            }
            list.add(copy.retain());
            copy = null;
        } finally {
            if(copy != null)
                copy.release();
        }
    }

}
