package TwitterPrak.twitterprak;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class Aufgabe4 {
    public static void main(String[] args) throws TwitterException, InterruptedException, FileNotFoundException {	
    	
        Scanner fileScan = new Scanner(new File("src/oauth.txt"));
        
        String consumerKey = fileScan.next();
        fileScan.nextLine();
        String consumerSecret = fileScan.next();
        fileScan.nextLine();
        String OAuthAccessToken = fileScan.next();
        fileScan.nextLine();
        String OAuthAccessTokenSecret = fileScan.nextLine();
             
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey)
				.setOAuthConsumerSecret(consumerSecret)
				.setOAuthAccessToken(OAuthAccessToken)
				.setOAuthAccessTokenSecret(OAuthAccessTokenSecret);
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		
		User user = twitter.showUser(twitter.getId());

		System.out.println( "TwitterID: " + user.getId() );
		System.out.println( "TwitterName: " + user.getScreenName() );
		System.out.println( "Anzahl Follower: " + user.getFollowersCount() );
		System.out.println( "Anzahl following: " + user.getFriendsCount() );
		System.out.println( "Anzahl tweets: " + user.getStatusesCount() );
		System.out.println( "Anzahl Likes: " + user.getFavouritesCount());	
    }
}
