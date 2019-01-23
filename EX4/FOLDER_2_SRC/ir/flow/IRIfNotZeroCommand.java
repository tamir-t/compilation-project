package ir.flow;

import ir.IRCommand;
import ir.Register;
import utils.NotNull;

public class IRIfNotZeroCommand extends IRCommand {
    @NotNull
    private final Register condition;
    @NotNull
    private final IRLabel label;
    public IRIfNotZeroCommand(@NotNull Register condition, @NotNull IRLabel label) {
        super("ifnz var1 goto label");
        this.condition = condition;
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("ifnz %s goto %s", condition, label);
    }
}
