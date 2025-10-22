import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

public class Extension implements BurpExtension {
    @Override
    public void initialize(MontoyaApi montoyaApi) {
        montoyaApi.extension().setName("SSE Viewer");
        
        // Register custom SSE tab in response viewer
        montoyaApi.userInterface().registerHttpResponseEditorProvider(
            new SSEResponseEditorProvider(montoyaApi)
        );
        
        montoyaApi.logging().logToOutput("SSE Viewer extension loaded successfully");
        montoyaApi.logging().logToOutput("SSE tab now available in all response viewers!");
    }
}