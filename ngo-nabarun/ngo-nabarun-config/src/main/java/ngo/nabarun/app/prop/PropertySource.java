package ngo.nabarun.app.prop;

public class PropertySource {

	public static DBPropertySource connectMongo(String connURI) throws Exception {
		return new MongoDBPropertySource(connURI);
	}
}
