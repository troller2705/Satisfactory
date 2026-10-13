package com.troller2705.satisfactory.integration;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class FTBQuestsBridge {

    @OnlyIn(Dist.CLIENT)
    public static ChapterGroup getChapterGroupByName(String targetName) {
        if (ClientQuestFile.INSTANCE == null) return null;

        for (ChapterGroup group : ClientQuestFile.INSTANCE.getChapterGroups()) {
            if (group.getTitle().getString().equalsIgnoreCase(targetName)) {
                return group;
            }
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public static List<Chapter> getHubTiers() {
        ChapterGroup hubGroup = getChapterGroupByName("HUB");
        if (hubGroup != null) {
            return hubGroup.getChapters();
        }
        return new ArrayList<>();
    }

    @OnlyIn(Dist.CLIENT)
    public static List<Quest> getMilestonesForTier(Chapter tier) {
        if (tier == null) return new ArrayList<>();
        return tier.getQuests();
    }

    @OnlyIn(Dist.CLIENT)
    public static Quest getQuestById(long id) {
        if (ClientQuestFile.INSTANCE == null || id == 0L) return null;
        return ClientQuestFile.INSTANCE.getQuest(id);
    }
}