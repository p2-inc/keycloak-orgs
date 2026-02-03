declare const locale: string;

class TimeUtil {
  private options: Intl.DateTimeFormatOptions = {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "numeric",
    minute: "numeric",
  };
  private shortOptions: Intl.DateTimeFormatOptions = {
    year: "numeric",
    month: "short",
    day: "numeric",
  };
  private getFormatter(
    options: Intl.DateTimeFormatOptions
  ): Intl.DateTimeFormat {
    try {
      return new Intl.DateTimeFormat(locale, options);
    } catch (e) {
      // unknown locale falling back to English
      return new Intl.DateTimeFormat("en", options);
    }
  }

  format(time: number): string {
    const formatter = this.getFormatter(this.options);
    return formatter.format(time);
  }

  formatShort(time: number): string {
    const formatter = this.getFormatter(this.shortOptions);
    return formatter.format(time);
  }

  formatISO(time: string): string {
    const dateObject = new Date(time);
    const formatter = this.getFormatter(this.options);
    return formatter.format(dateObject.getTime());
  }

  formatISOShort(time: string): string {
    const dateObject = new Date(time);
    const formatter = this.getFormatter(this.shortOptions);
    return formatter.format(dateObject.getTime());
  }
}

const TimeUtilInstance: TimeUtil = new TimeUtil();
export default TimeUtilInstance as TimeUtil;
