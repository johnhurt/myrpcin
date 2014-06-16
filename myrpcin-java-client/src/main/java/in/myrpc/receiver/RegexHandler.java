package in.myrpc.receiver;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handler Methods for regular expressions and their usage within scripting
 * environments
 *
 * @author kguthrie
 */
public class RegexHandler {

    private final Map<String, Pattern> regexCache;
    private final Map<String, Matcher> matcherCache;

    public RegexHandler() {
        this.regexCache = new HashMap<String, Pattern>();
        this.matcherCache = new HashMap<String, Matcher>();
    }

    /**
     * Handle the regular expression searching for the given pattern and
     * search string.
     * @param patternString regular expression to be used.  This pattern should
     *                      contain enough capturing groups to satisfy the
     *                      specified group number
     * @param searchString String to search for the the given pattern
     * @param stream the combination of pattern and search string might have
     *               been used before.  If it was, this flag indicates whether
     *               or not to continue scanning the search string where the
     *               last match stopped.  This flag also indicates that the
     *               pattern and search string combo should be stored until
     *               the last match is found
     * @param groupNumber number of the capturing group to be returned
     * @return
     */
    public StringBuilder handleRegex(String patternString, String searchString,
            boolean stream, int groupNumber) {

        Matcher matcher = null;
        String matcherKey = patternString + searchString;
        if (stream) {
            matcher = matcherCache.get(matcherKey);
        }

        if (matcher == null) {

            Pattern pattern = regexCache.get(patternString);

            if (pattern == null) {
                pattern = Pattern.compile(patternString,
                        Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);

                regexCache.put(patternString, pattern);
            }

            matcher = pattern.matcher(searchString);

            if (stream) {
                matcherCache.put(matcherKey, matcher);
            }
        }

        String matchResult = null;

        if (matcher.find()) {
            matchResult = matcher.group(groupNumber);
        }
        else if (stream) {
            matcherCache.remove(matcherKey);
        }

        return new StringBuilder(matchResult != null ? matchResult : "");
    }

    /**
     * Reset this handler back to a fresh state.
     */
    public void reset() {
        matcherCache.clear(); // Clear the matchers but not the cached patterns
    }
}
