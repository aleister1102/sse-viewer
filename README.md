# SSE Editor

A Burp Suite extension that adds an "SSE" tab to response viewers for parsing and highlighting Server-Side Events.

## Features

- 📊 **Table View** - All events displayed in a sortable table
- 📋 **One-Click Copy** - Copy button for each event's data
- 🎨 **JSON Syntax Highlighting** - Color-coded JSON (keys, strings, numbers, booleans)
- 🔍 **Format Detection** - Auto-detects JSON, XML, HTML, or plain text
- 📝 **Detail View** - Click any row to see formatted event data
- 🎯 **Split Pane UI** - Resizable table and detail panels
- 🖥️ **Burp Theme** - Inherits Burp's fonts and dark theme

## Installation

### Quick Start

```bash
./gradlew jar
```

Load in Burp: **Extensions > Installed > Add** → `build/libs/sse-viewer.jar`

### Requirements

- Burp Suite (Community or Professional)
- Java 21+

## Usage

1. Open any response in Proxy, Repeater, or other tools
2. Click the **"SSE"** tab (appears alongside Pretty, Raw, Hex, etc.)
3. View parsed events in the table
4. Click a row to see full details below
5. Click **📋 Copy** to copy event data to clipboard

## SSE Format

```
event: message
id: 1
data: {"status": "ok"}

event: update
data: Hello World
```

Supports: `event:`, `data:`, `id:`, `retry:`, comments (`:`)

## Project Structure

```
src/main/java/
├── Extension.java                 # Entry point
├── SSEResponseEditor.java         # Table UI & highlighting
├── SSEResponseEditorProvider.java # Editor provider
├── SSEParser.java                 # SSE parser
├── SSEEvent.java                  # Event model
├── FormatDetector.java            # Format detection
└── FormatType.java                # Format enum
```

## Build

```bash
# Build JAR
./gradlew jar

# Clean build
./gradlew clean jar

# Offline mode
./gradlew jar --offline
```

## Troubleshooting

**Extension doesn't load**
- Verify Java 21+ installed
- Check Extensions > Errors tab

**No events showing**
- Response must contain `data:` fields or `Content-Type: text/event-stream`

**Build timeout**
```bash
rm -rf ~/.gradle/wrapper/dists
./gradlew jar
```

## Credits

Built with assistance from **Claude** (Anthropic) - October 2025

## License

Provided as-is for educational and security testing purposes.

## Links

- [Montoya API Docs](https://portswigger.github.io/burp-extensions-montoya-api/javadoc/burp/api/montoya/MontoyaApi.html)
- [Burp Extension Guide](https://portswigger.net/burp/documentation/desktop/extend-burp/extensions/creating)
