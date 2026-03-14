package org.example.model.match;

import org.example.model.cards.buildingCards.BuildingCard;
import org.example.model.cards.characters.Character;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class Player {

    private final String nickname;
    private int points = 0;
    private int food = 0;
    private int discountOnSustenance = 0;
    private int discountOnBuilding = 0;
    private int shamanStars = 0;
    private final ArrayList<BuildingCard> ownedBuildings = new ArrayList<>();
    private final ArrayList<Character> ownedCharacters = new ArrayList<>();


    public Player(String nickname) {

        if (nickname == null) {
            throw new IllegalArgumentException("Nickname cannot be null");
        }
        this.nickname = nickname;

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

        if ( discountOnSustenance <= 0 ) {
            throw new IllegalArgumentException("discountOnSustenance must be positive");
        }

        this.discountOnSustenance += discountOnSustenance;
    }


    public int getDiscountOnBuilding() {
        return discountOnBuilding;
    }


    public void addDiscountOnBuilding(int discountOnBuilding) {

        if ( discountOnBuilding <= 0 ) {
            throw new IllegalArgumentException("discountOnBuilding must be positive");
        }

        this.discountOnBuilding += discountOnBuilding;
    }


    public int getShamanStars() {
        return shamanStars;
    }


    public void addShamanStars(int shamansStars) {

        if ( shamansStars <= 0 ) {
            throw new IllegalArgumentException("shamansStars must be positive");
        }

        this.shamanStars += shamansStars;
    }


    public List<BuildingCard> getOwnedBuildings() {
        return Collections.unmodifiableList(ownedBuildings);
    }


    public void addOwnedBuilding(BuildingCard buildingCard) {

        if ( buildingCard == null ) {
            throw new IllegalArgumentException("buildingCard must not be null");
        }

        this.ownedBuildings.add(buildingCard);
    }


    public List<Character> getOwnedCharacters() {
        return ownedCharacters;
    }


    public void addOwnedCharacter(Character character) {

        if ( character == null ) {
            throw new IllegalArgumentException("character must not be null");
        }


        this.ownedCharacters.add(character);
    }


    @Override
    public boolean equals(Object obj) {

        if ( this == obj ) {
            return true;
        }

        if ( obj == null || getClass() != obj.getClass() ) {
            return false;
        }

        Player p2 = (Player) obj;

        return this.nickname.equals(p2.getNickname());
    }

    @Override
    public int hashCode() {
        return this.nickname.hashCode(); // nickname è final e non-null, quindi è sicuro
    }
}