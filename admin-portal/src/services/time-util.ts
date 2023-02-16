declare const locale: string;

class TimeUtil {
    private options: Intl.DateTimeFormatOptions = { year: 'numeric', month: 'long', day: 'numeric', hour: 'numeric', minute: 'numeric' };
    private formatter: Intl.DateTimeFormat;

    constructor() {
        try {
            this.formatter = new Intl.DateTimeFormat(locale, this.options);
        } catch(e) {
            // unknown locale falling back to English
            this.formatter = new Intl.DateTimeFormat('en', this.options);
        }
    }

    format(time: number): string {
        return this.formatter.format(time);
    }
}

const TimeUtilInstance: TimeUtil = new TimeUtil();
export default TimeUtilInstance as TimeUtil;
