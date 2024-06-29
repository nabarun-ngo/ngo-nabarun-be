package ngo.nabarun.app.infra.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import ngo.nabarun.app.common.enums.NotificationType;
import ngo.nabarun.app.common.util.CommonUtils;

@Data
public class NotificationDTO {

	private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";

	private String id;
	private NotificationType type;
	private String title;
	private String summary;
	private String image;
	private boolean read;
	private String command;

	private List<UserDTO> target;
	private UserDTO source;
	// private String extLink;
	private String link;
	private String refItemId;
	private String refItemType;
	private Date notificationDate;
	private Date itemClosureDate;
	private boolean itemClosed;
	private UserDTO itemClosedBy;
	private Date readDate;

	public NotificationDTO(Map<String, Object> sourceMap) {
		this.id = sourceMap.get("id") == null ? null : sourceMap.get("id").toString();
		this.title = sourceMap.get("title") == null ? null : sourceMap.get("title").toString();
		this.summary = sourceMap.get("summary") == null ? null : sourceMap.get("summary").toString();
		this.image = sourceMap.get("image") == null ? null : sourceMap.get("image").toString();

		this.type = sourceMap.get("type") == null ? null : NotificationType.valueOf(sourceMap.get("type").toString());
		this.read = sourceMap.get("read") == null ? false : Boolean.valueOf(sourceMap.get("read").toString());
		this.command = sourceMap.get("action") == null ? null : sourceMap.get("action").toString();
		this.source = sourceMap.get("src_user") == null ? null : UserDTO.class.cast(sourceMap.get("src_user"));
		// this.extLink=sourceMap.get("ext_link") == null ? null :
		// sourceMap.get("ext_link").toString();
		this.link = sourceMap.get("link") == null ? null : sourceMap.get("link").toString();
		this.refItemId = sourceMap.get("ref_item_id") == null ? null : sourceMap.get("ref_item_id").toString();
		this.refItemType = sourceMap.get("ref_item_type") == null ? null : sourceMap.get("ref_item_type").toString();
		this.notificationDate = sourceMap.get("notification_date") == null ? null
				: CommonUtils.getFormattedDate(sourceMap.get("notification_date").toString(), dateFormat);
		this.itemClosureDate = sourceMap.get("close_date") == null ? null
				: CommonUtils.getFormattedDate(sourceMap.get("close_date").toString(), dateFormat);
		this.itemClosed = sourceMap.get("is_closed") == null ? false
				: Boolean.valueOf(sourceMap.get("is_closed").toString());
		this.itemClosedBy = sourceMap.get("close_by") == null ? null : UserDTO.class.cast(sourceMap.get("close_by"));
		this.readDate = sourceMap.get("read_date") == null ? null
				: CommonUtils.getFormattedDate(sourceMap.get("read_date").toString(), dateFormat);
	}

	public NotificationDTO() {
	}

	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<>();
		if (id != null) {
			map.put("id", this.id);
		}

		if (title != null) {
			map.put("title", this.title);
		}
		if (summary != null) {
			map.put("summary", this.summary);
		}
		map.put("read", String.valueOf(this.read));
		if (type != null) {
			map.put("type", this.type.name());
		}
		if (command != null) {
			map.put("action", this.command);
		}
		map.put("sender", source == null ? "System" : source.getName());
		map.put("senderImage", source == null ? "" : source.getImageUrl());
		if (link != null) {
			map.put("actionLink", link);
		}
		if (refItemId != null) {
			map.put("refItemId", refItemId);
		}
		map.put("date", CommonUtils.getFormattedDateString(notificationDate, dateFormat));
		map.put("open", String.valueOf(!itemClosed));
		return map;
	}

	public Map<String, Object> toSourceMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("id", this.id);
		map.put("title", this.title);
		map.put("summary", this.summary);
		map.put("image", this.image);

		map.put("read", this.read);
		map.put("read_date", CommonUtils.getFormattedDateString(readDate, dateFormat));

		map.put("type", this.type);
		map.put("action", this.command);
		map.put("target_users", this.target);
		map.put("src_user", this.source);
		// map.put("ext_link", this.extLink);
		map.put("link", this.link);

		map.put("ref_item_id", refItemId);
		map.put("ref_item_type", refItemType);

		map.put("notification_date", CommonUtils.getFormattedDateString(notificationDate, dateFormat));
		map.put("is_closed", itemClosed);
		map.put("close_date", CommonUtils.getFormattedDateString(itemClosureDate, dateFormat));
		map.put("close_by", itemClosedBy);

		return map;
	}

	@Data
	public static class NotificationDTOFilter {
		private String targetUserId;
		private Boolean read;
		private Date startDate;
		private Date endDate;
		private String refItemId;
	}

}
