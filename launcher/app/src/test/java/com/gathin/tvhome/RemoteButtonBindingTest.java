package com.gathin.tvhome;

import org.junit.Test;
import static org.junit.Assert.*;

public class RemoteButtonBindingTest {
    @Test public void replacingOrRemovingOneButtonPreservesOthers() {
        RemoteButtonBinding a = new RemoteButtonBinding(190, 10, "YouTube", "a.b");
        RemoteButtonBinding b = new RemoteButtonBinding(191, 11, "Netflix", "c.d");
        java.util.List<RemoteButtonBinding> list = java.util.Arrays.asList(a, b);
        java.util.List<RemoteButtonBinding> replaced = RemoteButtonBinding.updated(list,
                new RemoteButtonBinding(190, 10, "YouTube", "e.f"));
        assertEquals(2, replaced.size());
        assertEquals("e.f", RemoteButtonBinding.find(replaced,190,10,0x201).app);
        assertEquals("c.d", RemoteButtonBinding.find(replaced,191,11,0x201).app);
        assertNull(RemoteButtonBinding.find(replaced,190,10,0x401));
        assertEquals(1, RemoteButtonBinding.without(replaced,a.id()).size());
        assertEquals(2, list.size());
    }
    @Test public void essentialControlsCannotBeMapped() {
        for (int key : new int[]{0,3,4,19,20,21,22,23,24,25,26,66,91,122,123,160,164,170,176,177,178,179,181,223,224,280,281,282,283}) {
            assertFalse("protected key " + key, RemoteButtonBinding.assignable(key, 0x201));
        }
    }
    @Test public void appButtonsCanBeMappedButGamepadsPassThrough() {
        for (int key : new int[]{190,191,192,193,194,195,196,206,207,208,209,4065}) {
            assertTrue(RemoteButtonBinding.assignable(key, 0x201));
            assertFalse(RemoteButtonBinding.assignable(key, 0x401));
            assertFalse(RemoteButtonBinding.assignable(key, 0x1000010));
        }
    }
    @Test public void alternateNavigationAndVoiceControlsAlsoStayProtected() {
        for (int key : new int[]{187,219,231,264,268,269,270,271,276,284}) {
            assertFalse("alternate essential key " + key, RemoteButtonBinding.assignable(key, 0x201));
        }
    }
    @Test public void sharedKeycodesAreDistinguishedByScanCode() {
        RemoteButtonBinding netflix = new RemoteButtonBinding(196, 485, "Netflix", "com.community.mbox.tv");
        assertTrue(netflix.matches(196, 485));
        assertFalse(netflix.matches(196, 490));
        assertFalse(netflix.matches(195, 485));
        assertFalse(netflix.matches(196, 0));
        assertNotEquals(netflix.id(), new RemoteButtonBinding(196, 490, "Prime", "ar.tvplayer.tv").id());
    }
    @Test public void noScanBindingMatchesOnlyItsKeycode() {
        RemoteButtonBinding button = new RemoteButtonBinding(190, 0, "YouTube", "io.gh.reisxd.tizentube.cobalt");
        assertTrue(button.matches(190, 406));
        assertFalse(button.matches(191, 406));
    }
    @Test public void malformedAndUnsafeBindingsAreRejected() {
        assertFalse(new RemoteButtonBinding(3, 10, "Home", "a.b").valid());
        assertFalse(new RemoteButtonBinding(190, -1, "YouTube", "a.b").valid());
        assertFalse(new RemoteButtonBinding(190, 1, "YouTube", "settings").valid());
        assertFalse(new RemoteButtonBinding(190, 1, "YouTube", "a.b;am start evil").valid());
        assertFalse(new RemoteButtonBinding(190, 1, "", "a.b").valid());
        assertTrue(new RemoteButtonBinding(190, 1, "YouTube", "a.b").valid());
    }
}
