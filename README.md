# SSE Crayon - Server-Side Events Highlighter

A powerful Burp Suite extension that automatically detects and highlights Server-Side Events (SSE) in proxy and repeater responses, with intelligent format detection for JSON, XML, HTML, and plain text data.

## Features

✨ **Automatic Detection**: Detects SSE responses by Content-Type header and SSE format patterns
🎨 **Smart Highlighting**: Applies visual markers to SSE event data in Burp Suite's UI
📋 **Format Detection**: Automatically identifies data format:
  - **JSON**: Detects balanced brackets and validates JSON structure
  - **XML**: Recognizes XML declarations and tags
  - **HTML**: Identifies HTML tags and structures
  - **Plain Text**: Fallback for unrecognized formats

🔍 **Works Everywhere**: Highlights SSE in:
  - Proxy responses (when intercepting traffic)
  - Repeater responses (manual request testing)

⚡ **Zero Configuration**: Works out of the box with no setup required

## Installation

### Prerequisites
- Burp Suite (Community or Professional)
- Java 21+

### Build from Source

1. Clone this repository
2. Build the extension:
   ```bash
   ./gradlew jar
   ```
3. The JAR will be created at: `build/libs/sse-crayon.jar`

### Load into Burp Suite

1. Open Burp Suite
2. Go to **Extensions > Installed**
3. Click **Add**
4. Under **Extension details**, click **Select file**
5. Choose the JAR file: `build/libs/sse-crayon.jar`
6. Click **Next** and then **Close**

The extension is now loaded and will start highlighting SSE responses automatically!

## How It Works

### Server-Sent Events Format
SSE responses follow this format:
```
event: update
id: 1
data: {"status": "processing"}

event: complete
id: 2
data: <result>Success</result>

```

### Detection
The extension detects SSE responses by:
1. Checking for `Content-Type: text/event-stream` header
2. Looking for SSE field patterns: `data:`, `event:`, `id:`, `retry:`

### Highlighting
Once detected, the extension:
1. Parses individual SSE events
2. Detects the format of each event's data
3. Applies visual markers to highlight the data in Burp's UI

### Supported SSE Fields
- `event:` - Event name
- `data:` - Event data (supports multiple lines)
- `id:` - Event ID
- `retry:` - Reconnection time in milliseconds
- `:` - Comments (ignored)

## Project Structure

```
src/main/java/
├── Extension.java                    # Main entry point, registers handlers
├── SSEProxyResponseHandler.java      # Handles proxy responses
├── SSEResponseProcessor.java         # Handles repeater/general HTTP responses
├── SSEHighlighter.java               # Core highlighting logic
├── SSEParser.java                    # Parses SSE format
├── FormatDetector.java               # Detects data format (JSON/XML/HTML)
├── FormatType.java                   # Format type enum
└── SSEEvent.java                     # SSE event data model
```

## Architecture

### SSE Detection Flow
```
HTTP Response Received
    ↓
Check if SSE (Content-Type or body pattern)
    ↓
Parse SSE events
    ↓
Detect format of each event's data
    ↓
Create visual markers
    ↓
Display highlighted response in Burp UI
```

### Key Components

**Extension.java**
- Initializes the extension
- Registers proxy and HTTP handlers

**SSEProxyResponseHandler.java**
- Implements `ProxyResponseHandler` interface
- Intercepts responses in Burp's proxy
- Triggers highlighting for detected SSE responses

**SSEResponseProcessor.java**
- Implements `HttpHandler` interface
- Handles responses from Repeater and other tools
- Applies same SSE detection and highlighting

**SSEParser.java**
- Parses raw SSE format from response body
- Extracts event, data, id, and retry fields
- Handles multiline data fields correctly

**FormatDetector.java**
- Analyzes event data to detect format
- Validates JSON by checking brackets and structure
- Recognizes XML by tags and declarations
- Identifies HTML by common tag patterns

**SSEHighlighter.java**
- Orchestrates the highlighting process
- Finds data positions in response body
- Creates markers for Burp's UI

## Build Requirements

- **Java**: 21+
- **Gradle**: 8.10+
- **Dependencies**: Montoya API 2025.8 (compile-only)

## Debugging

To see extension output:
1. In Burp Suite, go to **Extensions > Installed**
2. Select SSE Crayon
3. Check the **Output** and **Errors** tabs for log messages

The extension logs:
- When SSE events are detected and highlighted
- Number of events found in each response
- Any errors during marker creation

## Example Usage

### Testing with Curl
```bash
curl -N https://example.com/stream \
  -H "Accept: text/event-stream"
```

Then in Burp Proxy, you'll see the SSE response highlighted automatically.

### In Repeater
1. Send an SSE request to Repeater
2. Send the request
3. The response will be automatically highlighted with markers

## Performance

The extension is designed for minimal performance impact:
- Only processes responses with SSE patterns
- Efficient string searching for event markers
- Lightweight marker creation
- No background threads or polling

## Troubleshooting

### Extension doesn't load
- Check that Java 21+ is installed
- Verify the JAR file exists at `build/libs/`
- Check **Extensions > Errors** tab for detailed error messages

### Highlighting not appearing
- Ensure the response has `Content-Type: text/event-stream` header or SSE-format body
- Check the **Output** tab for SSE Crayon log messages
- Verify the response contains `data:` fields

### Build fails with timeout
See the [Gradle timeout fix](#gradle-timeout-fix) section

## Gradle Timeout Fix

If you experience gradle timeout issues:

1. **Increase network timeout** (already configured in `gradle.properties`):
   ```properties
   org.gradle.internal.http.socketTimeout=120000
   org.gradle.internal.http.connectionTimeout=120000
   ```

2. **Use offline mode** (if dependencies are cached):
   ```bash
   ./gradlew jar --offline
   ```

3. **Clear gradle cache**:
   ```bash
   rm -rf ~/.gradle/wrapper/dists
   ```

## License

This project is provided as-is for educational and security testing purposes.

## Support

For issues, questions, or feature requests, refer to the Burp Suite extension documentation:
- [Creating Burp Extensions](https://portswigger.net/burp/documentation/desktop/extend-burp/extensions/creating)
- [Montoya API Documentation](https://portswigger.github.io/burp-extensions-montoya-api/javadoc/burp/api/montoya/MontoyaApi.html)
- [Example Extensions](https://github.com/PortSwigger/burp-extensions-montoya-api-examples)