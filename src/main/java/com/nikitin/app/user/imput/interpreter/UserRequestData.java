package com.nikitin.app.user.imput.interpreter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class UserRequestData {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private String startDate;
    private String endDate;

    public String getParameters() {
        Scanner scanner = new Scanner(System.in);

        while (true) {

            this.startDate = askDate(scanner, "Введите дату начала поиска в формате дд.мм.гггг (или 'restart' для перезапуска, 'exit' для выхода): ");

            if (handleCommands(this.startDate)) {
                continue;
            }

            this.endDate = askDate(scanner, "Введите дату конца поиска в формате дд.мм.гггг (или 'restart' для перезапуска, 'exit' для выхода): ");

            if (handleCommands(this.endDate)) {
                continue;
            }

            try {
                LocalDate start = LocalDate.parse(this.startDate, FORMATTER);
                LocalDate end = LocalDate.parse(this.endDate, FORMATTER);

                if (end.isBefore(start)) {
                    System.out.println("Дата конца поиска не может быть раньше даты начала. Попробуйте снова.\n");
                    continue;
                }

                return "?filterminloaddate=" + startDate + "&filtermaxloaddate=" + endDate;

            } catch (DateTimeParseException e) {
                System.out.println("Неверный формат даты. Используйте дд.мм.гггг\n");
            }
        }
    }

    private String askDate(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("restart") || input.equalsIgnoreCase("exit")) {
                return input.toLowerCase();
            }
            try {
                LocalDate.parse(input, FORMATTER);
                return input;
            } catch (DateTimeParseException e) {
                if (input.isEmpty()) {
                    System.out.println("Введена пустая дата. Пожалуйста, введите дату в формате дд.мм.гггг");
                } else {
                    System.out.println("Неверный формат даты. Используйте дд.мм.гггг");
                }
            }
        }
    }

    private boolean handleCommands(String input) {
        if (input.equalsIgnoreCase("restart")) {
            System.out.println("Перезапуск программы по запросу пользователя...\n");
            return true;
        }
        if (input.equalsIgnoreCase("exit")) {
            System.out.println("Завершение программы по запросу пользователя...");
            System.exit(0);
        }
        return false;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getStartDate() {
        return startDate;
    }
}