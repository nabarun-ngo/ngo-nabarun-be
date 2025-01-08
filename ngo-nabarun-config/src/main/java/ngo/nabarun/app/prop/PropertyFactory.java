package ngo.nabarun.app.prop;

public class PropertyFactory {

	public static PropertySource initDoppler(String projectName, String configName, String token) {
		return new DopplerPropertySource(projectName, configName, token);
	}
}
