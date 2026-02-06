package mosbach.dhbw.de.tasks.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "stars",
        "comment"
})
public class RecipeRatingCreateConv {

    @JsonProperty("stars")
    private Integer stars;

    @JsonProperty("comment")
    private String comment;

    public RecipeRatingCreateConv() {
    }

    public RecipeRatingCreateConv(Integer stars, String comment) {
        this.stars = stars;
        this.comment = comment;
    }

    @JsonProperty("stars")
    public Integer getStars() {
        return stars;
    }

    @JsonProperty("stars")
    public void setStars(Integer stars) {
        this.stars = stars;
    }

    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    @JsonProperty("comment")
    public void setComment(String comment) {
        this.comment = comment;
    }
}
