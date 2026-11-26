package GameTrial.entity;

public enum AnimatronicType {
    FREDDY("freddy", "FreddyJumpscareFrames"),
    BONNIE("bonnie", "BonnieJumpscareFrames"),
    CHICA("chica", "ChicaJumpscareFrames"),
    GOLDEN_FREDDY("golden_freddy", "Golden FreddyJumpscareFrames"),
    FOXY("foxy", "FoxyJumpscareFrames");

    private final String folder;
    private final String jumpscareFolder;

    AnimatronicType(String folder, String jumpscareFolder) {
        this.folder = folder;
        this.jumpscareFolder = jumpscareFolder;
    }

    public String folder() {
        return folder;
    }

    /** The folder of frame files, which does not follow the texture folder names. */
    public String jumpscareFolder() {
        return jumpscareFolder;
    }
}
