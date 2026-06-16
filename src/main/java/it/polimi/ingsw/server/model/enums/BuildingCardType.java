package it.polimi.ingsw.server.model.enums;

/**
 * Identifiers for the different building card effects.
 */
public enum BuildingCardType {
    /** End-game points for set collection. */
    SetCollectionEndPointsBC,
    /** Food bonus for set collection. */
    SetCollectionFoodBC,
    /** Discount on sustenance costs. */
    SustenanceDiscountBC,
    /** End-game points for character types. */
    CharacterEndPointsBC,
    /** Combo effect for inventor cards. */
    InventorComboBC,
    /** Double points for shamanic ritual. */
    ShamanicDoublePointsBC,
    /** Ignore malus during shamanic ritual. */
    ShamanicNoMalusBC,
    /** Bonus stars for shamanic ritual. */
    ShamanicStarsBC,
    /** Boost for cave painting events. */
    CavePaintingEventBoostBC,
    /** Boost for hunt events. */
    HuntEventBoostBC,
    /** Round flow modifier. */
    RoundFlowBC,
    /** Round flow modifier with totem effect. */
    RoundFlowTotemBC,
    /** End-game bonus points. */
    EndGameBonusBC,
    /** End-game bonus points for 25 threshold. */
    EndGameBonus25BC
}
