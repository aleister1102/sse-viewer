public enum FormatType {
    JSON("JSON"),
    XML("XML"),
    HTML("HTML"),
    PLAIN_TEXT("Plain Text");
    
    private final String displayName;
    
    FormatType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
