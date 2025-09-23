package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.List;

import lombok.Data;

@Data
public class ImportantLinks {
	private List<LinkCategoryDetail> policies;
	private List<LinkCategoryDetail> userGuides;
}
