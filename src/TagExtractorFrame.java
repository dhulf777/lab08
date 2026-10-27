import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class TagExtractorFrame extends JFrame {
    private JTextArea textArea;
    private JLabel fileLabel;
    private File selectedTextFile;
    private File selectedStopWordFile;
    private Map<String, Integer> tagMap;

    public TagExtractorFrame() {
        setTitle("Tag / Keyword Extractor");
        setSize(650, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tagMap = new TreeMap<>();

        JPanel topPanel = new JPanel(new BorderLayout());
        fileLabel = new JLabel(" Selected File: None", JLabel.LEFT);
        fileLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel.add(fileLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        JButton selectTextButton = new JButton("Select Text File");
        JButton selectStopWordButton = new JButton("Select Stop Words");
        JButton extractButton = new JButton("Extract Tags");
        JButton saveButton = new JButton("Save Output");

        buttonPanel.add(selectTextButton);
        buttonPanel.add(selectStopWordButton);
        buttonPanel.add(extractButton);
        buttonPanel.add(saveButton);

        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        selectTextButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedTextFile = chooser.getSelectedFile();
                fileLabel.setText(" Selected File: " + selectedTextFile.getName());
            }
        });

        selectStopWordButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedStopWordFile = chooser.getSelectedFile();
                JOptionPane.showMessageDialog(this, "Stop words file loaded: " + selectedStopWordFile.getName());
            }
        });

        extractButton.addActionListener(e -> {
            if (selectedTextFile == null) {
                JOptionPane.showMessageDialog(this, "Please select a text file first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (selectedStopWordFile == null) {
                JOptionPane.showMessageDialog(this, "Please select a stop words file first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            extractTags();
        });

        saveButton.addActionListener(e -> saveOutput());
    }

    private void extractTags() {
        tagMap.clear();
        textArea.setText("");

        try {
            Set<String> stopWords = new TreeSet<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedStopWordFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String word = line.trim().toLowerCase();
                    if (!word.isEmpty()) {
                        stopWords.add(word);
                    }
                }
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(selectedTextFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.split("\\s+");
                    for (String token : tokens) {
                        String cleaned = token.replaceAll("[^a-zA-Z]", "").toLowerCase();

                        if (!cleaned.isEmpty() && !stopWords.contains(cleaned)) {
                            tagMap.put(cleaned, tagMap.getOrDefault(cleaned, 0) + 1);
                        }
                    }
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-25s | %s\n", "Tag / Keyword", "Frequency"));
            sb.append("--------------------------------------------------\n");
            for (Map.Entry<String, Integer> entry : tagMap.entrySet()) {
                sb.append(String.format("%-25s | %d\n", entry.getKey(), entry.getValue()));
            }
            textArea.setText(sb.toString());
            JOptionPane.showMessageDialog(this, "Tag extraction completed successfully!");

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error processing files: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveOutput() {
        if (tagMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tags extracted to save.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File outFile = chooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(outFile))) {
                writer.println("Tag / Keyword Frequency Report");
                writer.println("Source File: " + (selectedTextFile != null ? selectedTextFile.getName() : "Unknown"));
                writer.println("----------------------------------------");
                for (Map.Entry<String, Integer> entry : tagMap.entrySet()) {
                    writer.println(entry.getKey() + ": " + entry.getValue());
                }
                JOptionPane.showMessageDialog(this, "Successfully saved output to " + outFile.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TagExtractorFrame().setVisible(true);
        });
    }
}