import org.example.server.model.match.Match;
import org.example.server.model.match.Player;

import java.util.List;

public class CreateObject {
        public static Player player(String nome, int points, int food,
                                    int discountOnSustanance, int discountOnBuilding, int schamanStars){
            Player p = new Player(nome);
            p.addFood(food);
            p.addPoints(points);
            p.addDiscountOnSustenance(discountOnSustanance);
            p.addDiscountOnBuilding(discountOnBuilding);
            p.addShamanStars(p.getShamanStars());
            return p;
        }

        public static Match match(List<Player> players){
            Match m = new Match(players);
            for (int i = 0; i < players.size(); i++) {
                int food = switch (i) {
                    case 0 -> -2;
                    case 1, 2 -> -3;
                    case 3, 4 -> -4;
                    default -> throw new IllegalArgumentException("Invalid list of players");
                };
                m.getPlayers().get(i).addFood(food);
            }
            return m;
        }

}
