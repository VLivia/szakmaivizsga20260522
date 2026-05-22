import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class App extends Application {
    private static final String CSV_FILE_NAME = "chef_koltsegek_2025.csv";
    private static final DateTimeFormatter CSV_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TABLE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy. MM. dd.");
    private static final String[] CATEGORIES = {
            "Travel", "Ingredients", "Accommodation", "Equipment", "Other"
    };

    private final ObservableList<ChefExpense> expenses = FXCollections.observableArrayList();

    private TableView<ChefExpense> expenseTable;
    private TextField chefNameField;
    private DatePicker datePicker;
    private ComboBox<String> categoryCombo;
    private TextField amountField;
    private TextArea noteArea;
    private Label recordCountValueLabel;
    private Label totalAmountValueLabel;

    private Path csvPath;

    @Override
    public void start(Stage stage) {
        csvPath = resolveCsvPath();

        expenseTable = createExpenseTable();

        chefNameField = new TextField();
        datePicker = new DatePicker(LocalDate.now());
        datePicker.setEditable(false);

        categoryCombo = new ComboBox<>(FXCollections.observableArrayList(CATEGORIES));
        categoryCombo.setPromptText("Valassz kategoriat");

        amountField = new TextField();

        noteArea = new TextArea();
        noteArea.setPrefRowCount(4);
        noteArea.setWrapText(true);

        recordCountValueLabel = new Label("0");
        totalAmountValueLabel = new Label("0.00 EUR");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(8);
        formGrid.setVgap(8);
        formGrid.addRow(0, new Label("Sef neve:"), chefNameField);
        formGrid.addRow(1, new Label("Datum:"), datePicker);
        formGrid.addRow(2, new Label("Kategoria:"), categoryCombo);
        formGrid.addRow(3, new Label("Osszeg (EUR):"), amountField);
        formGrid.add(new Label("Megjegyzes:"), 0, 4);
        formGrid.add(noteArea, 1, 4);

        GridPane.setHgrow(chefNameField, Priority.ALWAYS);
        GridPane.setHgrow(datePicker, Priority.ALWAYS);
        GridPane.setHgrow(categoryCombo, Priority.ALWAYS);
        GridPane.setHgrow(amountField, Priority.ALWAYS);
        GridPane.setHgrow(noteArea, Priority.ALWAYS);

        Button addButton = new Button("Hozzaadas");
        addButton.setOnAction(e -> addExpense());

        HBox summaryBar = new HBox(
                10,
                new Label("Rekordszam:"),
                recordCountValueLabel,
                new Label("   Osszes koltseg:"),
                totalAmountValueLabel,
                addButton
        );
        summaryBar.setAlignment(Pos.CENTER_RIGHT);

        VBox bottomPanel = new VBox(10, formGrid, summaryBar);
        bottomPanel.setPadding(new Insets(12));

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setCenter(expenseTable);
        root.setBottom(bottomPanel);

        loadExpenses();
        refreshTable();
        updateSummary();

        Scene scene = new Scene(root, 980, 700);
        stage.setTitle("ChefExpenses (JavaFX)");
        stage.setMinWidth(960);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
    }

    private TableView<ChefExpense> createExpenseTable() {
        TableView<ChefExpense> table = new TableView<>(expenses);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ChefExpense, Integer> idColumn = new TableColumn<>("Id");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setMaxWidth(80);

        TableColumn<ChefExpense, String> chefNameColumn = new TableColumn<>("ChefName");
        chefNameColumn.setCellValueFactory(new PropertyValueFactory<>("chefName"));

        TableColumn<ChefExpense, String> dateColumn = new TableColumn<>("Datum");
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().format(TABLE_DATE_FORMAT)));

        TableColumn<ChefExpense, String> categoryColumn = new TableColumn<>("Kategoria");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<ChefExpense, String> amountColumn = new TableColumn<>("Osszeg");
        amountColumn.setCellValueFactory(data -> new SimpleStringProperty(formatAmount(data.getValue().getAmount())));

        TableColumn<ChefExpense, String> noteColumn = new TableColumn<>("Megjegyzes");
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));

        table.getColumns().addAll(idColumn, chefNameColumn, dateColumn, categoryColumn, amountColumn, noteColumn);
        return table;
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
            showWarning(message.toString());
        }
    }

    private void refreshTable() {
        FXCollections.sort(expenses, (a, b) -> Integer.compare(a.getId(), b.getId()));
        expenseTable.refresh();
    }

    private void addExpense() {
        String chefName = chefNameField.getText().trim();
        String selectedCategory = categoryCombo.getValue();

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

        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            showWarning("Valasszon datumot.");
            datePicker.requestFocus();
            return;
        }

        int nextId = expenses.stream().mapToInt(ChefExpense::getId).max().orElse(0) + 1;

        ChefExpense newExpense = new ChefExpense(
                nextId,
                sanitizeField(chefName),
                selectedDate,
                selectedCategory,
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

        int lastRow = expenses.size() - 1;
        if (lastRow >= 0) {
            expenseTable.getSelectionModel().select(lastRow);
            expenseTable.scrollTo(lastRow);
        }

        clearForm();
    }

    private void clearForm() {
        chefNameField.setText("");
        amountField.setText("");
        noteArea.setText("");
        categoryCombo.setValue(null);
        datePicker.setValue(LocalDate.now());
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

            List<ChefExpense> sorted = new ArrayList<>(expenses);
            sorted.sort((a, b) -> Integer.compare(a.getId(), b.getId()));

            for (ChefExpense exp : sorted) {
                writer.write(exp.getId() + ";"
                        + sanitizeField(exp.getChefName()) + ";"
                        + exp.getDate().format(CSV_DATE_FORMAT) + ";"
                        + sanitizeField(exp.getCategory()) + ";"
                        + exp.getAmount() + ";"
                        + sanitizeField(exp.getNote()));
                writer.newLine();
            }
        }

        Files.move(tempFile, csvPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private void updateSummary() {
        recordCountValueLabel.setText(Integer.toString(expenses.size()));
        double total = expenses.stream().mapToDouble(ChefExpense::getAmount).sum();
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
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Figyelmeztetes");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hiba");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        Locale.setDefault(new Locale("hu", "HU"));
        launch(args);
    }

    public static final class ChefExpense {
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

        public int getId() {
            return id;
        }

        public String getChefName() {
            return chefName;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getCategory() {
            return category;
        }

        public double getAmount() {
            return amount;
        }

        public String getNote() {
            return note;
        }
    }
}
