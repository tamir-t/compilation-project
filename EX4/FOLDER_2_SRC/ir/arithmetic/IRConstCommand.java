package ir.arithmetic;

import ir.IRAssignmentCommand;
import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public class IRConstCommand extends IRAssignmentCommand  {
    private final int value;
    public IRConstCommand(@NotNull Register dest, int value) {
        super("var := const", dest);
        this.value = value;
    }

    @Override
    public Set<Register> getDependencies() {
        return setOf();
    }

    @Override
    public Set<Register> getInvalidates() {
        return setOf(dest);
    }

    @Override
    public String toString() {
        return String.format("%s := %d", dest, value);
    }
}
