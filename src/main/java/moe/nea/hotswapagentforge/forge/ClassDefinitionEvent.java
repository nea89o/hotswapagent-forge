package moe.nea.hotswapagentforge.forge;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Event triggered when a class is defined anywhere in the JVM.
 * Note that the class may not be loaded in the same class loader as minecraft and might therefore not be accessible to
 * the running code under that same name.
 */
@AllArgsConstructor
public class ClassDefinitionEvent extends HotswapEvent {
    /**
     * The fully qualified name of the class that was just defined.
     */
    @Getter
    private String fullyQualifiedName;

    protected ClassDefinitionEvent() {
    }

    /**
     * This event is fired when a class is first defined in a certain context. See {@link ClassDefinitionEvent} for
     * more information.
     */
    public static class Definition extends ClassDefinitionEvent {
        public Definition(String fullyQualifiedName) {
            super(fullyQualifiedName);
        }

        @Override
        public String toString() {
            return "ClassDefinitionEvent.Definition(" + getFullyQualifiedName() + ")";
        }
    }

    /**
     * This event is fired when a class is redefined in a certain context. This should only ever happen
     * from a hotswap. See {@link ClassDefinitionEvent} for more information.
     */
    public static class Redefinition extends ClassDefinitionEvent {
        public Redefinition(String fullyQualifiedName) {
            super(fullyQualifiedName);
        }

        @Override
        public String toString() {
            return "ClassDefinitionEvent.Redefinition(" + getFullyQualifiedName() + ")";
        }
    }

}
