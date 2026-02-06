package mosbach.dhbw.de.tasks.persistence.repo;

import mosbach.dhbw.de.tasks.persistence.entity.RecipeRatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipeRatingRepository extends JpaRepository<RecipeRatingEntity, Long> {

    Optional<RecipeRatingEntity> findByRecipe_IdAndRater_Id(Long recipeId, Long raterId);

    List<RecipeRatingEntity> findTop20ByRecipe_IdOrderByUpdatedAtDesc(Long recipeId);

    @Query("""
            select rr.recipe.id as recipeId,
                   avg(rr.stars) as avgStars,
                   count(rr) as ratingCount
            from RecipeRatingEntity rr
            where rr.recipe.id in :ids
            group by rr.recipe.id
            """)
    List<RatingSummaryProjection> findSummariesByRecipeIds(@Param("ids") List<Long> ids);

    @Query("""
            select avg(rr.stars)
            from RecipeRatingEntity rr
            where rr.recipe.id = :recipeId
            """)
    Double findAverageByRecipeId(@Param("recipeId") Long recipeId);

    @Query("""
            select count(rr)
            from RecipeRatingEntity rr
            where rr.recipe.id = :recipeId
            """)
    Long countByRecipeId(@Param("recipeId") Long recipeId);
}
