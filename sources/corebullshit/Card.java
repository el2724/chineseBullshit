
package corebullshit;

public class Card
{
    private Suit suit;
    private Rank rank;

    public Card(Suit suit, Rank rank){
        this.suit = suit;
        this.rank = rank;
    }

    public Rank getRank(){
        return this.rank;
    }

    public Suit getSuit(){
        return this.suit;
    }

    @Override public String toString(){
        return this.rank.toString() + this.suit.toString();
    }
}
