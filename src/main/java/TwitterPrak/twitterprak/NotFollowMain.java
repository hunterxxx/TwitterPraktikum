package TwitterPrak.twitterprak;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class NotFollowMain {
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
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Name of MongoCollection: ");
		String nameOfMongoCollection = scanner.next();
		
		System.out.print("Alter des letzten Posts (Tage): ");
		int tage = scanner.nextInt();
		
		scanner.close();
		
		MongoClientURI mongoClientURI = new MongoClientURI("mongodb://localhost:27017");
		MongoClient mongoClient = new MongoClient(mongoClientURI);
		
		String mongoCollectionName = nameOfMongoCollection;
		
		MongoDatabase mongoDatabase = mongoClient.getDatabase(mongoCollectionName);
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(mongoCollectionName);
		
		Datenbank.unfollowWhoNotFollowsBack(twitter, mongoCollection, tage);
		
		mongoClient.close();
	}
}
