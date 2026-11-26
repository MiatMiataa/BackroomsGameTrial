package fnaf.entity;

public enum AnimatronicType {
    FREDDY("freddy", "FreddyJumpscareFrames"),
    BONNIE("bonnie", "BonnieJumpscareFrames"),
    CHICA("chica", "ChicaJumpscareFrames"),
    GOLDEN_FREDDY("golden_freddy", "Golden FreddyJumpscareFrames"),
    FOXY("foxy", "FoxyJumpscareFrames");

    private final String textureFolder;
    private final String jumpscareFolder;

    AnimatronicType(String textureFolder, String jumpscareFolder) {
        this.textureFolder = textureFolder;
        this.jumpscareFolder = jumpscareFolder;
    }

    /** The folder of the walking textures, inside src/textures/. */
    public String textureFolder() {
        return textureFolder;
    }

    /** The folder of frame files, which does not follow the texture folder names. */
    public String jumpscareFolder() {
        return jumpscareFolder;
    }
}
