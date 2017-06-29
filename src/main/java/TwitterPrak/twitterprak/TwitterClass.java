package TwitterPrak.twitterprak;

import java.util.ArrayList;

import java.util.List;

import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterClass {

	// Timeline anzeigen
	public static void showTimeline(Twitter t) throws TwitterException {
		List<Status> status = t.getHomeTimeline();
		for (Status st : status) {
			System.out.println(st.getUser().getName() + st.getText());
		}
	}

	// liefert ResponseList<User> der Follower
	public static ResponseList<User> getFollowerList(Twitter twitter) throws IllegalStateException, TwitterException {
		long followerCursor = -1;
		// A data interface representing array of numeric IDs.
		IDs followerIds;
		ResponseList<User> followerList;
		do {
			followerIds = twitter.getFollowersIDs(twitter.getId(), followerCursor);
			followerList = twitter.lookupUsers(followerIds.getIDs());
		} while ((followerCursor = followerIds.getNextCursor()) != 0);
		return followerList;
	}

	// liefert ArrayList aller Tweets
	public static ArrayList<Status> collectStatuses(Twitter twitter) throws IllegalStateException, TwitterException {
		ArrayList<Status> statusList = new ArrayList<Status>();
		int pageNumber = 1;
		while (true) {
			try {
				int size = statusList.size();
				Paging page = new Paging(pageNumber++, 100);
				statusList.addAll(twitter.getUserTimeline(twitter.getScreenName(), page));
				if (statusList.size() == size) {
					break;
				}
			} catch (TwitterException e) {
				e.printStackTrace();
			}
		}
		return statusList;
	}

	public static void showList(Twitter twitter, ArrayList<Long> list) throws TwitterException {
		if(list.isEmpty()) {
			System.out.println("Keine Listeneinträge!");
		}
		for (int i = 0; i < list.size(); i++) {
			System.out.println(twitter.showUser(list.get(i)).getScreenName());
		}
		System.out.println(list.size() + " Listeneinträge.");
	}

	// liefert Gesamtzahl aller Retweets
	public static int countRetweets(ArrayList<Status> statusList) {
		int count = 0;
		for (int i = 0; i < statusList.size(); i++) {
			count = count + statusList.get(i).getRetweetCount();
		}
		return count;
	}

	// liefert Gesamtzahl likes (nur 52 obwohl ich 56 habe!?)
	public static int countLikes(ArrayList<Status> statusList) throws IllegalStateException, TwitterException {
		int likes = 0;
		for (int i = 0; i < statusList.size(); i++) {
			likes = likes + statusList.get(i).getFavoriteCount();
		}
		return likes;
	}

	// liefert Anzahl Mentions, geht aber nur bis 100 und ist bereits erreicht
	private static int countMentions(Twitter twitter, User user) throws TwitterException {
		Query query = new Query("@" + user.getScreenName());
		// Maximum ist 100!
		query.setCount(100);
		QueryResult search = twitter.search(query);
		return search.getCount();
	}

}
