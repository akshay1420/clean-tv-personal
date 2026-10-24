package com.gathin.tvhome;
import org.junit.Test;
import static org.junit.Assert.*;
public class AppRedirectPolicyTest {
    @Test public void returningFromTheReplacementIsANewOpeningEvenWithinTwoSeconds() {
        AppRedirectPolicy.Gate gate = new AppRedirectPolicy.Gate();
        assertTrue(gate.opening("com.google.android.youtube.tv",true,100));
        assertFalse(gate.opening("com.google.android.youtube.tv",true,150));
        assertFalse(gate.opening("io.gh.reisxd.tizentube.cobalt",false,200));
        assertTrue(gate.opening("com.google.android.youtube.tv",true,250));
    }
    @Test public void onlyChosenOriginalAppSourcesAndSafeDestinationsAreAllowed() {
        assertTrue(AppRedirectPolicy.allowed("com.google.android.youtube.tv","io.gh.reisxd.tizentube.cobalt"));
        assertTrue(AppRedirectPolicy.allowed("com.netflix.ninja","com.community.mbox.tv"));
        assertTrue(AppRedirectPolicy.allowed("com.amazon.amazonvideo.livingroom","ar.tvplayer.tv"));
        assertFalse(AppRedirectPolicy.allowed("com.android.settings","ar.tvplayer.tv"));
        assertFalse(AppRedirectPolicy.allowed("com.netflix.ninja","com.netflix.ninja"));
        assertFalse(AppRedirectPolicy.allowed("com.netflix.ninja","com.amazon.amazonvideo.livingroom"));
        assertFalse(AppRedirectPolicy.allowed("com.netflix.ninja","com.gathin.tvhome"));
        assertFalse(AppRedirectPolicy.allowed("com.netflix.ninja","not a package"));
    }
}
