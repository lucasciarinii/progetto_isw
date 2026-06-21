package it.polimi.ingsw.network.snapshots;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.server.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.server.model.cards.characters.*;
import it.polimi.ingsw.server.model.cards.characters.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable snapshot of a player's public state.
 */
@SuppressWarnings("ClassCanBeRecord")
public class PlayerSnapshot implements Serializable {
    private final String nickname;
    private final int food;
    private final int points;
    private final int discountOnBuilding;
    private final List<Hunter> ownedHunters;
    private final List<Gatherer> ownedGatherers;
    private final List<Builder> ownedBuilders;
    private final List<Shaman> ownedShamans;
    private final List<Artist> ownedArtists;
    private final List<Inventor> ownedInventors;
    private final List<BuildingCard> ownedBuildings;


    @JsonCreator
    public PlayerSnapshot(
            @JsonProperty("nickname") String nickname,
            @JsonProperty("food") int food,
            @JsonProperty("points") int points,
            @JsonProperty("discountOnBuilding") int discountOnBuilding,
            @JsonProperty("ownedHunters") List<Hunter> hunters,
            @JsonProperty("ownedGatherers") List<Gatherer> gatherers,
            @JsonProperty("ownedBuilders") List<Builder> builders,
            @JsonProperty("ownedShamans") List<Shaman> shamans,
            @JsonProperty("ownedArtists") List<Artist> artists,
            @JsonProperty("ownedInventors") List<Inventor> inventors,
            @JsonProperty("ownedBuildings") List<BuildingCard> ownedBuildings) {
        this.nickname = nickname;
        this.food = food;
        this.points = points;
        this.discountOnBuilding = discountOnBuilding;
        this.ownedHunters = new ArrayList<>(hunters);
        this.ownedGatherers = new ArrayList<>(gatherers);
        this.ownedBuilders = new ArrayList<>(builders);
        this.ownedShamans = new ArrayList<>(shamans);
        this.ownedArtists = new ArrayList<>(artists);
        this.ownedInventors = new ArrayList<>(inventors);
        this.ownedBuildings = ownedBuildings;
    }


    /** @return the player's nickname */
    public String getNickname() {
        return nickname;
    }

    /** @return the player's food count */
    public int getFood() {
        return food;
    }

    /** @return the player's points */
    public int getPoints() {
        return points;
    }

    /** @return the player's building discount */
    public int getDiscountOnBuilding() { return discountOnBuilding; }

    /** @return the owned hunters */
    public List<Hunter>       getOwnedHunters()   { return ownedHunters; }
    /** @return the owned gatherers */
    public List<Gatherer>     getOwnedGatherers() { return ownedGatherers; }
    /** @return the owned builders */
    public List<Builder>      getOwnedBuilders()  { return ownedBuilders; }
    /** @return the owned shamans */
    public List<Shaman>       getOwnedShamans()   { return ownedShamans; }
    /** @return the owned artists */
    public List<Artist>       getOwnedArtists()   { return ownedArtists; }
    /** @return the owned inventors */
    public List<Inventor>     getOwnedInventors() { return ownedInventors; }
    /** @return the owned building cards */
    public List<BuildingCard> getOwnedBuildings() { return ownedBuildings; }

    /**
     * Prints all owned cards to standard output.
     */
    public void printAllCards() {
        ownedHunters.forEach(System.out::println);
        ownedGatherers.forEach(System.out::println);
        ownedBuilders.forEach(System.out::println);
        ownedShamans.forEach(System.out::println);
        ownedArtists.forEach(System.out::println);
        ownedInventors.forEach(System.out::println);
        ownedBuildings.forEach(System.out::println);
    }
}
