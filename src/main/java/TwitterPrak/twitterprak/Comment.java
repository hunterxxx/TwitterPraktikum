package TwitterPrak.twitterprak;

import java.util.ArrayList;

public class Comment {
	// liefert einen zufälligen Kommentar aus der commentList
	static String randomComment(ArrayList<String> commentList) {
		int count = commentList.size();
		int random = (int) (Math.random() * 100);
		int commentNumber = random % count;
		return commentList.get(commentNumber);
	}
}
