package com.serveressentials.api.nick;

import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Objects;


public final class NickValidationRules {
    private final int minLength;
    private final int maxLength;
    private final boolean allowFormatting;
    private final boolean allowReset;
    private final boolean allowDuplicates;
    private final int cooldown;
    private final int maxChangesPerDay;
    private final @NotNull List<String> blockedWords;
    private final @NotNull List<String> blacklistPatterns;

    public NickValidationRules(int minLength, int maxLength, boolean allowFormatting,
                               boolean allowReset, boolean allowDuplicates, int cooldown,
                               int maxChangesPerDay, @NotNull List<String> blockedWords,
                               @NotNull List<String> blacklistPatterns) {
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.allowFormatting = allowFormatting;
        this.allowReset = allowReset;
        this.allowDuplicates = allowDuplicates;
        this.cooldown = cooldown;
        this.maxChangesPerDay = maxChangesPerDay;
        this.blockedWords = Objects.requireNonNull(blockedWords, "blockedWords cannot be null");
        this.blacklistPatterns = Objects.requireNonNull(blacklistPatterns, "blacklistPatterns cannot be null");
    }

    public int getMinLength() {
        return minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public boolean isAllowFormatting() {
        return allowFormatting;
    }

    public boolean isAllowReset() {
        return allowReset;
    }

    public boolean isAllowDuplicates() {
        return allowDuplicates;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getMaxChangesPerDay() {
        return maxChangesPerDay;
    }

    public @NotNull List<String> getBlockedWords() {
        return List.copyOf(blockedWords);
    }

    public @NotNull List<String> getBlacklistPatterns() {
        return List.copyOf(blacklistPatterns);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NickValidationRules)) return false;
        NickValidationRules that = (NickValidationRules) obj;
        return minLength == that.minLength &&
                maxLength == that.maxLength &&
                allowFormatting == that.allowFormatting &&
                allowReset == that.allowReset &&
                allowDuplicates == that.allowDuplicates &&
                cooldown == that.cooldown &&
                maxChangesPerDay == that.maxChangesPerDay &&
                blockedWords.equals(that.blockedWords) &&
                blacklistPatterns.equals(that.blacklistPatterns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minLength, maxLength, allowFormatting, allowReset, allowDuplicates,
                cooldown, maxChangesPerDay, blockedWords, blacklistPatterns);
    }

    @Override
    public String toString() {
        return "NickValidationRules{" +
                "minLength=" + minLength +
                ", maxLength=" + maxLength +
                ", allowFormatting=" + allowFormatting +
                ", allowReset=" + allowReset +
                ", allowDuplicates=" + allowDuplicates +
                ", cooldown=" + cooldown +
                ", maxChangesPerDay=" + maxChangesPerDay +
                ", blockedWords=" + blockedWords +
                ", blacklistPatterns=" + blacklistPatterns +
                '}';
    }
}