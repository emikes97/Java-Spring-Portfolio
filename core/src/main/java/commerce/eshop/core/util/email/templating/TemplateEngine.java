package commerce.eshop.core.util.email.templating;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TemplateEngine {

    // matches {{ key }} or {{key}}

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([A-Za-z0-9_]+)\\s*\\}\\}");

    private TemplateEngine() {}

    /** Replaces {{key}} with model.get("key"); missing keys become empty string. */
    public static String render(String template, Map<String, Object> model) {
        Matcher m = PLACEHOLDER.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            Object val = model.getOrDefault(key, "");
            m.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(val)));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
