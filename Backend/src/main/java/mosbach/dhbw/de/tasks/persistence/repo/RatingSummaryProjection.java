package mosbach.dhbw.de.tasks.persistence.repo;

public interface RatingSummaryProjection {
    Long getRecipeId();
    Double getAvgStars();
    Long getRatingCount();
}
