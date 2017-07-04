package TwitterPrak.twitterprak;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class ReadOauth {
	public static void main(String[] args) throws TwitterException, InterruptedException, IOException {
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
		
       System.out.println("Consumer key: " + consumerKey);      
       System.out.println("Consumer Secret: " + consumerSecret);
       System.out.println("OAuthAccessToken: " + OAuthAccessToken);      
       System.out.println("OAuthAccessTokenSecret: " + OAuthAccessTokenSecret);
        
	}
}
