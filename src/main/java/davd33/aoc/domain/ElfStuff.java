package davd33.aoc.domain;

import lombok.*;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(staticName = "of")
public class ElfStuff {
    private int nItems = 0;
    private int calories = 0;

    public ElfStuff addItem(int calories) {
        return ElfStuff.of(
                this.nItems + 1,
                this.calories + calories);
    }
}
