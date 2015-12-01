package balazs.hajdu.raspberry.domain;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * Simple Twitter client, which can update a twitter account.
 *
 * @author Hajdu Balazs
 */
public class TwitterClient {

    private static final String TOKEN = "zzz";
    private static final String TOKEN_SECRET = "ababab";
    private static final String APP_KEY = "xxxx";
    private static final String APP_SECRET = "zzz";

    private final Twitter twitter;

    public TwitterClient() {
        twitter = setupTwitter();
    }

    public void updateStatus(String message) {
        if (twitter != null) {
            try {
                twitter.updateStatus(message);
            } catch (TwitterException e) {
                System.out.println("TwitterException: " + e.getMessage());
            }
        }
    }

    private Twitter setupTwitter() {
        TwitterFactory factory = new TwitterFactory();
        Twitter t = factory.getInstance();
        AccessToken accessToken = new AccessToken(TOKEN, TOKEN_SECRET);

        authenticateTwitter(accessToken, t);

        return t;
    }

    private void authenticateTwitter(AccessToken accessToken, Twitter twitter) {
        twitter.setOAuthConsumer(APP_KEY, APP_SECRET);
        twitter.setOAuthAccessToken(accessToken);
    }
}
