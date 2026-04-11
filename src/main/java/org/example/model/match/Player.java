package org.example.model.match;

import org.example.model.cards.Card;
import org.example.model.cards.buildingCards.BuildingCard;
import org.example.model.cards.characters.*;
import org.example.model.cards.characters.Character;
import org.example.model.interfaces.Visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class Player  {

    private final String nickname;
    private final List<BuildingCard> ownedBuildings = new ArrayList<>();
    private final List<Inventor> inventors = new ArrayList<>();
    private final List<Gatherer> gatherers = new ArrayList<>();
    private final List<Shaman> shamans = new ArrayList<>();
    private final List<Builder> builders = new ArrayList<>();
    private final List<Artist> artists = new ArrayList<>();
    private final List<Hunter> hunters = new ArrayList<>();

    private int points = 0;
    private int food = 0;
    private int discountOnSustenance = 0;
    private int discountOnBuilding = 0;
    private int shamanStars = 0;

    private final Visitor addToListVisitor = new Visitor() {
        // Every override adds the selected Character to the appropriate list
        @Override
        public void visit(Inventor inventor) {
            inventors.add(inventor);
        }

        @Override
        public void visit(Gatherer gatherer) {
            gatherers.add(gatherer);
            discountOnSustenance += 3;
        }

        @Override
        public void visit(Shaman shaman) {
            shamans.add(shaman);
        }

        @Override
        public void visit(Builder builder) {
            builders.add(builder);
            addDiscountOnBuilding(-builder.getDiscountBuilding());
        }

        @Override
        public void visit(Artist artist) {
            artists.add(artist);
        }

        @Override
        public void visit(Hunter hunter) {
            hunters.add(hunter);
        }

        @Override
        public void visit(BuildingCard building) {
            ownedBuildings.add(building);
        }
    };

    public Player(String nickname) {
        this.nickname = Objects.requireNonNull(nickname, "Nickname cannot be null");
    }

    public String getNickname() {
        return nickname;
    }


    public int getPoints() {
        return points;
    }


    public void addPoints(int points) {
        this.points += points;
    }


    public int getFood() {
        return food;
    }


    public void addFood(int food) {
        this.food += food;
    }


    public int getDiscountOnSustenance() {
        return discountOnSustenance;
    }


    public void addDiscountOnSustenance(int discountOnSustenance) {

        if (discountOnSustenance <= 0) {
            throw new IllegalArgumentException("discountOnSustenance must be positive");
        }

        this.discountOnSustenance += discountOnSustenance;
    }


    public int getDiscountOnBuilding() {
        return discountOnBuilding;
    }


    public void addDiscountOnBuilding(int discountOnBuilding) {

        if (discountOnBuilding <= 0) {
            throw new IllegalArgumentException("discountOnBuilding must be positive");
        }

        this.discountOnBuilding += discountOnBuilding;
    }


    public int getShamanStars() {
        return shamanStars;
    }


    public void addShamanStars(int shamansStars) {

        if (shamansStars <= 0) {
            throw new IllegalArgumentException("shamansStars must be positive");
        }

        this.shamanStars += shamansStars;
    }


    public List<BuildingCard> getOwnedBuildings() {
        return Collections.unmodifiableList(ownedBuildings);
    }

    public void addCharacter(Character character) {

        if (character == null) {
            throw new IllegalArgumentException("character must not be null");
        }

        // Double dispatch: il tipo runtime di character seleziona il visit(...) corretto.
        // In questo modo evitiamo if/else o instanceof nel Player.
        // Double dispatch: the runtime type of character select the correct visit(...) in the Characters classes.
        character.accept(addToListVisitor);

    }

    public void addBuilding(BuildingCard building) {

        if (building == null) {
            throw new IllegalArgumentException("building must not be null");
        }

        building.accept(addToListVisitor);

    }

    public List<Inventor> getInventors() {
        return Collections.unmodifiableList(inventors);
    }

    public List<Gatherer> getGatherers() {
        return Collections.unmodifiableList(gatherers);
    }

    public List<Shaman> getShamans() {
        return Collections.unmodifiableList(shamans);
    }

    public List<Builder> getBuilders() {
        return Collections.unmodifiableList(builders);
    }

    public List<Artist> getArtists() {
        return Collections.unmodifiableList(artists);
    }

    public List<Hunter> getHunters() {
        return Collections.unmodifiableList(hunters);
    }


    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Player p2 = (Player) obj;

        return this.nickname.equals(p2.getNickname());
    }

    @Override
    public int hashCode() {
        return this.nickname.hashCode();
    }

    public void acceptCard(Card card) {
        if (card != null) {
            card.accept(addToListVisitor);
        }
    }
}