package ir;

import symbols.Symbol;
import utils.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class VTable implements Iterable<Map.Entry<Symbol, Integer>> {
    private static final int OFFSET_JUMP = 4;
    private int maxOffset = 0;

    @NotNull
    private HashMap<@NotNull Symbol, @NotNull Integer> offsets = new HashMap<>();


    public void set(@NotNull Symbol symbol, int offset) {
        offsets.put(symbol, offset);
        if (offset >= maxOffset) {
            maxOffset = offset + OFFSET_JUMP;
        }
    }

    public void append(@NotNull Symbol symbol) {
        offsets.put(symbol, maxOffset);
        maxOffset += OFFSET_JUMP;
    }

    public int get(@NotNull Symbol symbol) {
        return offsets.get(symbol);
    }

    @Override
    public Iterator<Map.Entry<Symbol, Integer>> iterator() {
        return offsets.entrySet().iterator();
    }

    public static VTable extend(@NotNull VTable base) {
        VTable table = new VTable();
        base.forEach(item -> table.set(item.getKey(), item.getValue()));
        return table;
    }
}
