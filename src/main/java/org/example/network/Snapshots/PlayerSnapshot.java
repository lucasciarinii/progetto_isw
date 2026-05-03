package org.example.network.Snapshots;

import org.example.server.model.cards.buildingCards.BuildingCard;
import org.example.server.model.cards.characters.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    private final List<BuildingCard> ownedBuildings; // List of building cards owned by the player (can be empty)


    public PlayerSnapshot(String nickname, int food, int points, int discountOnBuilding, List<Hunter> hunters, List<Gatherer> gatherers, List<Builder> builders, List<Shaman> shamans, List<Artist> artists, List<Inventor> inventors, List<BuildingCard> ownedBuildings) {
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


    public String getNickname() {
        return nickname;
    }

    public int getFood() {
        return food;
    }

    public int getPoints() {
        return points;
    }

    public int getDiscountOnBuilding() { return discountOnBuilding; }

    public List<Hunter>       getOwnedHunters()   { return ownedHunters; }
    public List<Gatherer>     getOwnedGatherers() { return ownedGatherers; }
    public List<Builder>      getOwnedBuilders()  { return ownedBuilders; }
    public List<Shaman>       getOwnedShamans()   { return ownedShamans; }
    public List<Artist>       getOwnedArtists()   { return ownedArtists; }
    public List<Inventor>     getOwnedInventors() { return ownedInventors; }
    public List<BuildingCard> getOwnedBuildings() { return ownedBuildings; }

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
