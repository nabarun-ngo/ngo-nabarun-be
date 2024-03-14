package ngo.nabarun.app.infra.misc;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Data
@Deprecated
public class EmailTemplate{
	
	@JsonProperty("templateName")
	private String templateName;
	
	@JsonProperty("subject")
	private String subject;
	
	@JsonProperty("body")
	private EmailBodyTemplate body;
	
	@Data
	@NoArgsConstructor
	public static class EmailBodyTemplate {
		
		@JsonProperty("header")
		private HeaderTemplate header;
		
		@JsonProperty("content")
		private ContentTemplate content;
		
		@JsonProperty("footer")
		private FooterTemplate footer;
		
		public EmailBodyTemplate(HeaderTemplate header,ContentTemplate content,FooterTemplate footer) {
			this.header=header;
			this.content=content;
			this.footer=footer;
		}
		
		
		@Builder
		@Getter
		@Setter
		@AllArgsConstructor
		@NoArgsConstructor
		public static class HeaderTemplate {

			@JsonProperty("heading")
			private String heading;
			
			@JsonProperty("subHeading")
			private String subHeading;
		}
		
		@Builder
		@Getter
		@Setter
		@AllArgsConstructor
		@NoArgsConstructor
		public static class FooterTemplate {

			@JsonProperty("footer")
			private String footerText;
			
			@JsonProperty("footerPanel")
			private String footerPanel;
		}
		
		@Builder
		@Getter
		@Setter
		@AllArgsConstructor
		@NoArgsConstructor
		public static class ContentTemplate {

			@JsonProperty("salutation")
			private String salutation;
			
			@JsonProperty("paragraph1_blue")
			private String paragraph1Blue;
			
			@JsonProperty("paragraph2_blue")
			private String paragraph2Blue;
			
			@JsonProperty("paragraph3_blue")
			private String paragraph3Blue;
			
			@JsonProperty("paragraph4_orange")
			private String paragraph4Orange;
			
			
			@JsonProperty("signature")
			private String signature;
			
			@JsonProperty("disclaimer")
			private String disclaimer;
			
			@JsonProperty("table")
			private List<TableTemplate> table;
			
			@JsonProperty("details")
			private List<DetailsTemplate> details;
		}
		
		
		@Builder
		@Getter
		@Setter
		@AllArgsConstructor
		@NoArgsConstructor
		public static class TableTemplate {

			@JsonProperty("heading")
			private String heading;
			
			
			@JsonProperty("colWidth")
			private String colWidth;
			
			@JsonProperty("data")
			private String[][] data;
		}
		
		@Builder
		@Getter
		@Setter
		@AllArgsConstructor
		@NoArgsConstructor
		public static class DetailsTemplate {

			@JsonProperty("heading")
			private String heading;

			
			@JsonProperty("fields")
			private List<FieldTemplate> data;

			@Builder
			@Getter
			@Setter
			@AllArgsConstructor
			@NoArgsConstructor
			public static class FieldTemplate {
			
				@JsonProperty("name")
				private String name;

				@JsonProperty("value")
				private String value;
			}
		}
	}
}
