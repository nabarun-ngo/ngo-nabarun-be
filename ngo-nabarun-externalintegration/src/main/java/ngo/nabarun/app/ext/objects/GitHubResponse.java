package ngo.nabarun.app.ext.objects;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubResponse  implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DataObj data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataObj  implements Serializable{
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Repository repository;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository  implements Serializable {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Discussion discussion;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Discussion  implements Serializable{
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String id;
        private String title;

        @JsonProperty("bodyHTML")
        private String bodyHtml;
    }
}
