package TwitterPrak.twitterprak;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class Datenbank {
	// holt User IDs aus einer Collection
	static ArrayList<Long> getUserIDsFromDB(MongoCollection<Document> mongoCollection) {
		MongoCursor<Document> idCursor;
		Document docUser;
		idCursor = mongoCollection.find().iterator();
		ArrayList<Long> userIDs = new ArrayList<Long>();
		while (idCursor.hasNext()) {
			docUser = idCursor.next();
			userIDs.add(docUser.getLong("User_ID"));
		}
		return userIDs;
	}

	// Collection Inhalt anzeigen (Json optional)
	static void showCollection(MongoCollection<Document> mongoCollection, String mongoCollectionName) {
		System.out.println("Einträge in Collection " + mongoCollectionName + ":");
		for (Document doc : mongoCollection.find()) {
			System.out.println("      " + doc); // doc.toJson() falls man es in
												// Json braucht!?!?
		}
		System.out.println(mongoCollection.count() + " Einträge in DB ");
	}

	static void insertUserInDB(User user, MongoCollection<Document> mongoCollection, boolean isFollowing,
			boolean getsFollowed) {
		ArrayList<Long> userIDsInCollection = getUserIDsFromDB(mongoCollection);
		// User wird nur eingetragen, wenn er nicht schon drin steht
		if (!(userIDsInCollection.contains(user.getId()))) {
			Document insertDoc = new Document();
			insertDoc.append("ScreenName", user.getScreenName()).append("User_ID", user.getId())
					.append("isFollowing", isFollowing) // er folgt mir
					.append("getsFollowed", getsFollowed) // ich folge ihm
					.append("inserted_at", new Date());
			mongoCollection.insertOne(insertDoc);
			System.out.println("User wurde eingefügt!");
		} else {
			System.out.println("User ist schon in der Collection!");
		}
	}

	// Zeigt Anzahl und ScreenName der nicht mehr folgenden User an
	static void showUnfollowers(MongoCollection<Document> mongoCollection) {
		MongoCursor<Document> unFollowCursor;
		Document unFollowDoc;
		Bson filterStatus = new Document("isFollowing", false);
		unFollowCursor = mongoCollection.find(filterStatus).iterator();
		int unFollowerCount = 0;
		while (unFollowCursor.hasNext()) {
			unFollowDoc = unFollowCursor.next();
			System.out.println("User folgt jetzt nicht mehr!");
			unFollowerCount++;
		}
		System.out.println(unFollowerCount + " User folgen jetzt nicht mehr!");
	}

	// liefert ArrayList mit IDs der Follower eines beliebigen Accounts
	// @param userName: SCREENNAME!!!
	static ArrayList<Long> getFollowerIdArrayList(Twitter twitter, String userName) throws TwitterException {
		long cursor = -1;
		IDs followersIds = twitter.getFollowersIDs(userName, cursor);
		long[] idArray = followersIds.getIDs();
		// erzeuge ArrayList der IDs da sich mit dieser besser weiter arbeiten
		// lässt
		ArrayList<Long> idArrayList = new ArrayList<Long>();
		for (int i = 0; i < idArray.length; i++) {
			idArrayList.add(idArray[i]);
		}
		return idArrayList;
	}

	// follow einer Menge von Nutzern und gleichzeitig EINTRAGEN IN DB
	static void followUserSetAndInsertInColl(Twitter twitter, ArrayList<Long> userIDs,
			MongoCollection<Document> mongoCollection, long ownID) throws TwitterException, InterruptedException {
		// prüfen auf bereits gesandte Freundschaftsanfragen, deren Bestätigung
		// noch aussteht(pending)
		IDs pendingIDs = twitter.getOutgoingFriendships(-1);
		long[] pendingIdArray = pendingIDs.getIDs();
		ArrayList<Long> pendingArrayList = new ArrayList<Long>();
		for (int i = 0; i < pendingIdArray.length; i++) {
			pendingArrayList.add(pendingIdArray[i]);
		}
		ArrayList<Long> idsToFollow = new ArrayList<Long>();

		// prüfe IDs, denen gefolgt werden soll
		for (int i = 0; i < userIDs.size(); i++) {
			// ist die zu folgende ID meine eigene oder folge ich dem User bereits
			// oder eine Follow-Anfrage ist "pending", überspringen!
			// entfernt: || (whoIfollowArrayList.contains(userIDs.get(i))) s.o.!
			if ((userIDs.get(i) == ownID) || (pendingArrayList.contains(userIDs.get(i)))) {
				System.out.println("ownID or pending");
				continue;
			}
			// sonst ID in die Liste der zu folgenden IDs eintragen
			else {
				idsToFollow.add(userIDs.get(i));
			}
		}
	
		// folge allen IDs, die geprüft wurden und trage sie in DB ein
		for (int i = 0; i < idsToFollow.size(); i++) {
			twitter.createFriendship(idsToFollow.get(i));
			insertUserInDB(twitter.showUser(idsToFollow.get(i)), mongoCollection, false, true); // isFollowing:false,
																								// getsFollowed:true
			// warte zw. 3 und 7 Sekunden um evtl. Sperre zu vermeiden
			Thread.sleep(randomTime());
		}
		System.out.println(idsToFollow.size() + " neue User wurden in Collection gespeichert.");
	}

	 /**folge einer Anzahl Usern und trage sie in jeweilige Collection ein
	 je nach Aufgabe 2a,b,c in entspr.Collection! und jeweils mit evtl.
	 Zusatzaktion!
	 nutzt die Methode followUserSetAndInsertInColl
	 @param numberOfUsers: how many Users to follow
	 @param MongoCollection: coll2a, coll2b, coll2c oder coll300
	 @param mongoCollectionName:
	 coll2a: folge 100 Usern
	 coll2b: folge 100 Usern und like einen aktuellen Post
	 coll2c: folge 100 Usern, like einen aktuellen Post und kommentiere diesen
	 mit einem zufälligen Kommentar
	 oder jeweils *_f mit gefilterten Usern aus der filteredUserColl
	 @param userIDs: followerIDs des Influencer Accounts **/
	static void followManyAndInsertInColl(Twitter twitter, ArrayList<Long> userIDs, long ownID,
			MongoCollection<Document> mongoCollection, int numberOfUsers, String mongoCollectionName,
			ArrayList<String> commentList) throws TwitterException, InterruptedException {
		// in dieser Liste werden die UserIDs gesammelt, denen am Ende gefolgt
		// wird
		ArrayList<Long> idsToFollow = new ArrayList<Long>();

		// Liste derer, denen ich aktuell folge
		long cursor = -1;
		IDs friendsList = twitter.getFriendsIDs(ownID, cursor);
		long[] friendsArray = friendsList.getIDs();
		ArrayList<Long> friendsArrayList = new ArrayList<Long>();
		for (int i = 0; i < friendsArray.length; i++) {
			friendsArrayList.add(friendsArray[i]);
		}

		// bereinigte Liste erstellen ausschliesslich mit Usern, denen ich noch
		// nicht folge und die mehr als 5 tweets haben
		ArrayList<Long> clearedIDList = new ArrayList<Long>();
		for (int i = 0; i < userIDs.size(); i++) {
			if (!(friendsArrayList.contains(userIDs.get(i)))) {
				clearedIDList.add(userIDs.get(i));
			}
		}
		// Liste der IDs zum folgen erstellen (aus clearedIDList!)
		int i = 0;
		int clearedIDListLength = clearedIDList.size();
		if (clearedIDListLength == 0) {
			System.out.println("Allen Freunden des Accounts wird bereits gefolgt!");
		}

		// wird die 2.Bed. nicht geprüft, evtl. IndexOutOfBound!
		while ((idsToFollow.size() < numberOfUsers) && (clearedIDListLength > 0)) {
			// User kommt nur in die Liste wenn er mehr als 5 tweets hat
			// wird eigentlich schon mit dem Filter geprüft, aber manchmal gabs
			// doch Abstürze
			if (twitter.showUser(clearedIDList.get(i)).getStatusesCount() > 5) {
				idsToFollow.add(clearedIDList.get(i));
			}
			i++;
			clearedIDListLength--;
		}
		if (idsToFollow.size() < numberOfUsers) {
			System.out.println("Es konnten keine " + numberOfUsers
					+ " sinnvollen User (mehr als 5 tweets und noch ungefolgt) gefunden werden, Abbruch!");
			return;
		} else {
			System.out.println("100 sinnvolle User (mehr als 5 tweets und noch ungefolgt) wurden gefunden!");
		}
		// folge den Usern in idsToFollow, passiert immer, unabh. von Aufgabe
		followUserSetAndInsertInColl(twitter, idsToFollow, mongoCollection, ownID);

		// geliked wird bei 2b, 2c, 2b_f oder 2c_f
		if ((mongoCollectionName == "coll2b") || (mongoCollectionName == "coll2c")
				|| (mongoCollectionName == "coll2b_f") || (mongoCollectionName == "coll2c_f")) {
			User user;
			Status status;
			String comment;
			StatusUpdate statusUpdate;
			for (int j = 0; j < idsToFollow.size(); j++) {
				user = twitter.showUser(idsToFollow.get(j));
				// aktueller Status des Users
				status = user.getStatus();
				// warte zw. 3 und 7 Sekunden um evtl. Sperre zu vermeiden
				Thread.sleep(randomTime());
				// like aktuellen Status
				try {
					twitter.createFavorite(status.getId());
					System.out.println("Tweet wurde geliked.");
				} catch (java.lang.NullPointerException e) {
					System.out.println("User hat noch nicht getweetet, liken unmöglich! (bzw. privates Profil???");
					continue;
				} catch (TwitterException e) {
					System.out.println("Tweet wurde bereits geliked, continue!");
					continue;
				}
				// if 2c oder 2c_f: zusätzlich wird der aktuelle Status des
				// aktuellen Users kommentiert
				if ((mongoCollectionName == "coll2c") || (mongoCollectionName == "coll2c_f")) {
					comment = Comment.randomComment(commentList);
					statusUpdate = new StatusUpdate(comment + "@" + user.getScreenName());
					statusUpdate.setInReplyToStatusId(status.getId());
					twitter.updateStatus(statusUpdate);
					System.out.println("Tweet wurde kommentiert.");
				}
			}
		}
	}

	// liefert eine zufällige Zeit, die gewartet wird zw. 3 und 7 Sekunden
	static int randomTime() {
		// mind. 3000 ms
		int randomTime = 3000;
		int randomAdd = (((int) (Math.random() * 100)) % 5) * 1000;
		return randomTime + randomAdd;
	}

	// prüft die Einträge einer Collection darauf, ob sie meinem Account folgen
	// setzt isFollowing auf true
	static void checkAndSetIsFollowing(Twitter twitter, MongoCollection<Document> mongoCollection)
			throws IllegalStateException, TwitterException {
		ArrayList<Long> followerAtTheMoment = getFollowerIdArrayList(twitter,
				twitter.showUser(twitter.getId()).getScreenName());

		MongoCursor<Document> cursor = mongoCollection.find().iterator();
		Document userDoc;
		Long userID;
		int newFollower = 0;
		int followersInColl = 0;
		boolean userIsFollowingState;

		while (cursor.hasNext()) {
			userDoc = cursor.next();
			userID = userDoc.getLong("User_ID");
			userIsFollowingState = userDoc.getBoolean("isFollowing");

			if (followerAtTheMoment.contains(userID)) {
				followersInColl++;
				if (userIsFollowingState == false) {
					Bson newValue = new Document("isFollowing", true);
					Bson updateOperationDocument = new Document("$set", newValue);
					mongoCollection.updateOne(userDoc, updateOperationDocument);
					newFollower++;
				}
			}
		}
		System.out.println(newFollower + " neue User dieser Coll. folgen jetzt.");
		System.out.println(followersInColl + " Followers in der Coll. insgesamt.");
	}

	// prüft die Einträge einer Collection darauf, ob ich ihnen folge
	// setz getsFollowed auf true
	static void checkAndSetGetsFollowed(Twitter twitter, MongoCollection<Document> mongoCollection)
			throws IllegalStateException, TwitterException {

		ArrayList<Long> friendsAtTheMoment = new ArrayList<Long>();
		IDs friendsList = twitter.getFriendsIDs(twitter.getId(), -1);
		long[] friendsArray = friendsList.getIDs();
		for (int i = 0; i < friendsArray.length; i++) {
			friendsAtTheMoment.add(friendsArray[i]);
		}

		MongoCursor<Document> cursor = mongoCollection.find().iterator();
		Document userDoc;
		Long userID;
		int newFriend = 0;
		int friendsInColl = 0;
		boolean userGetsFollowedState;

		while (cursor.hasNext()) {
			userDoc = cursor.next();
			userID = userDoc.getLong("User_ID");
			userGetsFollowedState = userDoc.getBoolean("getsFollowed");

			if (friendsAtTheMoment.contains(userID)) {
				friendsInColl++;
				if (userGetsFollowedState == false) {
					Bson newValue = new Document("getsFollowed", true);
					Bson updateOperationDocument = new Document("$set", newValue);
					mongoCollection.updateOne(userDoc, updateOperationDocument);
					newFriend++;
				}
			}
		}
		System.out.println(newFriend + " neue Friends wurden in dieser Coll. gefunden.");
		System.out.println("Insgesamt wird " + friendsInColl + " Usern dieser Coll. gefolgt.");
	}

	// entfolge einem Nutzer der nach 3 Tagen immer noch nicht zurück folgt
	// und setze follow status in DB auf false
	static void unfollowWhoNotFollowsBack(Twitter twitter, MongoCollection<Document> mongoCollection, int tage)
			throws TwitterException {
		int count = 0;
		MongoCursor<Document> cursor;
		Date insertDate;
		Date dateNow = new Date();
		long id;
		boolean isFollowing;
		Document userDoc;
		cursor = mongoCollection.find().iterator();
		while (cursor.hasNext()) {
			userDoc = cursor.next();
			insertDate = userDoc.getDate("inserted_at");
			id = userDoc.getLong("User_ID");
			isFollowing = userDoc.getBoolean("isFollowing");
			// sind mehr als 3 Tage seit dem folgen vergangen und der User folgt
			// nicht zurück->unfollow
			if (((double) (dateNow.getTime() - insertDate.getTime()) / 86400000) > tage) {
				if (!isFollowing) {
					twitter.destroyFriendship(id);
					// setze followStatus des Users auf false
					Bson filter = new Document("User_ID", id);
					Bson newValue = new Document("getsFollowed", false);
					Bson updateOperationDocument = new Document("$set", newValue);
					mongoCollection.updateOne(filter, updateOperationDocument);
					count++;
				}
			}
		}
		System.out.println(count + " User wurden unfollowed!");
	}

	// prüft, ob ein Account zuletzt vor einer Mindestzahl an Tagen gepostet hat
	// und seine Ratio Follower/Following
	// ausserde, ob er mind. 10 tweets hat
	static boolean filterForActions(Twitter twitter, Long userID, double lowerlimit, double upperlimit, int tweetCounts,
			int tage) throws TwitterException {
		boolean timeSatisfied = false;
		boolean ratioSatisfied = false;
		User user;
		Date date;
		double days;
		double ratio;
		DecimalFormat f = new DecimalFormat("#0.00");
		try {
			user = twitter.showUser(userID);
		} catch (TwitterException e) {
			System.out.println("Profil existiert nicht mehr!");
			return false;
		}

		try {
			// hat der User mindestens tweets?
			if (user.getStatusesCount() > tweetCounts) {

				System.out.println("status count im Limit");
				// hier manchmal ABSTURZ wg. NullPointerException! Privates Profil
				date = user.getStatus().getCreatedAt();
				days = (((double) (new Date().getTime() - date.getTime())) / 86400000);
				// Anzahl Tage nicht 3 sondern 5! Da sonst zu wenige User gefunden werden!
				if (days < tage) {
					System.out.println("Letzer tweet ist " + f.format(days) + " Tage alt, im Limit.");
					timeSatisfied = true;
				}
			} else {
				// ohne Mindestanzahl tweets kann direkt false zurückgegeben werden
				System.out.println("Kein Post, return false!");
				return false;
			}
		} catch (java.lang.NullPointerException e) {
			System.out.println("NullPointerException in Methode filterForActions, privates Profil!");
			System.out.println("return false, User übersprungen");
			return false;
		}
		// prüfe ob getFriendsCount !=0 ist!
		if (user.getFriendsCount() == 0) {
			System.out.println("NULL Freunde!");
			return false;
		}

		// prüfe User Ratio Follower/Following
		ratio = ((double) user.getFollowersCount()) / ((double) user.getFriendsCount());
		// die Ratio ist nicht wie in der Aufgabe da sonst so gut wie kein User
		// gefunden wird und ich nach ewigem suchen irgendwann ins Rate-Limit
		// komme!
		if ((lowerlimit < ratio) && (ratio < upperlimit)) {
			System.out.println("Ratio: " + ratio + ", im Limit!");
			ratioSatisfied = true;
		}
		return (timeSatisfied && ratioSatisfied);
	}

	// filtere User eines Influencer-Accounts und speichere diese in
	// filteredUserColl
	// @param userIDs: followerIDs des Influencer Accounts
	static void filterUsersAndSaveInDB(MongoCollection<Document> mongoCollection, Twitter twitter,
			ArrayList<Long> userIDs, double lowerlimit, double upperlimit, int tweetCounts,
			int tage) throws TwitterException {

		int count = 0;
		if (userIDs.size() < 100) {
			System.out.println("Der Influencer hat weniger als 100 Follower, Abbruch!");
			return;
		}

		// Anzahl zu findender User,die den Filter erfüllen
		while (count < 200) {
			// es wird immer der 1. User der Liste geprüft, danach sofort
			// gelöscht!
			if (filterForActions(twitter, userIDs.get(0), lowerlimit, upperlimit, tweetCounts, tage )) {
				// User wird in filteredUserColl gespeichert
				// isFollowing und getsFollowed = false, muss später noch
				// geprüft/gesetzt werden!
				System.out.println("Filter erfüllt, User wird in der Collection gespeichert!");
				insertUserInDB(twitter.showUser(userIDs.get(0)), mongoCollection, false, false);
				count++;
			}
			userIDs.remove(0);

			if (userIDs.isEmpty()) {
				System.out.println("Alles durchsucht, Abbruch!");
				System.out.println("Es wurden " + count + " User herausgefiltert.");
				return;
			}
		}
		System.out.println("Es wurden " + count + " User herausgefiltert.");
	}

	// suche aus einer Collection 100 User mit isFollowing=false=getsFollowed)
	static ArrayList<Long> findFilteredUsersForAction(Twitter twitter, MongoCollection<Document> mongoCollection)
			throws IllegalStateException, TwitterException {
		MongoCursor<Document> cursor;
		Document userDoc;
		cursor = mongoCollection.find().iterator();
		ArrayList<Long> fineUsers = new ArrayList<Long>();
		while (cursor.hasNext() && fineUsers.size() < 100) {
			userDoc = cursor.next();
			if (!(userDoc.getBoolean("isFollowing")) && !(userDoc.getBoolean("getsFollowed"))) {
				fineUsers.add(userDoc.getLong("User_ID"));
			}
		}

		if (fineUsers.size() < 100) {
			System.out.println("Es wurden nur noch " + fineUsers.size() + " fineUsers gefunden, "
					+ "collection muss erweitert werden! Abbruch");
			System.exit(0);
		} else {
			System.out.println(fineUsers.size() + " User wurden erfolgreich extrahiert.");
		}
		return fineUsers;
	}

	// lies die Follower/Following Ratio aus einer Collection
	static double getDBRatio(MongoCollection<Document> mongoCollection) {
		int followerCount = 0;
		int friendsCount = 0;

		MongoCursor<Document> cursor;
		Document userDoc;
		cursor = mongoCollection.find().iterator();
		while (cursor.hasNext()) {
			userDoc = cursor.next();
			if (userDoc.getBoolean("isFollowing", true)) {
				followerCount++;
			}
			if (userDoc.getBoolean("getsFollowed", true)) {
				friendsCount++;
			} 
		} if(friendsCount == 0){
			System.out.println("Keine neuen User dieser Coll. folgen jetzt.");
			return 0;
		}
		return (double) (followerCount / friendsCount);
	}
}
