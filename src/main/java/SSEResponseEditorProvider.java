import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpResponseEditor;
import burp.api.montoya.ui.editor.extension.HttpResponseEditorProvider;

public class SSEResponseEditorProvider implements HttpResponseEditorProvider {
    
    private final MontoyaApi montoyaApi;

    public SSEResponseEditorProvider(MontoyaApi montoyaApi) {
        this.montoyaApi = montoyaApi;
    }

    @Override
    public ExtensionProvidedHttpResponseEditor provideHttpResponseEditor(EditorCreationContext creationContext) {
        return new SSEResponseEditor(montoyaApi);
    }
}

