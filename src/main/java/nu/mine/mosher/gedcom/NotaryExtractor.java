package nu.mine.mosher.gedcom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotaryExtractor {
    private final Pattern extractor;

    public NotaryExtractor(final String mask) {
        final String q = Pattern.quote(mask);
        final String p = q + ": (.*?) " + q;
        this.extractor = Pattern.compile("(.*?)" + p + "(.*)", Pattern.DOTALL);
    }

    public String[] extract(final String from) {
        final Matcher m = this.extractor.matcher(from);
        if (!m.find()) {
            return new String[]{"", from};
        }

        return new String[]{m.group(2), m.group(1) + m.group(3)};
    }
}
