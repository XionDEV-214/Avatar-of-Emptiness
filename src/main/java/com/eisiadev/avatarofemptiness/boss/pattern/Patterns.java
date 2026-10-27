package com.eisiadev.avatarofemptiness.boss.pattern;

import com.eisiadev.avatarofemptiness.boss.AvatarEntity;
import com.eisiadev.avatarofemptiness.boss.BossPhase;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Patterns {

    public record Entry(String id, BossPhase phase, Function<AvatarEntity, Pattern> factory,
                        int ownCooldown, int globalCooldown, Predicate<AvatarEntity> available) {}

    private static final Predicate<AvatarEntity> ALWAYS = b -> true;

    public static final List<Entry> ALL = List.of(
            new Entry("star_feast_1", BossPhase.RIFT, Phase1Patterns.StarFeastI::new, 0, 0, ALWAYS),
            new Entry("thorn_feast", BossPhase.RIFT, Phase1Patterns.ThornFeast::new, 0, 0, ALWAYS),
            new Entry("ambergris_1", BossPhase.RIFT, Phase1Patterns.AmbergrisI::new, 0, 0, ALWAYS),
            new Entry("ambergris_2", BossPhase.RIFT, Phase1Patterns.AmbergrisII::new, 0, 0, ALWAYS),
            new Entry("star_feast_2", BossPhase.RIFT, Phase1Patterns.StarFeastII::new, 0, 0, ALWAYS),
            new Entry("metaphysical_pessimist", BossPhase.AVATAR, Phase2Patterns.MetaphysicalPessimist::new, 0, 0,
                    b -> !b.hasViscera()),
            new Entry("metaphysical_pessimist_2", BossPhase.AVATAR, Phase2Patterns.MetaphysicalPessimistII::new, 0, 0, ALWAYS),
            new Entry("reality_crash_1", BossPhase.AVATAR, Phase2Patterns.RealityCrashI::new, 0, 0, ALWAYS),
            new Entry("reality_crash_2", BossPhase.AVATAR, Phase2Patterns.RealityCrashII::new, 0, 0, ALWAYS),
            new Entry("visceral_remnant", BossPhase.AVATAR, Phase2Patterns.VisceralRemnant::new, 0, 0, ALWAYS),
            new Entry("visceral_fountain", BossPhase.WRATH, Phase3Patterns.VisceralFountain::new, 20 * 20, 15 * 20, ALWAYS),
            new Entry("reality_shatter", BossPhase.WRATH, Phase3Patterns.RealityShatter::new, 20 * 20, 15 * 20, ALWAYS),
            new Entry("subzero_onslaught", BossPhase.WRATH, Phase3Patterns.SubzeroOnslaught::new, 40 * 20, 35 * 20, ALWAYS),
            new Entry("universal_annihilation", BossPhase.WRATH, Phase3Patterns.UniversalAnnihilation::new, 26 * 20, 21 * 20, ALWAYS)
    );

    public static List<Entry> forPhase(BossPhase phase) {
        return ALL.stream().filter(e -> e.phase() == phase).toList();
    }

    public static Entry byId(String id) {
        for (Entry e : ALL) if (e.id().equals(id)) return e;
        return null;
    }

    private Patterns() {}
}
