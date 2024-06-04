package io.phasetwo.service.util;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateFormatter {

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public static LocalDate parse(String s) throws ParseException {
    if (!s.matches("\\d{4}-\\d{2}-\\d{2}")) {
      throw new ParseException("Wrong date format", 0);
    }
    return LocalDate.parse(s, formatter);
  }

  public static String format(LocalDate d) {
    return formatter.format(d);
  }
}
