package com.gathin.tvhome;
import java.io.*;
import java.nio.file.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class WallpaperPackTest {
    @Test public void importsNewPhotoButPreservesUserReplacement() throws Exception {
        Path folder=Files.createTempDirectory("pack"); File target=folder.resolve("photo.jpg").toFile();
        assertTrue(WallpaperPack.copyMissing(new ByteArrayInputStream(new byte[]{1,2}),target));
        assertArrayEquals(new byte[]{1,2},Files.readAllBytes(target.toPath()));
        assertFalse(WallpaperPack.copyMissing(new ByteArrayInputStream(new byte[]{9}),target));
        assertArrayEquals(new byte[]{1,2},Files.readAllBytes(target.toPath()));
    }
    @Test public void failedTransferDoesNotLeaveTruncatedPhoto() throws Exception {
        Path folder=Files.createTempDirectory("pack"); File target=folder.resolve("photo.jpg").toFile();
        InputStream broken=new InputStream() { public int read() throws IOException { throw new IOException("broken"); } };
        try { WallpaperPack.copyMissing(broken,target); fail("Copy failure hidden"); } catch(IOException expected) { }
        assertFalse(target.exists()); assertEquals(0,folder.toFile().list().length);
    }
}
