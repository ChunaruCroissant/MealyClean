package mosbach.dhbw.de.tasks.model;

import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import mosbach.dhbw.de.tasks.data.api.UserIF;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "userName",
        "email",
        "password"
})
public class UserConv implements UserIF {

    @JsonProperty("userName")
    private String userName;
    @JsonProperty("email")
    private String email;
    @JsonProperty("password")
    private String password;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<>();

    public UserConv() {
    }

    public UserConv(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
    }

    @JsonProperty("userName")
    public String getUserName() {
        return userName;
    }

    @JsonProperty("userName")
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}


