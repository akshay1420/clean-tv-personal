package com.gathin.tvhome;
import java.io.*;
import java.nio.file.Files;
/** Copy bundled photos once, without replacing a user's files or leaving incomplete photos. */
final class WallpaperPack {
    static boolean copyMissing(InputStream source,File destination) throws IOException {
        if(destination.isFile()) return false;
        File parent=destination.getParentFile();
        if(parent==null || (!parent.isDirectory() && !parent.mkdirs())) throw new IOException("Photo storage unavailable");
        File temporary=File.createTempFile(".photo-",".tmp",parent);
        try {
            try(OutputStream output=new FileOutputStream(temporary)) {
                byte[] buffer=new byte[8192]; int count,total=0;
                while((count=source.read(buffer))!=-1) {
                    total+=count;
                    if(total>2*1024*1024) throw new IOException("Photo exceeds bundled-photo limit");
                    output.write(buffer,0,count);
                }
            }
            Files.move(temporary.toPath(),destination.toPath());
            return true;
        } finally { Files.deleteIfExists(temporary.toPath()); }
    }
}
