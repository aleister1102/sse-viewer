import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

public class Extension implements BurpExtension {
    @Override
    public void initialize(MontoyaApi montoyaApi) {
        montoyaApi.extension().setName("SSE Crayon");
        
        // Register the proxy response handler for highlighting SSE in proxy
        montoyaApi.proxy().registerResponseHandler(new SSEProxyResponseHandler(montoyaApi));
        
        // Register HTTP handler for repeater support
        montoyaApi.http().registerHttpHandler(new SSEResponseProcessor(montoyaApi));
        
        montoyaApi.logging().logToOutput("SSE Crayon extension loaded successfully");
    }
}