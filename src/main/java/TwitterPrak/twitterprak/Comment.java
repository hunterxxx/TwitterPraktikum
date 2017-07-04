package TwitterPrak.twitterprak;

import java.util.ArrayList;
import java.util.Arrays;

public class Comment {
	// liefert einen zufälligen Kommentar aus der commentList
	static String randomComment(ArrayList<String> commentList) {
		int count = commentList.size();
		int random = (int) (Math.random() * 100);
		int commentNumber = random % count;
		return commentList.get(commentNumber);
	}
	
	static ArrayList<String> commentList(){
		ArrayList<String> commentList = new ArrayList<String>();
		commentList.addAll(Arrays.asList("Super!", "Schönes Bild!", "Wunderbar!", "Cool!", "Top!", "Wahnsinnig cool!",
				"Ich mag das total!", "Fantastisch!", "Wahnsinnig cool!", "Lässig!", "Sieht echt gut aus!",
				"Sehr gelungenes Bild!", "Stark!", "Echt gut getroffen!", "Super Foto!", "Was für ein Foto!",
				"Gefällt mir wahnsinnig gut!", "Grossartig!"));
		return commentList;
	}
}
