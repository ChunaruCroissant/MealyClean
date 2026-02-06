package mosbach.dhbw.de.tasks.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "recipe_rating",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_recipe_rater",
                columnNames = {"recipe_id", "rater_user_id"}
        )
)
public class RecipeRatingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private RecipeEntity recipe;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "rater_user_id", nullable = false)
    private UserEntity rater;

    @Column(nullable = false)
    private int stars;

    @Column(columnDefinition = "text")
    private String comment;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RecipeEntity getRecipe() {
        return recipe;
    }

    public void setRecipe(RecipeEntity recipe) {
        this.recipe = recipe;
    }

    public UserEntity getRater() {
        return rater;
    }

    public void setRater(UserEntity rater) {
        this.rater = rater;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
