import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.proxy.http.InterceptedResponse;
import burp.api.montoya.proxy.http.ProxyResponseHandler;
import burp.api.montoya.proxy.http.ProxyResponseReceivedAction;
import burp.api.montoya.proxy.http.ProxyResponseToBeSentAction;

public class SSEProxyResponseHandler implements ProxyResponseHandler {
    private final MontoyaApi montoyaApi;
    private final SSEHighlighter highlighter;

    public SSEProxyResponseHandler(MontoyaApi montoyaApi) {
        this.montoyaApi = montoyaApi;
        this.highlighter = new SSEHighlighter(montoyaApi);
    }

    @Override
    public ProxyResponseReceivedAction handleResponseReceived(InterceptedResponse interceptedResponse) {
        HttpResponse response = interceptedResponse;

        if (response != null) {
            // Check if this is an SSE response
            if (isSSEResponse(response)) {
                // Process the response for highlighting
                highlighter.highlightSSEResponse(interceptedResponse);
            }
        }

        return ProxyResponseReceivedAction.continueWith(interceptedResponse);
    }

    @Override
    public ProxyResponseToBeSentAction handleResponseToBeSent(InterceptedResponse interceptedResponse) {
        // We don't need to process responses before sending
        return ProxyResponseToBeSentAction.continueWith(interceptedResponse);
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
