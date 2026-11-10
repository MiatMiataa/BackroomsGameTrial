package GameTrial.entity;

public enum AnimatronicType {
    FREDDY("freddy"),
    BONNIE("bonnie"),
    CHICA("chica"),
    GOLDEN_FREDDY("golden_freddy"),
    FOXY("foxy");

    private final String folder;

    AnimatronicType(String folder) { 
        this.folder = folder;
    }
    
    public String folder() { 
        return folder;
    }
}