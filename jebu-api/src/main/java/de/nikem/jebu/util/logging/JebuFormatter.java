package de.nikem.jebu.util.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

public class JebuFormatter extends Formatter {
	private LogManager manager = LogManager.getLogManager();

    // format string for printing the log record
    private String format = manager.getProperty(getClass().getName() + ".format");
    private final Date dat = new Date();

    /**
     * Format the given LogRecord.
     * <p>
     * The formatting can be customized by specifying the
     * <a href="../Formatter.html#syntax">format string</a>
     * in the <a href="#formatting">
     * {@code java.util.logging.SimpleFormatter.format}</a> property.
     * The given {@code LogRecord} will be formatted as if by calling:
     * <pre>
     *    {@link String#format String.format}(format, date, source, logger, level, message, thrown);
     * </pre>
     * where the arguments are:<br>
     * <ol>
     * <li>{@code format} - the {@link java.util.Formatter
     *     java.util.Formatter} format string specified in the
     *     {@code java.util.logging.SimpleFormatter.format} property
     *     or the default format.</li>
     * <li>{@code threadName} - the current thread name.</li>
     * <li>{@code date} - a {@link Date} object representing
     *     {@linkplain LogRecord#getMillis event time} of the log record.</li>
     * <li>{@code source} - a string representing the caller, if available;
     *     otherwise, the logger's name.</li>
     * <li>{@code logger} - the logger's name.</li>
     * <li>{@code level} - the {@linkplain java.util.logging.Level#getLocalizedName
     *     log level}.</li>
     * <li>{@code message} - the formatted log message
     *     returned from the {@link Formatter#formatMessage(LogRecord)}
     *     method.  It uses {@link java.text.MessageFormat java.text}
     *     formatting and does not use the {@code java.util.Formatter
     *     format} argument.</li>
     * <li>{@code thrown} - a string representing
     *     the {@linkplain LogRecord#getThrown throwable}
     *     associated with the log record and its backtrace
     *     beginning with a newline character, if any;
     *     otherwise, an empty string.</li>
     * </ol>
     *
     * <p>Some example formats:<br>
     * <ul>
     * <li> {@code de.nikem.jebu.util.logging.JebuFormatter.format=[%2$tF %2$tT.%2$tL][%1s] %5$-6.6s %3$s - %6$s%n%7$s}
     *     <p>This prints the following line
     *     <pre>
     *     [2016-03-30 23:21:04.608][Server-Thread] INFORM org.eclipse.jetty.util.log.Log initialized - Logging initialized @226ms
     *     </pre></li>
     * </ul>
     * <p>This method can also be overridden in a subclass.
     * It is recommended to use the {@link Formatter#formatMessage}
     * convenience method to localize and format the message field.
     *
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format(LogRecord record) {
        dat.setTime(record.getMillis());
        String source;
        if (record.getSourceClassName() != null) {
            source = record.getSourceClassName();
            if (record.getSourceMethodName() != null) {
               source += " " + record.getSourceMethodName();
            }
        } else {
            source = record.getLoggerName();
        }
        String message = formatMessage(record);
        String throwable = "";
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }
        return String.format(format,
        					 Thread.currentThread().getName(),
                             dat,
                             source,
                             record.getLoggerName(),
                             record.getLevel().getLocalizedName(),
                             message,
                             throwable);
    }

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
}