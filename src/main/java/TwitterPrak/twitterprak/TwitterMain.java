package TwitterPrak.twitterprak;

import java.util.ArrayList;
import java.util.Arrays;

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

public class TwitterMain {
    public static void main(String[] args) throws TwitterException, InterruptedException {	
    	
	ArrayList<String> commentList = new ArrayList<String>();
	commentList.addAll(Arrays.asList("Super!", "Schönes Bild!", "Wunderbar!", "Cool!", "Top!", "Wahnsinnig cool!",
			"Ich mag das total!", "Fantastisch!", "Wahnsinnig cool!", "Lässig!", "Sieht echt gut aus!", "Sehr gelungenes Bild!",
			"Stark!","Echt gut getroffen!","Super Foto!","Was für ein Foto!","Gefällt mir wahnsinnig gut!","Grossartig!"));
	
	//OAuth
	ConfigurationBuilder cb = new ConfigurationBuilder();
	cb.setDebugEnabled(true)
	.setOAuthConsumerKey("8xJfQeMZV1MyFmvMS7OzJiGhh")
	.setOAuthConsumerSecret("7S2NcZtmiPyPHI4FaAlHj0hYFikZYEUwIJMxlaul5UWrSzVKyL")
	.setOAuthAccessToken("859134068021243906-wTL2dQ92NJmIFW6jwsp5pfwLNEwn5Ej")
	.setOAuthAccessTokenSecret("BJKO7sEEMXK7vLmktYmIjGVPgcUH8kIoGG6TA1fJgVJo2");
	TwitterFactory tf = new TwitterFactory(cb.build());	
	Twitter twitter = tf.getInstance();
	
	//user erzeugen!
	User user = twitter.showUser( twitter.getId() );
	//Liste aller tweets anlegen, da die mehrmals gebraucht wird
	//ArrayList<Status> statusList = collectStatuses( twitter );	
	//Liste aller Follower
	//ResponseList<User> followerList = TwitterClass.getFollowerList( twitter ); 	
	
	//MongoDB---------------------------------------------------------------------------------------------------	
	MongoClientURI mongoClientURI = new MongoClientURI("mongodb://localhost:27017");
	MongoClient mongoClient = new MongoClient(mongoClientURI);
	
	//------------------------------------------------------------------------------------------
	//verwendete Collection setzen!!!	VORSICHT!!!
	//fColl (Followers), coll2a, coll2b, coll2c, coll2a_f, coll2b_f, coll2c_f oder testColl
	String mongoCollectionName = "coll2c_f";
	
	MongoDatabase mongoDatabase = mongoClient.getDatabase(mongoCollectionName);
	MongoCollection<Document> mongoCollection = mongoDatabase.getCollection( mongoCollectionName );
	
	//diese wird EXTRA benötigt!!! wenn man aus ihr User extrahieren und sie in coll2a_f,
	//coll2b_f oder coll2c_f einfügen will
	MongoDatabase filteredDatabase = mongoClient.getDatabase("filteredUserColl");
	MongoCollection<Document> filteredCollection = filteredDatabase.getCollection( "filteredUserColl" );
	//---------------------------------------------------------------------------------------			
	
	//---------------------------------------------------------------------------------------------------
	//DONE (DONT TOUCH!!!)
	//Use SCREENNAMES!!! Weiter oben den Namen der jeweiligen Collection setzen!!! (mongoCollectionName)	
	//100 Usern folgen
	//Datenbank.followManyAndInsertInColl(twitter, Datenbank.getFollowerIdArrayList(twitter, "SauberF1Team"), , mongoCollection, 100, mongoCollectionName, commentList);
	//100 Usern folgen und einen aktuellen Post liken
	//Datenbank.followManyAndInsertInColl(twitter, Datenbank.getFollowerIdArrayList(twitter, "hurricanefstvl"), twitter.getId(), mongoCollection, 100, mongoCollectionName, commentList);
	//100 Usern folgen, einen aktuellen Post liken und kommentieren
	//Datenbank.followManyAndInsertInColl(twitter, Datenbank.getFollowerIdArrayList(twitter, "SkySportsF1"), twitter.getId(), mongoCollection, 100, mongoCollectionName, commentList);
	
	//IMMER die beiden laufen lassen, nachdem eine collection erstellt wurde!!!	
	//lieber hier, NICHT DIREKT in den Methoden wegen RateLimit!
	//Datenbank.checkAndSetGetsFollowed(twitter, mongoCollection);
	//Datenbank.checkAndSetIsFollowing(twitter, mongoCollection);	
	//-------------------------------------------------------------------------------------------------
	
	//User filtern für die filteredUserColl
	//bereits gefiltert: rockamring summerjam MercedesAMGF1 redbullracing MBrundleF1 rockimpark_com southsidefstvl highfieldfstvl
	//Datenbank.filterUsersAndSaveInDB(mongoCollection, twitter, Datenbank.getFollowerIdArrayList(twitter, ""));	
	
	//-------------------------------------------------------------------------------------------------------	
	//DONE (DONT TOUCH!!!)
	//Aufgabe 2a MIT FILTER:
	//@param filteredCollection für die find Methode!!!
	//@param mongoCollection: die, in die eingefügt wird, wie üblich, MUSS OBEN GESETZT WERDEN!!!
	//followUserSet reicht! Weil ja nur gefolgt werden soll und die User bereits vorselektiert sind!
	//erst NUR diese Methode! DANACH die Checks!
	//Datenbank.followUserSetAndInsertInColl(twitter, Datenbank.findFilteredUsersForAction(twitter, filteredCollection), mongoCollection, twitter.getId());
	//WICHTIG!!! Danach immer ausführen, auf der filteredCollection!!!!
	//Datenbank.checkAndSetGetsFollowed(twitter, filteredCollection);
	//Datenbank.checkAndSetIsFollowing(twitter, filteredCollection);
	//Datenbank.showCollection(mongoCollection, mongoCollectionName);	//zeige coll2a_f
	
	//Aufgabe 2b MIT FILTER:
	//@param filteredCollection für die find Methode!!!
	//@param mongoCollection: die, in die eingefügt wird, wie üblich, MUSS OBEN GESETZT WERDEN!!!	
	//Datenbank.followManyAndInsertInColl(twitter, Datenbank.findFilteredUsersForAction(twitter, filteredCollection), twitter.getId(), mongoCollection, 100, mongoCollectionName, commentList);
	//WICHTIG!!! Danach immer ausführen, auf der filteredCollection!!!!
	//Datenbank.checkAndSetGetsFollowed(twitter, filteredCollection);
	//Datenbank.checkAndSetIsFollowing(twitter, filteredCollection);
	//Datenbank.showCollection(mongoCollection, mongoCollectionName);	//zeige coll2b_f
	
	//Aufgabe 2c MIT FILTER:
	//@param filteredCollection für die find Methode!!!
	//@param mongoCollection: die, in die eingefügt wird, wie üblich, MUSS OBEN GESETZT WERDEN!!!	
	//Datenbank.followManyAndInsertInColl(twitter, Datenbank.findFilteredUsersForAction(twitter, filteredCollection), twitter.getId(), mongoCollection, 100, mongoCollectionName, commentList);
	//WICHTIG!!! Danach immer ausführen, auf der filteredCollection!!!!
	//Datenbank.checkAndSetGetsFollowed(twitter, filteredCollection);
	//Datenbank.checkAndSetIsFollowing(twitter, filteredCollection);
	//Datenbank.showCollection(mongoCollection, mongoCollectionName);	//zeige coll2c_f	
	//--------------------------------------------------------------------------------------------------------------
	
	
	
	//Datenbank.unfollowUsers(twitter, Datenbank.getUserIDsFromDB(mongoCollection));
	
	//Datenbank.insertNewFollowersInDB(mongoCollection, followerList);
	
	//wenn bei "follow and like" was schiefgeht:
	//Datenbank.likeSomeUsers(twitter, mongoCollection);	
	
	/*
	System.out.println( "TwitterID: " + user.getId() );
	System.out.println( "TwitterName: " + user.getScreenName() );
	System.out.println( "Anzahl Follower: " + user.getFollowersCount() );
	//liefert 15 obwohl ich 16 Follower habe???
	//"You won't see suspected spam accounts here"!
	System.out.println( "Anzahl Follower_m: " + followerList.size() );
	System.out.println( "Anzahl following: " + user.getFriendsCount() );
	//System.out.println( "Anzahl Tweets: " + countTweets(twitter) );  //ALT!!!!
	System.out.println( "Anzahl tweets: " + user.getStatusesCount() );
	//dazu steht nix in der JavaDoc ???
	System.out.println( "Anzahl Likes_???: " + user.getFavouritesCount() );		
	System.out.println( "Anzahl Likes_m: " + countLikes(statusList) );
	*/
	
	//nicht möglich???
	//System.out.println( "Anzahl replies_m: " +  );	
	
	//Anzahl weicht ab von TwitterAnalytics
	//System.out.println( "Anzahl Retweets: " + countRetweets(statusList) );
	
	//Maximum = 100 ?!?!
	//System.out.println( "Anzahl Mentions: " + countMentions( twitter, user ) );	
	
	
	//User aus DB löschen
	/*
	Bson filter = new Document("Follower Name", "netzpolitik");	
	mongoCollection.findOneAndDelete(filter);	
	*/
	
	//Collection löschen
	//mongoCollection.drop();
	
	mongoClient.close();
    }
}
