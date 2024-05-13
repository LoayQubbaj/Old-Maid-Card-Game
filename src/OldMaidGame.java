import java.util.ArrayList;

public class OldMaidGame {
    private final int numPlayers;
    private final ArrayList<Player> players;
    private final Deck deck;
    private boolean isTurnComplete;

    public OldMaidGame(int numPlayers) {
        this.numPlayers = numPlayers;
        this.players = new ArrayList<>();
        this.deck = new Deck();
        this.isTurnComplete = false;

        initializePlayers();
    }
    private void initializePlayers() {
        for (int i = 1; i <= numPlayers; i++) {
            Player player = new Player(i, this);
            players.add(player);
        }
    }
    public void startGame() {
        deck.shuffle();
        dealCards();

        while (!isGameOver()) {
            playRound();

            try {
                // delay to make the game more visible
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (this) {
                isTurnComplete = true;
                notifyAll();
            }
        }

        synchronized (this) {
            isTurnComplete = true;
            notifyAll();
        }

        for (Player player : players) {
            player.join();
        }

        reportResults();
    }
    private void dealCards() {
        int currentPlayerIndex = 0;

        while (deck.getSize() > 0) {
            Player currentPlayer = players.get(currentPlayerIndex);
            Card card = deck.dealCard();

            if (card != null) {
                currentPlayer.addCard(card);
                currentPlayerIndex = (currentPlayerIndex + 1) % numPlayers;
            }
        }
    }
    private void playRound() {
        boolean pairsThrown;

        do {
            // Check for pairs
            pairsThrown = checkForPairs();
            displayGameState();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Remove pairs from all players' hands
            throwMatchingPairs();

        } while (pairsThrown);
        if (isGameOver()) {
            reportResults();
            return;
        }

        // Passing a random card to the next player
        for (int i = 0; i < numPlayers; i++) {
            Player currentPlayer = players.get(i);
            Player nextPlayer = players.get((i + 1) % numPlayers);
            Card randomCard = nextPlayer.takeRandomCardFrom(currentPlayer);// Take a card
            if (randomCard != null) {
                nextPlayer.addCard(randomCard);
                displayGameState();
                nextPlayer.throwMatchingPairs();
            }

            displayGameState();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Notify players to finish their turns
        synchronized (this) {
            isTurnComplete = true;
            notifyAll();
        }

        for (Player player : players) {
            player.join();
        }

        // Check if the game is over
        if (!isGameOver()) {
            synchronized (this) {
                isTurnComplete = false;
            }
        } else {
            reportResults();
        }
    }
    private boolean checkForPairs() {
        for (Player player : players) {
            if (player.hasPairs()) {
                return true;
            }
        }
        return false;
    }
    public boolean isGameOver() {
        int playersWithJoker = 0;

        for (Player player : players) {
            if (player.hasJoker()) {
                playersWithJoker++;
            }
        }
        // Check if all players except the one with the Joker have empty hands
        for (Player player : players) {
            if (!player.hasJoker() && !player.getHand().isEmpty()) {
                return false;
            }
        }

        return playersWithJoker == 1;
    }
    private void displayGameState() {
        for (Player player : players) {
            System.out.println("Player " + player.getPlayerId() + " Hand: " + player.getHand());
        }
        System.out.println("-------------------------------");
    }
    private void throwMatchingPairs() {
        for (Player player : players) {
            player.throwMatchingPairs();
        }
    }
    private void reportResults() {
        System.out.println("Game Over!");
        Player loser = findLoser();
        System.out.println("Player " + loser.getPlayerId() + " is the Loser!");

    }
    private Player findLoser() {
        for (Player player : players) {
            if (player.hasJoker()) {
                return player;
            }
        }
        return null;
    }
    public static void main(String[] args) {
        int numPlayers = 4;
        OldMaidGame game = new OldMaidGame(numPlayers);
        game.startGame();
    }
}
