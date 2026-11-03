package at.marcelr.mrwaypoints;

import at.marcelr.mrwaypoints.util.Versions;

/**
 * Which versions of the MRwaypoints mod this plugin works with. See docs/COMPATIBILITY.md.
 * <p>
 * Two checks: the <b>protocol</b> (format of the messages between mod and plugin, built into both) must be in
 * {@link #MIN_PROTOCOL}..{@link #MAX_PROTOCOL}, and the mod must be at least the version the server admin set in
 * {@code mod.minimum-version} (optional, e.g. to force a bug fix).
 */
public final class Compatibility {

    /** Oldest protocol this plugin still understands. Raise it only when dropping support for old mods. */
    public static final int MIN_PROTOCOL = 1;
    /** Newest protocol this plugin speaks. Raise it together with the mod whenever a message changes. */
    public static final int MAX_PROTOCOL = 1;

    /** Result of the handshake; the code is sent to the mod (never reuse or reorder codes). */
    public enum Status {
        OK(0),
        /** Mod too old (protocol below {@link #MIN_PROTOCOL} or below mod.minimum-version) - update the mod. */
        MOD_TOO_OLD(1),
        /** Mod newer than this plugin (protocol above {@link #MAX_PROTOCOL}) - the server has to update the plugin. */
        MOD_TOO_NEW(2);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }
    }

    private Compatibility() {
    }

    /**
     * @param protocol       protocol the mod speaks
     * @param modVersion     version of the mod, e.g. "0.2.0+mc26.3"
     * @param minimumVersion mod.minimum-version from the config ("" = none)
     */
    public static Status check(int protocol, String modVersion, String minimumVersion) {
        if (protocol < MIN_PROTOCOL) {
            return Status.MOD_TOO_OLD;
        }
        if (protocol > MAX_PROTOCOL) {
            return Status.MOD_TOO_NEW;
        }
        if (minimumVersion != null && !minimumVersion.isBlank()
                && Versions.compare(modVersion, minimumVersion.trim()) < 0) {
            return Status.MOD_TOO_OLD;
        }
        return Status.OK;
    }
}
