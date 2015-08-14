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

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

/**
 * Created by JackWhite20 on 14.08.2015.
 */
public class ConfigLoader {

    private static final Gson GSON = new Gson();

    private static final String LINE_SEPARATOR = System.lineSeparator();

    public static <T> T loadConfig(String name, Class<T> type) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(name));
        StringBuilder stringBuilder = new StringBuilder();

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(LINE_SEPARATOR);
        }

        return GSON.fromJson(stringBuilder.toString(), type);
    }

    public static void loadFile(String name, List<String> listToFill) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(name));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if(line != "")
                listToFill.add(line);
        }
    }
}
