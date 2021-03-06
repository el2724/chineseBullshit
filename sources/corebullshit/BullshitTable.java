
package corebullshit;

import java.util.*;
import structures.*;

public class BullshitTable extends Table
{
    // fields
    private ArrayList<Card> pile;
    //private int lastPlayer;
    //private int nextPlayer;
    private BullshitPlayer lastPlayer;
    private BullshitPlayer nextPlayer;
    private RosterIterator<Player> turnIterator;
    // gameplay values
    private boolean canCallBS;
    private int numCardsPlayed;
    private Rank rankCalled;
    private ArrayList<BullshitPlayer> winners;

    // constructors
    public BullshitTable(int numPlayers, int numDecks){
        super(numDecks);
        this.pile = new ArrayList<Card>();
        // add the BullshitPlayers
        for (int i=0; i<numPlayers; i++){
            super.addPlayer(new BullshitPlayer(this));
        }
        this.winners = new ArrayList<BullshitPlayer>();
        this.turnIterator = super.iterator();
    }

    // methods
    public void resetTable(int numDecks, BullshitPlayer startingPlayer){
        super.resetDeck(numDecks);
        this.pile.clear();
        // add any winners (removed from game) back
        for (BullshitPlayer player : this.winners){
            super.addPlayer(player);
        }
        this.winners.clear();
        this.canCallBS = false;
        this.numCardsPlayed = 0;
        this.rankCalled = null;
        this.lastPlayer = startingPlayer;
        this.nextPlayer = startingPlayer;
        this.turnIterator = super.iterator(startingPlayer);
    }

    public void resetTable(int numDecks){
        this.resetTable(numDecks, (BullshitPlayer)this.turnIterator.next());
    }

    public void resetTable(){
        this.resetTable(super.getNumDecks());
    }

    public boolean verify(){
        // returns true if the cards are consistent with the call,
        // aka if the challenge fails
        for (int i=pile.size()-1; i>pile.size()-1-this.numCardsPlayed; i--){
            if (pile.get(i).getRank() != rankCalled){
                return false;
            }
        }
        return true;
    }

    public ArrayList<Card> peekPile(){
        ArrayList<Card> topCards = new ArrayList<Card>();
        for (int i=pile.size()-1; i>pile.size()-1-this.numCardsPlayed; i--){
            topCards.add(pile.get(i));
        }
        return topCards;
    }

    public ArrayList<Card> clearPile() {
        ArrayList<Card> cards = this.pile;
        this.pile = new ArrayList<Card>();
        return cards;
    }

    public boolean isCallBS() {return this.canCallBS;}// currently unused
    public BullshitPlayer getLastPlayer() {return this.lastPlayer;}
    public BullshitPlayer getNextPlayer() {return this.nextPlayer;}
    public int getSizePile() {return this.pile.size();}
    public int getSizeCalled() {return this.numCardsPlayed;}
    public Rank getRankCalled() {return this.rankCalled;}
    public int numWinners() {return this.winners.size();}
    public ArrayList<BullshitPlayer> getWinners(){return this.winners;}
   

    // gameplay logic methods
    public boolean nextTurnChallenged(BullshitPlayer challenger) throws InvalidMoveException {
        // takes care of turn mechanics after bullshit is called
        // return true if the challenge is successful;
        // return false if cards on table are consistent with the call
        if (this.canCallBS == false){
            throw new InvalidMoveException("Can't call BS right now!");
        }
        if (!super.hasPlayer(challenger)){
            throw new InvalidMoveException("Player not at table!");
        }
        boolean isConsistent = verify();
        Player loser;
        // set the winner to be the next player
        if (isConsistent){
            // the challenge fails - give turn to accused
            //  (unless accused has won due to this challenge)
            loser = challenger;
            if (this.lastPlayer.sizeHand() == 0){
                // turn logic if someone is now out of cards
                this.winners.add(this.removeLastPlayer());
                if (this.nextPlayer == challenger){
                    // skip this loser's turn
                    this.shiftTurn();
                }
            }
            else {
                // turn logic if nobody has just won
                this.nextPlayer = this.lastPlayer;
            }
        }
        else {
            // the challenge stands - give turn to challenger
            loser = this.lastPlayer;
            this.nextPlayer = challenger;
        }
        // make the loser take all the cards
        loser.takeCards(this.clearPile());

        // reset gameplay values
        this.canCallBS = false;
        this.numCardsPlayed = 0;
        this.rankCalled = null;

        return !isConsistent;
    }

    public void placeCards(Player player)
        throws InvalidMoveException, IndexOutOfBoundsException{

        if (player != this.nextPlayer){
            throw new InvalidMoveException("Not your turn!");
        }
        int previousSize = this.pile.size();
        // note that this also can throw an exception
        this.pile.addAll(player.playCards());
        // update gameplay values, update turns
        if (this.canCallBS == true){
            // check if any player won
            if (this.lastPlayer.sizeHand() == 0){
                this.winners.add(this.removeLastPlayer());
            }
            // continue the play
            this.canCallBS = false;
            this.numCardsPlayed = this.pile.size()-previousSize;
        }
        else{
            this.numCardsPlayed += this.pile.size()-previousSize;
        }
    }

    public BullshitPlayer removeLastPlayer(){
        BullshitPlayer player = (BullshitPlayer)super.removePlayer(this.lastPlayer);
        this.lastPlayer = this.nextPlayer;
        return player;
    }

    public void chooseRank(Player player, Rank rank) throws InvalidMoveException{
        if (player != this.nextPlayer){
            throw new InvalidMoveException("Not your turn!");
        }
        if (this.rankCalled != null) {
            // aka the chinese bullshit condition
            throw new InvalidMoveException("Can't choose new rank now!");
        }
        this.rankCalled = rank;
    }

    public void shiftTurn(){
        this.lastPlayer = this.nextPlayer;
        this.nextPlayer = (BullshitPlayer) this.turnIterator.next();
    }

    public void nextTurnPlayed(Player player) throws InvalidMoveException{
        // takes care of turn mechanics after cards have been played
        
        if (player != this.nextPlayer){
            throw new InvalidMoveException("Not your turn!");
        }
        if (this.canCallBS == true) {
            throw new InvalidMoveException(
                    "Can't continue without playing or calling!");
        }
        if (this.rankCalled == null){
            throw new InvalidMoveException("No rank called!");
        }
        // update turns once a player calls the rank of his cards
        this.shiftTurn();
        this.canCallBS = true;
    }

}


