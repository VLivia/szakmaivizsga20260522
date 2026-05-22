import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

public class Berles {
    private final int uid;
    private final int chefId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int dailyRate;
    private final String name;
    private final String cuisine;

    public Berles(int uid, int chefId, LocalDate startDate, LocalDate endDate, int dailyRate, String name, String cuisine) {
        this.uid = uid;
        this.chefId = chefId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dailyRate = dailyRate;
        this.name = name;
        this.cuisine = cuisine;
    }

    public int getUid() {
        return uid;
    }

    public int getChefId() {
        return chefId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getDailyRate() {
        return dailyRate;
    }

    public String getName() {
        return name;
    }

    public String getCuisine() {
        return cuisine;
    }

    public long getDurationDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1L;
    }

    public long getTotalPrice() {
        return getDurationDays() * dailyRate;
    }

    public long getRevenueForMonth(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();

        if (endDate.isBefore(monthStart) || startDate.isAfter(monthEnd)) {
            return 0;
        }

        LocalDate overlapStart = startDate.isAfter(monthStart) ? startDate : monthStart;
        LocalDate overlapEnd = endDate.isBefore(monthEnd) ? endDate : monthEnd;

        long overlapDays = ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1L;
        return overlapDays * dailyRate;
    }
}
