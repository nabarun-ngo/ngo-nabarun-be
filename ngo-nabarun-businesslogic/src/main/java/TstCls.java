import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.web.util.HtmlUtils;
import org.stringtemplate.v4.ST;

import lombok.Data;

public class TstCls {
	public static void main(String[] args) {
		System.out.println(HtmlUtils.htmlEscape("Dear <user.name>, Your regular donation of ₹<donation.amount> has been "));
		User user= new User();
		user.setName("Souvik");
		user.setRoles(List.of("haat"));
		Password password = new Password();
		password.setCreatedBy("Sourav");
		password.setPass("12888");
		History h1= new History();
		h1.setOldPass("1234");
		
		History h2= new History();
		h2.setOldPass("6656");
		
		password.setPasswordHistory(List.of(h1,h2));
		user.setPassword(password);

		Map<String, Object> map = new HashMap<>();
		map.put("name", "Hi my name is ${object.name}");
		map.put("password.pass", "my password is <object.password.pass>");
		map.put("password.createdBy", "this password is created by <object.password.createdBy>");
		
		//map.put("roles[0]", "My role 1 <object.roles>");
	//	map.put("roles[1]", "My role 2 <object.roles>");
		//map.put("password.passwordHistory[0].oldPass", "my old pass 1 is <object.password.passwordHistory[0].oldPass>");

		
		
		for(String key:map.keySet()) {
			if(map.get(key) != null) {
				String text=map.get(key).toString();
				System.out.println(text);
				ST st= new ST(text);
				
				st.add("object", user);
				map.put(key, st.render());
				//System.err.println(st.render());
			}
			
		}
		
		User one = new User();
		BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(one);
		wrapper.setAutoGrowNestedPaths(true);
		wrapper.setPropertyValues(map);

		System.out.println(one.getName());
		System.out.println(one.getPassword());
		System.out.println(one.getRoles());

	}
	
	@Data
	public static class User{
		private String name;
		private Password password;
		private List<String> roles;
	}
	
	@Data
	public static class Password{
		private String pass;
		private String createdBy;
		private List<History> passwordHistory;
	}
	
	@Data
	public static class History{
		private String oldPass;

	}
	
	
	
	
	
	
}
