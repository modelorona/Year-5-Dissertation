/*
 * Copyright (c) 2010-2020 Haifeng Li. All rights reserved.
 *
 * Smile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Smile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Smile.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.anguel.dissertation.ml.smile.io;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Static methods that return the InputStream/Reader of a file or URL.
 *
 * @author Haifeng Li
 */
public interface Input {
    /**
     * Returns the reader of a file path or URI.
     *
     * @param path    the input file path.
     * @param context android context
     * @return the file reader.
     * @throws IOException when fails to read the file.
     */
    static BufferedReader reader(String path, Context context) throws IOException {
        FileInputStream fis = context.openFileInput(path);
        InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
        return new BufferedReader(inputStreamReader);
    }

}
