package TwitterPrak.twitterprak;

import java.util.Scanner;

public class Mother {
	public static void main(String[] args) {
		// create a scanner so we can read the command-line input
		Scanner scanner = new Scanner(System.in);

		System.out.print("Schwellwerte für Following/Followee Ratio: ");
		String username = scanner.next();

		System.out.print("Mindestanzahl Posts: ");
		int age = scanner.nextInt();
		
		System.out.print("Mindestanzahl Posts: ");
		int posts = scanner.nextInt();
		
		System.out.print("Alter des letzten Posts (Tage): ");
		int tage = scanner.nextInt();

		System.out.println(String.format("%s, your age is %d", username, age));
		
	}
}
