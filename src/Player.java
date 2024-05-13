import java.util.*;
import java.util.stream.Collectors;

public class Player {
    private final int playerId;
    private final Thread playerThread;
    private final ArrayList<Card> hand;
    private final OldMaidGame game;

    public Player(int playerId, OldMaidGame game) {
        this.playerId = playerId;
        this.playerThread = new Thread(new PlayerRunnable());
        this.hand = new ArrayList<>();
        this.game = game;
        this.playerThread.start();
    }
    public int getPlayerId() {
        return playerId;
    }
    public ArrayList<Card> getHand() {
        return hand;
    }
    public void addCard(Card card) {
        hand.add(card);
    }
    public void playTurn() {
        synchronized (game) {
            game.notifyAll();
        }
    }
    public Card takeRandomCardFrom(Player otherPlayer) {
        synchronized (otherPlayer) {
            List<Card> nonEmptyCards = otherPlayer.hand.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!nonEmptyCards.isEmpty()) {
                Random random = new Random();
                int randomIndex = random.nextInt(nonEmptyCards.size());
                Card removedCard = nonEmptyCards.remove(randomIndex);
                otherPlayer.hand.remove(removedCard);
                return removedCard;
            }
        }

        return null;
    }

    public void throwMatchingPairs() {
        List<Card> cardsToRemove = new ArrayList<>();
        Set<String> removedRanks = new HashSet<>();
        int pairsRemoved = 0;

        for (int i = 0; i < hand.size() && pairsRemoved < 2; i++) {
            Card currentCard = hand.get(i);
            if (currentCard != null && !removedRanks.contains(currentCard.getRank())) {
                for (int j = i + 1; j < hand.size() && pairsRemoved < 2; j++) {
                    Card otherCard = hand.get(j);
                    if (otherCard != null && !removedRanks.contains(otherCard.getRank()) && isMatchingPair(currentCard, otherCard)) {
                        cardsToRemove.add(currentCard);
                        cardsToRemove.add(otherCard);
                        removedRanks.add(currentCard.getRank());
                        removedRanks.add(otherCard.getRank());
                        pairsRemoved++;
                    }
                }
            }
        }

        hand.removeAll(cardsToRemove);
    }
    private boolean isMatchingPair(Card card1, Card card2) {
        return card1 != null && card2 != null &&
                card1.getRank().equals(card2.getRank());
    }
    public boolean hasPairs() {
        for (int i = 0; i < hand.size(); i++) {
            Card currentCard = hand.get(i);
            if (currentCard != null) {
                for (int j = i + 1; j < hand.size(); j++) {
                    Card otherCard = hand.get(j);
                    if (otherCard != null && isMatchingPair(currentCard, otherCard)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public boolean hasJoker() {
        for (Card card : hand) {
            if (card != null && card.getSuit().equals("Joker") && card.getRank().equals("Joker")) {
                return true;
            }
        }
        return false;
    }
    public void join() {
        try {
            playerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private class PlayerRunnable implements Runnable {

        @Override
        public void run() {
            while (!game.isGameOver()) {
                playTurn();
            }
        }
    }
}
