package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.List;

import lombok.Data;

@Data
public class LinkCategoryDetail {
    private String name;
    private List<KeyValue> documents;
}
