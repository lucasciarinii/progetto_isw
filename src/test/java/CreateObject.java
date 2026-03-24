import org.example.model.match.Player;

import java.util.List;

public class CreateObject {
        public static Player player(String nome, int points, int food,
                                    int discountOnSustanance, int discountOnBuilding, int chamanStars){
            Player p = new Player(nome);
            p.addFood(food);
            p.addPoints(points);
            p.addDiscountOnSustenance(discountOnSustanance);
            p.addDiscountOnBuilding(discountOnBuilding);
            p.addShamanStars(p.getShamanStars());
            return p;
        }

        public static Match match(List<Player> players, List<Player> playersCustum)

}
