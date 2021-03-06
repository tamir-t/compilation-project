package ir.analysis;


import ir.commands.IRCommand;
import ir.commands.flow.IRFlowCommand;
import ir.commands.flow.IRGotoCommand;
import ir.commands.flow.IRLabel;
import ir.commands.functions.IRReturnCommand;
import utils.NotNull;
import utils.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Receives a stream of commands using {@link #handle(IRCommand)}, and on the end {@link #finish()} will return the list of {@link IRBlock} generated.
 */
public class IRBlockGenerator {
    /**
     * Mapping between labels and blocks. A block will be created the first time a label is encountered in {@link ir.commands.flow.IRFlowCommand} or in label deceleration.
     */
    @NotNull
    private final HashMap<IRLabel, IRBlock> labelBlocks = new HashMap<>();
    /**
     * The current handled block
     */
    @Nullable
    private IRBlock currentBlock = null;
    /**
     * The previous block. Will not be null iff needed to be attached to the new block
     */
    @Nullable
    private IRBlock previousBlock = null;
    /**
     * Will the new block be reachable from the previous block.
     */
    private boolean isReachable = false;

    /**
     * All blocks that were seen.
     */
    @NotNull
    private final List<IRBlock> blocks = new ArrayList<>();

    public void handle(@NotNull IRCommand command) {
        if (command instanceof IRFlowCommand) {
            handleFlow(((IRFlowCommand) command));
        } else if (command instanceof IRLabel) {
            handleLabel(((IRLabel) command));
        } else {
            handleNormal(command);
        }
    }

    public List<IRBlock> finish() {
        if (currentBlock != null) {
            blocks.add(currentBlock);
        }
        return blocks;
    }

    private void handleNormal(IRCommand command) {
        if (currentBlock == null) {
            replaceCurrent(new IRBlock());
            prevAttach();
        }

        currentBlock.commands.add(command);

        if (command instanceof IRReturnCommand) {
            replaceCurrent(null);
        }
    }

    private void handleFlow(@NotNull IRFlowCommand command) {
        if (currentBlock == null) {
            replaceCurrent(new IRBlock());
            prevAttach();
        }
        currentBlock.commands.add(command);

        IRBlock possibleNextBlock = labelBlocks.computeIfAbsent(command.getLabel(), IRBlock::new);
        possibleNextBlock.prev.add(currentBlock);
        currentBlock.next.add(possibleNextBlock);

        previousBlock = currentBlock;
        isReachable = !(command instanceof IRGotoCommand);
        replaceCurrent(null);
    }

    private void handleLabel(@NotNull IRLabel label) {
        IRBlock nextBlock = labelBlocks.computeIfAbsent(label, IRBlock::new);
        if (currentBlock != null) {
            previousBlock = currentBlock;
            isReachable = true;
        }
        replaceCurrent(nextBlock);
        prevAttach();
    }

    private void prevAttach() {
        assert currentBlock != null;

        if (previousBlock != null) {
            previousBlock.realNextBlock = currentBlock;
            if (isReachable) {
                previousBlock.next.add(currentBlock);
                currentBlock.prev.add(previousBlock);
            }
        }
        previousBlock = null;
    }

    private void replaceCurrent(@Nullable IRBlock newBlock) {
        if (currentBlock != null)
            blocks.add(currentBlock);
        currentBlock = newBlock;
    }
}
