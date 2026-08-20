package com.example.snowisland;

import com.example.snowisland.entity.LocationNpc;
import com.example.snowisland.util.NpcRoster;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class NpcRosterTest {

    @Test
    public void canonicalRosterIsTwelveOpenNpcs() {
        assertEquals(12, NpcRoster.canonical().size());
        Set<String> names = new HashSet<>();
        for (NpcRoster.Spec spec : NpcRoster.canonical()) {
            assertTrue(names.add(spec.name), "duplicate NPC name: " + spec.name);
            assertNotNull(spec.personality);
            assertFalse(spec.personality.isEmpty());
            assertNotNull(spec.dialogueStyle);
            assertFalse(spec.dialogueStyle.isEmpty());
            assertNotEquals(LocationNpc.Attitude.喜好, spec.attitudeScourge);
        }
        assertEquals(3, NpcRoster.unusedNames().size());
        assertTrue(NpcRoster.unusedNames().contains("艾琳娜·费舍尔"));
        assertTrue(NpcRoster.unusedNames().contains("奥拉夫·斯滕"));
        assertTrue(NpcRoster.unusedNames().contains("莉莉安·韦弗"));
    }

    @Test
    public void bobPrefersRulersNotRebels() {
        NpcRoster.Spec bob = NpcRoster.canonical().stream()
                .filter(s -> "鲍勃·塔克".equals(s.name))
                .findFirst()
                .orElseThrow();
        assertEquals(LocationNpc.Attitude.喜好, bob.attitudeRuler);
        assertEquals(LocationNpc.Attitude.厌恶, bob.attitudeRebel);
        assertEquals(LocationNpc.Attitude.忽视, bob.attitudeAdventurer);
        assertEquals(LocationNpc.Attitude.忽视, bob.attitudeScourge);
    }
}
