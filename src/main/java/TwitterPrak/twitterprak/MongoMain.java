package TwitterPrak.twitterprak;
import java.io.File;
import java.io.FileNotFoundException;
/**
 * filter user and save in db
 */
import java.util.Scanner;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class MongoMain {
	public static void main(String[] args) throws TwitterException, InterruptedException, FileNotFoundException {

		Scanner scanner = new Scanner(System.in);
		
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

		System.out.print("Schwellwerte für Following/Followee Ratio (Obergrenze): ");
		double upperlimit = scanner.nextDouble();

		System.out.print("Schwellwerte für Following/Followee Ratio (Untergrenze): ");
		double lowerlimit = scanner.nextDouble();

		System.out.print("Mindestanzahl Posts: ");
		int tweetCounts = scanner.nextInt();

		System.out.print("Alter des letzten Posts (Tage): ");
		int tage = scanner.nextInt();

		System.out.println("Influencer's Name for filteredUserColl: ");	
		String followerIDs = scanner.next();
		
		System.out.println("Name of MongoCollection: ");
		String nameOfMongoCollection = scanner.next(); 
		
		scanner.close();
		
		MongoClientURI mongoClientURI = new MongoClientURI("mongodb://localhost:27017");
		MongoClient mongoClient = new MongoClient(mongoClientURI);

		//verwendete Collection setzen
		//fColl (Followers), coll2a, coll2b, coll2c, coll2a_f, coll2b_f, coll2c_f oder testColl
		String mongoCollectionName = nameOfMongoCollection;

		MongoDatabase mongoDatabase = mongoClient.getDatabase(mongoCollectionName);
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(mongoCollectionName);
		
		//diese wird EXTRA benötigt!!! wenn man aus ihr User extrahieren und sie in coll2a_f,
		//coll2b_f oder coll2c_f einfügen will
		MongoDatabase filteredDatabase = mongoClient.getDatabase("filteredUserColl");
		MongoCollection<Document> filteredCollection = filteredDatabase.getCollection( "filteredUserColl" );
		
		// Filtert User eines Influencers Account, und speichert gefundene User in Collection
		Datenbank.filterUsersAndSaveInDB(mongoCollection, twitter, Datenbank.getFollowerIdArrayList(twitter, followerIDs), lowerlimit, upperlimit, tweetCounts, tage);
		 
		//Beispiel 2a Ohne Filter, coll2a gibt es keine Kommentare
//		Datenbank.followManyAndInsertInColl(twitter, Datenbank.getFollowerIdArrayList(twitter, "MBrundleF1"), twitter.getId(), mongoCollection, 100, mongoCollectionName, Comment.commentList());		 		
//		System.out.println(Datenbank.getDBRatio(mongoCollection));
		
		//Beispiel Aufgabe 2c MIT FILTER:
		//@param filteredCollection für die find Methode!!!
		//@param mongoCollection: die, in die eingefügt wird, wie üblich, MUSS OBEN GESETZT WERDEN!!!	
		//Datenbank.followManyAndInsertInColl(twitter, Datenbank.findFilteredUsersForAction(twitter, filteredCollection), twitter.getId(), mongoCollection, 100, mongoCollectionName, commentList);
		//WICHTIG!!! Danach immer ausführen, auf der filteredCollection!!!! Nicht gleichzeitig durchgeführt wegen Rate Limit
		//Datenbank.checkAndSetGetsFollowed(twitter, filteredCollection);
		//Datenbank.checkAndSetIsFollowing(twitter, filteredCollection);
		//Datenbank.showCollection(mongoCollection, mongoCollectionName);	//zeige coll2c_f	
		
		mongoClient.close();
	}
}
