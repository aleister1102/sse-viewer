import java.util.ArrayList;
import java.util.List;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Marker;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.proxy.http.InterceptedResponse;

public class SSEHighlighter {
    private final MontoyaApi montoyaApi;
    private final SSEParser sseParser;
    private final FormatDetector formatDetector;

    public SSEHighlighter(MontoyaApi montoyaApi) {
        this.montoyaApi = montoyaApi;
        this.sseParser = new SSEParser();
        this.formatDetector = new FormatDetector();
    }

    public void highlightSSEResponseForRepeater(HttpResponse response) {
        if (response == null) {
            return;
        }

        String body = response.bodyToString();
        
        // Parse SSE events
        List<SSEEvent> events = sseParser.parseSSE(body);
        
        if (events.isEmpty()) {
            return;
        }

        // Create markers for highlighting
        List<Marker> markers = createMarkersForEvents(events, body);

        // Apply highlights to response
        if (!markers.isEmpty()) {
            HttpResponse highlightedResponse = response.withMarkers(markers);
            montoyaApi.logging().logToOutput("SSE Crayon: Found " + events.size() + " SSE events in repeater response with highlighting applied");
        }
    }

    public void highlightSSEResponse(InterceptedResponse interceptedResponse) {
        if (interceptedResponse == null) {
            return;
        }

        String body = interceptedResponse.bodyToString();
        
        // Parse SSE events
        List<SSEEvent> events = sseParser.parseSSE(body);
        
        if (events.isEmpty()) {
            return;
        }

        // Create markers for highlighting
        List<Marker> markers = createMarkersForEvents(events, body);

        // Apply highlights to response
        if (!markers.isEmpty()) {
            HttpResponse highlightedResponse = interceptedResponse.withMarkers(markers);
            montoyaApi.logging().logToOutput("SSE Crayon: Found " + events.size() + " SSE events in proxy response with highlighting applied");
        }
    }

    private List<Marker> createMarkersForEvents(List<SSEEvent> events, String body) {
        List<Marker> markers = new ArrayList<>();
        
        for (SSEEvent event : events) {
            if (event.data != null && !event.data.isEmpty()) {
                // Detect format of the data
                FormatType format = formatDetector.detectFormat(event.data);
                
                // Find the position of this data in the response body
                int startPos = findDataInBody(body, event.data);
                if (startPos >= 0) {
                    Marker marker = createMarker(startPos, event.data.length(), format);
                    if (marker != null) {
                        markers.add(marker);
                    }
                }
            }
        }
        
        return markers;
    }

    private int findDataInBody(String body, String data) {
        // Look for "data: " prefix in SSE format
        String searchPattern = "data: " + data;
        int pos = body.indexOf(searchPattern);
        if (pos >= 0) {
            return pos + 6; // Skip "data: " prefix
        }
        
        // Fallback: just find the data string
        return body.indexOf(data);
    }

    private Marker createMarker(int start, int length, FormatType format) {
        try {
            switch (format) {
                case JSON:
                    return Marker.marker(start, start + length);
                case XML:
                    return Marker.marker(start, start + length);
                case HTML:
                    return Marker.marker(start, start + length);
                case PLAIN_TEXT:
                default:
                    return Marker.marker(start, start + length);
            }
        } catch (Exception e) {
            montoyaApi.logging().logToError("SSE Crayon: Error creating marker - " + e.getMessage());
            return null;
        }
    }
}
