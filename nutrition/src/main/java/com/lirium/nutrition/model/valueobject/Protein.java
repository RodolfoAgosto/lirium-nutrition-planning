package com.lirium.nutrition.model.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public record Protein(@Column(name = "grams") int grams) {

  public Protein {
    if (grams < 0) {
      throw new IllegalArgumentException("Protein must be >= 0");
    }
  }

  public Protein add(Protein other) {
    Objects.requireNonNull(other);
    int result = Math.addExact(this.grams, other.grams);
    return new Protein(result);
  }

  public Protein subtract(Protein other) {
    Objects.requireNonNull(other, "Protein to subtract cannot be null");

    int result = Math.subtractExact(this.grams, other.grams);

    if (result < 0) {
      throw new IllegalArgumentException("Resulting protein cannot be negative: " + result);
    }

    return new Protein(result);
  }

  public double toCalories() {
    return grams * 4.0;
  }

  public String toDisplayString() {
    return grams + " g";
  }
}
