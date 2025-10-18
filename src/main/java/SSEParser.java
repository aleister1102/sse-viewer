import java.util.ArrayList;
import java.util.List;

public class SSEParser {
    
    public List<SSEEvent> parseSSE(String body) {
        List<SSEEvent> events = new ArrayList<>();
        
        if (body == null || body.isEmpty()) {
            return events;
        }
        
        // Split by double newline (event separator in SSE format)
        String[] eventBlocks = body.split("\n\n");
        
        for (String block : eventBlocks) {
            if (block.trim().isEmpty()) {
                continue;
            }
            
            SSEEvent event = parseEventBlock(block);
            if (event != null) {
                events.add(event);
            }
        }
        
        return events;
    }
    
    private SSEEvent parseEventBlock(String block) {
        SSEEvent event = new SSEEvent();
        String[] lines = block.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.isEmpty()) {
                continue;
            }
            
            if (line.startsWith(":")) {
                // Comment line - ignore
                continue;
            } else if (line.startsWith("event:")) {
                event.event = line.substring(6).trim();
            } else if (line.startsWith("data:")) {
                String dataValue = line.substring(5).trim();
                // Handle multiline data (data: on multiple lines gets concatenated)
                if (event.data == null) {
                    event.data = dataValue;
                } else {
                    event.data += "\n" + dataValue;
                }
            } else if (line.startsWith("id:")) {
                event.id = line.substring(3).trim();
            } else if (line.startsWith("retry:")) {
                try {
                    event.retry = Long.parseLong(line.substring(6).trim());
                } catch (NumberFormatException e) {
                    // Ignore invalid retry values
                }
            }
        }
        
        // Only consider it a valid event if it has data
        if (event.data != null && !event.data.isEmpty()) {
            return event;
        }
        
        return null;
    }
}
