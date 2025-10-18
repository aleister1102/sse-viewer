import java.util.regex.Pattern;

public class FormatDetector {
    
    private static final Pattern JSON_PATTERN = Pattern.compile("^\\s*[\\{\\[].*[\\}\\]]\\s*$", Pattern.DOTALL);
    private static final Pattern XML_PATTERN = Pattern.compile("^\\s*<[?!]?[a-zA-Z].*>.*</.*>\\s*$", Pattern.DOTALL);
    private static final Pattern HTML_PATTERN = Pattern.compile("<(html|head|body|div|p|span|a|img|form|input|button|table|tr|td|ul|li|script|style)[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    public FormatType detectFormat(String data) {
        if (data == null || data.trim().isEmpty()) {
            return FormatType.PLAIN_TEXT;
        }
        
        String trimmed = data.trim();
        
        // Try to detect JSON
        if (isJSON(trimmed)) {
            return FormatType.JSON;
        }
        
        // Try to detect HTML
        if (isHTML(trimmed)) {
            return FormatType.HTML;
        }
        
        // Try to detect XML
        if (isXML(trimmed)) {
            return FormatType.XML;
        }
        
        return FormatType.PLAIN_TEXT;
    }
    
    private boolean isJSON(String data) {
        try {
            String trimmed = data.trim();
            // Check if it looks like JSON
            if ((trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
                // Try to validate it's valid JSON by parsing
                // Simple validation - check for balanced braces/brackets
                return isBalanced(trimmed);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isXML(String data) {
        try {
            String trimmed = data.trim();
            if (trimmed.startsWith("<") && trimmed.endsWith(">")) {
                // Check for XML declaration or root element
                if (trimmed.startsWith("<?xml") || 
                    (trimmed.matches("<[a-zA-Z][^>]*>.*</[a-zA-Z][^>]*>"))) {
                    return true;
                }
                // Also check for self-closing tags or CDATA
                if (trimmed.contains("CDATA") || trimmed.contains("/>")) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isHTML(String data) {
        try {
            String lowerData = data.toLowerCase();
            // Check for common HTML tags
            return HTML_PATTERN.matcher(data).find() || 
                   lowerData.contains("<!doctype html") ||
                   lowerData.contains("<html");
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isBalanced(String data) {
        int braceCount = 0;
        int bracketCount = 0;
        boolean inString = false;
        boolean escaped = false;
        
        for (char c : data.toCharArray()) {
            if (escaped) {
                escaped = false;
                continue;
            }
            
            if (c == '\\') {
                escaped = true;
                continue;
            }
            
            if (c == '"') {
                inString = !inString;
                continue;
            }
            
            if (!inString) {
                if (c == '{') braceCount++;
                else if (c == '}') braceCount--;
                else if (c == '[') bracketCount++;
                else if (c == ']') bracketCount--;
                
                if (braceCount < 0 || bracketCount < 0) {
                    return false;
                }
            }
        }
        
        return braceCount == 0 && bracketCount == 0 && !inString;
    }
}
