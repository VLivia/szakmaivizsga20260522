import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class App {
    private static final String FILE_NAME = "chef_berlesek_2025.csv";

    public static void main(String[] args) {
        List<Berles> berlesek;

        try {
            berlesek = loadData(FILE_NAME);
        } catch (IOException e) {
            System.out.println("Hiba a fajl beolvasasa kozben: " + e.getMessage());
            return;
        }

        if (berlesek.isEmpty()) {
            System.out.println("Nincs feldolgozhato adat.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        int honap = askMonth(scanner);

        long haviBevetel = berlesek.stream()
                .mapToLong(b -> b.getRevenueForMonth(2025, honap))
                .sum();

        long evesBevetel = berlesek.stream()
                .mapToLong(Berles::getTotalPrice)
                .sum();

        Berles legdragabb = berlesek.stream()
                .max(Comparator.comparingLong(Berles::getTotalPrice))
                .orElse(null);
        if (legdragabb == null) {
            System.out.println("Nincs feldolgozhato adat.");
            return;
        }

        long kulonbozoSefekSzama = berlesek.stream()
                .map(Berles::getChefId)
                .distinct()
                .count();

        Map<String, Integer> sefBerlesDb = new HashMap<>();
        for (Berles b : berlesek) {
            sefBerlesDb.merge(b.getName(), 1, Integer::sum);
        }

        Map.Entry<String, Integer> legtobbszorBereltSef = sefBerlesDb.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        if (legtobbszorBereltSef == null) {
            System.out.println("Nincs feldolgozhato adat.");
            return;
        }

        Map<String, Integer> konyhaSzerintiDb = new LinkedHashMap<>();
        for (Berles b : berlesek) {
            konyhaSzerintiDb.merge(b.getCuisine(), 1, Integer::sum);
        }

        double atlagosNap = berlesek.stream()
                .mapToLong(Berles::getDurationDays)
                .average()
                .orElse(0.0);

        System.out.printf("A(z) %d. honap bevetele: %d euro%n", honap, haviBevetel);
        System.out.printf("A teljes 2025-os eves bevetel: %d euro%n", evesBevetel);
        System.out.printf(
            "A legdragabb berles %s seftol volt, teljes ar: %d euro%n",
                legdragabb.getName(),
                legdragabb.getTotalPrice());
        System.out.printf("Osszesen %d kulonbozo sefet bereltek ki.%n", kulonbozoSefekSzama);
        System.out.printf(
            "A legtobbszor berelt sef: %s (%d berles)%n",
                legtobbszorBereltSef.getKey(),
                legtobbszorBereltSef.getValue());

        System.out.println("Berlesek szama konyhatipusonkent:");
        for (Map.Entry<String, Integer> entry : konyhaSzerintiDb.entrySet()) {
            System.out.printf("%s: %d berles%n", entry.getKey(), entry.getValue());
        }

        System.out.printf(
                Locale.forLanguageTag("hu-HU"),
            "Atlagos berlesi idotartam: %.2f nap%n",
                atlagosNap);
    }

    private static int askMonth(Scanner scanner) {
        while (true) {
            System.out.print("Adjon meg egy honapot (1-12): ");
            String input = scanner.nextLine().trim();

            try {
                int honap = Integer.parseInt(input);
                if (honap >= 1 && honap <= 12) {
                    return honap;
                }
            } catch (NumberFormatException ignored) {
            }

            System.out.println("Hibas honap! Kerem 1 es 12 kozotti szamot adjon meg.");
        }
    }

    private static List<Berles> loadData(String fileName) throws IOException {
        List<Berles> berlesek = new ArrayList<>();
        Path path = Paths.get(fileName);

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line = br.readLine();
            if (line == null) {
                return berlesek;
            }

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length != 7) {
                    continue;
                }

                int uid = Integer.parseInt(parts[0].trim());
                int chefId = Integer.parseInt(parts[1].trim());
                LocalDate startDate = parseDateSafe(parts[2].trim());
                LocalDate endDate = parseDateSafe(parts[3].trim());
                int dailyRate = Integer.parseInt(parts[4].trim());
                String name = parts[5].trim();
                String cuisine = parts[6].trim();

                if (endDate.isBefore(startDate)) {
                    LocalDate tmp = startDate;
                    startDate = endDate;
                    endDate = tmp;
                }

                berlesek.add(new Berles(uid, chefId, startDate, endDate, dailyRate, name, cuisine));
            }
        }

        return berlesek;
    }

    private static LocalDate parseDateSafe(String value) {
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            String[] ymd = value.split("-");
            if (ymd.length == 3) {
                int year = Integer.parseInt(ymd[0]);
                int month = Integer.parseInt(ymd[1]);
                int day = Integer.parseInt(ymd[2]);
                int maxDay = YearMonth.of(year, month).lengthOfMonth();
                int clampedDay = Math.max(1, Math.min(day, maxDay));
                return LocalDate.of(year, month, clampedDay);
            }
            throw ex;
        }
    }
}
