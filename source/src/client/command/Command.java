package client.command;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Map;

/**
 * @author Edwin
 *         Created on 4/20/2016
 *         Object created after a user types a certain command.
 */
public class Command {
    /**
     * Actual typed string
     */
    private String content;
    /**
     * onMap map the player is on
     */
    private Map m;
    /**
     * chr player who sent the command
     */
    private Character chr;

    /**
     * Is the command allowed to be executed?
     * @return true or false depending on wether it's allowed or not
     */
    private boolean isAllowed() {
        // todo: formulas
        throw new NotImplementedException();
    }

    public Command(String content, Map m, Character chr) {
        this.content = content;
        this.m = m;
        this.chr = chr;
    }

    public void execute() {
        if (isAllowed()) {
            // todo: execution part
        }
    }

    /**
     * Basically !, @, #, etc.
     */
    public enum PrefixType {
        PLAYER,
        DONOR,
        GM,
        SUPERGM,
        ADMIN
    }
}
