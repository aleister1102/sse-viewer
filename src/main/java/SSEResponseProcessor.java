import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.handler.ResponseReceivedAction;
import burp.api.montoya.http.message.responses.HttpResponse;

public class SSEResponseProcessor implements HttpHandler {
    private final MontoyaApi montoyaApi;
    private final SSEHighlighter highlighter;

    public SSEResponseProcessor(MontoyaApi montoyaApi) {
        this.montoyaApi = montoyaApi;
        this.highlighter = new SSEHighlighter(montoyaApi);
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent httpRequestToBeSent) {
        return RequestToBeSentAction.continueWith(httpRequestToBeSent);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived httpResponseReceived) {
        HttpResponse response = httpResponseReceived;

        if (response != null) {
            // Check if this is an SSE response
            if (isSSEResponse(response)) {
                // Process the response for highlighting
                highlighter.highlightSSEResponseForRepeater(response);
            }
        }

        return ResponseReceivedAction.continueWith(response);
    }

    private boolean isSSEResponse(HttpResponse response) {
        // Check if Content-Type is text/event-stream
        String contentType = response.headerValue("Content-Type");
        if (contentType != null && contentType.contains("text/event-stream")) {
            return true;
        }
        
        // Also check if the response body looks like SSE format
        if (response.body().length() > 0) {
            String body = response.bodyToString();
            return body.contains("data:") || body.contains("event:") || body.contains("id:");
        }
        
        return false;
    }
}
