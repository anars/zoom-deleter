/**
 * Zoom Deleter - Deletes zoom and prevents you from reinstalling it.
 * Copyright (c) 2021 Anar Software LLC. < http://anars.com >
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see < http://www.gnu.org/licenses/ >
 */
package com.anars;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

public class ZoomDeleter {
    private static final double VERSION = 0.1;
    private final WatchService watcher = FileSystems.getDefault().newWatchService();
    private final Map<WatchKey, Path> keys = new HashMap<WatchKey, Path>();
    private String[] _entities = new String[]{};

    ZoomDeleter(Path path, String[] entities) throws IOException {
        _entities = entities;
        for (String item : _entities)
            delete(path.resolve(item).toFile());
        WatchKey key = path.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);
        keys.put(key, path);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Zoom Deleter v" + VERSION + " - Deletes zoom and prevents you from reinstalling it.");
        System.out.println("Copyright (c) 2021 Anar Software LLC. < http://anars.com >\n");
        if (System.getProperty("os.name").equalsIgnoreCase("Mac OS X"))
            new ZoomDeleter(Paths.get("/Users/kay/Downloads/"), new String[]{"zoom.us.app", "zoom.app", "Zoom Rooms.app"}).processEvents();
        else
            System.out.println("Error : Unsupported operating system.");
    }

    void processEvents() {
        for (; ; ) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException interruptedException) {
                return;
            }
            Path path = keys.get(key);
            if (path == null)
                continue;
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
                if (kind == OVERFLOW)
                    continue;
                String entity = event.context().toString();
                for (String item : _entities)
                    if (entity.equalsIgnoreCase(item))
                        delete(path.resolve(((WatchEvent<Path>) event).context()).toFile());
            }
            if (!key.reset()) {
                keys.remove(key);
                if (keys.isEmpty())
                    break;
            }
        }
    }

    void delete(File fullPath) {
        File[] contents = fullPath.listFiles();
        if (contents != null)
            for (File file : contents)
                delete(file);
        fullPath.delete();
    }
}