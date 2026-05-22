import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.JSpinner;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class App extends JFrame {
    private static final String CSV_FILE_NAME = "chef_koltsegek_2025.csv";
    private static final DateTimeFormatter CSV_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] CATEGORIES = {
            "Travel", "Ingredients", "Accommodation", "Equipment", "Other"
    };

    private final List<ChefExpense> expenses = new ArrayList<>();
    private final DefaultTableModel tableModel;
    private final JTable expenseTable;

    private final JTextField chefNameField;
    private final JSpinner dateSpinner;
    private final JComboBox<String> categoryCombo;
    private final JTextField amountField;
    private final JTextArea noteArea;
    private final JLabel recordCountValueLabel;
    private final JLabel totalAmountValueLabel;

    private final Path csvPath;

    public App() {
        super("ChefExpenses");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 700);
        setMinimumSize(new Dimension(960, 680));
        setLocationRelativeTo(null);

        csvPath = resolveCsvPath();

        tableModel = new DefaultTableModel(new Object[]{
                "Id", "ChefName", "Datum", "Kategoria", "Osszeg", "Megjegyzes"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        expenseTable = new JTable(tableModel);
        expenseTable.setFillsViewportHeight(true);
        expenseTable.setRowSelectionAllowed(true);
        expenseTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        JScrollPane tableScroll = new JScrollPane(expenseTable);
        tableScroll.setPreferredSize(new Dimension(940, 350));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(tableScroll, BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        chefNameField = new JTextField(22);
        dateSpinner = createDateSpinner();
        categoryCombo = new JComboBox<>(CATEGORIES);
        categoryCombo.setSelectedIndex(-1);
        amountField = new JTextField(22);
        noteArea = new JTextArea(5, 45);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);

        recordCountValueLabel = new JLabel("0");
        totalAmountValueLabel = new JLabel("0.00 EUR");

        int row = 0;
        addLabelAndControl(formPanel, gbc, row++, "Sef neve:", chefNameField);
        addLabelAndControl(formPanel, gbc, row++, "Datum:", dateSpinner);
        addLabelAndControl(formPanel, gbc, row++, "Kategoria:", categoryCombo);
        addLabelAndControl(formPanel, gbc, row++, "Osszeg (EUR):", amountField);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Megjegyzes:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(new JScrollPane(noteArea), gbc);

        JButton addButton = new JButton("Hozzaadas");
        addButton.addActionListener(e -> addExpense());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionPanel.add(new JLabel("Rekordszam:"));
        actionPanel.add(recordCountValueLabel);
        actionPanel.add(new JLabel("   Osszes koltseg:"));
        actionPanel.add(totalAmountValueLabel);
        actionPanel.add(addButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.CENTER);

        loadExpenses();
        refreshTable();
        updateSummary();
    }

    private static JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy. MMMM dd., EEEE");
        spinner.setEditor(editor);
        return spinner;
    }

    private static void addLabelAndControl(JPanel panel, GridBagConstraints gbc, int row, String labelText, java.awt.Component control) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panel.add(control, gbc);
    }

    private Path resolveCsvPath() {
        Path current = Paths.get(CSV_FILE_NAME).toAbsolutePath();
        if (Files.exists(current)) {
            return current;
        }

        Path workDir = Paths.get(System.getProperty("user.dir"));
        Path candidate = workDir.resolve(CSV_FILE_NAME);
        if (Files.exists(candidate)) {
            return candidate;
        }

        return candidate;
    }

    private void loadExpenses() {
        expenses.clear();

        if (!Files.exists(csvPath)) {
            try {
                saveExpenses();
            } catch (IOException ex) {
                showError("Nem sikerult letrehozni a CSV fajlt: " + ex.getMessage());
            }
            return;
        }

        Set<Integer> usedIds = new HashSet<>();
        List<String> warnings = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(";", 6);
                if (parts.length < 6) {
                    warnings.add("Hibas sor kihagyva: " + line);
                    continue;
                }

                int id;
                try {
                    id = Integer.parseInt(parts[0].trim());
                } catch (NumberFormatException ex) {
                    warnings.add("Hibas azonosito kihagyva: " + line);
                    continue;
                }

                if (!usedIds.add(id)) {
                    warnings.add("Duplikalt azonosito kihagyva: " + line);
                    continue;
                }

                LocalDate date;
                try {
                    date = LocalDate.parse(parts[2].trim(), CSV_DATE_FORMAT);
                } catch (DateTimeParseException ex) {
                    warnings.add("Hibas datum kihagyva: " + line);
                    continue;
                }

                String category = parts[3].trim();
                if (!Arrays.asList(CATEGORIES).contains(category)) {
                    warnings.add("Ervenytelen kategoria kihagyva: " + line);
                    continue;
                }

                double amount;
                try {
                    amount = Double.parseDouble(parts[4].trim());
                } catch (NumberFormatException ex) {
                    warnings.add("Hibas osszeg kihagyva: " + line);
                    continue;
                }

                ChefExpense expense = new ChefExpense(id, parts[1].trim(), date, category, amount, parts[5].trim());
                expenses.add(expense);
            }
        } catch (IOException ex) {
            showError("Nem sikerult betolteni a CSV fajlt: " + ex.getMessage());
        }

        if (!warnings.isEmpty()) {
            StringBuilder message = new StringBuilder();
            int max = Math.min(5, warnings.size());
            for (int i = 0; i < max; i++) {
                message.append(warnings.get(i)).append("\n");
            }
            if (warnings.size() > 5) {
                message.append("... es tovabbi ").append(warnings.size() - 5).append(" figyelmeztetes.");
            }
            JOptionPane.showMessageDialog(this, message.toString(), "Figyelmeztetes", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        expenses.sort((a, b) -> Integer.compare(a.id, b.id));
        for (ChefExpense exp : expenses) {
            tableModel.addRow(new Object[]{
                    exp.id,
                    exp.chefName,
                    exp.date.format(DateTimeFormatter.ofPattern("yyyy. MM. dd.")),
                    exp.category,
                    formatAmount(exp.amount),
                    exp.note
            });
        }
    }

    private void addExpense() {
        String chefName = chefNameField.getText().trim();
        Object selectedCategory = categoryCombo.getSelectedItem();

        if (chefName.isEmpty()) {
            showWarning("A sef neve kotelezo.");
            chefNameField.requestFocus();
            return;
        }

        if (selectedCategory == null) {
            showWarning("Valasszon kategoriat.");
            categoryCombo.requestFocus();
            return;
        }

        Double amount = parseAmount(amountField.getText().trim());
        if (amount == null || amount <= 0) {
            showWarning("Az osszeg ervenyes, pozitiv szam legyen.");
            amountField.requestFocus();
            return;
        }

        Date selectedDate = (Date) dateSpinner.getValue();
        LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        int nextId = expenses.stream().mapToInt(e -> e.id).max().orElse(0) + 1;
        ChefExpense newExpense = new ChefExpense(
                nextId,
                sanitizeField(chefName),
                localDate,
                selectedCategory.toString(),
                amount,
                sanitizeField(noteArea.getText().trim())
        );

        expenses.add(newExpense);

        try {
            saveExpenses();
        } catch (IOException ex) {
            expenses.remove(newExpense);
            showError("A mentes nem sikerult, a rekord nem kerult rogzitesre.\n" + ex.getMessage());
            return;
        }

        refreshTable();
        updateSummary();

        int lastRow = tableModel.getRowCount() - 1;
        if (lastRow >= 0) {
            expenseTable.setRowSelectionInterval(lastRow, lastRow);
            expenseTable.scrollRectToVisible(expenseTable.getCellRect(lastRow, 0, true));
        }

        clearForm();
    }

    private void clearForm() {
        chefNameField.setText("");
        amountField.setText("");
        noteArea.setText("");
        categoryCombo.setSelectedIndex(-1);
        dateSpinner.setValue(new Date());
        chefNameField.requestFocus();
    }

    private void saveExpenses() throws IOException {
        if (csvPath.getParent() != null) {
            Files.createDirectories(csvPath.getParent());
        }

        Path tempFile = csvPath.resolveSibling("." + CSV_FILE_NAME + ".tmp");

        try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
            writer.write("id;chefname;datum;kategoria;osszeg;megjegyzes");
            writer.newLine();

            expenses.sort((a, b) -> Integer.compare(a.id, b.id));
            for (ChefExpense exp : expenses) {
                writer.write(exp.id + ";"
                        + sanitizeField(exp.chefName) + ";"
                        + exp.date.format(CSV_DATE_FORMAT) + ";"
                        + sanitizeField(exp.category) + ";"
                        + exp.amount + ";"
                        + sanitizeField(exp.note));
                writer.newLine();
            }
        }

        Files.move(tempFile, csvPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private void updateSummary() {
        recordCountValueLabel.setText(Integer.toString(expenses.size()));
        double total = expenses.stream().mapToDouble(e -> e.amount).sum();
        totalAmountValueLabel.setText(formatAmount(total) + " EUR");
    }

    private static String formatAmount(double value) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(value);
    }

    private static Double parseAmount(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        String normalized = raw.trim().replace(',', '.');

        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ignored) {
        }

        try {
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
            return nf.parse(raw).doubleValue();
        } catch (ParseException ex) {
            return null;
        }
    }

    private static String sanitizeField(String value) {
        if (value == null) {
            return "";
        }

        return value.replace(';', ',').replace('\n', ' ').replace('\r', ' ').trim();
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Figyelmeztetes", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Hiba", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        Locale.setDefault(new Locale("hu", "HU"));
        javax.swing.SwingUtilities.invokeLater(() -> {
            App app = new App();
            app.setVisible(true);
        });
    }

    private static final class ChefExpense {
        private final int id;
        private final String chefName;
        private final LocalDate date;
        private final String category;
        private final double amount;
        private final String note;

        private ChefExpense(int id, String chefName, LocalDate date, String category, double amount, String note) {
            this.id = id;
            this.chefName = chefName;
            this.date = date;
            this.category = category;
            this.amount = amount;
            this.note = note;
        }
    }
}
