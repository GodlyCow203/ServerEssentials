package com.serveressentials.api.scoreboard;

import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Objects;

public final class ScoreboardLayout {
    private final @NotNull String title;
    private final @NotNull List<String> lines;
    private final int maxLines;

    public ScoreboardLayout(@NotNull String title, @NotNull List<String> lines, int maxLines) {
        this.title = Objects.requireNonNull(title, "title cannot be null");
        this.lines = Objects.requireNonNull(lines, "lines cannot be null");
        this.maxLines = maxLines;
    }

    public @NotNull String getTitle() {
        return title;
    }

    public @NotNull List<String> getLines() {
        return lines;
    }

    public int getMaxLines() {
        return maxLines;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScoreboardLayout that = (ScoreboardLayout) o;
        return maxLines == that.maxLines && Objects.equals(title, that.title) && Objects.equals(lines, that.lines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, lines, maxLines);
    }

    @Override
    public @NotNull String toString() {
        return "ScoreboardLayout{" + "title='" + title + '\'' + ", lines=" + lines + ", maxLines=" + maxLines + '}';
    }
}