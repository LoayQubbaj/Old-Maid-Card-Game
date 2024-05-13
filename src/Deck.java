import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    public final ArrayList<Card> cardList = new ArrayList<>();

    public Deck() {
        initializeDeck();
        shuffle();
    }
    private void initializeDeck() {
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};

        for (String suit : suits) {
            for (String rank : ranks) {
                cardList.add(new Card(suit, rank));
            }
        }
        cardList.add(new Card("Joker", "Joker"));
    }
    public void shuffle() {
        Collections.shuffle(cardList);
    }
    public Card dealCard() {
        if (!cardList.isEmpty()) {
            return cardList.remove(cardList.size() - 1);
        }
        return null;
    }
    public int getSize() {
        return cardList.size();
    }
}
