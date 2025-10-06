package com.nikitin.app.user.imput.interpreter;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class UserRequestData {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private String startDate;
    private String endDate;
    int pageSize;

    public String interpret() {

        Scanner scanner = new Scanner(System.in);

        this.startDate = askDate(scanner, "Введите дату начала поиска в формате дд.мм.гггг" +
                                            " (или просто нажмите Enter, чтобы пропустить): ");

        this.endDate = askDate(scanner, "Введите дату конца поиска в формате дд.мм.гггг" +
                                          " (или просто нажмите Enter, чтобы пропустить): ");

        String pageSize = askPageSize(scanner,"Введите искомое количество страниц." +
                                              " Нажмите Enter, чтобы пропустить: ");
        this.pageSize = Integer.parseInt(pageSize);

        StringBuilder urlBuilder = new StringBuilder();
        boolean hasParam = false;
        if (!startDate.isEmpty()) {
            urlBuilder.append("?");
            urlBuilder.append("filterminloaddate=").append(startDate);
            hasParam = true;
        }

        if (!endDate.isEmpty()) {
            urlBuilder.append(hasParam ? "&" : "?");
            urlBuilder.append("filtermaxloaddate=").append(endDate);
            hasParam = true;
        }

        if (!pageSize.isEmpty()) {
            urlBuilder.append(hasParam ? "&" : "?");
            urlBuilder.append("pageSize=").append(pageSize);
        }

        return urlBuilder.toString();
    }

    private String askDate(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return "";
            }
            try {
                LocalDate.parse(input, FORMATTER);
                return input;
            } catch (DateTimeParseException e) {
                System.out.println("Неверный формат даты. Используйте дд.мм.гггг или нажмите Enter, чтобы не указывать дату.");
            }
        }
    }

    private String askPageSize(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return "";
            }
            try {
                int pages = Integer.parseInt(input);
                if (pages > 0) {
                    return input;
                } else {
                    System.out.println(
                            "Количество страниц должно быть положительным числом. " +
                            "В случае, если Вы не хотите указывать число страниц, нажмите Enter.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Неверный формат. Введите число или нажмите Enter, чтобы оставить поле пустым.");
            }
        }
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }


    public String getEndDate() {
        return endDate;
    }

    public String getStartDate() {
        return startDate;
    }
}