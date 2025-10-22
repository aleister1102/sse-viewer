import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpResponseEditor;

public class SSEResponseEditor implements ExtensionProvidedHttpResponseEditor {
    
    private final MontoyaApi montoyaApi;
    private final SSEParser parser;
    private final FormatDetector formatDetector;
    private final JPanel mainPanel;
    private final JTable eventsTable;
    private final JTextPane detailPane;
    private final SSETableModel tableModel;
    private HttpRequestResponse currentRequestResponse;
    private List<SSEEvent> currentEvents;

    public SSEResponseEditor(MontoyaApi montoyaApi) {
        this.montoyaApi = montoyaApi;
        this.parser = new SSEParser();
        this.formatDetector = new FormatDetector();
        
        // Get Burp's font
        Font burpFont = montoyaApi.userInterface().currentEditorFont();
        
        // Create main panel with split pane
        mainPanel = new JPanel(new BorderLayout());
        
        // Create table model and table
        tableModel = new SSETableModel();
        eventsTable = new JTable(tableModel);
        eventsTable.setFont(burpFont);
        eventsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventsTable.setRowHeight(30);
        eventsTable.setBackground(new Color(40, 40, 40));
        eventsTable.setForeground(new Color(200, 200, 200));
        eventsTable.setGridColor(new Color(60, 60, 60));
        eventsTable.getTableHeader().setBackground(new Color(50, 50, 50));
        eventsTable.getTableHeader().setForeground(new Color(220, 220, 220));
        eventsTable.getTableHeader().setFont(burpFont.deriveFont(Font.BOLD));
        
        // Set column widths
        eventsTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // Event #
        eventsTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Type
        eventsTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // ID
        eventsTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Format
        eventsTable.getColumnModel().getColumn(4).setPreferredWidth(300); // Preview
        eventsTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Copy button
        
        // Custom renderer for all text columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        centerRenderer.setBackground(new Color(40, 40, 40));
        centerRenderer.setForeground(new Color(200, 200, 200));
        
        eventsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        eventsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        eventsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        eventsTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        
        // Button renderer and editor for copy column
        eventsTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        eventsTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor());
        
        JScrollPane tableScroll = new JScrollPane(eventsTable);
        
        // Create detail pane
        detailPane = new JTextPane();
        detailPane.setEditable(false);
        detailPane.setFont(burpFont);
        detailPane.setBackground(new Color(30, 30, 30));
        detailPane.setForeground(new Color(200, 200, 200));
        detailPane.setCaretColor(new Color(200, 200, 200));
        
        JScrollPane detailScroll = new JScrollPane(detailPane);
        
        // Add selection listener to show details
        eventsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = eventsTable.getSelectedRow();
                if (row >= 0 && currentEvents != null && row < currentEvents.size()) {
                    showEventDetail(currentEvents.get(row));
                }
            }
        });
        
        // Create split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, detailScroll);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.4);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
    }

    @Override
    public HttpResponse getResponse() {
        return currentRequestResponse != null ? currentRequestResponse.response() : null;
    }

    @Override
    public void setRequestResponse(HttpRequestResponse requestResponse) {
        this.currentRequestResponse = requestResponse;
        
        if (requestResponse != null && requestResponse.response() != null) {
            HttpResponse response = requestResponse.response();
            String body = response.bodyToString();
            
            // Parse SSE events
            currentEvents = parser.parseSSE(body);
            
            if (currentEvents.isEmpty()) {
                tableModel.setEvents(null);
                detailPane.setText("No SSE events detected in this response.\n\n" +
                    "This tab shows Server-Sent Events (SSE) parsed from responses.\n" +
                    "SSE format typically has:\n" +
                    "  event: event_name\n" +
                    "  data: event_data\n" +
                    "  id: event_id\n\n" +
                    "Or contains data: fields in the response body.");
            } else {
                tableModel.setEvents(currentEvents);
                // Show first event by default
                if (eventsTable.getRowCount() > 0) {
                    eventsTable.setRowSelectionInterval(0, 0);
                    showEventDetail(currentEvents.get(0));
                }
            }
        }
    }

    @Override
    public boolean isEnabledFor(HttpRequestResponse requestResponse) {
        return requestResponse != null && requestResponse.response() != null;
    }

    @Override
    public String caption() {
        return "SSE";
    }

    @Override
    public Component uiComponent() {
        return mainPanel;
    }

    @Override
    public Selection selectedData() {
        String selected = detailPane.getSelectedText();
        if (selected != null && !selected.isEmpty()) {
            return Selection.selection(burp.api.montoya.core.ByteArray.byteArray(selected));
        }
        return null;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    private void showEventDetail(SSEEvent event) {
        StyledDocument doc = detailPane.getStyledDocument();
        
        try {
            doc.remove(0, doc.getLength());
            
            // Style definitions
            SimpleAttributeSet headerStyle = new SimpleAttributeSet();
            StyleConstants.setForeground(headerStyle, new Color(100, 200, 255));
            StyleConstants.setBold(headerStyle, true);
            
            SimpleAttributeSet labelStyle = new SimpleAttributeSet();
            StyleConstants.setForeground(labelStyle, new Color(150, 150, 150));
            
            SimpleAttributeSet valueStyle = new SimpleAttributeSet();
            StyleConstants.setForeground(valueStyle, new Color(200, 200, 200));
            
            // Header
            doc.insertString(doc.getLength(), "Event Details\n", headerStyle);
            doc.insertString(doc.getLength(), "═══════════════════════════════════════════════\n\n", headerStyle);
            
            // Event info
            if (event.event != null) {
                doc.insertString(doc.getLength(), "Type: ", labelStyle);
                doc.insertString(doc.getLength(), event.event + "\n", valueStyle);
            }
            if (event.id != null) {
                doc.insertString(doc.getLength(), "ID: ", labelStyle);
                doc.insertString(doc.getLength(), event.id + "\n", valueStyle);
            }
            if (event.retry != null) {
                doc.insertString(doc.getLength(), "Retry: ", labelStyle);
                doc.insertString(doc.getLength(), event.retry + " ms\n", valueStyle);
            }
            
            FormatType format = formatDetector.detectFormat(event.data != null ? event.data : "");
            doc.insertString(doc.getLength(), "Format: ", labelStyle);
            doc.insertString(doc.getLength(), format.getDisplayName() + "\n\n", valueStyle);
            
            doc.insertString(doc.getLength(), "Data:\n", headerStyle);
            doc.insertString(doc.getLength(), "───────────────────────────────────────────────\n", headerStyle);
            
            // Data with syntax highlighting
            if (event.data != null) {
                if (format == FormatType.JSON) {
                    highlightJSON(doc, event.data);
                } else {
                    doc.insertString(doc.getLength(), event.data, valueStyle);
                }
            } else {
                doc.insertString(doc.getLength(), "(no data)", labelStyle);
            }
            
            detailPane.setCaretPosition(0);
            
        } catch (Exception e) {
            montoyaApi.logging().logToError("Error showing event detail: " + e.getMessage());
        }
    }

    private void highlightJSON(StyledDocument doc, String json) throws Exception {
        // Color definitions
        SimpleAttributeSet keyStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(keyStyle, new Color(150, 200, 255));
        
        SimpleAttributeSet stringStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(stringStyle, new Color(144, 238, 144));
        
        SimpleAttributeSet numberStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(numberStyle, new Color(255, 200, 100));
        
        SimpleAttributeSet booleanStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(booleanStyle, new Color(255, 150, 150));
        
        SimpleAttributeSet punctStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(punctStyle, new Color(180, 180, 180));
        
        String indented = prettifyJSON(json);
        String[] lines = indented.split("\n");
        
        for (String line : lines) {
            int i = 0;
            while (i < line.length()) {
                char c = line.charAt(i);
                
                if (c == '"') {
                    int end = findStringEnd(line, i + 1);
                    String str = line.substring(i, end + 1);
                    
                    boolean isKey = false;
                    for (int j = end + 1; j < line.length(); j++) {
                        if (line.charAt(j) == ':') {
                            isKey = true;
                            break;
                        } else if (!Character.isWhitespace(line.charAt(j))) {
                            break;
                        }
                    }
                    
                    doc.insertString(doc.getLength(), str, isKey ? keyStyle : stringStyle);
                    i = end + 1;
                } else if (Character.isDigit(c) || (c == '-' && i + 1 < line.length() && Character.isDigit(line.charAt(i + 1)))) {
                    int end = i;
                    if (c == '-') end++;
                    while (end < line.length() && (Character.isDigit(line.charAt(end)) || 
                           line.charAt(end) == '.' || line.charAt(end) == 'e' || line.charAt(end) == 'E' ||
                           line.charAt(end) == '+' || line.charAt(end) == '-')) {
                        end++;
                    }
                    doc.insertString(doc.getLength(), line.substring(i, end), numberStyle);
                    i = end;
                } else if (line.substring(i).startsWith("true") || line.substring(i).startsWith("false") || 
                           line.substring(i).startsWith("null")) {
                    String word = line.substring(i).startsWith("true") ? "true" : 
                                 line.substring(i).startsWith("false") ? "false" : "null";
                    doc.insertString(doc.getLength(), word, booleanStyle);
                    i += word.length();
                } else {
                    doc.insertString(doc.getLength(), String.valueOf(c), punctStyle);
                    i++;
                }
            }
            doc.insertString(doc.getLength(), "\n", punctStyle);
        }
    }

    private int findStringEnd(String str, int start) {
        boolean escaped = false;
        for (int i = start; i < str.length(); i++) {
            char c = str.charAt(i);
            if (escaped) {
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                return i;
            }
        }
        return str.length() - 1;
    }

    private String prettifyJSON(String json) {
        StringBuilder result = new StringBuilder();
        int indent = 0;
        boolean inString = false;
        boolean escaped = false;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (escaped) {
                result.append(c);
                escaped = false;
                continue;
            }
            
            if (c == '\\' && inString) {
                result.append(c);
                escaped = true;
                continue;
            }
            
            if (c == '"') {
                inString = !inString;
                result.append(c);
                continue;
            }
            
            if (inString) {
                result.append(c);
                continue;
            }
            
            switch (c) {
                case '{':
                case '[':
                    result.append(c);
                    result.append('\n');
                    indent++;
                    result.append("  ".repeat(indent));
                    break;
                case '}':
                case ']':
                    result.append('\n');
                    indent--;
                    result.append("  ".repeat(indent));
                    result.append(c);
                    break;
                case ',':
                    result.append(c);
                    result.append('\n');
                    result.append("  ".repeat(indent));
                    break;
                case ':':
                    result.append(c);
                    result.append(' ');
                    break;
                default:
                    if (!Character.isWhitespace(c)) {
                        result.append(c);
                    }
            }
        }
        
        return result.toString();
    }

    // Table Model
    private class SSETableModel extends AbstractTableModel {
        private List<SSEEvent> events;
        private final String[] columnNames = {"Event #", "Type", "ID", "Format", "Data Preview", "Copy"};

        public void setEvents(List<SSEEvent> events) {
            this.events = events;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return events == null ? 0 : events.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (events == null || rowIndex >= events.size()) return "";
            
            SSEEvent event = events.get(rowIndex);
            switch (columnIndex) {
                case 0: return String.valueOf(rowIndex + 1);
                case 1: return event.event != null ? event.event : "N/A";
                case 2: return event.id != null ? event.id : "N/A";
                case 3: 
                    FormatType format = formatDetector.detectFormat(event.data != null ? event.data : "");
                    return format.getDisplayName();
                case 4:
                    String preview = event.data != null ? event.data : "";
                    if (preview.length() > 80) {
                        preview = preview.substring(0, 80) + "...";
                    }
                    return preview;
                case 5: return "Copy";
                default: return "";
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 5; // Only copy button is editable
        }
    }

    // Button Renderer
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText("📋 Copy");
            setBackground(new Color(70, 120, 180));
            setForeground(Color.WHITE);
            return this;
        }
    }

    // Button Editor
    private class ButtonEditor extends javax.swing.DefaultCellEditor {
        private JButton button;
        private String data;
        private int currentRow;

        public ButtonEditor() {
            super(new javax.swing.JCheckBox());
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                if (currentEvents != null && currentRow < currentEvents.size()) {
                    SSEEvent event = currentEvents.get(currentRow);
                    if (event.data != null) {
                        StringSelection selection = new StringSelection(event.data);
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                        montoyaApi.logging().logToOutput("SSE Viewer: Copied event #" + (currentRow + 1) + " data to clipboard");
                        button.setText("✓ Copied!");
                        
                        // Reset button text after 1 second
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                                button.setText("📋 Copy");
                            } catch (InterruptedException ex) {
                                // Ignore
                            }
                        }).start();
                    }
                }
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            button.setText("📋 Copy");
            button.setBackground(new Color(70, 120, 180));
            button.setForeground(Color.WHITE);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return data;
        }
    }
}
