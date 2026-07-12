package advocate.com.advocate_app.dto;

import java.util.List;

public class IncomeExpenseDTO {
    private List<MonthEntry> items;

    public IncomeExpenseDTO() {}

    public IncomeExpenseDTO(List<MonthEntry> items) {
        this.items = items;
    }

    public List<MonthEntry> getItems() { return items; }
    public void setItems(List<MonthEntry> items) { this.items = items; }

    public static class MonthEntry {
        private String month;
        private double income;
        private double expense;
        private double net;

        public MonthEntry() {}

        public MonthEntry(String month, double income, double expense) {
            this.month = month;
            this.income = income;
            this.expense = expense;
            this.net = income - expense;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public double getIncome() { return income; }
        public void setIncome(double income) { this.income = income; }

        public double getExpense() { return expense; }
        public void setExpense(double expense) { this.expense = expense; }

        public double getNet() { return net; }
        public void setNet(double net) { this.net = net; }
    }
}
